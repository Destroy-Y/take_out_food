package com.itzc.schoolfood.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itzc.schoolfood.common.CustomException;
import com.itzc.schoolfood.dto.DishDto;
import com.itzc.schoolfood.entity.*;
import com.itzc.schoolfood.mapper.CategoryMapper;
import com.itzc.schoolfood.mapper.DishMapper;
import com.itzc.schoolfood.service.CategoryService;
import com.itzc.schoolfood.service.DishFlavorService;
import com.itzc.schoolfood.service.DishService;
import com.itzc.schoolfood.service.SetmealDishService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {

    @Autowired
    private DishFlavorService dishFlavorService;
    @Autowired
    private SetmealDishService setmealDishService;

    @Value("${reggie.path}")
    private String basePath;   //图片文件位置

    /**
     * 新增菜品，同时保存对应的口味数据
     * @param dishDto
     */
    @Override
    @Transactional  //开启事务
    public void saveWithFlavor(DishDto dishDto) {

        //保存菜品的基本信息到菜品表
        this.save(dishDto);

        Long dishId = dishDto.getId();  //菜品id
        List<DishFlavor> flavors = dishDto.getFlavors();    //菜品口味
        flavors.stream().map((item) -> {
            item.setDishId(dishId);
            return item;
        }).collect(Collectors.toList());

        //保存菜品口味数据到口味表
        dishFlavorService.saveBatch(flavors);

    }

    /**
     * 根据id来查询菜品信息和对应的口味信息
     * @param id
     * @return
     */
    @Override
    public DishDto getByIdWithFlavor(Long id) {
        //查询菜品基本信息
        Dish dish = this.getById(id);

        //对象拷贝
        DishDto dishDto = new DishDto();
        BeanUtils.copyProperties(dish,dishDto);

        //查询菜品对应的口味信息
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId,dish.getId());    //设置查询条件
        List<DishFlavor> flavors = dishFlavorService.list(queryWrapper);    //通过id查询dish的口味名字
        dishDto.setFlavors(flavors);    //将口味属性放到DishDto对象中

        return dishDto;
    }

    //更新菜品信息，同时更新对应的口味信息
    @Override
    @Transactional
    public void updateWithFlavor(DishDto dishDto) {
        //更新菜品表对应信息
        this.updateById(dishDto);

        //清理当前菜品对应的口味数据
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId,dishDto.getId());
        dishFlavorService.remove(queryWrapper);

        //提交当前的口味数据
        Long dishId = dishDto.getId();  //菜品id
        List<DishFlavor> flavors = dishDto.getFlavors();    //菜品口味
        flavors.stream().map((item) -> {
            item.setDishId(dishId);
            return item;
        }).collect(Collectors.toList());

        dishFlavorService.saveBatch(flavors);

    }

    @Override
    @Transactional
    public void removeWithFlavor(Long[] ids) {
        //查询当前菜品是否关联了套餐
        LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(SetmealDish::getDishId,ids);
        int count = setmealDishService.count(queryWrapper);
        if (count > 0){
            //已关联套餐，抛出业务异常
            throw new CustomException("当前菜品关联了套餐，无法删除");
        }

        for (int i = 0; i < ids.length; i++) {
            Dish dish = this.getById(ids[i]);
            String image = dish.getImage();
            //删除文件夹中图片
            File file = new File(basePath + image);
            if (file.exists()){
                file.delete();
            }

            //删除对象
            this.removeById(ids[i]);
        }
//        super.removeByIds(Arrays.asList(ids)); //批量删除对象（以id判断）


        LambdaQueryWrapper<DishFlavor> dishFlavorLambdaQueryWrapper = new LambdaQueryWrapper<>();
        dishFlavorLambdaQueryWrapper.in(DishFlavor::getDishId,ids);
        dishFlavorService.remove(dishFlavorLambdaQueryWrapper);

    }
}
