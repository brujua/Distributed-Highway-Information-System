package cars;

import java.io.Serializable;
import java.time.Instant;

import common.Position;

public class Pulse implements Serializable {
	//private String idSender;
	private String msgID;
	private Position position;
	private double velocity;
	private Instant timestamp;
	
	public Pulse(String msgID, Position position, double velocity, Instant timestamp) {
		super();
		this.msgID = msgID;
		this.position = position;
		this.velocity = velocity;
		this.timestamp = timestamp;
	}

	public String getMsgID() {
		return msgID;
	}

	public Position getPosition() {
		return position;
	}

	public double getVelocity() {
		return velocity;
	}

	public Instant getTimestamp() {
		return timestamp;
	}
	
}
