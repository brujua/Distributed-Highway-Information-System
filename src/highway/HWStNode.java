package highway;

import common.StNode;
import network.Messageable;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

public class HWStNode implements Messageable, Serializable {
	private StNode stNode;
	private List<Segment> segments;

	public HWStNode(StNode stNode, List<Segment> segments) {
		this.stNode = stNode;
		this.segments = segments;
	}

	public StNode getStNode() {
		return stNode;
	}

	public void setStNode(StNode stNode) {
		this.stNode = stNode;
	}

	public int getSegmentCount() {
		return segments.size();
	}

	public List<Segment> getSegments() {
		return segments;
	}

	public void setSegments(List<Segment> segments) {
		this.segments = segments;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		HWStNode hwStNode = (HWStNode) o;
		return Objects.equals(stNode, hwStNode.stNode);
	}

	@Override
	public int hashCode() {
		return Objects.hash(stNode);
	}

	public void addSegments(List<Segment> newSegments) {
		this.segments.addAll(newSegments);
	}

	@Override
	public String getIP() {
		return stNode.getIP();
	}

	@Override
	public int getPort() {
		return stNode.getPort();
	}

	@Override
	public String getId() {
		return stNode.getId();
	}
}
