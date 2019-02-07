package common;

import network.Messageable;

import java.io.Serializable;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Objects;

/**
 *  Lightweight representation of a node. Fulfills the purpose of identifying it and storing the data to send messages to it.
 * 
 * @implSpec This class is immutable and thread-safe.
*/
public final class StNode implements Serializable, Messageable {
    private static final String DEFAULT_IP = "127.0.0.1";
	private final String id;
	private final String ip;
	private final int port;


    public StNode(String id, String ip, int port) {
        if (ip == null) {
            String ipAux; //need aux var to avoid "might be already initialized" compiler error.
            try (final DatagramSocket socket = new DatagramSocket()) {
                socket.connect(InetAddress.getByName("8.8.8.8"), 10002);
                ipAux = socket.getLocalAddress().getHostAddress();
            } catch (SocketException | UnknownHostException e) {
                ipAux = DEFAULT_IP;
            }
            this.ip = ipAux;
        } else
            this.ip = ip;

        this.id = id;
        this.port = port;
    }

    public StNode(String id, int port) {
        this(id, null, port);
    }

	@Override
	public String getIP() {
		return this.ip;
	}


	@Override
	public int getPort() {
		return this.port;
	}



	@Override
	public String getId() {

		return this.id;
	}



	@Override
	public int hashCode() {
		return Objects.hash(id);
	}



	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 * this equals only evaluates for equal id
	 * so, if two StNodes has different attributes but same id, are considered equal
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		StNode other = (StNode) obj;
		if (id == null) {
			return other.id == null;
		} else return id.equals(other.id);
	}

	@Override
	public String toString() {
        String idStr = (id.length() >= 5) ? id.substring(0, 5) : id;
        return "StNode[id=" + idStr + " ip=" + ip + " port=" + port + "]";
	}


	public StNode changeIp(String ip) {
		return new StNode(id, ip, port);
	}

}
