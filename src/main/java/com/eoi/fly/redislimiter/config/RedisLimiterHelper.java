package com.eoi.fly.redislimiter.config;

import com.eoi.fly.redislimiter.aspect.LimitInterceptor;
import com.eoi.fly.redislimiter.constant.LimitType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.io.Serializable;

@Configuration
@ConditionalOnClass({RedisLimiterCondition.class})
@EnableConfigurationProperties(RedisLimiterProperties.class)
@ComponentScan("com.eoi.fly.redislimiter.*")
public class RedisLimiterHelper {

    private static final Logger logger = LoggerFactory.getLogger(RedisLimiterHelper.class);

    @Autowired
    private RedisLimiterProperties redisLimiterProperties;
    @Bean
    public RedisTemplate<String, Serializable> limitRedisTemplate(LettuceConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Serializable> template = new RedisTemplate<>();
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.setConnectionFactory(redisConnectionFactory);
        return template;
    }

    @Bean
    public RedisLimiterCondition getRedisLimiterCondition(){
        RedisLimiterCondition redisLimiterCondition = new RedisLimiterCondition();
        String type = redisLimiterProperties.getType();
        redisLimiterCondition.setType(type);
        redisLimiterCondition.setCapacity(redisLimiterProperties.getCapacity());
        redisLimiterCondition.setRate(redisLimiterProperties.getRate());
        redisLimiterCondition.setTokens(redisLimiterProperties.getTokens());
        if(type.equalsIgnoreCase(LimitType.FIXEDTIMEWINDOWLIMIT.getCode())){
            //固定时间窗口限流
            logger.info("======= 集成RedisLimiter限流 <固定时间窗口> ========");
        }else if(type.equalsIgnoreCase(LimitType.SLIDETIMEWINDOWLIMIT.getCode())){
            //滑动时间窗口限流
            logger.info("======= 集成RedisLimiter限流 <滑动时间窗口> ========");
        }else if(type.equalsIgnoreCase(LimitType.LEAKYBUCKETLIMIT.getCode())){
            //漏桶限流
            logger.info("======= 集成RedisLimiter限流 <漏桶> ========");
        }else if(type.equalsIgnoreCase(LimitType.TOKENBUCKETLIMIT.getCode())){
            //令牌桶限流
            logger.info("======= 集成RedisLimiter限流 <令牌桶> ========");
        }
        logger.info(redisLimiterCondition.toString());


        return redisLimiterCondition;
    }
}