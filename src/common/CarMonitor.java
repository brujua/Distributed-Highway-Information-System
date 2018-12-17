package common;

import cars.CarStNode;
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

/**
 * This class maintains a list of CarStNode, adding and updating them by CarMonitor.add().
 * Starts a scheduled task that checks every  {@link CarMonitor}.TIMEOUT_CHECK_REFRESH_TIME
 * if every car was updated at least once in MAX_TIMEOUT milliseconds, if not, removes the car.
 */
public class CarMonitor {

    public static final long timeOutCheckRefreshTime = 2000;
    public static final long MAX_TIMEOUT = 5000;

	private Logger logger;
	private final Map<CarStNode,Instant> cars ;
	private ScheduledExecutorService timeoutScheduler = Executors.newSingleThreadScheduledExecutor();

	public CarMonitor( String name) {
		super();
		cars = new ConcurrentHashMap<>();
        logger = LoggerFactory.getLogger(CarMonitor.class.getName() + name);

		timeoutScheduler.scheduleWithFixedDelay(new TimeOutChecker(), timeOutCheckRefreshTime, timeOutCheckRefreshTime, TimeUnit.MILLISECONDS);
	}

	public void update(CarStNode car){
		cars.remove(car);
		cars.put(car,Instant.now());
	}
		
	/**
	 * @return the copy of the list of the cars being monitored
	 */
	public List<CarStNode> getList() {
        //make a copy of the list
		List<CarStNode> list;
		synchronized (cars) {
			list = new ArrayList<>(cars.size());
			list.addAll(cars.keySet());
		}
		return list;
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
