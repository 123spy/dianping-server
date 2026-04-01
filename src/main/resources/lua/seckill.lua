local stockKey = KEYS[1]
local userSetKey = KEYS[2]

local userId = ARGV[1]

-- 1. 用户是否已经买过
if redis.call('SISMEMBER', userSetKey, userId) == 1 then
    return 2
end

-- 2. 库存 key 是否存在
local stockValue = redis.call('GET', stockKey)
if not stockValue then
    return 3
end

-- 3. 库存是否足够
local stock = tonumber(stockValue)
if not stock or stock <= 0 then
    return 1
end

-- 4. 扣减库存
redis.call('DECR', stockKey)

-- 5. 记录该用户已买过
redis.call('SADD', userSetKey, userId)

-- 6. 返回成功
return 0

--   0：成功
--   1：库存不足
--   2：重复购买
--   3：库存 key 不存在