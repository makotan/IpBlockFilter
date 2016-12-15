package com.makotan.lib.web.filter.ipblocker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Created by makotan on 2016/12/11.
 */
public class IpBlockerFactory {
    private static Logger log = LoggerFactory.getLogger(IpBlockerFactory.class);
    private static IpBlockerFactory factory = new IpBlockerFactory();

    private long defaultTimeWindowMs = 60 * 1000L;
    private long defaultBlockingTimeMs = 60 * 60 * 1000L;
    private int defaultBlockingStatus = 403;
    private int defaultCount = 10;
    private int defaultMaxIpSize = 10_000;

    public static IpBlockerFactory get() {
        return factory;
    }

    public IpBlocker getIpBlocker(Map<String,String> initMap) {

        BlockingStatus status = new BlockingStatus();
        status.status = parseInt("blockingStatus", initMap,  defaultBlockingStatus);
        return new IpBlocker(
                parseLong("timeWindowMs", initMap, defaultTimeWindowMs),
                parseInt("count" , initMap , defaultCount),
                status,
                parseLong("blockingTimeMs", initMap, defaultBlockingTimeMs) ,
                parseInt("maxIpSize", initMap , defaultMaxIpSize)
        );
    }

    private Long parseLong(String initParam, Map<String,String> initMap, Long defaultValue) {
        String str = initMap.getOrDefault(initParam, "" + defaultValue);
        try {
            return Long.parseLong(str);
        } catch (NumberFormatException ex) {
            log.error("initParam:"+initParam+" value:" + str , ex);
        }
        return defaultValue;
    }

    private Integer parseInt(String initParam, Map<String,String> initMap, int defaultValue) {
        String str = initMap.getOrDefault(initParam, "" + defaultValue);
        try {
            return Integer.parseInt(str);
        } catch (NumberFormatException ex) {
            log.error("initParam:" + initParam + " value:" + str , ex);
        }
        return defaultValue;
    }

    public long getDefaultTimeWindowMs() {
        return defaultTimeWindowMs;
    }

    public void setDefaultTimeWindowMs(long defaultTimeWindowMs) {
        this.defaultTimeWindowMs = defaultTimeWindowMs;
    }

    public long getDefaultBlockingTimeMs() {
        return defaultBlockingTimeMs;
    }

    public void setDefaultBlockingTimeMs(long defaultBlockingTimeMs) {
        this.defaultBlockingTimeMs = defaultBlockingTimeMs;
    }

    public int getDefaultBlockingStatus() {
        return defaultBlockingStatus;
    }

    public void setDefaultBlockingStatus(int defaultBlockingStatus) {
        this.defaultBlockingStatus = defaultBlockingStatus;
    }

    public int getDefaultCount() {
        return defaultCount;
    }

    public void setDefaultCount(int defaultCount) {
        this.defaultCount = defaultCount;
    }

    public int getDefaultMaxIpSize() {
        return defaultMaxIpSize;
    }

    public void setDefaultMaxIpSize(int defaultMaxIpSize) {
        this.defaultMaxIpSize = defaultMaxIpSize;
    }
}
