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
    private ConcurrentHashMap<String,IpBlockData> blockMap;
    private Map<String,Long> ipMap;

    protected long timeWindowMs;
    protected long blockingTime;
    protected int count;
    protected BlockingStatus status;

    IpBlocker(long timeWindowMs, int count, BlockingStatus status, long blockingTime, int maxIpSize) {
        blockMap = new ConcurrentHashMap<>();
        this.timeWindowMs = timeWindowMs;
        this.blockingTime = blockingTime;
        this.count = count;
        this.status = status;
        log.trace("timeWindowMs={}, blockingTime={}, count={}" , timeWindowMs, blockingTime, count);

        this.ipMap = Collections.synchronizedMap(new LRUMap<String,Long>(maxIpSize) {
            protected boolean removeLRU(final LinkEntry<String,Long> entry) {
                long time = currentTimeMs();
                if ((time-entry.getValue()) > entry.getValue()) {
                    log.trace("remote block {}", entry.getKey());
                    blockMap.remove(entry.getKey());
                    return true;
                }
                log.debug("no remove {}", entry.getKey());
                return false;
            }
        });
    }

    public Optional<BlockingStatus> checkBlock(String remoteAddr) {
        IpBlockData ibd = blockMap.computeIfAbsent(remoteAddr, this::createIpBlockData);
        long time = currentTimeMs();
        this.ipMap.put(remoteAddr, time);
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
        IpBlockData ibd = blockMap.computeIfAbsent(remoteAddr, this::createIpBlockData);
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
