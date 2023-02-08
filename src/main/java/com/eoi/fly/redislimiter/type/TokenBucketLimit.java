package com.eoi.fly.redislimiter.type;

import com.eoi.fly.redislimiter.config.RedisLimiterCondition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.Arrays;

@Component
public class TokenBucketLimit extends RedisLimit{
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private RedisLimiterCondition redisLimiterCondition;
    @Autowired
    private RedisTemplate<String, Serializable> limitRedisTemplate;

    @Override
    public boolean limit(String key, int limitCount, int limitPeriod) {
        Object o = redisTemplate.opsForValue().get(key);
        if(o != null && Long.valueOf(String.valueOf(o)) < 1){
            //无令牌，限流
            return false;
        }
        //桶容量
        long capacity = redisLimiterCondition.getCapacity();//k1
        //令牌放入速度
        long rate = redisLimiterCondition.getRate();//k2
        //key k3
        //初始化令牌数量
        long tokens = redisLimiterCondition.getTokens();//v1

        long now = System.currentTimeMillis();//v2

        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
        redisScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("META-INF/tokenBucket.lua")));
        redisScript.setResultType(Long.class);
        Long result = limitRedisTemplate.execute(redisScript, Arrays.asList(key,key+"_timestamp"),
                capacity,rate,tokens,now);
        int r = result.intValue();
        if (r == -1) {
            return false;
        }else{
            return true;
        }
    }
}
