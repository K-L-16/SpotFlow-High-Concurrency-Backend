package com.kl.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kl.dto.Result;
import com.kl.entity.ShopType;

import com.kl.repository.ShopTypeRepository;
import com.kl.service.IShopTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static com.kl.utils.RedisConstants.CACHE_SHOP_TYPE_KEY;
import static com.kl.utils.RedisConstants.CACHE_SHOP_TYPE_TTL;


@Service
public class ShopTypeServiceImpl  implements IShopTypeService {


    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private ShopTypeRepository shopTypeRepository;

    @Override
    public Result queryShopTypeZSet() {
        // 1.从 Redis 中查询商铺缓存
        Set<String> shopTypeJsonSet = stringRedisTemplate.opsForZSet().range(CACHE_SHOP_TYPE_KEY, 0, -1);

        // 2.判断 Redis 中是否有该缓存
        if (shopTypeJsonSet.size() != 0) {
            // 2.1.若 Redis 中存在该缓存，则直接返回
            List<ShopType> shopTypes = new ArrayList<>();
            for (String str : shopTypeJsonSet) {
                try {
                    ShopType shopType = objectMapper.readValue(str, ShopType.class);
                    shopTypes.add(shopType);
                } catch (JsonProcessingException e) {
                    throw new RuntimeException("json转换错误",e);
                }
            }
            return Result.ok(shopTypes);
        }

        // 2.2.若 Redis 中无该数据的缓存，则查询数据库
        List<ShopType> shopTypes = shopTypeRepository.findAllByOrderBySortAsc();

        // 3.判断数据库中是否存在
        if (shopTypes == null || shopTypes.isEmpty()) {
            // 3.1.数据库中也不存在，则返回 false
            return Result.fail("分类不存在！");
        }

        // 3.2.数据库中存在，则将查询到的信息存入 Redis
        for (ShopType shopType : shopTypes) {
            try {
                String shoptypeStr = objectMapper.writeValueAsString(shopType);
                stringRedisTemplate.opsForZSet().add(CACHE_SHOP_TYPE_KEY, shoptypeStr,shopType.getSort());
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
        // 3.3.设置过期时间
        stringRedisTemplate.expire(CACHE_SHOP_TYPE_KEY, CACHE_SHOP_TYPE_TTL, TimeUnit.MINUTES);

        // 3.3返回
        return Result.ok(shopTypes);
    }



}
