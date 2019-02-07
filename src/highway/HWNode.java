package highway;

import cars.CarStNode;
import common.*;
import network.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.MissingResourceException;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;


public class HWNode implements MsgListener {

    private static final String CONFIG_FILE_NAME = "config-hwnodes";
    private static final long SLEEP_BETWEEN_TRIES_REG_IN_NET = 5000;

    public static final String ip = "localhost";

    //configurable parameters
    private int tentativePortCars = 7000;
    private int tentativePortHighway = 8000;
    private long aliveToCarsFrequency = 3000;
    private int timeoutTime = 5000;
    private int timeoutCheckFrequency = 2000;
    private Logger logger;


	private String id;
	private StNode stNode;
    private String name = null;
	private int portCars;
	private int portHighway;
    private List<? extends Messageable> posibleCoordinator;
    private Messageable coordinator;
    private List<Segment> segments;
    private List<HWStNode> hwlist; //other highway nodes
    private StNode nextHWNode;
    private Instant lastHWUpdate;
    private final List<MT_Broadcast> broadcastMsgs = new ArrayList<>();


    private MsgHandler carMsgHandler;
    private MsgHandler hwMsgHandler;
    private CarMonitor carMonitor;
    private SingleNodeMonitor coordinatorMonitor;

    private ScheduledExecutorService aliveForCarsScheduler = Executors.newSingleThreadScheduledExecutor();
    private ExecutorService threadService = Executors.newCachedThreadPool();
    private ReentrantReadWriteLock hwLock = new ReentrantReadWriteLock();




    public HWNode() {
		super();
        initConfigProperties();
		id = UUID.randomUUID().toString();
		logger = LoggerFactory.getLogger(getName());
		portCars = Util.getAvailablePort(tentativePortCars);
		portHighway = Util.getAvailablePort(tentativePortHighway);
        carMonitor = new CarMonitor(getName(), timeoutTime, timeoutCheckFrequency);
        coordinatorMonitor = new SingleNodeMonitor(timeoutTime, timeoutCheckFrequency, getName());
		carMsgHandler = new MsgHandler(portCars, getName());
		carMsgHandler.addMsgListener(this);
		stNode = new StNode(id, ip, portCars);
        coordinator = null;
        segments = null;
    }

    public HWNode(String name) {
        this();
        this.name = name;
        logger = LoggerFactory.getLogger(getName());
    }

    /**
     * This method reads the configuration file for the cars, named 'config-hwnodes.properties' under resources.
     * if at any point the configuration fails, the configurable parameters pending will remain untouched.
     */
    private void initConfigProperties() {
        try {
            ConfigReader configReader = new ConfigReader(CONFIG_FILE_NAME);
            tentativePortCars = configReader.getIntProperty("tentative-port-cars");
            tentativePortHighway = configReader.getIntProperty("tentative-port-highway");
            aliveToCarsFrequency = configReader.getIntProperty("alive-to-cars-frequency");
            timeoutTime = configReader.getIntProperty("timeout-time");
            timeoutCheckFrequency = configReader.getIntProperty("timeout-check-frequency");
            posibleCoordinator = configReader.getNodes();
        } catch (MissingResourceException e) {
            logger.error("config file its missing or corrupt: " + e.getMessage());
        }

    }

    /**
     * Starts a thread that sends an alive to the cars periodically
     *
     * @return this, fluent api
     */
    public HWNode sendAliveToCarsPeriodically() {
        aliveForCarsScheduler.scheduleWithFixedDelay(() -> {
            for (CarStNode car : carMonitor.getList()) {
                sendAlive(car);
            }
        }, aliveToCarsFrequency, aliveToCarsFrequency, TimeUnit.MILLISECONDS);
        return this;
    }

	public HWNode listenForMsgs() {
		portCars = Util.getAvailablePort(tentativePortCars);
		portHighway = Util.getAvailablePort(tentativePortHighway);
		carMsgHandler = new MsgHandler(portCars, getName());
		carMsgHandler.addMsgListener(this);
		hwMsgHandler = new MsgHandler(portHighway, getName());
		hwMsgHandler.addMsgListener(this);

		carMsgHandler.listenForUDPMsgs();
		hwMsgHandler.listenForTCPMsgs();
        logger.info("Listening coordinator on port: " + hwMsgHandler.getPort());
        logger.info("Listening for cars on port: " + portCars);

		return this; // fluent
	}

	private String getName() {
        return name != null ? name : "HW" + id.substring(0, 5);
	}

