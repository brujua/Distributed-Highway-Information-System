package common;

import java.io.Serializable;
import java.util.Objects;

/**
 * This class models a point of an object in space
 *
 * @implSpec This class is immutable and thread-safe.
 */
public final class Position implements Serializable{

	private final Double cordx;
	private final Double cordy;

    public Position(Double coordX, Double coordY) {
		super();
		this.cordx = coordX;
		this.cordy = coordY;
	}

	public Double getCordx() {
		return cordx;
	}

	public Double getCordy() {
		return cordy;
	}

    public double distance(Position position) {
		
		double distX = Math.pow((position.cordx - this.cordx),2); 
		double distY = Math.pow((position.cordy - this.cordy),2);
				
		return Math.sqrt(distX+distY) ;
		
	}

	@Override
	public String toString() {
		return "[cordx=" + cordx + ", cordy=" + cordy + "]";
	}

	@Override
	public int hashCode() {
		return Objects.hash(cordx,cordy);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Position other = (Position) obj;
		if (cordx == null) {
			if (other.cordx != null)
				return false;
		} else if (!cordx.equals(other.cordx))
			return false;
		if (cordy == null) {
			return other.cordy == null;
		} else return cordy.equals(other.cordy);
	}
}
	


