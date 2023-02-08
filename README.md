 # 一，使用场景

高并发场景下根据不同维度对用户请求进行限流

# 二，如何使用

使用此组件的前提：项目需引入Redis

引入依赖

```XML
<dependency>
    <groupId>com.eoi.fly</groupId>       
    <artifactId>common-redislimiter-starter</artifactId>
    <version>2.0.0</version>
</dependency>
```

此组件共支持四种限流方式

- 固定时间窗口限流
- 滑动时间窗口限流
- 漏桶限流
- 令牌桶限流



1，**引入依赖后，如果不进行任何配置，直接在接口处加上@Limit限流注解，则默认固定时间窗口限流**

2，**如果想要更换限流方式，配置文件进行配置（当为固定/滑动时间窗口时，capacity/rate/tokens不用配置，这三个参数只有配置为漏桶或令牌桶时才用的）**

```YAML
redislimit: 
    # 限流算法选用，默认按照固定时间窗口算法走
    # FIXEDTIMEWINDOWLIMIT 固定时间窗口
    # SLIDETIMEWINDOWLIMIT 滑动时间窗口
    # LEAKYBUCKETLIMIT     漏桶
    # TOKENBUCKETLIMIT     令牌桶
    type: TOKENBUCKETLIMIT 
    # 限流算法选用 漏桶或令牌桶时 生效
    # 桶容量 不配置 默认100
    capacity: 20
    # 限流算法选用 漏桶或令牌桶时 生效
    # 漏桶时-----水漏出的速度（每秒系统能处理的请求数）
    # 令牌桶时-----令牌放入速度
    # 不配置 默认10
    rate: 5
    # 限流算法选用 漏桶或令牌桶时 生效
    # 漏桶时-----初始化水量
    # 令牌桶时-----初始化令牌数量
    # 不配置 默认20
    tokens: 10
```

3, 当为漏桶/令牌桶限流方式时，@Limit注解的period/count参数不生效，不填即可，将以配置文件配置的capacity/rate/tokens执行



使用方式如下：在需要限流的接口处增加注解@Limit，**在period秒的单位时间内，最多请求次数为count次**

```Java
@RequestMapping("/test")
@Limit(period=5,count=2,limintType=LimitType.IP)
public R test(){
return R.success();
}
```

## 注解说明

```Java
public @interface Limit {

    //key
    String key() default "";
    //时间周期
    int period();
    //在时间周期内的最多访问次数
    int count();
    //key定义的类型
    KeyType keyType() default KeyType.OTHERS;
}
```

> KeyType 支持三种key定义
>
> 1. 自定义key
> 2. 请求IP作为key
> 3. 全路径类名&&接口方法名作为key

**不指定LimitType则默认走③**

## 异常

此组件**限流时抛出的异常为RedisLimiterException**，项目上引用此组件之后，如果用户请求被限流，则会抛出RedisLimiterException异常，**项目对此异常进行捕获，然后做出处理（封装异常/重定向）**即可。