package highway;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.math.BigInteger;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.ArrayList;

import cars.Pulse;
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
		if (isInZone()) {
			Message msg = new Message(MsgType.HELLO_RESPONSE,getIp(),getPort(),carNodes);
			StNode carst = new StNode(m.getId(),m.getIp(),m.getPort());
			carNodes.add(carst); 
			msgHandler.sendMsg((Messageable) m, msg);
		}else {
			redirect(m);
		}
	}
	
	private boolean isInZone() {
		// TODO buscar si esta en la zona
		return false;
	}


	private void redirect(Message m) {
		// TODO redirecccionar a hw correspondiente
		StNode hwRedirect = serchRedirect(((Position) m.getData()));
		Message msg = new Message(MsgType.REDIRECT,getIp(),getPort(),hwRedirect);
		StNode carst = new StNode(m.getId(),m.getIp(),m.getPort());
		carNodes.add(carst); 
		msgHandler.sendMsg((Messageable) m, msg);
	}


	private StNode serchRedirect(Position position) {
		// TODO Auto-generated method stub
			StNode hwNode = null;
		return hwNode;
	}


	private void ack(Message m) {
		
		//Message msg = new Message(MsgType.ACK,getIp(),getPort(),m.getId());
		
		//msgHandler.sendMsg((Messageable) m.getOrigin(), msg);
	}
	
}
