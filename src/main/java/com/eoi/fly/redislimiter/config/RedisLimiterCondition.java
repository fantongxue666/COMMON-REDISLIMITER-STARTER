package com.eoi.fly.redislimiter.config;

public class RedisLimiterCondition {

    private String type;
    private long capacity;
    private long rate;
    private long tokens;

    @Override
    public String toString() {
        return "限流配置 {" +
                "限流类型 = '" + type + '\'' +
                ", 桶容量 = " + capacity +
                ", 速率 = " + rate +
                ", 初始容量/令牌 = " + tokens +
                '}';
    }

    public String getType() {
        return type;
    }

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

    public void setType(String type) {
        this.type = type;
    }
}
