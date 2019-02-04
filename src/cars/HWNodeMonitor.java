package cars;

import common.StNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class HWNodeMonitor {
    private Logger logger;
    private ScheduledExecutorService timeoutScheduler = Executors.newSingleThreadScheduledExecutor();
    private ReentrantReadWriteLock nodeLock = new ReentrantReadWriteLock();

    private long timeoutTime;
    private long timeoutCheckRefreshTime;
    private StNode node;
    private long lastUpdate;

    public HWNodeMonitor(long timeoutTime, long timeoutCheckRefreshTime, String name) {
        this.timeoutCheckRefreshTime = timeoutCheckRefreshTime;
        this.timeoutTime = timeoutTime;
        logger = LoggerFactory.getLogger(name + "-HWNodeMonitor");
        node = null;
        lastUpdate = Instant.now().toEpochMilli();
    }

    public void setNode(StNode node) {
        nodeLock.writeLock().lock();
        this.node = node;
        nodeLock.writeLock().unlock();
        lastUpdate = Instant.now().toEpochMilli();
    }

    public void update(StNode node) {
        nodeLock.readLock().lock();
        if (this.node != null && this.node.equals(node))
            lastUpdate = Instant.now().toEpochMilli();
        nodeLock.readLock().unlock();
    }

    public void startMonitoring(Runnable timeOutCallback) {
        timeoutScheduler.scheduleWithFixedDelay(() -> checkTimeOut(timeOutCallback), timeoutCheckRefreshTime, timeoutCheckRefreshTime, TimeUnit.MILLISECONDS);
    }

    private void checkTimeOut(Runnable timeOutCallback) {
        long now = Instant.now().toEpochMilli();
        long timeSinceLastUpdate = now - lastUpdate;
        boolean doCallback = false;
        nodeLock.readLock().lock();
        if (node != null) {
            if (timeSinceLastUpdate > timeoutTime) {
                logger.error("HWNode haven't sent alive in " + timeoutTime + " milliseconds");
                node = null;
                doCallback = true;
            }
        }
        nodeLock.readLock().unlock();
        if (doCallback)
            timeOutCallback.run();
    }

    public void shutdown() {
        timeoutScheduler.shutdown();
        try {
            timeoutScheduler.awaitTermination(5000, TimeUnit.MILLISECONDS);
            timeoutScheduler.shutdownNow();
        } catch (InterruptedException e) {
            timeoutScheduler.shutdownNow();
        }

    }


}
