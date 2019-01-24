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
    public static final long timeOutCheckRefreshTime = 2000;
    public static final long MAX_TIMEOUT = 5000;
    private Logger logger;
    private ScheduledExecutorService timeoutScheduler = Executors.newSingleThreadScheduledExecutor();
    private ReentrantReadWriteLock nodeLock = new ReentrantReadWriteLock();
    private StNode node;
    private long lastUpdate;

    public HWNodeMonitor(String name) {
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
        if (this.node.equals(node)) {
            lastUpdate = Instant.now().toEpochMilli();
        }
        nodeLock.readLock().unlock();
    }

    public void startMonitoring(Runnable timeOutCallback) {
        timeoutScheduler.scheduleWithFixedDelay(() -> checkTimeOut(timeOutCallback), timeOutCheckRefreshTime, timeOutCheckRefreshTime, TimeUnit.MILLISECONDS);
    }

    private void checkTimeOut(Runnable timeOutCallback) {
        long now = Instant.now().toEpochMilli();
        long timeSinceLastUpdate = now - lastUpdate;
        boolean doCallback = false;
        nodeLock.readLock().lock();
        if (node != null) {
            if (timeSinceLastUpdate > MAX_TIMEOUT) {
                logger.error("HWNode haven't sent alive in " + MAX_TIMEOUT + " milliseconds");
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
