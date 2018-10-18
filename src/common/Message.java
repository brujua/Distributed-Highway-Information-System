package common;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.UUID;

public class Message implements Serializable{
	private MsgType type;
	private String ip;
	private int port;
	private String id;
	private Object data;
	private Messageable Origin;
	
	


	public Message(MsgType type,String ip, int port, Object data) {
		super();
		this.type = type;
		this.data = data;
		this.id = UUID.randomUUID().toString();;
		this.ip = ip;
		this.port = port;
	}
	

	public Messageable getOrigin() {
		return Origin;
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
		return ip;
	}		

	public int getPort() {
		return port;
	}
	
	public void setIp(String ip) {
		this.ip = ip;
	}

	public void setPort(int port) {
		this.port = port;
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
	
	
}
