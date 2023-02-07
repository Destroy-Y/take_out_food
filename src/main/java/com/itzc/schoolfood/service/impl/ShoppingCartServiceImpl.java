package com.itzc.schoolfood.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itzc.schoolfood.entity.ShoppingCart;
import com.itzc.schoolfood.mapper.ShoppingCartMapper;
import com.itzc.schoolfood.service.ShoppingCartService;
import org.springframework.stereotype.Service;

@Service
public class ShoppingCartServiceImpl extends ServiceImpl<ShoppingCartMapper, ShoppingCart> implements ShoppingCartService {
}
