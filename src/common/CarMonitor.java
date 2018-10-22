package common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;;

public class CarMonitor {
	private double range;
	private Position position;
	private List<StNode> cars;
	
	public CarMonitor(double range) {
		this.range = range;
		cars = Collections.synchronizedList(new ArrayList<StNode>());
	}
	
	public void setRange(double range) {
		this.range = range;
	}
	
	public boolean isInRange(StNode node) {
		double distance = position.distance(node.getPosition());
		return (distance <= range);
	}
	
	/**
	 * @param car
	 * @return true if it was updated, false if its not in range and removed from the list
	 */
	public boolean update(StNode car) {
		synchronized(cars) {
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
	
	public void updatePosition(Position position) {
		synchronized(this.position) {
			this.position = position;
		}
	}
	
	/**
	 * @note Its important to synchronize the reading of this list while iterating over it.
	 *
	 * @return the list of the cars being monitored
	 */
	public List<StNode> getList(){
		return cars;
	}
	
	
	
}
