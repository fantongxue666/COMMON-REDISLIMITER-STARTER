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

/**
 * 漏桶限流
 */
@Component
public class LeakyBucketLimit extends RedisLimit{
    @Autowired
    private RedisLimiterCondition redisLimiterCondition;
    @Autowired
    private RedisTemplate<String, Serializable> limitRedisTemplate;
    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public boolean limit(String key, int limitCount, int limitPeriod) {
        //桶容量
        long capacity = redisLimiterCondition.getCapacity();//k1
        //水漏出的速度（每秒系统能处理的请求数）
        long rate = redisLimiterCondition.getRate();//k2
        //key  k3
        //初始水量
        long tokens = redisLimiterCondition.getTokens();//v1

        long now = System.currentTimeMillis();//v2

        Object o = redisTemplate.opsForValue().get(key);
        if(o != null && (Long.valueOf(String.valueOf(o)) + 1) > capacity){
            //桶满，限流
            return false;
        }

        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
        redisScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("META-INF/leakyBucket.lua")));
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
