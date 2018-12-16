package highway;

import common.Position;

import java.io.Serializable;
import java.security.InvalidParameterException;

/**
 * Represents a piece of Highway. Shouldn't overlap with other segments
 *
 * @implSpec This class is immutable and thread-safe.
 */
public class Segment implements Serializable, Comparable<Segment> {

    private int index;
    private double beginX;
    private double endX;
    private double beginY;
    private double endY;

    public Segment() {
        super();
    }

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


    public void setBeginX(double beginX) {
        this.beginX = beginX;
    }

    public void setEndX(double endX) {
        this.endX = endX;
    }

    public void setBeginY(double beginY) {
        this.beginY = beginY;
    }

    public void setEndY(double endY) {
        this.endY = endY;
    }

    public void setIndex(int index) {
        this.index = index;
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
    public String toString() {
        return "Segment{" +
                "index=" + index +
                ", beginX=" + beginX +
                ", endX=" + endX +
                ", beginY=" + beginY +
                ", endY=" + endY +
                '}';
    }
}
