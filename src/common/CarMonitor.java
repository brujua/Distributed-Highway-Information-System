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
 * if every car was updated at least once in timeoutTime milliseconds, if not, removes the car.
 */
public class CarMonitor {
    private final Map<CarStNode, Instant> cars;
    private Logger logger;
    private ScheduledExecutorService timeoutScheduler = Executors.newSingleThreadScheduledExecutor();
    private long timeoutTime;

    public CarMonitor(String name, long timeoutTime, long timeoutCheckFrequency) {
		super();
        this.timeoutTime = timeoutTime;
        cars = new ConcurrentHashMap<>();
        logger = LoggerFactory.getLogger(CarMonitor.class.getName() + name);

        timeoutScheduler.scheduleWithFixedDelay(new TimeOutChecker(), timeoutCheckFrequency, timeoutCheckFrequency, TimeUnit.MILLISECONDS);
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
                    if ((now - entry.getValue().toEpochMilli() >= timeoutTime)) {
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
