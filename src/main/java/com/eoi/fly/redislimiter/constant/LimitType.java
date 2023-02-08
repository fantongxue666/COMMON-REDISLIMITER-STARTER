package com.eoi.fly.redislimiter.constant;

public enum LimitType {
    //固定时间窗口
    FIXEDTIMEWINDOWLIMIT("FIXEDTIMEWINDOWLIMIT","固定时间窗口"),

    //漏桶
    LEAKYBUCKETLIMIT("LEAKYBUCKETLIMIT","漏桶"),

    //令牌桶
    TOKENBUCKETLIMIT("TOKENBUCKETLIMIT","令牌桶"),

    //滑动时间窗口
    SLIDETIMEWINDOWLIMIT("SLIDETIMEWINDOWLIMIT","滑动时间窗口");

    private String code = null;
    private String desc = null;

    LimitType(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}
