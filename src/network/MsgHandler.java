package network;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;


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
		listeners = new ArrayList<MsgListener>();
			
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
					
			      	
					while(listen) {
						if(Thread.currentThread().isInterrupted()) {
							//TODO log
		                    System.out.println("Listening thread interrupted");
		                    break;
		                }
						byte[] packetBuffer = new byte[4096];
						DatagramPacket receiverPacket = new DatagramPacket(packetBuffer, packetBuffer.length);
						ByteArrayInputStream bais = new ByteArrayInputStream(packetBuffer);
						receiveS.receive(receiverPacket);
						ObjectInputStream ois = new ObjectInputStream(bais);
						Message m = (Message)ois.readObject();
						ois.close();
						if(!respMonitor.check(m))
							for (MsgListener listener : listeners) {
								listener.notify(m);
							}
					}
				} catch(IOException | ClassNotFoundException e) {
					e.printStackTrace();
					System.out.println("problemas thread escucha puerto " + port);
				}
				
				/*catch (Exception e) {
					//TODO log
					System.out.println("Problemas en el thread de escucha");
					e.printStackTrace();
				}*/
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
		
		try(DatagramSocket sendSocket = new DatagramSocket();) {
			
			byte[] serializedHello = msg.toByteArr();
			DatagramPacket helloPckt;
			helloPckt = new DatagramPacket(serializedHello, serializedHello.length, InetAddress.getByName(dest.getIP()) , dest.getPort());
			sendSocket.send(helloPckt);
			sendSocket.close();
		} catch (Exception e) {
			// TODO log
			System.out.println("Err Mensaje no enviado");
			e.printStackTrace();
		}
		
	}
	
	/**
	 * @param dest
	 * @param msg
	 * @return
	 */
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
					if(!receivedResp) {
						response.completeExceptionally(new TimeoutException("retries exceeded"));
					}
					// sleep some in case a duplicate response arrives and then remove from monitor
					Thread.sleep(DEFAULT_TIMEOUT);
					respMonitor.remove(msg);
				
				} catch(Exception e) {
					System.out.println("Problemas enviando mensaje");
					//TODO log
					e.printStackTrace();
				}
					
			}
		});
		
		return response;
	}
	
	/**
	 * Stops listening and sending, interrupting all threads that are currently running 
	 */
	public void close() {
		threadService.shutdownNow();
	}
}
