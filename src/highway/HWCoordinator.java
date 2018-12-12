package highway;

import common.HWNodeList;
import common.StNode;
import common.Util;
import network.CorruptDataException;
import network.Message;
import network.MsgHandler;
import network.MsgType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 *  Class responsible of maintain
 */
public class HWCoordinator {

	private final static Logger logger = LoggerFactory.getLogger(HWCoordinator.class);

	public static final String ip = "localhost";

	public static final int tentativePort = 5007;

	private List<HWListNode> nodes;
	private double begin,end; // highway range
	private int port= 0;

	private ExecutorService threadService = Executors.newCachedThreadPool();
	private ServerSocket serverSocket;
	private boolean listening;

	public HWCoordinator(double begin, double end) {
		this.begin = begin;
		this.end = end;
		nodes = new ArrayList<>();
	}

	public void listenForMsgs(){
		port = Util.getAvailablePort(tentativePort);
		try {
			while (listening){
				Socket accept = serverSocket.accept();
				threadService.execute(() ->{

				});
			}
		} catch (IOException e) {
			e.printStackTrace();
		}


	}

	public void shutDown(){
		//TODO
		try {
			listening = false;
			Thread.sleep(3000);
			serverSocket.close();
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * This function its call by the listening thread to notify of incoming msgs
	 * @param msg received message
	 */
	public void msgReceived(Message msg) {
		try{

			switch(msg.getType()){
				case REGISTER: {
					handleRegister(msg);
					break;
				}
				default: {
					logger.error("Received msg of wrong type");
					break;
				}
			}
		} catch (CorruptDataException e){
			logger.error("Corrupted msg received");
		}
	}

	private void handleRegister(Message msg) throws CorruptDataException {
		if(msg.getType() != MsgType.REGISTER || ! (msg.getData() instanceof StNode)){
			throw new CorruptDataException();
		}
		StNode node = (StNode) msg.getData();
		logger.info("Register from node " + node);
	}
}
