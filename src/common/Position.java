package common;

import java.io.Serializable;

public class Position implements Serializable{

	private Double cordx;
	private Double cordy;
	private Units unity;
	//max distance to a near node
	private final double maxDistance = 10;
	
	
	
	public Position(Double coordX, Double coordY, Units kilometers) {
		super();
		coordX = cordx;
		coordY = cordy;
		kilometers = kilometers;
		
		
		
	}

	public Double getCordx() {
		return cordx;
	}

	public Double getCordy() {
		return cordy;
	}
	
	public Units getUnity() {
		return this.unity;
	}
	
	public double distance(Position position) {
		
		double distX = Math.pow((position.cordx - this.cordx),2); 
		double distY = Math.pow((position.cordy - this.cordy),2);
				
		return Math.sqrt(distX+distY) ;
		
	}
	

}
	


