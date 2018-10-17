package common;

import java.io.ByteArrayInputStream;

import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class MsgHandler implements MsgObservable{

	private static final int DEFAULT_MAX_RETRIES = 3;
	private static final int DEFAULT_TIMEOUT = 200;
	
	private int port;
	private ArrayList<MsgListener> listeners;
	private boolean listen;
	private ExecutorService threadService = Executors.newCachedThreadPool();
	private ResponseMonitor respMonitor;
	
	public MsgHandler(int port) {
		this(port, DEFAULT_MAX_RETRIES);
	}
	
	public MsgHandler(int port, int maxRetries) {
		super();
		this.port = port;
		listen = true;
		// create the Response monitor that will track if an expected response was receive
		respMonitor = new ResponseMonitor();
		//start listening thread
		listenForMsgs();
	}

	/*
	 * Starts a thread and listen for msgs
	 * this will need refactor if we doesn't want to lose incoming messages while processing one
	 * unless notify(), on the listener side, starts a thread to process the msg and doesn't block
	*/
	private void listenForMsgs() {
		threadService.execute(new Runnable() {			
			@Override
			public void run() {
				try {
					DatagramSocket receiveS = new DatagramSocket(port);
					byte[] packetBuffer = new byte[1024];
					DatagramPacket receiverPacket = new DatagramPacket(packetBuffer, packetBuffer.length);
					ByteArrayInputStream bais = new ByteArrayInputStream(packetBuffer);
			      	ObjectInputStream ois = new ObjectInputStream(bais);
					while(listen) {
						receiveS.receive(receiverPacket);
		
						Message m = (Message)ois.readObject();
						if(!respMonitor.check(m))
							for (MsgListener listener : listeners) {
								listener.notify(m);
							}
					}
				} catch (Exception e) {
					//TODO log
					System.out.println("Problemas en el thread de escucha");
					e.printStackTrace();
				}
			}
		});		
	}

	@Override
	public void addListener(MsgListener l) {
		listeners.add(l);
		
	}

	@Override
	public void removeListener(MsgListener l) {
		listeners.remove(l);
	}
	
	/*
	 * Emits the msg to each of the targets on the list	
	*/
	public void emitMessage(List<Messageable> dest, Message msg) {
		for (Messageable messageable : dest) {
			this.sendMsg(messageable, msg);
		}
	};
	
	public void sendMsg(Messageable dest, Message msg) {
		
		try {
			DatagramSocket sendSocket = new DatagramSocket();
			byte[] serializedHello = msg.toByteArr();
			DatagramPacket helloPckt;
			helloPckt = new DatagramPacket(serializedHello, serializedHello.length, InetAddress.getByName(dest.getIP()) , dest.getPort());
			sendSocket.send(helloPckt);
			sendSocket.close();
		} catch (Exception e) {
			// TODO log
			System.out.println("Err Mensaje no enviado");
			e.printStackTrace();
		} finally {
		}
	}
	
	public CompletableFuture<Message> sendMsgWithResponse(Messageable dest, Message msg){
		return sendMsgWithResponse(dest, msg, DEFAULT_TIMEOUT, DEFAULT_MAX_RETRIES);
	}
	
	public CompletableFuture<Message> sendMsgWithResponse(Messageable dest, Message msg, int timeout, int maxtries){
		CompletableFuture<Message> response = new CompletableFuture<Message>();
		respMonitor.addMsg(msg, response);
		threadService.execute(new Runnable() {
			
			@Override
			public void run() {
				try {
					boolean receivedResp = false;
					for(int tries = 0;tries<maxtries && !receivedResp; tries++) {
						sendMsg(dest, msg);
						Thread.sleep(timeout);
						receivedResp = response.isDone();
					}
					
				
				} catch(Exception e) {
					System.out.println("Problemas enviando mensaje");
					//TODO log
					e.printStackTrace();
				}
					
			}
		});
		
		return response;
	}
}
