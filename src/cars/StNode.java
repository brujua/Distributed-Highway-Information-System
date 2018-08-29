package cars;

import java.io.Serializable;

/*
 * Structure of information of a Node
*/
public class StNode implements Serializable {
	private String id;
	private String ip;
	private int port;
	Pulse lastestData;
	
	public StNode(String id, String ip, int port) {
		super();
		this.id = id;
		this.ip = ip;
		this.port = port;
	}
	
	
	
}
