package com.me.mall.marketingservice.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PromotionService {

    @GetMapping("/promotion")
    public String getPromotionById(){
        return "查询到指定商品的促销信息";
    }
}
