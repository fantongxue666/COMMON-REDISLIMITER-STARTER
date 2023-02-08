package com.eoi.fly.redislimiter.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "redislimit")
public class RedisLimiterProperties {

    /**
     * 限流算法选用，默认按照固定时间窗口算法走
     * FIXEDTIMEWINDOWLIMIT 固定时间窗口
     * SLIDETIMEWINDOWLIMIT 滑动时间窗口
     * LEAKYBUCKETLIMIT     漏桶
     * TOKENBUCKETLIMIT     令牌桶
     */
    private String type = "FIXEDTIMEWINDOWLIMIT";

    /**
     * 限流算法选用 漏桶或令牌桶时 生效
     * 桶容量
     */
    private long capacity = 100;

    /**
     * 限流算法选用 漏桶或令牌桶时 生效
     * 漏桶时-----水漏出的速度（每秒系统能处理的请求数）
     * 令牌桶时-----令牌放入速度
     */
    private long rate = 10;

    /**
     * 限流算法选用 漏桶或令牌桶时 生效
     * 漏桶时-----初始化水量
     * 令牌桶时-----初始化令牌数量
     */
    private long tokens = 20;

    public long getCapacity() {
        return capacity;
    }

    public void setCapacity(long capacity) {
        this.capacity = capacity;
    }

    public long getRate() {
        return rate;
    }

    public void setRate(long rate) {
        this.rate = rate;
    }

    public long getTokens() {
        return tokens;
    }

    public void setTokens(long tokens) {
        this.tokens = tokens;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
