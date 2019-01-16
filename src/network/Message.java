package network;

import java.io.*;
import java.util.UUID;

public class Message implements Serializable{
	private MsgType type;
	private String origIp;	//origin
	private int origPort;	//origin
	private String id;
	private Object data;
	
	


	public Message(MsgType type,String ip, int port, Object data) {
		super();
		this.type = type;
		this.data = data;
		this.id = UUID.randomUUID().toString();
		this.origIp = ip;
		this.origPort = port;
	}
	
	
	public MsgType getType() {
		return type;
	}
	public Object getData() {
		return data;
	}
	
	public String getId() {
		return id;
	}
	
	public String getIp() {
		return origIp;
	}		

	public int getPort() {
		return origPort;
	}
	
	public void setIp(String ip) {
		this.origIp = ip;
	}

	public void setPort(int port) {
		this.origPort = port;
	}

	public byte[] toByteArr() {
		ByteArrayOutputStream bStream = new ByteArrayOutputStream();		
		try {
			ObjectOutput oo = new ObjectOutputStream(bStream); 
			oo.writeObject(this);
			oo.close();	
		} catch (IOException e) {
			// TODO log error 
			e.printStackTrace();
		}			
		return bStream.toByteArray();
	}
	
	public String getResponseId() {
		switch(this.type) {
		case ACK:
			if(data instanceof String)
				return (String) data;
			break;
		case HELLO_RESPONSE:
			if(data instanceof MT_HelloResponse) {
				MT_HelloResponse resp = (MT_HelloResponse) data;
				return resp.getResponseId();
			}
			break;
		case REDIRECT:
			if(data instanceof MT_Redirect) {
				MT_Redirect resp = (MT_Redirect) data;
				return resp.getResponseId();
			}
			break;
		default:
			break;		
		}
		return null;
	}

	@Override
	public String toString() {
		return "Message{" +
				"type=" + type +
				", origIp='" + origIp + '\'' +
				", origPort=" + origPort +
				"data=" + data + '}';
	}
}
