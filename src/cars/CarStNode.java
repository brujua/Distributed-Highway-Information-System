package cars;

import common.Position;
import common.Pulse;
import common.StNode;
import network.Messageable;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 * Lightweight representation of a car node. Fulfills the purpose of identifying it and storing the data to send messages to it.
 *
 * @implSpec This class is immutable and thread-safe.
 */
public class CarStNode implements Messageable, Serializable {

	private final StNode node;
	private final Pulse pulse;


	public CarStNode(String id, String ip, int port, Pulse pulse) {
		this(new StNode(id, ip, port), pulse);
	}

	public CarStNode(String id, String ip, int port, Position position) {
		this(new StNode(id, ip, port), new Pulse(position, 0, Instant.now()));
	}

	public CarStNode(StNode node, Pulse pulse) {
		this.node = node;
		this.pulse = pulse;
	}


	public Position getPosition() {
		return pulse.getPosition();
	}

	public Instant getTimestamp() {
		return this.pulse.getTimestamp();
	}

	public double getVelocity() {
		return this.pulse.getVelocity();
	}

	public CarStNode updatePulse(Pulse pulse) {
		return new CarStNode(node.getId(), node.getIP(), node.getPort(), pulse);
	}

	public CarStNode changePulse(Pulse pulse) {
		return new CarStNode(node.getId(), node.getIP(), node.getPort(), pulse);
	}

	public Pulse getPulse() {
		return pulse;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		CarStNode carStNode = (CarStNode) o;
		return Objects.equals(node, carStNode.node);
	}

	@Override
	public int hashCode() {
		return Objects.hash(node);
	}

	@Override
	public String getId() {
		return node.getId();
	}

	@Override
	public int getPort() {
		return node.getPort();
	}

	@Override
	public String getIP() {
		return node.getIP();
	}

	public StNode getStNode() {
		return node;
	}

    @Override
    public String toString() {
        return node.toString();
    }
}