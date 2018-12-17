package common;

import network.Messageable;

import java.io.Serializable;
import java.util.Objects;

/**
 *  Lightweight representation of a node. Fulfills the purpose of identifying it and storing the data to send messages to it.
 * 
 * @implSpec This class is immutable and thread-safe.
*/
public final class StNode implements Serializable, Messageable {
	private final String id;
	private final String ip;
	private final int port;

	//private final Pulse pulse;



//	private static final double DEFAULT_POSITION_X = 0;
//	private static final double DEFAULT_POSITION_Y = 0;
//	private static final Units DEFAULT_UNIT = Units.KiloMeters;


//	public StNode(String id, String ip, int port) {
//		this(id,ip,port,new Position(DEFAULT_POSITION_X, DEFAULT_POSITION_Y, DEFAULT_UNIT));
//	}


	public StNode(String id, String ip, int port) {
		super();

		this.id = id;
		this.ip = ip;
		this.port = port;
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
        if (id.length() >= 5)
            return "StNode[id=" + id.substring(0, 5) + "]";
        return "StNode[id=" + id + "]";
	}


	public StNode changeIp(String ip) {
		return new StNode(id, ip, port);
	}

/*	public Pulse getPulse() {
		return pulse;
	}*/

}
