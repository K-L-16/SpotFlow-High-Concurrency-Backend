package com.kl.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import static com.kl.utils.RedisConstants.CACHE_SHOP_KEY;
import static com.kl.utils.RedisConstants.LOCK_SHOP_KEY;

@Component
@Slf4j
public class CacheClient {


    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;
    //线程池用于缓存重建
    private static final ExecutorService CACHE_REBUILD_EXECUTOR = Executors.newFixedThreadPool(10);

    //利用这个构造函数注入
    public CacheClient(StringRedisTemplate stringRedisTemplate,
                       ObjectMapper objectMapper) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * 设置缓存
     * @param key
     * @param value
     * @param time
     * @param unit
     */
    public void set(String key, Object value, Long time, TimeUnit unit) {
        try {
            String s = objectMapper.writeValueAsString(value);
            stringRedisTemplate.opsForValue().set(key, s,time,unit);

        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 设置逻辑过期
     * @param key
     * @param value
     * @param time
     * @param unit
     */
    public void setWithLogicalExpire(String key, Object value, Long time, TimeUnit unit) {

        //封装逻辑过期时间
        RedisData redisData = new RedisData();
        redisData.setExpireTime(LocalDateTime.now().plusSeconds(unit.toSeconds(time)));
        redisData.setData(value);
        try {
            //写入redis
            String s = objectMapper.writeValueAsString(redisData);
            //存入redis按照string存储
            stringRedisTemplate.opsForValue().set(key, s);

        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 缓存穿透
     * @param keyPrefix
     * @param id
     * @param type
     * @param dbFallback
     * @param time
     * @param unit
     * @return
     * @param <R>
     * @param <ID>
     */
    public <R,ID> R queryWithCachePenetration(String keyPrefix, ID id, Class<R> type, Function<ID,R> dbFallback, Long time, TimeUnit unit) {
        String key = keyPrefix + id;
        //1. 从redis里查询商铺缓存
        String json = stringRedisTemplate.opsForValue().get(key);
        //2.判断是否存在
        if (StringUtils.isNotBlank( json)) {
            try {
                //存在直接返回
                R result = objectMapper.readValue(json, type);
                return result;
            } catch (Exception e) {
                throw new RuntimeException("JSON解析失败", e);
            }
        }
        //判断命中的是否是空值,说明这个之前的空值进入了redis缓存（缓存穿透策略）
        if(json != null){
            return null;
        }
        //4.不存在，根据id查询数据库
        R r = dbFallback.apply(id);
        //5.不存在，返回错误
        if (r == null){
            //讲空值写入redis(缓存穿透)
            stringRedisTemplate.opsForValue().set(key, "", RedisConstants.CACHE_NULL_TTL, TimeUnit.MINUTES);
            return null;
        }
        //6.存在，写入redis
        this.set(key, r, time, unit);
        //7.返回
        return r;
    }

    /**
     *缓存击穿，逻辑过期解决缓存击穿
     * @param id
     * @return
     */
    public <R,ID> R queryCacheBreakWithLogicalExpire(String keyPrefix, ID id, Class<R> type,Function<ID,R> dbFallback, Long time, TimeUnit unit) {
        String key = keyPrefix + id;
        //1. 从redis里查询商铺缓存（得到一个string类型的json格式字符串）
        String json = stringRedisTemplate.opsForValue().get(key);
        //2.判断是否存在
        if (StringUtils.isBlank(json)) {
            //3.不存在，直接返回
            return null;
        }
        //4.命中，需要先把json反序列化为对象
        RedisData redisData = null;
        try {
            redisData = objectMapper.readValue(json, RedisData.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        Object data = redisData.getData();
        //由于objectmapper不知道我们里面的这个object是啥，所以它底层会封装进一个liner map，所以我们再用convervalue进行转换
        R r = objectMapper.convertValue(data,type);
        LocalDateTime expireTime = redisData.getExpireTime();
        //5.判断是否过期
        if(expireTime.isAfter(LocalDateTime.now())){
            //5.1未过期，直接返回店铺信息
            return r;
        }
        //5.2已过期，需要缓存重建
        //6缓存重建
        //6.1 获取互斥锁
        String lockKey = LOCK_SHOP_KEY + id;
        boolean isLock = tryLock(lockKey);
        //6.2判断是否获取所成功
        if (isLock){
            //6.3成功，开启独立线程，实现缓存重建

            //第一步先双重检查，避免重复重建
            json = stringRedisTemplate.opsForValue().get(key);
            if (StringUtils.isBlank(json)) {
                unLock(lockKey);
                return null;
            }
            try {
                redisData = objectMapper.readValue(json, RedisData.class);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
            data = redisData.getData();
            r = objectMapper.convertValue(data,type);
            expireTime = redisData.getExpireTime();
            //5.判断是否过期
            if(expireTime.isAfter(LocalDateTime.now())){
                //5.1未过期，直接返回店铺信息
                unLock(lockKey);
                return r;
            }

            //经过了双重检查，这里可以进行重建
            CACHE_REBUILD_EXECUTOR.submit(() -> {
                //重建缓存
                try {
                    //查询数据库
                    R r1 = dbFallback.apply(id);
                    //写入redis
                    this.setWithLogicalExpire(key, r1, time, unit);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }finally {
                    //释放锁
                    unLock(lockKey);
                }

            });
        }
        //6.4 获取所失败，返回过期商品信息
        return r;
    }

    /**
     * 尝试获取锁
     * @param key
     * @return
     */
    private boolean tryLock(String  key) {
        Boolean flag = stringRedisTemplate.opsForValue().setIfAbsent(key, "1", 10, TimeUnit.SECONDS);
        return Boolean.TRUE.equals(flag);
    }
    /**
     * 释放锁
     */
    private void unLock(String key) {
        stringRedisTemplate.delete(key);
    }
}
