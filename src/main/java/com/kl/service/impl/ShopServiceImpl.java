package com.kl.service.impl;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.kl.dto.Result;
import com.kl.entity.Shop;
import com.kl.repository.ShopRepository;
import com.kl.service.IShopService;
import com.kl.utils.CacheClient;
import com.kl.utils.SystemConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

import static com.kl.utils.RedisConstants.*;


@Service
public class ShopServiceImpl  implements IShopService {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CacheClient cacheClient;

    @Autowired
    private ShopRepository shopRepository;


    /**
     * 查询商铺信息
     * @param id
     * @return
     */
    @Override
    public Result queryById(Long id) {
        //逻辑过期解决缓存击穿，+ 缓存穿透(这个有个问题，就是缓存必须提前加载,也就是说这个redis里面必须有redisdata这个)
        Shop shop = cacheClient.queryCacheBreakWithLogicalExpire(
                CACHE_SHOP_KEY,
                id,
                Shop.class,
                id1 -> shopRepository.findById(id1).orElse(null),
                CACHE_SHOP_TTL,
                TimeUnit.MINUTES
        );
        if (shop == null) {
            return Result.fail("店铺不存在");
        }
        //返回
        return Result.ok(shop);
    }


    /**
     * 更新商铺信息
     * @param shop
     * @return
     */
    @Override
    @Transactional
    public Result update(Shop shop) {
        Long id  =  shop.getId();
        if (id == null){
            return Result.fail("店铺id不能为空");
        }
        //1.更新数据库
        shopRepository.save(shop);
        //2.删除缓存
        stringRedisTemplate.delete(CACHE_SHOP_KEY + shop.getId());
        return Result.ok();
    }

    /**
     * 根据类型分页查询商铺信息
     * @param typeId
     * @param current
     * @param x
     * @param y
     * @return
     */
    @Override
    public Result queryShopByType(Long typeId, Integer current, Double x, Double y) {
        //这里就先不实现这个版本了
        //1.判断是否需要更加坐标查询
        Pageable pageable = PageRequest.of(
                current - 1,
                SystemConstants.DEFAULT_PAGE_SIZE
        );

        Page<Shop> page = shopRepository.findByTypeId(typeId, pageable);

        return Result.ok(page.getContent());
    }

    /**
     * 保存商铺信息
     * @param shop
     */
    @Override
    public void save(Shop shop) {
        shopRepository.save(shop);
    }

    /**
     * 根据名称查询商铺信息
     * @param name
     * @param current
     * @return
     */
    @Override
    public Result queryShopByName(String name, Integer current) {
        Pageable pageable = PageRequest.of(
                current - 1,
                SystemConstants.MAX_PAGE_SIZE
        );

        Page<Shop> page;
        if (name == null || name.trim().isEmpty()) {
            page = shopRepository.findAll(pageable);
        } else {
            page = shopRepository.findByNameContaining(name, pageable);
        }

        return Result.ok(page.getContent());
    }

}
