package common;

import java.io.Serializable;

/*
 * Structure of information of a Node
*/
public class StNode implements Serializable, Messageable {
	private String id;
	private String ip;
	private int port;
	//Pulse lastestData;
	
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
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((ip == null) ? 0 : ip.hashCode());
		result = prime * result + port;
		return result;
	}



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
		if (ip == null) {
			if (other.ip != null)
				return false;
		} else if (!ip.equals(other.ip))
			return false;
		if (port != other.port)
			return false;
		return true;
	}



	@Override
	public Messageable getOrigin() {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	
	
}
