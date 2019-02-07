package network;

import common.StNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.UUID;

public class Message implements Serializable{

    public static final Logger logger = LoggerFactory.getLogger(Message.class);

    MsgType type;
    StNode sender;
    String id;
    String responseId;

    public Message(MsgType type, StNode sender, String responseId) {
        this.type = type;
        this.sender = sender;
        this.responseId = responseId;
        id = UUID.randomUUID().toString();
    }

    public Message(MsgType type, StNode sender) {
        this(type, sender, null);
    }


	public String getId() {
		return id;
	}
	
	public String getIp() {
        return sender.getIP();
    }

	public void setIp(String ip) {
        sender = sender.changeIp(ip);
    }

    public int getPort() {
        return sender.getPort();
	}

	public byte[] toByteArr() {
		ByteArrayOutputStream bStream = new ByteArrayOutputStream();		
		try {
			ObjectOutput oo = new ObjectOutputStream(bStream); 
			oo.writeObject(this);
			oo.close();	
		} catch (IOException e) {
            logger.error("Problems serializing msg:{}", e.getMessage());
		}			
		return bStream.toByteArray();
	}
	
	public String getResponseId() {
        return responseId;
    }

    public StNode getSender() {
        return sender;
	}

	@Override
	public String toString() {
        return "Message{id= " + id + "}";
    }

    public MsgType getType() {
        return type;
    }
}
