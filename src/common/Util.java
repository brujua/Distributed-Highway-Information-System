package common;

import java.io.IOException;
import java.net.BindException;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.SocketException;

public class Util {
	
	static public boolean isPortOccupied(int port) {
	    DatagramSocket sock = null;
		ServerSocket socktcp = null;
	    try {
	        sock = new DatagramSocket(port);
	        sock.close();
	        socktcp = new ServerSocket(port);
	        socktcp.close();
	        return false;
	    } catch (BindException ignored) {
	        return true;
	    } catch (SocketException ex) {
	        System.out.println(ex);
	        return true;
	    } catch (IOException e) {
			return true;
		}
	}

	public static int getAvailablePort(int tentativePort) {
		int avPort = tentativePort;
		while(Util.isPortOccupied(avPort)) {
			avPort++;
		}
		return avPort;
	}
}
