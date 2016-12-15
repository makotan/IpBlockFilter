package com.makotan.lib.web.filter.ipblocker;

import org.apache.commons.collections4.map.LRUMap;
import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletRequest;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by makotan on 2016/12/11.
 */
public class IpBlocker {
    private static Logger log = LoggerFactory.getLogger(IpBlocker.class);

    private static class IpBlockData {
        CircularFifoQueue<Long> timeQueue;
        long lastAccessTime;
        boolean useBlocking = false;
    }

    private Map<String,IpBlockData> ipMap;

    protected long timeWindowMs;
    protected long blockingTime;
    protected int count;
    protected BlockingStatus status;

    IpBlocker(long timeWindowMs, int count, BlockingStatus status, long blockingTime, int maxIpSize) {
        this(timeWindowMs, count, status, blockingTime, maxIpSize,  (int)(maxIpSize * 0.2));

    }
    IpBlocker(long timeWindowMs, int count, BlockingStatus status, long blockingTime, int maxIpSize, int initSize) {
        this.timeWindowMs = timeWindowMs;
        this.blockingTime = blockingTime;
        this.count = count;
        this.status = status;
        log.trace("timeWindowMs={}, blockingTime={}, count={}, maxIpSize={}",
                timeWindowMs, blockingTime, count, maxIpSize);

        this.ipMap = Collections.synchronizedMap(new LRUMap<String,IpBlockData>(maxIpSize, initSize) {
            protected boolean removeLRU(final LinkEntry<String,IpBlockData> entry) {
                long time = currentTimeMs();
                if ((time-entry.getValue().lastAccessTime) > entry.getValue().lastAccessTime) {
                    log.trace("remote block {}", entry.getKey());
                    return true;
                }
                log.debug("no remove {}", entry.getKey());
                return false;
            }
        });
    }

    public Optional<BlockingStatus> checkBlock(String remoteAddr) {
        IpBlockData ibd = ipMap.computeIfAbsent(remoteAddr, this::createIpBlockData);
        long time = currentTimeMs();
        //noinspection SynchronizationOnLocalVariableOrMethodParameter
        synchronized (ibd) {
            try {
                if (ibd.useBlocking) {
                    if ((time-ibd.lastAccessTime) < blockingTime) {
                        log.trace("useBlocking blockingTime {}", remoteAddr);
                        ibd.timeQueue.add(time);
                        return Optional.of(status);
                    } else {
                        ibd.useBlocking = false;
                        return Optional.empty();
                    }
                }
            } finally {
                ibd.lastAccessTime = time;
            }
        }
        return Optional.empty();
    }

    protected IpBlockData createIpBlockData(String key) {
        IpBlockData ibd = new IpBlockData();
        ibd.timeQueue = new CircularFifoQueue<>(count);
        ibd.lastAccessTime = currentTimeMs();
        return ibd;
    }

    public void calcBlock(String remoteAddr) {
        IpBlockData ibd = ipMap.computeIfAbsent(remoteAddr, this::createIpBlockData);
        //noinspection SynchronizationOnLocalVariableOrMethodParameter
        synchronized (ibd) {
            ibd.timeQueue.add(currentTimeMs());
            if (ibd.timeQueue.isAtFullCapacity()) {
                Long lastTime = ibd.timeQueue.peek();
                ibd.useBlocking = inTimeWindow(lastTime);
                log.debug("block {}", remoteAddr);
            }
        }
    }

    protected boolean inTimeWindow(Long lastTime) {
        if (lastTime == null || lastTime == 0L) {
            return false;
        }
        long blockCheckTime = currentTimeMs()-timeWindowMs;
        log.trace("in time window {} {}", blockCheckTime, lastTime);
        return blockCheckTime < lastTime;
    }

    protected Long currentTimeMs() {
        return System.currentTimeMillis();
    }
}
