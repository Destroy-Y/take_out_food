package com.itzc.schoolfood.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itzc.schoolfood.dto.SetmealDto;
import com.itzc.schoolfood.entity.Category;
import com.itzc.schoolfood.entity.Dish;
import com.itzc.schoolfood.entity.Setmeal;
import com.itzc.schoolfood.entity.SetmealDish;
import com.itzc.schoolfood.mapper.CategoryMapper;
import com.itzc.schoolfood.mapper.SetmealMapper;
import com.itzc.schoolfood.service.CategoryService;
import com.itzc.schoolfood.service.SetmealDishService;
import com.itzc.schoolfood.service.SetmealService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal> implements SetmealService {

    @Autowired
    private SetmealDishService setmealDishService;

    @Value("${reggie.path}")
    private String basePath;   //图片文件位置

    //新增套餐，同时需要保存套餐和菜品的关联关系
    @Override
    @Transactional
    public void saveWithDish(SetmealDto setmealDto) {
        //保存套餐的基本信息，操作setmeal，执行insert操作
        this.save(setmealDto);

        //如果直接给SetmealDto赋值的话，Setmeal的id值无法传输过去，需要重新给setmeal_id赋值
        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        setmealDishes.stream().map((item) -> {
            item.setSetmealId(setmealDto.getId());
            return item;
        }).collect(Collectors.toList());

        //保存套餐和菜品的关联信息
        setmealDishService.saveBatch(setmealDishes);    //批量保存

    }


    //删除套餐及套餐和菜品的关联数据
    @Transactional
    public void removeWithDish(List<Long> ids){
        //删除 套餐和菜品关联表 中数据
        LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(SetmealDish::getSetmealId,ids);
        setmealDishService.remove(queryWrapper);

        //删除套餐表中数据
        for (int i = 0; i < ids.size(); i++) {
            Setmeal setmeal = this.getById(ids.get(i));
            String image = setmeal.getImage();
            //删除文件夹中图片
            File file = new File(basePath + image);
            if (file.exists()){
                file.delete();
            }
            //删除对象
            this.removeById(ids.get(i));
        }

//        this.removeByIds(ids);
    }


    //根据id查询套餐及关联关系
    public SetmealDto getByIdWithDish(Long id){
        SetmealDto setmealDto = new SetmealDto();
        Setmeal setmeal = this.getById(id);
        //将setmeal查询到的值拷贝给setmealDto
        BeanUtils.copyProperties(setmeal,setmealDto);

        //查询套餐对应的菜品
        LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SetmealDish::getSetmealId,setmeal.getId());
        //获取对应的菜品数组
        List<SetmealDish> list = setmealDishService.list(queryWrapper);
        setmealDto.setSetmealDishes(list);

        return setmealDto;
    }

    //修改套餐
    @Transactional
    public void updateWithDish(SetmealDto setmealDto){
        //提交setmeal表数据
        this.updateById(setmealDto);

        //删除套餐菜品关联表信息
        LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SetmealDish::getSetmealId,setmealDto.getId());
        setmealDishService.remove(queryWrapper);

        //提交当前套餐包含菜品数据
        Long setmealId = setmealDto.getId();
        List<SetmealDish> list = setmealDto.getSetmealDishes(); //获取菜品集合
        list.stream().map((item) -> {
            item.setSetmealId(setmealId);
            return item;
        }).collect(Collectors.toList());

        setmealDishService.saveBatch(list);

    }

}
