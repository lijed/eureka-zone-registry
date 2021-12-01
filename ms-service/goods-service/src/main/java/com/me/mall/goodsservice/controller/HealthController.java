package com.me.mall.goodsservice.controller;


import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class HealthController {

    @GetMapping("/healthCheck")
    public String health(){
        log.info("healthCheck......");
        return "SUCCESS";
    }
}
