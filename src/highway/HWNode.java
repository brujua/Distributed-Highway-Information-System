package highway;

import common.CarMonitor;
import common.Position;
import common.StNode;
import common.Util;
import network.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public class HWNode implements MsgListener {
	private Logger logger;

	public static final String ip = "localhost";
	
	public static final int tentativePortCars = 5007;

	public static final int tentativePortCoordinator = 8000;
	
	private static final double MAX_RANGE = 0;

	private String id;
	private StNode stNode;
	private int portCars;
	private int portCoordinator;
	private List<Messageable> posibleCoordinator;
	
	//MSG tools
	private MsgHandler msgHandler;

	private List<HWStNode> hwlist; //other highway nodes
	private CarMonitor carMonitor;
	private List<Segment> segments;

	public HWNode(ArrayList<Messageable> posibleCoordinator) {
		super();
		this.posibleCoordinator = posibleCoordinator;
		id = UUID.randomUUID().toString();
		logger = LoggerFactory.getLogger(getName());
		portCars = Util.getAvailablePort(tentativePortCars);
		portCoordinator = Util.getAvailablePort(tentativePortCoordinator);

	
		carMonitor = new CarMonitor(MAX_RANGE,null, getName());


		msgHandler = new MsgHandler(portCars,getName());
		msgHandler.addMsgListener(this);

		stNode = new StNode(id, ip, portCars);
		
	}

	private String getName() {
		return "HW"+ id.substring(0,5);
	}

	public HWNode registerInNetwork() {

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


	private int getPort() {
		return portCars;
	}
	
	public StNode getStNode() {
		return new StNode(id, ip, portCars);
	}
	
	@Override
	public void msgReceived(Message m) {

		// The logic of the received msg will be handled on a different thread
		Thread thread = new Thread( new Runnable() {
			public void run() {
				try {
					switch(m.getType()) {
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
						//TODO log message of unknown type
					}
				}		
			
				} catch(CorruptDataException cde) {
					cde.printStackTrace();
					//TODO log
					System.out.println("corrupt data on hw-node response");
				}
			}

			private void responseACK(Message m) {
				StNode node = (StNode) m.getData() ;
				if(carMonitor.isInRange(node))
					msgHandler.sendMsg(node, ackMssg());

			}

			private Message ackMssg() {

				return new Message(MsgType.ACK,getIp(),getPort(),carMonitor.getList());
			}
		});
		
		thread.start();
	}
	

	private void handleHello(Message m) throws CorruptDataException {
		if(!(m.getData() instanceof StNode))
			throw new CorruptDataException();
		StNode node = (StNode) m.getData();
		if (isInZone( node.getPosition() ) ) {
//			Message msg = new Message(MsgType.HELLO_RESPONSE, getIp(),getPort(), new MT_HelloResponse(m.getId(), stNode, carNodes));
	//		carNodes.add(node); 
		
			Message msg = new Message(MsgType.HELLO_RESPONSE, getIp(),getPort(), new MT_HelloResponse(m.getId(), stNode, carMonitor.getList()));
			carMonitor.update(node);

			msgHandler.sendMsg(node, msg);

		}else {
			redirect(m);
		}
	}
	
	private boolean isInZone(Position pos) {
		
		/*if( (serchRedirect(pos)).equals(this.stNode)) {
			return true;
		}
		// TODO buscar si esta en la zona
		return false;*/
		return true;
	}

	
	private void handlePulse(Message msg) throws CorruptDataException {
		if(msg.getType() != MsgType.PULSE || ! (msg.getData() instanceof StNode )){
			throw new CorruptDataException();
		}
		StNode car = (StNode) msg.getData();
		logger.info("Pulse received on node: " + getStNode()+" from node: "+car);
		if(isInZone(car.getPosition()))
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
		msgHandler.sendMsg(carst, msg);
	}


	private StNode searchRedirect(Position position) {
		// TODO Auto-generated method stub
		return null;
	}

	private void ack(Message m) {
		
		//Message msg = new Message(MsgType.ACK,getIp(),getPort(),m.getId());
		
		//msgHandler.sendMsg((Messageable) m.getOrigin(), msg);
	}

	public List<Segment> getSegments() {
		return segments;
	}
}