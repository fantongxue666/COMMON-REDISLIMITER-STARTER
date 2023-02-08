package com.eoi.fly.redislimiter.constant;

public class RedisLimiterException extends RuntimeException{

    public RedisLimiterException(){

    }
    public RedisLimiterException(String s){
        super(s);
    }
}
