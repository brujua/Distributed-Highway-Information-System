package cars;

import java.io.Serializable;
import java.time.Instant;

public class Pulse implements Serializable {
	//private String idSender;
	private String msgID;
	private String position;
	private double velocity;
	private Instant timestamp;
	
	public Pulse(String msgID, String position, double velocity, Instant timestamp) {
		super();
		this.msgID = msgID;
		this.position = position;
		this.velocity = velocity;
		this.timestamp = timestamp;
	}

	public String getMsgID() {
		return msgID;
	}

	public String getPosition() {
		return position;
	}

	public double getVelocity() {
		return velocity;
	}

	public Instant getTimestamp() {
		return timestamp;
	}
	
}
