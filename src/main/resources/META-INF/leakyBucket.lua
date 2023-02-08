-- 漏桶限流
-- key
local key = KEYS[1]
-- 桶容量
local capacity = tonumber(ARGV[1])
-- 水漏出的速度
local rate = tonumber(ARGV[2])
-- 初始水量
local tokens = tonumber(ARGV[3])
-- 新的时间
local now = tonumber(ARGV[4])
-- 时间戳的key
local timestampName = KEYS[2]
-- 获取当前水量
local water = tonumber(redis.call('GET', key))
-- 获取时间戳
local timestamp = 0
-- 如果没有水量，设置初始水量
if (not(redis.call('GET', timestampName))) then
    redis.call('SET', key, tokens)
    redis.call('SET', timestampName, tostring(now))
    water = tokens
    timestamp = now
else
    timestamp = tonumber(redis.call('GET', timestampName))
end
-- 先执行漏水，计算剩余水量（计算剩余请求数）
local tempWater = tonumber(water - ((now - timestamp) / 1000) * rate)
if (tempWater and tempWater > 0) then
    water = tempWater
else
    water = 0
end
-- 更新水量
redis.call('SET', key, math.ceil(water))
-- 更新时间戳
redis.call('SET', timestampName, tostring(now))
if ((water + 1) < capacity) then
    redis.call('INCR', key)
    return 1
else
    return -1
end