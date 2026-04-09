package com.kl.controller;


import com.kl.dto.Result;
import com.kl.service.IShopTypeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;




@RestController
@RequestMapping("/shop-type")
@Tag(name = "ShopType api")
public class ShopTypeController {

    @Autowired
    private IShopTypeService typeService;

    /**
     * 查询所有商铺类型
     * @return
     */
    @GetMapping("list")
    @Operation(summary = "query all the shoptype list")
    public Result queryTypeList() {
        return typeService.queryShopTypeZSet();
    }
}
