package com.itzc.schoolfood.controller;

import com.itzc.schoolfood.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 订单明细
 */

@RequestMapping("/orderDetail")
@RestController
@Slf4j
public class OrderDetailController {

    @Autowired
    private OrderService orderService;



}
