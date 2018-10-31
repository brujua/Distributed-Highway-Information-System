package cars;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import common.Position;
import common.StNode;;

public class CarMonitor {
	private double range;
	private Position position;
	private List<StNode> cars;
	
	public CarMonitor(double range, Position initPos) {
		this.range = range;
		this.position = initPos;
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
	 * @return the copy of the list of the cars being monitored
	 */
	public List<StNode> getList(){
		//make a copy of the list;
		List<StNode> list;
		synchronized (cars) {
			list = new ArrayList<StNode>(cars.size());
			for (StNode car : cars) {
				list.add(car);
			}
		}
		return list;
	}
	
	
	
}
