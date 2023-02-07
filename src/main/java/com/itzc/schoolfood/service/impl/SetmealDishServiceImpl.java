package com.itzc.schoolfood.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itzc.schoolfood.entity.Setmeal;
import com.itzc.schoolfood.entity.SetmealDish;
import com.itzc.schoolfood.mapper.SetmealDishMapper;
import com.itzc.schoolfood.mapper.SetmealMapper;
import com.itzc.schoolfood.service.SetmealDishService;
import com.itzc.schoolfood.service.SetmealService;
import org.springframework.stereotype.Service;

@Service
public class SetmealDishServiceImpl extends ServiceImpl<SetmealDishMapper, SetmealDish> implements SetmealDishService {
}
