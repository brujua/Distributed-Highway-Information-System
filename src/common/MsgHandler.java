package common;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MsgHandler implements MsgObservable{

	private int port;
	private ArrayList<MsgListener> listeners;
	private Thread listeningThread;
	private boolean listen;
	private MsgType msg;
	private ExecutorService threadService = Executors.newCachedThreadPool();
	private ResponseMonitor respMonitor;
	
	
	public MsgHandler( int port) {
		super();
		this.port = port;
		listen = true;
		listenForMsgs();
	}


	/*
	 * Starts a thread and listen for msgs
	 * this will need refactor if we doesn't want to lose incoming messages while processing one
	 * unless notify(), on the listener side, starts a thread to process the msg and doesn't block
	*/
	
	private void listenForMsgs() {
		// TODO Auto-generated method stub
		listeningThread = new Thread() {
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
						for (MsgListener listener : listeners) {
							listener.notify(m);
						}
					}
				} catch (Exception e) {
					
				}
				
			}
		};
		
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
			MsgHandler.sendMsg(messageable, msg);
		}
	};
	
	public static void sendMsg(Messageable dest, Message msg) {
		
		try {
			DatagramSocket sendSocket = new DatagramSocket();
			byte[] serializedHello = msg.toByteArr();
			DatagramPacket helloPckt;
			helloPckt = new DatagramPacket(serializedHello, serializedHello.length, InetAddress.getByName(dest.getIP()) , dest.getPort());
			sendSocket.send(helloPckt);
		} catch (Exception e) {
			// TODO log
			System.out.println("Error Mensaje no enviado");
			e.printStackTrace();
		}
	}
	
	
	

}
