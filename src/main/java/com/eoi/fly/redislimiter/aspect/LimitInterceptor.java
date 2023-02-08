package com.eoi.fly.redislimiter.aspect;

import com.eoi.fly.redislimiter.annotaion.Limit;
import com.eoi.fly.redislimiter.config.RedisLimiterCondition;
import com.eoi.fly.redislimiter.constant.KeyType;
import com.eoi.fly.redislimiter.constant.LimitType;
import com.eoi.fly.redislimiter.constant.LimiterOperationException;
import com.eoi.fly.redislimiter.constant.RedisLimiterException;
import com.eoi.fly.redislimiter.type.*;
import com.eoi.fly.redislimiter.utils.IPUtil;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;

@Aspect
@Configuration
public class LimitInterceptor {
    private static final Logger logger = LoggerFactory.getLogger(LimitInterceptor.class);


    @Autowired
    private RedisLimiterCondition redisLimiterCondition;
    @Autowired
    private FixTimeWindowLimit fixTimeWindowLimit;
    @Autowired
    private SlideTimeWindowLimit slideTimeWindowLimit;
    @Autowired
    private LeakyBucketLimit leakyBucketLimit;
    @Autowired
    private TokenBucketLimit tokenBucketLimit;


    @Around("execution(public * *(..)) && @annotation(com.eoi.fly.redislimiter.annotaion.Limit)")
    public Object interceptor(ProceedingJoinPoint joinPoint) throws Throwable {

        Object target = joinPoint.getTarget();
        String className = target.getClass().getName();
        //获取连接点的方法签名对象
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        //获取方法实例
        Method method = methodSignature.getMethod();
        //获取注解实例
        Limit limitAnnotation = method.getAnnotation(Limit.class);
        //注解中的类型
        KeyType limitType = limitAnnotation.keyType();

        //获取key名称
        String key;
        //获取限制时间范围
        int limitPeriod = limitAnnotation.period();
        //获取限制访问次数
        int limitCount = limitAnnotation.count();
        switch (limitType){
            //如果类型是IP，则根据IP限制访问次数，key取IP地址
            case IP:
                // 获取请求信息
                HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
                key = IPUtil.getIpAdrress(request);
                break;
            //如果类型是customer，则根据key限制访问次数
            case CUSTOMER:
                key = limitAnnotation.key();
                if(StringUtils.isBlank(key)){
                    throw new LimiterOperationException("当前KEY类型为自定义KEY，而注解@Limit未指定自定义key");
                }
                break;
            //否则按照全路径类名+方法名称限制访问次数
            default:
                key = className + "." + StringUtils.upperCase(method.getName());
        }
        logger.info("KEY==>{}",key);
        boolean limit = false;
        if(redisLimiterCondition.getType().equalsIgnoreCase(LimitType.FIXEDTIMEWINDOWLIMIT.getCode())){
            //固定时间窗口限流
            limit = fixTimeWindowLimit.limit(key, limitCount, limitPeriod);
        }else if(redisLimiterCondition.getType().equalsIgnoreCase(LimitType.SLIDETIMEWINDOWLIMIT.getCode())){
            //滑动时间窗口限流
            limit = slideTimeWindowLimit.limit(key, limitCount, limitPeriod);
        }else if(redisLimiterCondition.getType().equalsIgnoreCase(LimitType.LEAKYBUCKETLIMIT.getCode())){
            //漏桶限流
            limit = leakyBucketLimit.limit(key, 0, 0);
        }else if(redisLimiterCondition.getType().equalsIgnoreCase(LimitType.TOKENBUCKETLIMIT.getCode())){
            //令牌桶限流
            limit = tokenBucketLimit.limit(key, 0, 0);
        }

        if(limit){
            return joinPoint.proceed();
        }else {
            throw new RedisLimiterException("访问超限");
        }


    }






}
