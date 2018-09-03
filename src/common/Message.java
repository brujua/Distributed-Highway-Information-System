package common;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class Message implements Serializable{
	private MsgType type;
	private Object data;
	
	
	public Message(MsgType type, Object data) {
		super();
		this.type = type;
		this.data = data;
	}
	
	public MsgType getType() {
		return type;
	}
	public Object getData() {
		return data;
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
	
	
}
