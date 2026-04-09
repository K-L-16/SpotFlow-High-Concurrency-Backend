package com.kl.service;


import com.kl.dto.Result;
import com.kl.entity.Shop;

public interface IShopService  {

    Result queryById(Long id);

    Result update(Shop shop);

    Result queryShopByType(Long typeId, Integer current, Double x, Double y);

    void save(Shop shop);

    Result queryShopByName(String name, Integer current);
}
