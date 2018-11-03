package highway;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.math.BigInteger;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.ArrayList;

import cars.Pulse;
import common.MT_HelloResponse;
import common.MT_Redirect;
import common.Message;
import common.Messageable;
import common.MsgHandler;
import common.MsgListener;
import common.MsgType;
import common.Position;
import common.StNode;

public class HighWay implements MsgListener{

	public final String ip = "localhost";
	
	//port server from cars
	public final int port = 9000;
	
	
	//port connections from Coordinator
	public final int portCoordinator = 8000;
	
	
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
	private ArrayList<StNode> carNodes; //cars in my zone to shared
	
	
	
	public HighWay ( ArrayList<StNode> neighs,Position position) {
		super();
		this.position= position;
		this.neighs = neighs;

				
		msgHandler = new MsgHandler(this.port);
		msgHandler.addListener(this);
		
		msgHandlerCoordinator = new MsgHandler(this.port);
		msgHandlerCoordinator.addListener(this);
		
		stNode = new StNode(this.id,this.ip,this.port);
		
	}

	
	public ArrayList<StNode> getCarNodes(){
		
		return this.carNodes;
		
		
	}
	
	private void addCarNode (StNode car){
		synchronized (this.carNodes) {
			this.carNodes.add(car);
		
		}
	}
	private String nextIdMsg() {
		msgCounter=msgCounter.add(BigInteger.ONE); //increment msgCounter
		return id+msgCounter;
	}

	
	private void removeCarNode (StNode car){
		
		synchronized (this.carNodes) {
			ArrayList<StNode> auxlist = new ArrayList<StNode>();
			
			for(StNode carNode: this.carNodes) {
				if (carNode.getId() != car.getId()) {
					auxlist.add(carNode);
				}
			}
			
			this.carNodes = auxlist;
		}
	}
	
	public void startHighWay() {
		
	}
	
	
	
	
	
//	public static void main(String[] args) {
//		try {
//			byte[] packetBuffer = new byte[1024];
//			DatagramPacket receiverPacket = new DatagramPacket(packetBuffer, packetBuffer.length);
//			DatagramSocket socket = new DatagramSocket(9000);
//			socket.receive(receiverPacket);
//			ByteArrayInputStream baos = new ByteArrayInputStream(packetBuffer);
//		      ObjectInputStream oos = new ObjectInputStream(baos);
//		      Message m = (Message)oos.readObject();
//		      System.out.println(m.getType());
//		      Pulse p =(Pulse) m.getData();
//		      System.out.println(p.getMsgID());
//		      
//		} catch (Exception e) {
//			
//		}
//
//	}
	
	
	private String getIp() {
		return ip;
	}


	private int getPort() {
		return port;
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
				switch(m.getType()) {
					case HELLO: {
						hello(m);
						break;
					}
					case PULSE: {
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
			}
		});
		
		thread.start();
		return;	
	}
	
	private void hello(Message m) {
		
		if (isInZone( ( (Pulse) m.getData() ).getPosition() ) ) {
			MT_HelloResponse hResponse = new MT_HelloResponse(m.getId(), stNode, carNodes);
			Message msg = new Message(MsgType.HELLO_RESPONSE,getIp(),getPort(),hResponse);
			StNode carst = new StNode(m.getId(),m.getIp(),m.getPort());
			carNodes.add(carst); 
			msgHandler.sendMsg(carst, msg);
		}else {
			redirect(m);
		}
	}
	
	private boolean isInZone(Position pos) {
		
		if( (serchRedirect(pos)).equals(this.stNode)) {
			return true;
		}
		// TODO buscar si esta en la zona
		return false;
	}


	private void redirect(Message m) {
		// TODO redirecccionar a hw correspondiente
		StNode hwRedirect = serchRedirect(((Position) m.getData()));
		MT_Redirect redirect = new MT_Redirect(m.getId(), hwRedirect);
		Message msg = new Message(MsgType.REDIRECT,getIp(),getPort(),redirect);
		StNode carst = new StNode(m.getId(),m.getIp(),m.getPort(),this.getPosition());
		carNodes.add(carst); 
		msgHandler.sendMsg(carst, msg);
	}


	private StNode serchRedirect(Position position) {
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
