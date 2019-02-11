package highway;

import common.Position;
import common.StNode;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Lightweight representation of a highway node. Fulfills the purpose of identifying it and storing the data to send messages to it.
 */
public class HWStNode extends StNode {


    private StNode hwStNode;
	private List<Segment> segments;

	public HWStNode(StNode carStNode, StNode stNode, List<Segment> segments) {
        super(carStNode);
        this.hwStNode = stNode;
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
        return this;
    }

    public StNode getHWStNode() {
        return hwStNode;
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
    public StNode changeIp(String ip) {
        return new HWStNode(new StNode(getId(), ip, getPort()), new StNode(hwStNode.getId(), ip, hwStNode.getPort()), segments);
    }

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		HWStNode hwStNode = (HWStNode) o;
        return Objects.equals(this.getId(), hwStNode.getId());
	}

	@Override
	public int hashCode() {
        return Objects.hash(getId());
	}

	public void addSegments(List<Segment> newSegments) {
        segments.addAll(newSegments);
        Collections.sort(segments);
	}


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(hwStNode.toString());
        sb.append("Segments:[");
        for (Segment seg : segments) {
            sb.append(seg.getIndex());
            sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }
}
