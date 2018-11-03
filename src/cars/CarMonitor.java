package cars;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import common.Position;
import common.Pulse;
import common.StNode;;

public class CarMonitor implements MotionObserver{
	private double range;
	private List<StNode> cars;
	private Position position;
	
	public CarMonitor(double range, MotionObservable carrier) {
		this.range = range;
		cars = Collections.synchronizedList(new ArrayList<StNode>());
		// subscribe to the carrier of this monitor to be notified in changes of position
		carrier.addObserver(this);
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

	@Override
	public void notify(Pulse pulse) {
		this.position = pulse.getPosition();
		
	}
	
	
	
}
