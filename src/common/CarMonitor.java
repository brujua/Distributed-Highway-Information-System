package common;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import cars.Car;
import cars.MotionObservable;
import cars.MotionObserver;
import network.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;;

public class CarMonitor implements MotionObserver{
	private static final Logger logger = LoggerFactory.getLogger(CarMonitor.class);
	private double range;
	//private List<StNode> cars;
	private Map<StNode,Instant> cars ;
		
	private Position position;
	
	public final double MAX_TIMEOUT = 5000;
	//private final static double DEFAULT_RANGE = 800;
	
	
	public CarMonitor(double range, MotionObservable carrier) {
		super();
		this.range = range;
		cars = new ConcurrentHashMap<StNode, Instant>();
		//cars = Collections.synchronizedList(new ArrayList<StNode>());
		// subscribe to the carrier of this monitor to be notified in changes of position
		if(carrier!=null) {
			carrier.addObserver(this);
		}


		Thread thread = new Thread(new Runnable() {
			
			@Override
			public void run() {
				checkTimeOut();
				
			}
			
		});
		thread.start();
	}

	public CarMonitor(Double range) {
		this(range,null);
	}
	
	public void setRange(double range) {
		this.range = range;
	}
	
	public boolean isInRange(StNode node) {
		/*
		double distance = position.distance(node.getPosition());
		return (distance <= range);
		*/
		return true;
	}
	
	/**
	 * @param car
	 * @return true if it was updated, false if its not in range and removed from the list
	 */
	public boolean update(StNode car) {
		if(cars.containsKey(car)) {
			if(!isInRange(car)) {
				cars.remove(car);
				return false;
			}else {
				cars.remove(car);
				cars.put(car, Instant.now());
				return true;
			}
		}else {
			cars.put(car, Instant.now());
			return true;
		}
		/*synchronized(cars) {
		
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
		}	*/	
	}
		
	/**
	 * @return the copy of the list of the cars being monitored
	 */
	public List<StNode> getList(){
		//make a copy of the list
		List<StNode> list;
		synchronized (cars) {
			list = new ArrayList<StNode>(cars.size());
			for (StNode car : cars.keySet()) {
				list.add(car);
			}
		}
		
	/*	synchronized (cars) {
			list = new ArrayList<StNode>(cars.size());
			for (StNode car : cars) {
				list.add(car);
			}
		}*/
		return list;
	}
	
	
	public void checkTimeOut() {
		try {
			while(true) {
				Thread.sleep(1000);
			
				long now = (Instant.now()).toEpochMilli() ;
				synchronized (cars) {
					for (StNode car : cars.keySet()) {
						if ((now -(car.getTimestamp()).toEpochMilli() >= MAX_TIMEOUT)) {

							cars.remove(car);
							logger.info("Removed " + car + " from monitor list due to timeout");
						}
					}
				}
			}
		} catch (InterruptedException e) {
			logger.error("CarMonitor timeout thread interrupted");
		}
	}
	

	@Override
	public void notify(Pulse pulse) {
		this.position = pulse.getPosition();
		
	}
	
	
	
}