	public HWNode registerInNetwork() {
        Message msg = new MsgRegister(getHWStNode());
		for (Messageable coordAux : posibleCoordinator) {
            logger.info("Attempting register to coordinator: " + coordAux);
			if (MsgHandler.sendTCPMsg(coordAux, msg)) {
                logger.info("registered in coordinator: " + coordAux);
				this.coordinator = coordAux;
				break;
			}
		}
        if (coordinator == null)
            logger.error("Could not register to coordinator");
        else {
            coordinatorMonitor.setNode(new StNode("0", coordinator.getIP(), coordinator.getPort()));
            coordinatorMonitor.startMonitoring(this::handleCoordDown);
        }
		return this;
	}

    private void handleCoordDown() {
        logger.error("Coordinator offline, periodically trying to re-register...");
        coordinator = null;
        while (coordinator == null) {
            try {
                registerInNetwork();
                Thread.sleep(SLEEP_BETWEEN_TRIES_REG_IN_NET);
            } catch (InterruptedException e) {
                logger.info("interrupted while sleeping handling coordinator down");
            }
        }
    }

    public List<CarStNode> getCarNodes() {
        return carMonitor.getList();
    }

	@Override
	public void msgReceived(Message m) {
		// The logic of the received msg will be handled on a different thread
        if (!threadService.isShutdown())
            threadService.execute(() -> {
                try {
                    switch (m.getType()) {
                        case UPDATE: {
                            handleUpdate(m);
                            break;
                        }
                        case HELLO: {
                            handleHello(m);
                            break;
                        }
                        case PULSE: {
                            handlePulse(m);
                            break;
                        }

                        case ALIVE: {
                            handleAlive(m);
                            break;
                        }
                        case ACK: {
                            handleACK(m);
                            break;
                        }
                        case BROADCAST: {
                            handleBroadcast(m);
                            break;
                        }
                        default: {
                            logger.error("Received message of wrong type: " + m.getType().toString());
                        }
                    } //end switch
                } catch (CorruptDataException cde) {
                    logger.error("Corrupt data exception on message: " + m + " /n exception msg: " + cde.getMessage());

                }
            });//end task
    }

    private void handleBroadcast(Message msg) throws CorruptDataException {
        if (msg.getType() != MsgType.BROADCAST || !(msg instanceof MT_Broadcast)) {
            throw new CorruptDataException();
        }
        MT_Broadcast broadcast = (MT_Broadcast) msg;
        synchronized (broadcastMsgs) {
            if (!broadcastMsgs.contains(broadcast) && broadcast.getTTL() > 0) {
                broadcastMsgs.add(broadcast);
                //if is from a car send it to the rest of the hwnodes
                if (broadcast.isFromCar()) {
                    logger.info("Broadcast Msg received from car: {}", broadcast.getMsg());
                    //resent as from here
                    broadcast.setSender(getHWStNode().getStNode(), false);
                    broadcast.decrementTTL();
                    for (HWStNode node : getHwlist()) {
                        if (!node.equals(this.getHWStNode())) {
                            MsgHandler.sendTCPMsg(node.getStNode(), broadcast);
                        }
                    }
                } else //If its from Hwnode send it to my cars
                {
                    logger.info("Broadcast received from HW-Node: {}", broadcast.getMsg());
                    broadcast.setSender(getStNode(), false);
                    broadcast.decrementTTL();
                    for (CarStNode node : carMonitor.getList()) {
                        carMsgHandler.sendUDP(node, broadcast);
                    }
                }
            }
        }
    }

    private void handleAlive(Message msg) {
        logger.debug("Alive received from coordinator");
        coordinatorMonitor.update(new StNode("0", msg.getIp(), msg.getPort()));
	}

	private void handleUpdate(Message msg) throws CorruptDataException {
        if (msg.getType() != MsgType.UPDATE || !(msg instanceof MT_Update)) {
			throw new CorruptDataException();
		}
        MT_Update update = (MT_Update) msg;
		logger.info("received update from coordinator");
		//check timestamp
		if (lastHWUpdate !=null){
			if (update.getTimestamp().isBefore(lastHWUpdate)) {
                logger.info("Received an old update: " + msg);
				return;
			}
		}
		updateHWList(update.getList());
		lastHWUpdate = update.getTimestamp();
	}

    /**
     * Update the list and finds this node in the list to update segments and nextHWNode.
     *
     * @param list new hwlist
     */
    private void updateHWList(List<HWStNode> list) {
        hwLock.writeLock().lock();
        hwlist = list;
        for (int i = 0; i < list.size(); i++) {
            HWStNode hwNode = list.get(i);
            if (getHWStNode().equals(hwNode)) {
                segments = hwNode.getSegments();
                // if im not the last one
                if (i + 1 < list.size()) {
                    nextHWNode = list.get(i + 1).getCarStNode();
                } else {
                    nextHWNode = null;
                }
            }
        }
        hwLock.writeLock().unlock();
    }

