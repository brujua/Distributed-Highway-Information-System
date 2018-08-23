package cars;

import java.time.Instant;

public class NodeData {
	private String msgID;
	private String position;
	private double velocity;
	private Instant timestamp;
	
	public NodeData(String msgID, String position, double velocity, Instant timestamp) {
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
