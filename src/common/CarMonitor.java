package common;

import java.util.ArrayList;
import java.util.List;;

public class CarMonitor {
	private double range;
	private List<StNode> cars;
	
	public CarMonitor(double range) {
		this.range = range;
		cars = new ArrayList<StNode>();		
	}
	
	public boolean isInRange(StNode node) {
		//? 
		return true;
	}
	
	/**
	 * @param car
	 * @return true if it was updated, false if its not in range
	 */
	public boolean update(StNode car) {
		int i = cars.indexOf(car);
		if(!isInRange(car)) {
			if(i!=-1)
				cars.remove(i);
			return false;
		}
		
		if(i != -1) 
			cars.set(i, car);
		else
			cars.add(car);
		return true;
	}
	
}
