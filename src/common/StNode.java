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
	
	
	
}
