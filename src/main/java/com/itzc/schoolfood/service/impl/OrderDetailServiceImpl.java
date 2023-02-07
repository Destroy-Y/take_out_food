package com.itzc.schoolfood.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itzc.schoolfood.entity.OrderDetail;
import com.itzc.schoolfood.mapper.OrderDetailMapper;
import com.itzc.schoolfood.service.OrderDetailService;
import com.itzc.schoolfood.service.OrderService;
import org.springframework.stereotype.Service;

@Service
public class OrderDetailServiceImpl extends ServiceImpl<OrderDetailMapper, OrderDetail> implements OrderDetailService {
}
