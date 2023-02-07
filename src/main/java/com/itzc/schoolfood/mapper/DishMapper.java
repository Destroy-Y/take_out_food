package com.itzc.schoolfood.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.itzc.schoolfood.entity.Category;
import com.itzc.schoolfood.entity.Dish;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DishMapper extends BaseMapper<Dish> {
}
