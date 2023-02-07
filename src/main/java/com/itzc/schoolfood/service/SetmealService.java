package com.itzc.schoolfood.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itzc.schoolfood.dto.SetmealDto;
import com.itzc.schoolfood.entity.Category;
import com.itzc.schoolfood.entity.Setmeal;

import java.util.List;

public interface SetmealService extends IService<Setmeal> {

    //新增套餐，同时需要保存套餐和菜品的关联关系
    public void saveWithDish(SetmealDto setmealDto);

    //删除套餐及套餐和菜品的关联数据
    public void removeWithDish(List<Long> ids);

    //根据id查询套餐及关联关系
    public SetmealDto getByIdWithDish(Long id);

    //修改套餐
    public void updateWithDish(SetmealDto setmealDto);

}
