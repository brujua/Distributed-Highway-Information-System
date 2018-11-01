package common;

import java.io.Serializable;

/**
 * This class models a point of an object in space
 *
 * @implSpec This class is immutable and thread-safe.
 */
public final class Position implements Serializable{

	private final Double cordx;
	private final Double cordy;
	private final Unit unit;	
	
	public Position(Double coordX, Double coordY, Unit unit) {
		super();
		this.cordx = coordX;
		this.cordy = coordY;
		this.unit = unit;		
	}

	public Position(Double coordX, Double coordY) {
		this(coordX, coordY, Unit.KiloMeters);
	}

	public Double getCordx() {
		return cordx;
	}

	public Double getCordy() {
		return cordy;
	}
	
	public Unit getUnity() {
		return this.unit;
	}
	
	public double distance(Position position) {
		
		double distX = Math.pow((position.cordx - this.cordx),2); 
		double distY = Math.pow((position.cordy - this.cordy),2);
				
		return Math.sqrt(distX+distY) ;
		
	}
	
	static double distancebetwen(Position position,Position position2) {
		
		double distX = Math.pow((position.cordx - position2.cordx),2); 
		double distY = Math.pow((position.cordy - position2.cordy),2);
				
		return Math.sqrt(distX+distY) ;
	}

	@Override
	public String toString() {
		return "Position [cordx=" + cordx + ", cordy=" + cordy + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((cordx == null) ? 0 : cordx.hashCode());
		result = prime * result + ((cordy == null) ? 0 : cordy.hashCode());
		return result;
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
			if (other.cordy != null)
				return false;
		} else if (!cordy.equals(other.cordy))
			return false;
		return true;
	}



	
}
	


