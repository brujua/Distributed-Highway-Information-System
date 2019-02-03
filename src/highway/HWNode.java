package highway;

import cars.CarStNode;
import common.CarMonitor;
import common.Position;
import common.StNode;
import common.Util;
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

    public static final String ip = "localhost";
    private static final int tentativePortCars = 7000;
    private static final int tentativePortHighway = 8000;
    private static final long ALIVE_TO_CARS_TIME_FREQ = 3000;
    private static final String CONFIG_COORDINATOR_PATH = "config-hwnodes";
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

    private ScheduledExecutorService aliveForCarsScheduler = Executors.newSingleThreadScheduledExecutor();
    private ExecutorService threadService = Executors.newCachedThreadPool();
    private ReentrantReadWriteLock hwLock = new ReentrantReadWriteLock();




    public HWNode() {
		super();
        this.posibleCoordinator = readConfig();
		id = UUID.randomUUID().toString();
		logger = LoggerFactory.getLogger(getName());
		portCars = Util.getAvailablePort(tentativePortCars);
		portHighway = Util.getAvailablePort(tentativePortHighway);
		carMonitor = new CarMonitor(getName());
		carMsgHandler = new MsgHandler(portCars, getName());
		carMsgHandler.addMsgListener(this);
		stNode = new StNode(id, ip, portCars);
        coordinator = null;
    }

    public HWNode(String name) {
        this();
        this.name = name;
        logger = LoggerFactory.getLogger(getName());
    }

    /**
     * Starts a thread that sends an alive to the cars periodically every {@value #ALIVE_TO_CARS_TIME_FREQ}
     *
     * @return this, fluent api
     */
    public HWNode sendAliveToCarsPeriodically() {
        aliveForCarsScheduler.scheduleWithFixedDelay(() -> {
            for (CarStNode car : carMonitor.getList()) {
                sendAlive(car);
            }
        }, ALIVE_TO_CARS_TIME_FREQ, ALIVE_TO_CARS_TIME_FREQ, TimeUnit.MILLISECONDS);
        return this;
    }

    /**
     * This method reads the configuration file for the hwnodes, named 'config-hwnodes.properties' under resources.
     * From there, extracts the list of possible locations (ip and range of ports) for coordinators
     *
     * @return the list of possible coordinators
     */
    public List<StNode> readConfig() {
        List<StNode> coords = new ArrayList<>();
        try {
            coords = Util.readNodeConfigFile(CONFIG_COORDINATOR_PATH);
        } catch (MissingResourceException e) {
            logger.error("Config file for HWNodes corrupted: " + e.getMessage());
        }
        return coords;
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
        Message msg = new Message(MsgType.REGISTER, ip, portHighway, getHWStNode());
		for (Messageable coordAux : posibleCoordinator) {
            logger.info("Attempting register to coordinator: " + coordAux);
			if (MsgHandler.sendTCPMsg(coordAux, msg)) {
                logger.info("registered in coordinator: " + coordAux);
				this.coordinator = coordAux;
				break;
			}
		}
        if (coordinator == null) {
            logger.error("Could not register to coordinator");
        }
		return this;
	}

	public List<CarStNode> getCarNodes(){

		return carMonitor.getList();
		//return this.carNodes;


	}

	private void addCarNode (CarStNode car){

		carMonitor.update(car);
	/*	synchronized (this.carNodes) {
			this.carNodes.add(car);


		}*/
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
                            responseACK(m);
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
        if (msg.getType() != MsgType.BROADCAST || !(msg.getData() instanceof MT_Broadcast)) {
            throw new CorruptDataException();
        }
        MT_Broadcast broadcast = (MT_Broadcast) msg.getData();
        synchronized (broadcastMsgs) {
            if (!broadcastMsgs.contains(broadcast) && broadcast.getTTL() > 0) {
                broadcastMsgs.add(broadcast);
                //if is from a car send it to the rest of the hwnodes
                if (broadcast.isFromCar()) {
                    logger.info("Broadcast Msg received from car: " + broadcast.getMsg());
                    broadcast.setFromCar(false); //to be resent as from hwnode
                    for (HWStNode node : getHwlist()) {
                        if (!node.equals(this.getHWStNode())) {
                            MsgHandler.sendTCPMsg(node.getStNode(), new Message(MsgType.BROADCAST, ip, portHighway, broadcast.decrementTTL()));
                        }
                    }
                } else //If its from Hwnode send it to my cars
                {
                    logger.info("Broadcast received from HW-Node: " + broadcast.getMsg());
                    for (CarStNode node : carMonitor.getList()) {
                        carMsgHandler.sendUDP(node, new Message(MsgType.BROADCAST, ip, portCars, broadcast.decrementTTL()));
                    }
                }
            }
        }
    }

    private void handleAlive(Message msg) {
        logger.debug("Alive received from coordinator");
	}

	private void handleUpdate(Message msg) throws CorruptDataException {
		if (msg.getType() != MsgType.UPDATE || !(msg.getData() instanceof MT_Update)) {
			throw new CorruptDataException();
		}
		MT_Update update = (MT_Update) msg.getData();
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
        logger.debug("hwlist updated correctly");
	}

    /**
     * Update the list and finds this node in the list to update segments and nextHWNode.
     *
     * @param list new hwlist
     */
    private void updateHWList(List<HWStNode> list) {
        logger.debug("waiting for write lock");
        hwLock.writeLock().lock();
        logger.debug("write lock adquired");
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

	private void responseACK(Message m) {
		CarStNode node = (CarStNode) m.getData();
		if (isInSegments(node.getPosition()))
			carMsgHandler.sendUDP(node, ackMssg());

	}

	private Message ackMssg() {

		return new Message(MsgType.ACK, getIp(), getPortCars(), carMonitor.getList());
	}

	private void handleHello(Message m) throws CorruptDataException {
		if(!(m.getData() instanceof CarStNode))
			throw new CorruptDataException();
		CarStNode node = (CarStNode) m.getData();
        logger.debug("Hello received from: " + node);
		if (isInSegments( node.getPosition() ) ) {
            Message msg = new Message(MsgType.HELLO_RESPONSE, getIp(), getPortCars(), new MT_HelloResponse(m.getId(), getStNode(), carMonitor.getList()));
			carMonitor.update(node);
            logger.debug(node + " accepted, sending hello response.");
			carMsgHandler.sendUDP(node, msg);

		}else {
			StNode nodeRedirect = searchRedirect(node.getPosition());
			if (nodeRedirect!=null) {
				redirect(m, nodeRedirect);
			}else{
                carMsgHandler.sendUDP(node, new Message(MsgType.ERROR, ip, portCars, "404: No hw-node for position " + node.getPosition()));
			}
		}
	}

    private void sendAlive(Messageable dest) {
        Message msg = new Message(MsgType.ALIVE, getIp(), getPortCars(), getStNode());
        carMsgHandler.sendUDP(dest, msg);
    }



	private void handlePulse(Message msg) throws CorruptDataException {
		if(msg.getType() != MsgType.PULSE || ! (msg.getData() instanceof CarStNode )){
			throw new CorruptDataException();
		}
		CarStNode car = (CarStNode) msg.getData();
        logger.debug("Pulse received from node: " + car);
		if(isInSegments(car.getPosition()))
			updateCar(car);
		else

            redirect(msg, getNextNode());

    }

    private void updateCar(CarStNode car) {
		carMonitor.update(car);
	}

	private void redirect(Message m, StNode hwRedirect) {

		//StNode hwRedirect = searchRedirect(((Position) m.getData()));
		MT_Redirect redirect = new MT_Redirect(m.getId(), hwRedirect);
		Message msg = new Message(MsgType.REDIRECT,getIp(),portCars,redirect);

		StNode carst = new StNode(m.getId(), m.getIp(), m.getPort());
		carMsgHandler.sendUDP(carst, msg);
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
        carMsgHandler.close();
        hwMsgHandler.close();
        aliveForCarsScheduler.shutdown();
        logger.info("Shutting down completed.");
    }
}