package common;

import java.io.IOException;
import java.net.BindException;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

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

    public static List<StNode> readNodeConfigFile(String filename) {
        ResourceBundle resourceBundle = ResourceBundle.getBundle(filename);
        List<StNode> nodes = new ArrayList<>();
        int numberOfPossibleNodes = Integer.valueOf(resourceBundle.getString("nodenumber"));
        for (int i = 0; i < numberOfPossibleNodes; i++) {
            String ip = resourceBundle.getString("node" + i);
            int port_start = Integer.valueOf(resourceBundle.getString("port_range_start" + i));
            int port_end = Integer.valueOf(resourceBundle.getString("port_range_end" + i));
            for (int port = port_start; port <= port_end; port++) {
                nodes.add(new StNode("0", ip, port));
            }
        }
        return nodes;
    }
}


