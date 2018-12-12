package highway;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import common.*;

import network.CorruptDataException;
import network.MT_HelloResponse;
import network.MT_Redirect;
import network.Message;

import network.MsgHandler;
import network.MsgListener;
import network.MsgType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class HighWay implements MsgListener{
	private Logger logger;

	public static final String ip = "localhost";
	
	public static final int tentativePortCars = 5007;

	public static final int tentativePortCoordinator = 8000;
	
	private static final double MAX_RANGE = 0;

	private String id;
	
	// Postion node datas
	private Position position;
	
	private StNode stNode;
	private int portCars;
	private int portCoordinator;
	
	//MSG tools
	private MsgHandler msgHandler;
	private MsgHandler msgHandlerCoordinator;
	
	//Negighs HW nodes
	private ArrayList<StNode> neighs; //othrs node highway tolking to me 

	private HWNodeList HWneighs;

	//private ArrayList<StNode> carNodes; //cars in my zone to shared
	//private SerializableList<StNode>

	private CarMonitor carMonitor;

	public HighWay (ArrayList<StNode> neighs , Position position) {
		super();
		this.position= position;
		this.neighs = neighs;
		id = UUID.randomUUID().toString();
		logger = LoggerFactory.getLogger(getName());
		portCars = Util.getAvailablePort(tentativePortCars);
		portCoordinator = Util.getAvailablePort(tentativePortCoordinator);
		//neighs = new ArrayList<>();

	
		carMonitor = new CarMonitor(MAX_RANGE,null, getName());
		
		HWneighs = new HWNodeList(position);
		//HWneighs.addAll(neighs);


		msgHandler = new MsgHandler(portCars,getName());
		msgHandlerCoordinator = new MsgHandler(portCoordinator,getName());
		msgHandler.addListener(this);
		msgHandlerCoordinator.addListener(this);
		
		stNode = new StNode(id,ip,portCars,position);
		
	}

	private String getName() {
		return "HW"+ id.substring(0,5);
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
//	private String nextIdMsg() {
//		msgCounter=msgCounter.add(BigInteger.ONE); //increment msgCounter
//		return id+msgCounter;
//	}

	
	private void removeCarNode (StNode car){
		/*
		synchronized (this.carNodes) {
			ArrayList<StNode> auxlist = new ArrayList<StNode>();
			
			for(StNode carNode: this.carNodes) {
				if (carNode.getId() != car.getId()) {
					auxlist.add(carNode);
				}
			}
			
			this.carNodes = auxlist;
		}*/
	}
	
	public void startHighWay() {
		
	}
	
	
	
	


	
	
	private String getIp() {
		return ip;
	}


	private int getPort() {
		return portCars;
	}
	
	public StNode getStNode() {
		return new StNode(id, ip, portCars, position);
	}


	private ArrayList<StNode> getNeighs() {
		return neighs;
	}


	public Position getPosition() {
		return position;
	}

	
	@Override
	public void notify(Message m) {

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
		return;	
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

		StNode carst = new StNode(m.getId(),m.getIp(),m.getPort(),this.getPosition());

		//carMonitor.update(carst);
		// wait until carmonitor removes the car because of timeout
		msgHandler.sendMsg(carst, msg);
	}


	private StNode searchRedirect(Position position) {
	// TODO Auto-generated method stub
		Double minDistance = this.position.distance(position);
		Position pos = this.position;
		//StNode nearHwNode = new StNode(this.id,this.ip,this.port);
		StNode nearHwNode = stNode;
		for(StNode hwNode : this.getNeighs()) {
			if(minDistance > (position.distance(hwNode.getPosition()))) {
				minDistance = position.distance(hwNode.getPosition());
				nearHwNode = hwNode;
			}
		}
		
		return nearHwNode;
	}




	private void ack(Message m) {
		
		//Message msg = new Message(MsgType.ACK,getIp(),getPort(),m.getId());
		
		//msgHandler.sendMsg((Messageable) m.getOrigin(), msg);
	}
	
}