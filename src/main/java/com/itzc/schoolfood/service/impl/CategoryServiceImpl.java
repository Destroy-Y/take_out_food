package com.itzc.schoolfood.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itzc.schoolfood.common.CustomException;
import com.itzc.schoolfood.entity.Category;
import com.itzc.schoolfood.entity.Dish;
import com.itzc.schoolfood.entity.Employee;
import com.itzc.schoolfood.entity.Setmeal;
import com.itzc.schoolfood.mapper.CategoryMapper;
import com.itzc.schoolfood.mapper.EmployeeMapper;
import com.itzc.schoolfood.service.CategoryService;
import com.itzc.schoolfood.service.DishService;
import com.itzc.schoolfood.service.EmployeeService;
import com.itzc.schoolfood.service.SetmealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

    @Autowired
    private DishService dishService;
    @Autowired
    private SetmealService setmealService;

    /**
     * 根据id删除分类
     * @param id
     */
    @Override
    public void remove(Long id) {
        //查询当前分类是否关联了菜品，如果已经关联，抛出一个业务异常
        LambdaQueryWrapper<Dish> dishLambdaQueryWrapper = new LambdaQueryWrapper<>();
        //添加查询条件，根据分类id进行查询
        dishLambdaQueryWrapper.eq(Dish::getCategoryId,id);
        int count1 = dishService.count(dishLambdaQueryWrapper);
        if (count1 > 0){
            //已关联菜品，抛出业务异常
            throw new CustomException("当前分类下关联了菜品，无法删除");
        }

        //查询当前分类是否关联了套餐，如果已经关联，抛出一个业务异常
        LambdaQueryWrapper<Setmeal> setmealLambdaQueryWrapper = new LambdaQueryWrapper<>();
        //添加查询条件，根据分类id进行查询
        setmealLambdaQueryWrapper.eq(Setmeal::getCategoryId,id);
        int count2 = setmealService.count(setmealLambdaQueryWrapper);
        if (count2 > 0){
            //已关联套餐，抛出业务异常
            throw new CustomException("当前分类下关联了套餐，无法删除");
        }

        //正常删除分类
        super.removeById(id);
    }
}
