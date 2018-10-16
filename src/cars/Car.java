package cars;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
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
	private StNode selectedHWNode;
	private ArrayList<StNode> farCars;
	
	public Car (Position position, double velocity) throws NoPeersFoundException {
		super();
		id = UUID.randomUUID().toString();
		this.position = position;
		this.velocity = velocity;
		msgCounter = BigInteger.valueOf(0);
		highWayNodes = new ArrayList<>();
		neighs = new ArrayList<>();
		farCars = new ArrayList<>();
		
		//first register in the highway network
		registerInNetwork();
		//then start listening for msgs with the MsgHandler
		msgHandler = new MsgHandler(this.port);
		msgHandler.addListener(this);
		
		
		emitPulses();
		monitorFarCars();
	}
	
	

	private void registerInNetwork() throws NoPeersFoundException {
		boolean registered = false;
		StNode highwNode;
		Iterator<StNode> nodIterator = highWayNodes.iterator();		
		while(!registered) {
			if(nodIterator.hasNext()) 
				highwNode = nodIterator.next();
			else 
				throw new NoPeersFoundException(); 
			if(tryRegister(highwNode))
				registered = true;
		}		
	}
	
	
	/**
	 * Attempts to register to a node, if it respond with a Redirect, makes a recursive call to the new node
	 * @param hwNode node to wich the registration will be attempted
	 * @return  returns true on success, false on failure.
	 */
	private boolean tryRegister(StNode hwNode) {
		try {
			if(hwNode==null)
				return false;
			// send hello			
			Message msg = new Message(nextIdMsg(),MsgType.HELLO, this.getPulse());
			MsgHandler.sendMsg(hwNode, msg);
			
			
			// receive response
			DatagramSocket receiveS = new DatagramSocket(this.port);
			byte[] packetBuffer = new byte[1024];
			DatagramPacket receiverPacket = new DatagramPacket(packetBuffer, packetBuffer.length);
			receiveS.setSoTimeout(HELLO_TIMEOUT);       // wait for response until timeout            
        	receiveS.receive(receiverPacket);
        	//decode response
        	ByteArrayInputStream bais = new ByteArrayInputStream(packetBuffer);
	      	ObjectInputStream ois = new ObjectInputStream(bais);
	      	Message m = (Message)ois.readObject();
	      	//bussiness logic 
        	switch(m.getType()) {
            	case HELLO_RESPONSE:{
            		Object data = m.getData();
            		//check and cast response, add neighs
            		if(data instanceof Iterable<?> ) {
            			Iterable<?> itData = (Iterable<?>) data;
            			for(Object obj : itData) {
            				if(itData instanceof StNode) {
            					StNode node = (StNode) obj;
                				if (isNear(node)) 
                					neighs.add(node);
                				else
                					farCars.add(node);		
            				} else 
            					throw new CorruptDataException();            				            					 				
            			}
            		} else {
            			throw new CorruptDataException(); 
            		}
            		selectedHWNode = hwNode;
            		return true;
            	}
            	
            	case REDIRECT: {
            		Object data = m.getData();
            		StNode hwNodeCandidate = null;
            		if(data instanceof StNode) {
            			hwNodeCandidate = (StNode) data;
            			
            		} else
            			throw new CorruptDataException(); 
            		return tryRegister(hwNodeCandidate);
            	}
            	default:{
            		//TODO log msg response of wrong type
            		System.out.println("Response from hw-node of wrong type");
            		return false;
            	}
        	}
        }
		catch (CorruptDataException e) {
			//TODO log
			System.out.println("corrupt data on hw-node response");
			return false;
		} 
        catch (SocketTimeoutException  e) {
            // timeout exception.
            //TODO log
        	System.out.println("node didnt respond in time");
        	//if it doesn't respond in time, try the next one
        	return false;
        }
		catch (SocketException e) {
			//TODO log
			System.out.println("problems opening socket");
			e.printStackTrace();
			return false;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}         
	}
	
	private String nextIdMsg() {
		msgCounter=msgCounter.add(BigInteger.ONE); //increment msgCounter
		return id+msgCounter;
	}



	private boolean isNear(StNode obj) {
		// TODO Auto-generated method stub
		return false;
	}

	private void emitPulses() {
		//call to method emitMessage() of the msgHandler
	}
	
	private void monitorFarCars() {
		// TODO Auto-generated method stub
		
	}

	public Pulse getPulse() {
		return new Pulse(id + msgCounter, position, velocity, Instant.now());
	}
	public static void main(String[] args) {
		String agusIP = "192.168.0.65";
		int port = 9000;
		
		Message msg = new Message("asdasID0",MsgType.HELLO, new Pulse("5555", new Position(0.0, 5.0,Units.KiloMeters), 4.5, Instant.now()));
		byte[] serializedMessage = msg.toByteArr();
		DatagramSocket socket;
		DatagramPacket udpPckt;
		try {
			socket = new DatagramSocket(5551);
			udpPckt = new DatagramPacket(serializedMessage, serializedMessage.length, InetAddress.getByName(agusIP) , port);
			socket.send(udpPckt);
			System.out.println("todo ok");
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			byte[] packetBuffer = new byte[1024];
			DatagramPacket receiverPacket = new DatagramPacket(packetBuffer, packetBuffer.length);
			DatagramSocket sockett = new DatagramSocket(9000);
			sockett.receive(receiverPacket);
			ByteArrayInputStream baos = new ByteArrayInputStream(packetBuffer);
		      ObjectInputStream oos = new ObjectInputStream(baos);
		      Message m = (Message)oos.readObject();
		      System.out.println(m.getType());
			
		} catch (Exception e) {
			
		}
		
	}



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
