package highway;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.io.Serializable;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import common.CarMonitor;
import common.Position;

import common.StNode;
import network.CorruptDataException;
import network.MT_HelloResponse;
import network.MT_Redirect;
import network.Message;

import network.MsgHandler;
import network.MsgListener;
import network.MsgType;

public class HighWay implements MsgListener{

	public final String ip = "localhost";
	
	//listening port for cars
	public final int portCars = 5007;
	
	
	//listening port for Coordinator
	public final int portCoordinator = 8000;
	
	private final double MAX_RANGE = 0;
	
	//node identificator
	private String id;
	
	// Postion node datas
	private Position position;
	
	//Thi StNode
	private StNode stNode; 
	
	//MSG tools
	private BigInteger msgCounter;
	private MsgHandler msgHandler;
	private MsgHandler msgHandlerCoordinator;
	
	//Negighs HW nodes
	private ArrayList<StNode> neighs; //othrs node highway tolking to me 
	//private ArrayList<StNode> carNodes; //cars in my zone to shared
	//private SerializableList<StNode>

	private CarMonitor carMonitor;
	
	public HighWay (ArrayList<StNode> neighs , Position position) {
		super();
		this.position= position;
		this.neighs = neighs;
		this.id = UUID.randomUUID().toString();
		neighs = new ArrayList<>();
		//carNodes = new ArrayList<>();
	
		carMonitor = new CarMonitor(MAX_RANGE);
		
		
		msgHandler = new MsgHandler(this.portCars);		
		msgHandlerCoordinator = new MsgHandler(this.portCoordinator);
		msgHandler.addListener(this);
		msgHandlerCoordinator.addListener(this);
		
		stNode = new StNode(this.id,this.ip,this.portCars,position);
		
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
	
	
	
	
	
	public static void main(String[] args) {
		try {
			/*byte[] packetBuffer = new byte[1024];
			DatagramPacket receiverPacket = new DatagramPacket(packetBuffer, packetBuffer.length);
			DatagramSocket socket = new DatagramSocket(9000);
			socket.receive(receiverPacket);
			ByteArrayInputStream baos = new ByteArrayInputStream(packetBuffer);
		      ObjectInputStream oos = new ObjectInputStream(baos);
		      Message m = (Message)oos.readObject();
		      System.out.println(m.getType());
		      Pulse p =(Pulse) m.getData();
		      System.out.println(p.getMsgID());*/
			
			HighWay hw = new HighWay(new ArrayList<>(), new Position(0.0, 0.0));
			System.out.println(hw.getStNode());
		      
		} catch (Exception e) {
			e.printStackTrace();
		}

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
						pulseRecive((Pulse) m.getData());
						break;
					}
					case REDIRECT: {
						break;
					}
					case ALIVE: {
						break;
					}
					case ACK: {
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

	
	private void pulseRecive(Pulse pulse) {
		
	}

	private void redirect(Message m) {
		// TODO redirecccionar a hw correspondiente

		StNode hwRedirect = searchRedirect(((Position) m.getData()));
		MT_Redirect redirect = new MT_Redirect(m.getId(), hwRedirect);
		Message msg = new Message(MsgType.REDIRECT,getIp(),getPort(),redirect);

		StNode carst = new StNode(m.getId(),m.getIp(),m.getPort(),this.getPosition());
		//carNodes.add(carst); 
		carMonitor.update(carst);
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