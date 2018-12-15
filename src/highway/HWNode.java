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
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantReadWriteLock;


public class HWNode implements MsgListener {
	private Logger logger;

	public static final String ip = "localhost";
	
	public static final int tentativePortCars = 5007;

	public static final int tentativePortHighway = 8000;
	
	private static final double MAX_RANGE = 0;

	private String id;
	private StNode stNode;
	private int portCars;
	private int portHighway;
	private List<Messageable> posibleCoordinator;
	private ExecutorService threadService = Executors.newCachedThreadPool();

	private ReentrantReadWriteLock hwLock = new ReentrantReadWriteLock();
	private StNode nextStNode;

	private MsgHandler carMsgHandler;
	private MsgHandler hwMsgHandler;

	private List<HWStNode> hwlist; //other highway nodes
	private final Object hwlistLock = new Object();
	private CarMonitor carMonitor;
	private List<Segment> segments;

	private Messageable coordinator;

	private Instant lastHWUpdate;
	public HWNode(List<Messageable> posibleCoordinator) {
		super();
		this.posibleCoordinator = posibleCoordinator;
		id = UUID.randomUUID().toString();
		logger = LoggerFactory.getLogger(getName());
		portCars = Util.getAvailablePort(tentativePortCars);
		portHighway = Util.getAvailablePort(tentativePortHighway);
		carMonitor = new CarMonitor(MAX_RANGE, null, getName()); //TODO maxrange?WTF
		carMsgHandler = new MsgHandler(portCars, getName());
		carMsgHandler.addMsgListener(this);
		stNode = new StNode(id, ip, portCars);

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

		return this; // fluent
	}

	private String getName() {
		return "HW"+ id.substring(0,5);
	}

	public HWNode registerInNetwork() {
		Message msg = new Message(MsgType.REGISTER, ip, portHighway, getStNodeForHW());

		for (Messageable coordAux : posibleCoordinator) {
			if (MsgHandler.sendTCPMsg(coordAux, msg)) {
				this.coordinator = coordAux;
				break;
			}
		}
		return this;
	}

	public List<StNode> getCarNodes(){

		return carMonitor.getList();
		//return this.carNodes;


	}

	private void addCarNode (StNode car){

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

	public StNode getStNodeForHW() {
		return new StNode(id, ip, portHighway);
	}

	public Messageable getCoordinator() {
		return coordinator;
	}

	@Override
	public void msgReceived(Message m) {
		// The logic of the received msg will be handled on a different thread

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
					case REDIRECT: {
						break;
					}
					case ALIVE: {
						break;
					}
					case ACK: {
						responseACK(m);
						break;
					}

					default: {
						logger.error("Received message of wrong type");
					}
				}

			} catch (CorruptDataException cde) {
				logger.error("Corrupt data exception on message: " + m + " /n exception msg: " + cde.getMessage());

			}
		});
	}

	private void handleUpdate(Message msg) throws CorruptDataException {
		if (msg.getType() != MsgType.UPDATE || !(msg.getData() instanceof MT_Update)) {
			throw new CorruptDataException();
		}
		MT_Update update = (MT_Update) msg.getData();
		//check timestamp
		if (update.getTimestamp().isBefore(lastHWUpdate)) {
			logger.info("Received an old update");
			return;
		}
		updateHWList(update.getList());
		lastHWUpdate = update.getTimestamp();
	}

	private void updateHWList(List<HWStNode> list) {
		//TODO

		for (HWStNode hwStNode: list ) {
			boolean check = false;
			hwLock.writeLock().lock();
			if(stNode.equals(hwStNode.getStNode())){
				segments = hwStNode.getSegments();
				nextStNode = list.get(list.indexOf(hwStNode)+1).getStNode();//get the next node in the list
				break;
			}
			hwLock.writeLock().unlock();
		}


	}

	private void responseACK(Message m) {
		StNode node = (StNode) m.getData();
		if (carMonitor.isInRange(node))
			carMsgHandler.sendUDP(node, ackMssg());

	}

	private Message ackMssg() {

		return new Message(MsgType.ACK, getIp(), getPortCars(), carMonitor.getList());
	}

	private void handleHello(Message m) throws CorruptDataException {
		if(!(m.getData() instanceof StNode))
			throw new CorruptDataException();
		CarStNode node = (CarStNode) m.getData();
		if (isInSegments( node.getPosition() ) ) {
//			Message msg = new Message(MsgType.HELLO_RESPONSE, getIp(),getPortCars(), new MT_HelloResponse(m.getId(), stNode, carNodes));
	//		carNodes.add(node); 

			Message msg = new Message(MsgType.HELLO_RESPONSE, getIp(), getPortCars(), new MT_HelloResponse(m.getId(), stNode, carMonitor.getList()));
			carMonitor.update(node);

			carMsgHandler.sendUDP(node, msg);

		}else {
			redirect(m);
		}
	}
	

	private boolean isInSegments(Position pos) {

		for (Segment seg:segments) {
			if(seg.contains(pos)){
				return true;
			}
		}


		return false;
	}

	
	private void handlePulse(Message msg) throws CorruptDataException {
		if(msg.getType() != MsgType.PULSE || ! (msg.getData() instanceof StNode )){
			throw new CorruptDataException();
		}
		CarStNode car = (CarStNode) msg.getData();
		logger.info("Pulse received on node: " + getStNode()+" from node: "+car);
		if(isInSegments(car.getPosition()))
			updateCar(car);
		else
			redirect(msg);

	}

	private void updateCar(StNode car) {
		carMonitor.update(car);
	}

	private void redirect(Message m) {
		// TODO redirecccionar a hw correspondiente

		StNode hwRedirect = searchRedirect(((Position) m.getData()));
		MT_Redirect redirect = new MT_Redirect(m.getId(), hwRedirect);
		Message msg = new Message(MsgType.REDIRECT,getIp(),portCars,redirect);

		StNode carst = new StNode(m.getId(), m.getIp(), m.getPort());

		//carMonitor.update(carst);
		// wait until carmonitor removes the car because of timeout
		carMsgHandler.sendUDP(carst, msg);
	}


	private StNode searchRedirect(Position position) {
		// TODO Auto-generated method stub

		return null;
	}

	private void ack(Message m) {

		//Message msg = new Message(MsgType.ACK,getIp(),getPortCars(),m.getId());

		//carMsgHandler.sendUDP((Messageable) m.getOrigin(), msg);
	}

	public List<Segment> getSegments() {
		return segments;
	}
}