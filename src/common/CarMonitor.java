package common;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;

import cars.MotionObservable;
import cars.MotionObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
//import sun.rmi.runtime.Log;

public class CarMonitor implements MotionObserver{
	private Logger logger;
	private double range;
	//private List<StNode> cars;
	private final Map<StNode,Instant> cars ;
	private ScheduledExecutorService timeoutScheduler = Executors.newSingleThreadScheduledExecutor();

	private Position position;

	private static final long timeOutCheckRefreshTime = 2000;
	private static final long MAX_TIMEOUT = 5000;
	//private final static double DEFAULT_RANGE = 800;
	
	public CarMonitor(double range, MotionObservable carrier, String name) {
		super();
		this.range = range;
		cars = new ConcurrentHashMap<StNode, Instant>();
		// subscribe to the carrier of this monitor to be notified in changes of position
		if(carrier!=null) {
			carrier.addObserver(this);
		}
		logger = LoggerFactory.getLogger(CarMonitor.class.getName() + name);

		timeoutScheduler.scheduleWithFixedDelay(new TimeOutChecker(), timeOutCheckRefreshTime, timeOutCheckRefreshTime, TimeUnit.MILLISECONDS);
	}

	public CarMonitor(double range, MotionObservable carrier){
		this(range,carrier,"");
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
		cars.remove(car);
		if(isInRange(car)){
			cars.put(car,Instant.now());
			return true;
		}
		return false;
	}
		
	/**
	 * @return the copy of the list of the cars being monitored
	 */
	public List<StNode> getList(){
		//make a copy of the list
		List<StNode> list;
		synchronized (cars) {
			list = new ArrayList<StNode>(cars.size());
			list.addAll(cars.keySet());
		}
		return list;
	}

	@Override
	public void notify(Pulse pulse) {
		this.position = pulse.getPosition();
		
	}

	private class TimeOutChecker implements Runnable {
		@Override
		public void run(){
			checkTimeOut();
		}

		private void checkTimeOut() {
			long now = (Instant.now()).toEpochMilli() ;
			synchronized (cars) {
				Iterator<Map.Entry<StNode, Instant>> iterator = cars.entrySet().iterator();
				while (iterator.hasNext()) {
					Map.Entry<StNode,Instant> entry = iterator.next();
					if ((now - entry.getValue().toEpochMilli() >= MAX_TIMEOUT)) {
						iterator.remove();
						logger.info("Removed " + entry.getKey() + " from monitor list due to timeout");
					}
				}
			}
		}
	}

	public void shutdown(){
		timeoutScheduler.shutdown();
	}
}
