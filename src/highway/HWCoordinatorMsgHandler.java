package highway;

import network.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;

public class HWCoordinatorMsgHandler implements Runnable {

	private final static Logger logger = LoggerFactory.getLogger("Coordinator");

	HWCoordinator coord;
	Socket connection;

	public HWCoordinatorMsgHandler(HWCoordinator coord, Socket connection) {
		this.coord = coord;
		this.connection = connection;
	}

	@Override
	public void run() {
		try {
			ObjectInputStream ois = new ObjectInputStream(connection.getInputStream());
			Message msg = (Message) ois.readObject();
			msg.setIp(connection.getInetAddress().toString());
			coord.msgReceived(msg);

		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			logger.error("Received Corrupt msg");
		}
	}
}
