package com.eoi.fly.redislimiter.annotaion;
import java.lang.annotation.*;
import com.eoi.fly.redislimiter.constant.KeyType;
@Target({ElementType.METHOD,ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface Limit {

    //资源key
    String key() default "";
    //时间 单位 秒
    int period() default 0;
    //最多访问次数
    int count() default 0;
    //类型
    KeyType keyType() default KeyType.OTHERS;
}
