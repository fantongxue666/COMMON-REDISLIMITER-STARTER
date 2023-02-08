package com.eoi.fly.redislimiter.type;

public abstract class RedisLimit {

    public abstract boolean limit(String key, int limitCount, int limitPeriod);
}
