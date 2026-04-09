package com.kl.controller;


import com.kl.dto.Result;
import com.kl.service.IShopTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;




@RestController
@RequestMapping("/shop-type")
public class ShopTypeController {

    @Autowired
    private IShopTypeService typeService;

    /**
     * 查询所有商铺类型
     * @return
     */
    @GetMapping("list")
    public Result queryTypeList() {
        return typeService.queryShopTypeZSet();
    }
}
