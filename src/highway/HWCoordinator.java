package highway;

import common.StNode;
import common.Util;
import network.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;


/**
 * Class responsible of maintain
 */
public class HWCoordinator implements Messageable, MsgListener {

	public static final String ip = "localhost";
	public static final int tentativePort = 5007;
	private final static Logger logger = LoggerFactory.getLogger(HWCoordinator.class);
	private final HWListManager hwlist;
	private String id = "0";
	private int port = 0;

	private MsgHandler msgHandler;

	public HWCoordinator(List<Segment> segments) {
		hwlist = new HWListManager(segments);
	}

	/**
	 * starts a thread that listens for connections while the listening flag its on.
	 * This thread starts a new thread for each new connection to read the message received.
	 *
	 * @return this -fluent api-
	 */
	public HWCoordinator listenForMsgs() {
		port = Util.getAvailablePort(tentativePort);
		msgHandler = new MsgHandler(port, "Coordinator");
		msgHandler.addMsgListener(this);
		msgHandler.listenForTCPMsgs();
		return this;
	}

	public void shutDown() {
		//TODO
		try {
			msgHandler.close();
			hwlist.shutDown();
			Thread.sleep(3000); //sleep in case a message its being attended.
		} catch (InterruptedException e) {
			logger.error("error shutting down");
		}
	}

	/**
	 * This function its call by the listening thread to inform of incoming msgs
	 *
	 * @param msg - received message
	 */
	public void msgReceived(Message msg) {
		try {

			switch (msg.getType()) {
				case REGISTER: {
					handleRegister(msg);
					break;
				}
				default: {
					logger.error("Received msg of wrong type");
					break;
				}
			}
		} catch (CorruptDataException e) {
			logger.error("Corrupted msg received");
		}
	}

	private void handleRegister(Message msg) throws CorruptDataException {
		if (msg.getType() != MsgType.REGISTER || !(msg.getData() instanceof StNode)) {
			throw new CorruptDataException();
		}
		//update ip for the one received on the packet
		StNode node = ((StNode) msg.getData()).changeIp(msg.getIp());

		logger.info("Register from node " + node);
		hwlist.add(node);
	}

	@Override
	public String getIP() {
		return ip;
	}

	@Override
	public int getPort() {
		return port;
	}

	@Override
	public String getId() {
		return id;
	}
}