    private void handleACK(Message m) throws CorruptDataException {
        if (m.getType() != MsgType.ACK)
            throw new CorruptDataException();
        logger.debug("ACK msg received.");
	}

	private void handleHello(Message m) throws CorruptDataException {
        if (m.getType() != MsgType.HELLO || !(m instanceof MsgHello))
			throw new CorruptDataException();
        MsgHello msgHello = (MsgHello) m;
        CarStNode node = msgHello.getCarNode();
        logger.debug("Hello received from: {}", node);
        if (segments == null) {
            logger.debug("not yet registered on network, ignoring hello.");
            return;
        }
		if (isInSegments( node.getPosition() ) ) {
            Message msg = new MT_HelloResponse(m.getId(), getStNode(), carMonitor.getList());
			carMonitor.update(node);
            logger.debug(node + " accepted, sending hello response.");
			carMsgHandler.sendUDP(node, msg);

		}else {
			StNode nodeRedirect = searchRedirect(node.getPosition());
			if (nodeRedirect!=null) {
				redirect(m, nodeRedirect);
			}else{
                sendErrorOutOfHighWay(node);
			}
		}
	}

    private void sendAlive(Messageable dest) {
        Message msg = new Message(MsgType.ALIVE, getStNode());
        carMsgHandler.sendUDP(dest, msg);
    }


    private void handlePulse(Message msg) throws CorruptDataException {
        if (msg.getType() != MsgType.PULSE || !(msg instanceof MsgPulse)) {
            throw new CorruptDataException();
        }
        MsgPulse msgPulse = (MsgPulse) msg;
        CarStNode car = msgPulse.getCarNode();
        logger.debug("Pulse received from node: {}", car);
        if(isInSegments(car.getPosition()))
            updateCar(car);
        else {
            StNode nextNode = getNextNode();
            if (nextNode == null)
                sendErrorOutOfHighWay(car);
            else
                redirect(msg, getNextNode());

        }

    }

    private void sendErrorOutOfHighWay(CarStNode node) {
        carMsgHandler.sendUDP(node, new MsgError(getStNode(), "404: No hw-node for position " + node.getPosition()));
    }

    private void updateCar(CarStNode car) {
		carMonitor.update(car);
	}

	private void redirect(Message m, StNode hwRedirect) {

        MT_Redirect redirect = new MT_Redirect(getStNode(), m.getId(), hwRedirect);
        StNode carst = m.getSender();
        carMsgHandler.sendUDP(carst, redirect);
        logger.debug("{} redirected to: {}", carst, hwRedirect);
	}


	private StNode searchRedirect(Position position) {
        StNode response = null;
        hwLock.readLock().lock();
        try {
            if (hwlist != null) {
                for (HWStNode node : hwlist)
                    if (node.isInSegment(position))
                        response = node.getCarStNode();
            } else
                logger.error("HW-List not yet initialized and a received a redirect request");
        } finally {
            hwLock.readLock().unlock();
        }
        return response;
	}

    private List<HWStNode> getHwlist() {
        List<HWStNode> list = new ArrayList<>();
        hwLock.readLock().lock();
        if (hwlist != null)
            list.addAll(hwlist);
        hwLock.readLock().unlock();
        return list;
    }

    private StNode getNextNode() {
        hwLock.readLock().lock();
        StNode response = nextHWNode;
        hwLock.readLock().unlock();
        return response;
    }

    public List<Segment> getSegments() {
        List<Segment> response = new ArrayList<>();
        hwLock.readLock().lock();
        if (segments != null) {
            response.addAll(segments);
        }
        hwLock.readLock().unlock();
        return response;
    }

    private boolean isInSegments(Position pos) {
        hwLock.readLock().lock();
        if (segments != null) {
            for (Segment seg : segments) {
                if (seg.contains(pos)) {
                    hwLock.readLock().unlock();
                    return true;
                }
            }
        }
        hwLock.readLock().unlock();
        return false;
    }

    public String getId() {
        return id;
    }

    private String getIp() {
        return ip;
    }


    private int getPortCars() {
        return portCars;
    }

    private int getPortHighway() {
        return portHighway;
    }

    public StNode getStNode() {
        return new StNode(id, ip, portCars);
    }

    public HWStNode getHWStNode() {
        StNode stNode = new StNode(id, ip, getPortHighway());
        StNode carstNode = new StNode(id, ip, getPortCars());
        return new HWStNode(carstNode, stNode, getSegments());
    }

    public Messageable getCoordinator() {
        return coordinator;
    }

    public void shutdown() {
        logger.info("Shutting down...");
        threadService.shutdown();
        carMonitor.shutdown();
        coordinatorMonitor.shutdown();
        carMsgHandler.close();
        hwMsgHandler.close();
        aliveForCarsScheduler.shutdown();
        logger.info("Shutting down completed.");
    }
}