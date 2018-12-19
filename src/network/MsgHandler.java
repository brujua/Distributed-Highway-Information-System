package network;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.*;
import java.util.ArrayList;
import java.util.concurrent.*;

public class MsgHandler implements MsgObservable{

	private static final int DEFAULT_MAX_RETRIES = 3;
	private static final int DEFAULT_TIMEOUT = 200;
	
	private int port;
	private ArrayList<MsgListener> listeners;
	private boolean listening;
	private ExecutorService threadService = Executors.newCachedThreadPool();
	private ResponseMonitor respMonitor;
	private Logger logger;

	public MsgHandler(int port, String name) {
		super();
		this.logger = LoggerFactory.getLogger(this.getClass().getName() + name);
		this.port = port;
		listening = false;
		listeners = new ArrayList<>();
		respMonitor = null; // will be instantiated only when listening for UDP
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

	public void listenForTCPMsgs() {
		if (listening)
			throw new IllegalStateException("Already listening");
		//throw new InvalidStateException("Already listening");
		listening = true;
		threadService.execute(() -> {
			try (ServerSocket serverSocket = new ServerSocket(port)) {
				// sets timeout to the socket.accept() so that this listening thread can be interrupted
				serverSocket.setSoTimeout(2000);
				while (listening) {
					if (Thread.currentThread().isInterrupted()) {
						logger.info("Listening thread interrupted");
						break;
					}
					try {
						Socket connection = serverSocket.accept();
						TCPMsgReaderThread msgReaderT = new TCPMsgReaderThread((message) -> {
							for (MsgListener listener : listeners) {
								listener.msgReceived(message);
							}
						}, connection);

						if (!threadService.isShutdown())
							threadService.execute(msgReaderT);
					} catch (SocketTimeoutException e) {
						continue;
					}
				}
			} catch (IOException e) {
				logger.error("Problems opening TCP listening socket: " + e.getMessage());
			}
		});
	}

	/*
	 * Starts a thread and listening for msgs
	 * this will need refactor if we doesn't want to lose incoming messages while processing one
	 * unless msgReceived(), on the listener side, starts a thread to process the msg and doesn't block
	 */
	public void listenForUDPMsgs() {
		// create the Response monitor that will track if an expected response was received
		if (listening)
			throw new IllegalStateException("Already listening");
		listening = true;
		respMonitor = new ResponseMonitor();
		threadService.execute(() -> {
			try {
				DatagramSocket receiveS = new DatagramSocket(port);
				//sets timeout so that the listing thread can be interrupted
				receiveS.setSoTimeout(2000);
				while (listening) {
					if(Thread.currentThread().isInterrupted()) {
	                    logger.info("Listening thread interrupted");
	                    break;
	                }
					byte[] packetBuffer = new byte[4096];
					DatagramPacket receiverPacket = new DatagramPacket(packetBuffer, packetBuffer.length);
					ByteArrayInputStream bais = new ByteArrayInputStream(packetBuffer);
					try {
						receiveS.receive(receiverPacket);
						ObjectInputStream ois = new ObjectInputStream(bais);
						Message m = (Message) ois.readObject();
						//before msgReceived, update ip in msg to the real ip from which the msg was received
						m.setIp(receiverPacket.getAddress().getHostAddress());
						ois.close();
						if (!respMonitor.check(m))
							for (MsgListener listener : listeners) {
								listener.msgReceived(m);
							}
					} catch (SocketTimeoutException e) {
						continue;
					}
				}
			} catch(IOException | ClassNotFoundException e) {
				logger.error("Unkown error on listening thread: " + e.getMessage());
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

	public void sendUDP(Messageable dest, Message msg) {
		
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
	public CompletableFuture<Message> sendUDPWithResponse(Messageable dest, Message msg) {
		return sendUDPWithResponse(dest, msg, DEFAULT_TIMEOUT, DEFAULT_MAX_RETRIES);
	}

	public CompletableFuture<Message> sendUDPWithResponse(Messageable dest, Message msg, int timeout, int maxtries) {
		CompletableFuture<Message> response = new CompletableFuture<>();
		respMonitor.addMsg(msg, response);
		threadService.execute(() -> {
			try {
				boolean receivedResp = false;
				for(int tries = 0;tries<maxtries && !receivedResp; tries++) {
					sendUDP(dest, msg);
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
				logger.info("Interrupted while sleeping in sendUDPWithResponse()");
			}
		});
		
		return response;
	}

	public CompletableFuture<Message> sendTCPWithResponse(Messageable dest, Message msg) {
		return sendTCPWithResponse(dest, msg, DEFAULT_TIMEOUT, DEFAULT_MAX_RETRIES);
	}

	public CompletableFuture<Message> sendTCPWithResponse(Messageable dest, Message msg, int timeout, int maxtries) {
		CompletableFuture<Message> response = new CompletableFuture<>();
		respMonitor.addMsg(msg, response);
		threadService.execute(() -> {
			try {
				boolean receivedResp = false;
				for(int tries = 0;tries<maxtries && !receivedResp; tries++) {
					sendTCPMsg(dest, msg);
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
				logger.info("Interrupted while sleeping in sendUDPWithResponse()");
			}
	});

	return response;
}


	
	/**
	 * Stops listening and sending, interrupting all threads that are currently running 
	 */
	public void close() {
		try {
            listening = false;
			threadService.shutdown();
			threadService.awaitTermination(3200, TimeUnit.MILLISECONDS);
			threadService.shutdownNow();

		} catch (InterruptedException e) {
			logger.error("Interrupted while awaiting termination of submitted tasks");
		}
	}

	public int getPort() {
		return port;
	}
}
