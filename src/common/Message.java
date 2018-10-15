package common;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class Message implements Serializable{
	private MsgType type;
	private int ip;
	private int port;
	private String id;
	private Object data;
	
	
	public Message(String id, MsgType type, Object data) {
		super();
		this.type = type;
		this.data = data;
		this.id = id;
	}
	
	public MsgType getType() {
		return type;
	}
	public Object getData() {
		return data;
	}
	
	public int getIp() {
		return ip;
	}

	public void setIp(int ip) {
		this.ip = ip;
	}

	public int getPort() {
		return port;
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
		case HELLO:
			break;
		case HELLO_RESPONSE:
			break;
		case PULSE:
			break;
		case REDIRECT:
			break;
		default:
			break;
		
		}
	}
	
	
}
