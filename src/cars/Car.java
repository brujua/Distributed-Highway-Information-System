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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import common.*;
import network.CorruptDataException;
import network.MT_HelloResponse;
import network.MT_Redirect;
import network.Message;
import network.MsgHandler;
import network.MsgListener;
import network.MsgType;

public class Car implements MsgListener{

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
		
		//initialize the MsgHandler
		msgHandler = new MsgHandler(this.port);
		msgHandler.addListener(this);
		
		//register in the highway network
		registerInNetwork();
		
		emitPulses();
		monitorFarCars();
	}
	
	

	private void registerInNetwork() throws NoPeersFoundException {
		boolean registered = false;
		StNode highwNode;
		Iterator<StNode> nodIterator = highWayNodes.iterator();		
		while(!registered) {
			if(nodIterator.hasNext()) {
				highwNode = nodIterator.next();
				registered = tryRegister(highwNode);
			} else 
				throw new NoPeersFoundException(); 
		}		
	}
	
	
	/**
	 * Attempts to register to a node, if it respond with a Redirect, makes a recursive call to the new node
	 * @param hwNode node to wich the registration will be attempted
	 * @return  returns true on success, false on failure.
	 */
	private boolean tryRegister(StNode hwNode) {
		CompletableFuture<Message> response;
		Message responseMsg;
		try {
			if(hwNode==null)
				return false;
			// send hello and wait for response			
			Message msg = new Message(MsgType.HELLO,this.ip,this.port, this.getPulse());
			response=msgHandler.sendMsgWithResponse(hwNode, msg);
			responseMsg = response.get();
		
	      	//bussiness logic 
        	switch(responseMsg.getType()) {
            	case HELLO_RESPONSE:{
            		handleHelloResponse(responseMsg);
            		return true;
        			}      	
            	case REDIRECT: {
            		return handleRedirect(responseMsg);
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
        catch (ExecutionException  e) {
        	try {
        		throw e.getCause();
        	} catch(TimeoutException toe) {
        		System.out.println("node doesn't respond");
        		//if it doesn't respond in time, try the next one
            	return false;
        		
        	} catch (Throwable e1) {
				e1.printStackTrace();
				return false;
			}       	
        } catch (InterruptedException e) {
			System.out.println("Interrupted while trying to register and waiting response");
			return false;
			
		}
	}
	
	/**
	 * @param responseMsg
	 * @throws CorruptDataException
	 */
	private void handleHelloResponse(Message responseMsg) throws CorruptDataException {
		Object data = responseMsg.getData();
		//check and cast response, add neighs
		if(!(data instanceof MT_HelloResponse))
			throw new CorruptDataException();             			
		MT_HelloResponse helloRsp = (MT_HelloResponse) data;
		addMultipleCars(helloRsp.getCars());		
		selectedHWNode = helloRsp.getStNode();		
	}
	

	/**
	 * @param cars
	 */
	private void addMultipleCars(Iterable<StNode> cars) {
		for(StNode car : cars) {
			//TODO
		}
		
	}



	private boolean handleRedirect(Message redirectMsg) throws CorruptDataException{
		Object data = redirectMsg.getData();
		MT_Redirect redi = null;
		if(!(data instanceof MT_Redirect)) 
			throw new CorruptDataException();
		redi = (MT_Redirect) data;           	
		return tryRegister(redi.getRedirectedNode());
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
		return new Pulse(position, velocity, Instant.now());
	}
	public static void main(String[] args) {
		String agusIP = "192.168.0.65";
		int port = 9000;
		
		Message msg = new Message(MsgType.HELLO,agusIP,port, new Pulse(new Position(0.0, 5.0,Unit.KiloMeters), 4.5, Instant.now()));
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
