package com.eoi.fly.redislimiter.type;

import cn.hutool.core.lang.UUID;
import com.eoi.fly.redislimiter.constant.LimiterOperationException;
import com.eoi.fly.redislimiter.constant.RedisLimiterException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.Arrays;

/**
 * 滑动时间窗口限流
 */
@Component
public class SlideTimeWindowLimit extends RedisLimit{

    private static final Logger logger = LoggerFactory.getLogger(SlideTimeWindowLimit.class);

    @Autowired
    private RedisTemplate<String, Serializable> limitRedisTemplate;
    @Autowired
    private RedisTemplate redisTemplate;


    @Override
    public boolean limit(String key, int limitCount, int limitPeriod) {
        Long aLong = redisTemplate.opsForZSet().zCard(key);
        if(aLong != null && aLong >= limitCount){
            //已到阈值，限流
            return false;
        }

        if(limitCount < 1 || limitPeriod < 1){
            throw new LimiterOperationException("注解@Limit的limitCount参数/limitPeriod参数缺失或配置错误，请检查注解参数");
        }
        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
        redisScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("META-INF/slideTimeWindow.lua")));
        redisScript.setResultType(Long.class);

        //key：zset的名称
        //每次请求的唯一标识
        String reqeustUuid = UUID.fastUUID().toString(true);
        //当前时间戳
        long now = System.currentTimeMillis();
        Long result = limitRedisTemplate.execute(redisScript, Arrays.asList(key, reqeustUuid, String.valueOf(now)),
                limitPeriod * 1000, limitCount, 1000);

        int r = result.intValue();
        if (r == -1 || r == -2) {
            return false;
        }else{
            return true;
        }
    }
}
