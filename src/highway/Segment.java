package highway;

import common.Position;

import java.io.Serializable;
import java.security.InvalidParameterException;
import java.util.Objects;

/**
 * Represents a piece of Highway. Shouldn't overlap with other segments
 *
 * @implSpec This class is immutable and thread-safe.
 */
public class Segment implements Serializable, Comparable<Segment> {

	private double beginX, endX, beginY, endY;
	private int index;

	public Segment(double beginX, double endX, double beginY, double endY, int index) {
		if (beginX >= endX || beginY >= endY)
			throw new InvalidParameterException("Segment without sense");
		this.index = index;
		this.beginX = beginX;
		this.endX = endX;
		this.beginY = beginY;
		this.endY = endY;
	}

	public double getBeginX() {
		return beginX;
	}


	public double getEndX() {
		return endX;
	}

	public double getBeginY() {
		return beginY;
	}


	public double getEndY() {
		return endY;
	}

	private int getIndex() {
		return index;
	}


	public boolean contains(Position pos) {
		double posX = pos.getCordx();
		double posY = pos.getCordy();
		return !(posX >= endX) && !(posX < beginX) && !(posY >= endY) && !(posY < beginY);
	}

	@Override
	public int compareTo(Segment s) {
		return this.index - s.getIndex();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Segment segment = (Segment) o;
		return index == segment.index;
	}

	@Override
	public int hashCode() {
		return Objects.hash(index);
	}
}
