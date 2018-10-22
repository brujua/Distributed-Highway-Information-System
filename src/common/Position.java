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

}
	


