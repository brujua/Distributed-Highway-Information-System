package highway;

import common.StNode;

import java.io.Serializable;

/**
 * Immutable
 *
 */
public class HWListNode implements Serializable {
	private final double start;
	private final double end;
	private final StNode stNode;


	public HWListNode(double start, double end, StNode stNode) {
		this.start = start;
		this.end = end;
		this.stNode = stNode;
	}

	public double getStart() {
		return start;
	}

	public double getEnd() {
		return end;
	}

	public StNode getStNode() {
		return stNode;
	}

}
