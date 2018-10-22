package common;

import java.io.Serializable;
import java.time.Instant;

/**
 * This class models a single instantaneous sample of a node trajectory 
 * 
 * @implSpec This class is immutable and thread-safe.
 *
 */
public final class  Pulse implements Serializable {
	private final Position position;
	private final double velocity;
	private final Instant timestamp;
	
	public Pulse(Position position, double velocity, Instant timestamp) {
		super();		
		this.position = position;
		this.velocity = velocity;
		this.timestamp = timestamp;
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
