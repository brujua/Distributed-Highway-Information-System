package network;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
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
	private Logger logger;

	public MsgHandler(int port, String name) {
		super();
		this.logger = LoggerFactory.getLogger(this.getClass().getName() + name);
		this.port = port;
		listen = true;
		listeners = new ArrayList<>();
		// create the Response monitor that will track if an expected response was received
		respMonitor = new ResponseMonitor();
		//start listening thread
		listenForMsgs();
	}

	public MsgHandler (int port) {
		this(port,"");
	}

	public static boolean sendTCPMsg(Messageable dest, Message msg) {
		try (Socket connection = new Socket(dest.getIP(), dest.getPort())) {
			ObjectOutputStream oos = new ObjectOutputStream(connection.getOutputStream());
			oos.flush();
			oos.writeObject(msg);
			return true;
		} catch (IOException e) {
			return false;
		}
	}

	/*
	 * Starts a thread and listen for msgs
	 * this will need refactor if we doesn't want to lose incoming messages while processing one
	 * unless msgReceived(), on the listener side, starts a thread to process the msg and doesn't block
	*/
	private void listenForMsgs() {
		threadService.execute(() -> {
			try {
				DatagramSocket receiveS = new DatagramSocket(port);
				while(listen) {
					if(Thread.currentThread().isInterrupted()) {
	                    logger.info("Listening thread interrupted");
	                    break;
	                }
					byte[] packetBuffer = new byte[4096];
					DatagramPacket receiverPacket = new DatagramPacket(packetBuffer, packetBuffer.length);
					ByteArrayInputStream bais = new ByteArrayInputStream(packetBuffer);
					receiveS.receive(receiverPacket);
					ObjectInputStream ois = new ObjectInputStream(bais);
					Message m = (Message)ois.readObject();
					//before msgReceived, update ip in msg to the real ip from which the msg was received
					m.setIp(receiverPacket.getAddress().toString());
					ois.close();
					if(!respMonitor.check(m))
						for (MsgListener listener : listeners) {
							listener.msgReceived(m);
						}
				}
			} catch(IOException | ClassNotFoundException e) {
				e.printStackTrace();
				logger.error("Unkown error on listening thread");
			}
		});
	}

	@Override
	public void removeListener(MsgListener l) {
		listeners.remove(l);
	}

	@Override
	public void addMsgListener(MsgListener l) {
		listeners.add(l);

	}

	public void sendMsg(Messageable dest, Message msg) {
		
		try(DatagramSocket sendSocket = new DatagramSocket()) {
			
			byte[] serializedHello = msg.toByteArr();
			DatagramPacket helloPckt;
			helloPckt = new DatagramPacket(serializedHello, serializedHello.length, InetAddress.getByName(dest.getIP()) , dest.getPort());
			sendSocket.send(helloPckt);
		}  catch (IOException e) {
			logger.error("IOException Error while sending msg to: "+ dest);
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
		CompletableFuture<Message> response = new CompletableFuture<>();
		respMonitor.addMsg(msg, response);
		threadService.execute(() -> {
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

			} catch (InterruptedException e) {
				logger.info("Interrupted while sleeping in sendMsgWithResponse()");
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

	/*
	 * Emits the msg to each of the targets on the list
	 */
	public void emitMessage(List<Messageable> dest, Message msg) {
		for (Messageable messageable : dest) {
			this.sendMsg(messageable, msg);
		}
	}
}
