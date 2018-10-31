package common;

import java.io.Serializable;
import java.time.Instant;

import network.Messageable;

/**
 * This class its the minimal model of a Node
 * 
 * @implSpec This class is immutable and thread-safe.
*/
public final class StNode implements Serializable, Messageable {
	private final String id;
	private final String ip;
	private final int port;
	private final Pulse pulse;	
	
	public StNode(String id, String ip, int port, Pulse pulse) {
		super();
		this.id = id;
		this.ip = ip;
		this.port = port;
		this.pulse = pulse;
	}

	/**
	 * Constructor for static nodes, velocity and timestamp are set to 0 and now() respectively
	 * @param id
	 * @param ip
	 * @param port
	 * @param position
	 */
	public StNode(String id, String ip, int port, Position position) {
		this(id,ip,port,new Pulse(position, 0, Instant.now()));
	}
	
	public Position getPosition() {
		return pulse.getPosition();
	}
	
	public Instant getTimestamp() {
		return this.pulse.getTimestamp();
	}
	
	public double getVelocity() {
		return this.pulse.getVelocity();
	}
	
	public StNode updatePulse(Pulse pulse) {
		return new StNode(id, ip, port, pulse);
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
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
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
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "StNode [id=" + id.substring(0, 5) + ", ip=" + ip + ", port=" +port+ "]";
	}
	
	
}
