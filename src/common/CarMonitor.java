package common;

import cars.CarStNode;
import cars.MotionObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
//import sun.rmi.runtime.Log;

public class CarMonitor implements MotionObserver {
	private Logger logger;
	private double range;
	//private List<StNode> cars;
	private final Map<CarStNode,Instant> cars ;
	private ScheduledExecutorService timeoutScheduler = Executors.newSingleThreadScheduledExecutor();

	private Position position;

	private static final long timeOutCheckRefreshTime = 2000;
	private static final long MAX_TIMEOUT = 5000;
	//private final static double DEFAULT_RANGE = 800;
	
	public CarMonitor( String name) {
		super();
		this.range = range;
		cars = new ConcurrentHashMap<CarStNode, Instant>();
		// subscribe to the carrier of this monitor to be notified in changes of position
/*		if(carrier!=null) {
			carrier.addObserver(this);
		}*/
		logger = LoggerFactory.getLogger(CarMonitor.class.getName() + name);

		timeoutScheduler.scheduleWithFixedDelay(new TimeOutChecker(), timeOutCheckRefreshTime, timeOutCheckRefreshTime, TimeUnit.MILLISECONDS);
	}

/*	public CarMonitor(double range, MotionObservable carrier){
		this(range,carrier,"");
	}*/

/*	public CarMonitor(Double range) {
		this(range,null);
	}*/
	
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

	 */
/*	public boolean update(StNode car) {
		cars.remove(car);
		if(isInRange(car)){
			cars.put(car,Instant.now());
			return true;
		}
		return false;
	}*/

	public void update(CarStNode car){
		cars.remove(car);
		cars.put(car,Instant.now());
	}
		
	/**
	 * @return the copy of the list of the cars being monitored
	 */
	public List<CarStNode> getList(){
		//make a copy of the list
		List<CarStNode> list;
		synchronized (cars) {
			list = new ArrayList<>(cars.size());
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
				Iterator<Map.Entry<CarStNode, Instant>> iterator = cars.entrySet().iterator();
				while (iterator.hasNext()) {
					Map.Entry<CarStNode,Instant> entry = iterator.next();
					if ((now - entry.getValue().toEpochMilli() >= MAX_TIMEOUT)) {
						iterator.remove();
						logger.info("Removed " + entry.getKey() + " from monitor list due to timeout");
					}
				}
			}
		}
	}

	public void shutdown(){
		timeoutScheduler.shutdownNow();
	}
}
