-- 令牌桶限流
-- key
local key = KEYS[1]
-- 桶容量
local capacity = tonumber(ARGV[1])
-- 发放令牌的速度
local rate = tonumber(ARGV[2])
-- 初始令牌数量
local tokens = tonumber(ARGV[3])
-- 新的时间
local now = tonumber(ARGV[4])
-- 时间戳的key
local timestampName = KEYS[2]
-- 获取当前令牌数量
local tokenNum = tonumber(redis.call('GET', key))
-- 获取时间戳
local timestamp = 0
-- 如果没有水量，设置初始水量
if (not(redis.call('GET', timestampName))) then
    redis.call('SET', key, tokens)
    redis.call('SET', timestampName, tostring(now))
    tokenNum = tokens
    timestamp = now
else
    timestamp = tonumber(redis.call('GET', timestampName))
end
-- 先添加令牌
local tempTokenNums = tonumber(tokenNum + ((now - timestamp) / 1000) * rate)
if (tempTokenNums > capacity) then
    tokenNum = capacity
else
    tokenNum = tempTokenNums
end
-- 更新令牌数
redis.call('SET', key, math.floor(tokenNum))
-- 更新时间戳
redis.call('SET', timestampName, tostring(now))
if (tokenNum < 1) then
    return -1
else
    -- 还有令牌，领取令牌
    redis.call('DECR', key)
    return 1
end