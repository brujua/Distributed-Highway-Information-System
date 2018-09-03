package cars;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.UUID;

import common.*;

public class Car implements MsgListener{
	private static final int HELLO_TIMEOUT = 1000;
	// temporary constants
	public final String ip = "localhost";
	public final int port = 5555;
	
	private String id;
	private Position position;
	private double velocity;
	private BigInteger msgCounter;
	private MsgHandler msgHandler;
	
	private ArrayList<StNode> highWayNodes; // centralized part of the network
	private ArrayList<StNode> neighs; //other cars near by
	
	public Car (Position position, double velocity) throws NoPeersFoundException {
		super();
		id = UUID.randomUUID().toString();
		this.position = position;
		this.velocity = velocity;
		msgCounter = BigInteger.valueOf(0);
		highWayNodes = new ArrayList<>();
		neighs = new ArrayList<>();
		//first register in the highway network
		registerInNetwork();
		//then start listening for msgs with the MsgHandler
		msgHandler = new MsgHandler(this.ip, this.port);
		msgHandler.addListener(this);
		
		
		emitPulses();
	}
	
	private void registerInNetwork() throws NoPeersFoundException {
		boolean registered = false;
		StNode highwNode;
		Iterator<StNode> nodIterator = highWayNodes.iterator();
		
		byte[] packetBuffer = new byte[1024];
		DatagramPacket receiverPacket = new DatagramPacket(packetBuffer, packetBuffer.length);
		ByteArrayInputStream bin = new ByteArrayInputStream(packetBuffer);
		DataInputStream din = new DataInputStream(bin);  
		
		while(!registered) {
			if(nodIterator.hasNext()) 
				highwNode = nodIterator.next();
			else 
				throw new NoPeersFoundException(); 
			try {
				//send hello
				Message msg = new Message(MsgType.HELLO, this.getPulse());
				byte[] serializedMessage = msg.toByteArr();
				DatagramPacket udpPckt = new DatagramPacket(serializedMessage, serializedMessage.length, InetAddress.getByName(highwNode.getIP()) , highwNode.getPort());
				
				DatagramSocket udpS = new DatagramSocket(this.port);
				//wait for response
				udpS.setSoTimeout(HELLO_TIMEOUT);  
		        while(true){    // receive data until timeout
		            try {
		                udpS.receive(receiverPacket);
		                registered=true;
		            }
		            catch (SocketTimeoutException e) {
		                // timeout exception.
		                //TODO log
		            	break;
		            }
		        }
		        
			} catch(Exception e) {
				e.printStackTrace();
				//TODO log
			}
		};
		
		
	}
	
	private void emitPulses() {
		//call to method emitMessage() of the msgHandler
	}

	public Pulse getPulse() {
		return new Pulse(id + msgCounter, position, velocity, Instant.now());
	}
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}



	@Override
	public void notify(Message m) {
		// TODO
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

}
