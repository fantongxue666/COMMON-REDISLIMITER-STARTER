package com.eoi.fly.redislimiter.constant;

public enum KeyType {
    //自定义key
    CUSTOMER,
    //默认，取方法名作为key
    OTHERS,
    //根据请求者IP
    IP;
}
