package common;

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
	
	
}
