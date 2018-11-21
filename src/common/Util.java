package common;

import java.net.BindException;
import java.net.DatagramSocket;
import java.net.SocketException;

public class Util {
	
	static public boolean isUDPPortOccupied(int port) {
	    DatagramSocket sock = null;
	    try {
	        sock = new DatagramSocket(port);
	        sock.close();
	        return false;
	    } catch (BindException ignored) {
	        return true;
	    } catch (SocketException ex) {
	        System.out.println(ex);
	        return true;
	    }
	}

	public static int getAvailablePort(int tentativePort) {
		int avPort = tentativePort;
		while(Util.isUDPPortOccupied(avPort)) {
			avPort++;
		}
		return avPort;
	}
}
