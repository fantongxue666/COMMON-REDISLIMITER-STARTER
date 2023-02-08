package com.eoi.fly.redislimiter.constant;

public class LimiterOperationException extends RuntimeException{

    public LimiterOperationException(){

    }
    public LimiterOperationException(String s){
        super(s);
    }
}
