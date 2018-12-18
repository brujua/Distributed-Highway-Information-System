package network;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;

public class TCPMsgReaderThread implements Runnable {

	private final static Logger logger = LoggerFactory.getLogger("Coordinator");

	MsgListener listener;
	Socket connection;

	public TCPMsgReaderThread(MsgListener listener, Socket connection) {
		this.listener = listener;
		this.connection = connection;
	}

	@Override
	public void run() {
		try {
			ObjectInputStream ois = new ObjectInputStream(connection.getInputStream());
			Message msg = (Message) ois.readObject();
			msg.setIp(connection.getInetAddress().getHostAddress());
			listener.msgReceived(msg);
			ois.close();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			logger.error("Received Corrupt msg");
		}
	}
}
