package highway;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.math.BigInteger;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.ArrayList;

import cars.Pulse;
import common.Message;
import common.MsgHandler;
import common.MsgListener;
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
	
	
	@Override
	public void notify(Message m) {

		// The logic of the received msg will be handled on a different thread
		Thread thread = new Thread( new Runnable() {
			public void run() {
				switch(m.getType()) {
					case HELLO: {
						break;
					}
					case PULSE: {
						break;
					}
					case REDIRECT: {
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
	
	
}
