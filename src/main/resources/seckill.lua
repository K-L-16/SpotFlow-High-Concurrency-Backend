-- 1. 参数列表、
-- 1.1 优惠券id
local voucherId = ARGV[1]
-- 1.2 用户id
local userId = ARGV[2]

-- 2.数据key
--2.1库存key
local stockKey = "seckill:stock:" .. voucherId
--2.2订单key(这个是个set，用于存储用户id)
local orderKey = "seckill:order:" .. voucherId

-- 3.脚本逻辑
--3.1判断库存是否充足(rediscall得到的是string所以要给他转成number)
local stock = redis.call("get", stockKey)
if (not stock) then
    return 1
end

if (tonumber(stock) <= 0) then
    return 1
end
--3.2 判断用户是否重复抢购,
if(redis.call('sismember',orderKey,userId) == 1) then
    -- 存在，锁门是重复下单，返回2
    return 2
end
-- 3.4 扣库存
redis.call("incrby", stockKey, -1)
--3.5下单（保存用户）
redis.call("sadd", orderKey, userId)
return 0