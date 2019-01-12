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
import java.util.concurrent.locks.ReentrantReadWriteLock;


public class HWNode implements MsgListener {
	private Logger logger;

	public static final String ip = "localhost";

    public static final int tentativePortCars = 7000;

	public static final int tentativePortHighway = 8000;
    public static final String CONFIG_COORDINATOR_PATH = "config-hwnodes";

	private static final double MAX_RANGE = 0;

	private String id;
	private StNode stNode;
    private String name = null;
	private int portCars;
	private int portHighway;
    private List<? extends Messageable> posibleCoordinator;
	private ExecutorService threadService = Executors.newCachedThreadPool();

	private ReentrantReadWriteLock hwLock = new ReentrantReadWriteLock();
	private StNode nextStNode;

	private MsgHandler carMsgHandler;
	private MsgHandler hwMsgHandler;

	private List<HWStNode> hwlist; //other highway nodes
	//private final Object hwlistLock = new Object();
	private CarMonitor carMonitor;

	private List<MT_Broadcast> broadcastMsgs = new ArrayList<>();
	private List<Segment> segments;

	private Messageable coordinator;

	private Instant lastHWUpdate;

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
		logger.info("Escuchando a coordinador en puerto: " + hwMsgHandler.getPort());

		return this; // fluent
	}

	private String getName() {
        return name != null ? name : "HW" + id.substring(0, 5);
	}

	public HWNode registerInNetwork() {
		Message msg = new Message(MsgType.REGISTER, ip, portHighway, getStNodeForHW());

		for (Messageable coordAux : posibleCoordinator) {
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

	private String getIp() {
		return ip;
	}


	private int getPortCars() {
		return portCars;
	}

	public StNode getStNode() {
		return new StNode(id, ip, portCars);
	}

	public HWStNode getStNodeForHW() {

		StNode stNode = new StNode(id, ip, portHighway);

		StNode carstNode = new StNode(id,ip,portCars);

		HWStNode hwStNode = new HWStNode(carstNode, stNode,getSegments());

		HWStNode response = hwStNode;
		return response;
	}

	public Messageable getCoordinator() {
		return coordinator;
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
							broadcasthandle(m);
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

	private void broadcasthandle(Message msg) throws CorruptDataException {
		if (msg.getType() != MsgType.BROADCAST || !(msg.getData() instanceof MT_Broadcast)) {
			throw new CorruptDataException();
		}
		MT_Broadcast broadcast = (MT_Broadcast) msg.getData();
		if (!broadcastMsgs.contains(broadcast)){
			hwLock.writeLock().lock();
			broadcastMsgs.add(broadcast);
			//System.err.println("HW Broadcast recived");
			hwLock.writeLock().unlock();
			//send msg to all HwNode if is a car
			if(broadcast.isCar()){
				logger.info("Broadcast Msg recived from car: ");

				hwLock.readLock().lock();
				for (HWStNode node:hwlist) {
					System.err.println("HW size:"+ hwlist.size());
					if(!msg.getId().equals(node.getId())){
						MsgHandler.sendTCPMsg(node.getStNode(),new Message(MsgType.BROADCAST,ip,portHighway,broadcast.setHw()));
						logger.info("Broadcast send to  hw :PORT "+ node.getStNode().getPort()+" IP:  "+node.getStNode().getIP());
					}
				}
				hwLock.readLock().unlock();
			}
			//send msg to all Cars if is a HW
			if(broadcast.isHw()){
				logger.info("HW Broadcast recived from hw: "+ msg.getData());
				hwLock.readLock().lock();
				List<CarStNode> cars = carMonitor.getList();
				for (CarStNode node:cars) {
					if(!msg.getId().equals(node.getId()))
                        carMsgHandler.sendUDP(node,new Message(MsgType.BROADCAST,ip,portCars,broadcast.setHw()));
				}
				hwLock.readLock().unlock();
			}
		}
			/*hwLock.writeLock().lock();
				broadcastMsgs.add(broadcast);
			hwLock.writeLock().unlock();*/

	}

	private void handleAlive(Message msg) {
        logger.info("Alive received from coordinator");
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
				logger.info("Received an old update");
				return;
			}
		}
		updateHWList(update.getList());
		lastHWUpdate = update.getTimestamp();
	}

	private void updateHWList(List<HWStNode> list) {
		hwLock.writeLock().lock();
		for (int i = 0; i < list.size(); i++) {
			HWStNode hwNode = list.get(i);
			if (stNode.equals(hwNode.getStNode())) {
				segments = hwNode.getSegments();
				hwlist = list;
				// if im not the last one
				if (i + 1 < list.size()) {
					nextStNode = list.get(i + 1).getCarStNode();
				} else {
					nextStNode = null;
				}
				break;
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
		if (isInSegments( node.getPosition() ) ) {
            Message msg = new Message(MsgType.HELLO_RESPONSE, getIp(), getPortCars(), new MT_HelloResponse(m.getId(), getStNode(), carMonitor.getList()));
			carMonitor.update(node);

			carMsgHandler.sendUDP(node, msg);

		}else {
			StNode nodeRedirect = searchRedirect(node.getPosition());
			if (nodeRedirect!=null) {
				redirect(m, nodeRedirect);
			}else{
				carMsgHandler.sendUDP(node,new Message(MsgType.ERROR,ip,portCars,null));
			}
		}
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

	
	private void handlePulse(Message msg) throws CorruptDataException {
		if(msg.getType() != MsgType.PULSE || ! (msg.getData() instanceof CarStNode )){
			throw new CorruptDataException();
		}
		CarStNode car = (CarStNode) msg.getData();
        logger.info("Pulse received from node: " + car);
		if(isInSegments(car.getPosition()))
			updateCar(car);
		else

            redirect(msg, getNextNode());

	}

    private StNode getNextNode() {
        hwLock.readLock().lock();
        StNode response = nextStNode;
        hwLock.readLock().unlock();
        return response;
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
		hwLock.readLock().lock();
        if (hwlist != null) {
            for (HWStNode node : hwlist) {
                if (node.isInSegment(position)) {
                    StNode response = node.getCarStNode();
                    hwLock.readLock().unlock();
                    return response;
                }
            }
        }
		hwLock.readLock().unlock();
		return null;
	}

    private List<HWStNode> gethwlist() {
        hwLock.readLock().lock();
        List<HWStNode> response = hwlist;
        hwLock.readLock().unlock();
        return response;
    }

    private void ack(Message m) {

		//Message msg = new Message(MsgType.ACK,getIp(),getPortCars(),m.getId());

		//carMsgHandler.sendUDP((Messageable) m.getOrigin(), msg);
	}

	public List<Segment> getSegments() {

        hwLock.readLock().lock();
        List<Segment> response= segments;
        hwLock.readLock().unlock();
        return response;
	}

    public void shutdown() {
        logger.info("Shutting down");
        threadService.shutdown();
        carMonitor.shutdown();
        carMsgHandler.close();
        hwMsgHandler.close();
    }

    public String getId() {
        return id;
    }
}