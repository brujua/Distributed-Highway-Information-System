package highway;

import common.Position;
import common.StNode;
import network.Messageable;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Lightweight representation of a highway node. Fulfills the purpose of identifying it and storing the data to send messages to it.
 */
public class HWStNode implements Messageable, Serializable {

	private StNode stNode;
	private StNode carStNode;
	private List<Segment> segments;

	public HWStNode(StNode carStNode, StNode stNode, List<Segment> segments) {
		this.stNode = stNode;
		this.carStNode = carStNode;
		this.segments = segments;
	}

	public boolean isInSegment(Position pos) {
		for (Segment seg : segments) {
			if (seg.contains(pos)) {
				return true;
			}
		}


		return false;
	}

	public StNode getCarStNode() {
		return carStNode;
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
        segments.addAll(newSegments);
        Collections.sort(segments);
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

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
	    sb.append(stNode.toString());
        sb.append("Segments:[");
        for (Segment seg : segments) {
            sb.append(seg.getIndex());
            sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }
}
