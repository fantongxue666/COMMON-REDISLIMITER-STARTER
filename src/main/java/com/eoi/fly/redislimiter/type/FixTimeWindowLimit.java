package com.eoi.fly.redislimiter.type;

import com.eoi.fly.redislimiter.aspect.LimitInterceptor;
import com.eoi.fly.redislimiter.constant.LimiterOperationException;
import com.eoi.fly.redislimiter.constant.RedisLimiterException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.Collections;

/**
 * 固定时间窗口限流
 *
 */
@Component
public class FixTimeWindowLimit extends RedisLimit{

    private static final Logger logger = LoggerFactory.getLogger(FixTimeWindowLimit.class);

    @Autowired
    private RedisTemplate<String, Serializable> limitRedisTemplate;
    @Autowired
    private RedisTemplate redisTemplate;
    @Override
    public boolean limit(String key, int limitCount, int limitPeriod) {
        Object o = redisTemplate.opsForValue().get(key);
        if(o != null && Integer.valueOf(String.valueOf(o)) > limitCount){
            //已到次数上限，限流
            return false;
        }
        if(limitCount < 1 || limitPeriod < 1){
            throw new LimiterOperationException("注解@Limit的limitCount参数/limitPeriod参数缺失或配置错误，请检查注解参数");
        }
        DefaultRedisScript<Number> redisScript = new DefaultRedisScript<>();
        redisScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("META-INF/fixedTimeWindow.lua")));
        redisScript.setResultType(Number.class);
        /**
         * redisScript：构建的lua脚本对象
         *
         * List<K> keys是key的集合
         * Object... args是val的集合
         *
         * key的集合，在lua中可以使用KEYS[1]、KEYS[2]……获取，注意KEYS必须大写不能拼错
         * val的集合，在lua中可以使用ARGV[1]、ARGV[2]……获取，注意ARGV必须大写不能拼错
         * 说白了，使用redisTemplate操作lua，也就是传key的集合和val的集合，这一串lua脚本可以保证其原子性的
         *
         * lua中的redis.call命令就是操作redis的命令，第一个参数就是redis的原始命令，后面的参数就是redis命令的参数
         */
        Number count = limitRedisTemplate.execute(redisScript, Collections.singletonList(key), limitCount, limitPeriod);
        logger.info("Access try count is {} for key = {}", count, key);
        if (count != null && count.intValue() <= limitCount) {
            return true;
        } else {
            return false;
        }
    }


    /**
     * lua限流脚本
     *
     * @return
     */
    private String buildLuaScript() {
        StringBuilder sb = new StringBuilder();
        //定义c
        sb.append("local c");
        //获取redis中的值
        sb.append("\nc = redis.call('get',KEYS[1])");
        //如果调用不超过最大值
        sb.append("\nif c and tonumber(c) > tonumber(ARGV[1]) then");
        //直接返回
        sb.append("\n return c;");
        //结束
        sb.append("\nend");
        //访问次数加一
        sb.append("\nc = redis.call('incr',KEYS[1])");
        //如果是第一次调用
        sb.append("\nif tonumber(c) == 1 then");
        //设置对应值的过期设置
        sb.append("\nredis.call('expire',KEYS[1],ARGV[2])");
        //结束
        sb.append("\nend");
        //返回
        sb.append("\nreturn c;");

        return sb.toString();
    }
}
