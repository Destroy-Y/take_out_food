package com.itzc.schoolfood.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itzc.schoolfood.common.R;
import com.itzc.schoolfood.dto.DishDto;
import com.itzc.schoolfood.entity.*;
import com.itzc.schoolfood.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 菜品
 */

@Slf4j
@RestController
@Component
@RequestMapping("/dish")
public class DishController {

    @Autowired
    private DishService dishService;

    @Autowired
    private DishFlavorService dishFlavorService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private SetmealService setmealService;

    @Autowired
    private SetmealDishService setmealDishService;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 新增菜品
     *
     * @param dishDto
     * @return
     */
    @PostMapping
    public R<String> save(@RequestBody DishDto dishDto) {
        log.info(String.valueOf(dishDto));

        dishService.saveWithFlavor(dishDto);

        //清理所有菜品的缓存数据
        Set keys = redisTemplate.keys("dish_*");
        redisTemplate.delete(keys);

        return R.success("新增菜品成功");
    }

    /**
     * 菜品信息分页查询
     *
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name) {

        //构造分页构造器
        Page<Dish> pageInfo = new Page<>(page, pageSize);
        Page<DishDto> dishDtoPage = new Page<>();

        //条件构造器
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        //添加过滤条件
        queryWrapper.like(name != null, Dish::getName, name);
        queryWrapper.orderByDesc(Dish::getUpdateTime);

        //执行分页查询
        dishService.page(pageInfo, queryWrapper);

        //对象拷贝
        //将pageInfo拷贝到dishDtoPage，并忽略records属性，records是一个list集合，装的就是页面中的菜品数据
        BeanUtils.copyProperties(pageInfo, dishDtoPage, "records");
        List<Dish> records = pageInfo.getRecords();     //获取到records集合（菜品数据）
        //通过steam流的方式给分类名进行赋值
        List<DishDto> list = records.stream().map((item) -> {
            DishDto dishDto = new DishDto();

            //将item中属性拷贝到新建的Dto对象中
            BeanUtils.copyProperties(item, dishDto);

            Long categoryId = item.getCategoryId(); //分类id
            Category category = categoryService.getById(categoryId); //根据分类id查询分类对象
            if (category != null) {
                String categoryName = category.getName();   //返回分类名称
                dishDto.setCategoryName(categoryName);  //将分类名称set到Dto对象中
            }
            return dishDto; //将Dto对象返回
        }).collect(Collectors.toList());

        dishDtoPage.setRecords(list);   //将list集合赋值给dishDtoPage

        return R.success(dishDtoPage);
    }

    /**
     * 根据id查询菜品信息和对应的口味信息
     *
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<DishDto> get(@PathVariable Long id) {
        DishDto dishDto = dishService.getByIdWithFlavor(id);
        return R.success(dishDto);
    }

    /**
     * 修改菜品
     *
     * @param dishDto
     * @return
     */
    @PutMapping
    public R<String> update(@RequestBody DishDto dishDto) {
        log.info(String.valueOf(dishDto));
        log.info("dishService:{}", dishService);
        dishService.updateWithFlavor(dishDto);

        //清理所有菜品的缓存数据
        Set keys = redisTemplate.keys("dish_*");
        redisTemplate.delete(keys);

        return R.success("修改菜品成功");
    }

    /**
     * 根据id删除分类
     *
     * @param ids
     * @return
     */
    @DeleteMapping
    public R<String> delete(Long[] ids) {
        log.info("删除分类，id为：{}", ids);
        dishService.removeWithFlavor(ids);    //自己定义的删除方法

        //清理所有菜品的缓存数据
        Set keys = redisTemplate.keys("dish_*");
        redisTemplate.delete(keys);

        return R.success("分类信息删除成功");
    }

    /**
     * 批量起售
     *
     * @param ids
     * @return
     */
    @PostMapping("/status/1")
    public R<String> status1(Long[] ids) {
        log.info("批量启售，id为：{}", ids);
        for (int i = 0; i < ids.length; i++) {
            Dish dish = dishService.getById(ids[i]);
            if (dish.getStatus() == 0) {
                dish.setStatus(1);
                dishService.updateById(dish);
            }
        }

        //清理所有菜品的缓存数据
        Set keys = redisTemplate.keys("dish_*");
        redisTemplate.delete(keys);

        return R.success("启售状态修改成功");
    }

    /**
     * 批量停售
     *
     * @param ids
     * @return
     */
    @PostMapping("/status/0")
    @Transactional
    public R<String> status0(Long[] ids) {
        log.info("批量停售，id为：{}", ids);
        for (int i = 0; i < ids.length; i++) {
            //停售菜品
            Dish dish = dishService.getById(ids[i]);
            if (dish.getStatus() == 1) {
                dish.setStatus(0);
                dishService.updateById(dish);
            }

            //停售包含该菜品的套餐
            LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(SetmealDish::getDishId, ids[i]);
            int count = setmealDishService.count(queryWrapper);
            if (count > 0) { //说明有套餐包含该菜品
                List<SetmealDish> list = setmealDishService.list(queryWrapper);
                for (SetmealDish setmealDish : list) {
                    Long setmealId = setmealDish.getSetmealId();//获取套餐的id
                    Setmeal setmeal = setmealService.getById(setmealId);    //通过id获取对象
                    if (setmeal.getStatus() == 1) {
                        setmeal.setStatus(0);   //将套餐的出售状态设为禁售
                        setmealService.updateById(setmeal);
                    }
                }
            }
        }

        //清理所有菜品的缓存数据
        Set keys = redisTemplate.keys("dish_*");
        redisTemplate.delete(keys);

        return R.success("停售状态修改成功");
    }

//    /**
//     * 根据条件查询对应的菜品数据
//     * @param dish
//     * @return
//     */
//    @GetMapping("/list")
//    public R<List<Dish>> list(Dish dish){
//        //构建查询条件
//        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
//        queryWrapper.eq(dish.getCategoryId() != null,Dish::getCategoryId, dish.getCategoryId());
//        queryWrapper.eq(Dish::getStatus,1);
//        if (dish.getName() != null){
//            queryWrapper.like(Dish::getName,dish.getName());
//        }
//        //添加排序条件
//        queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);
//
//        List<Dish> list = dishService.list(queryWrapper);
//
//        return R.success(list);
//    }

    /**
     * 根据条件查询对应的菜品数据
     *
     * @param dish
     * @return
     */
    @GetMapping("/list")
    public R<List<DishDto>> list(Dish dish) {
        List<DishDto> dishDtoList = null;

        //动态构造key
        String key = "dish_" + dish.getCategoryId() + "_" + dish.getStatus();
        //先从Redis中获取缓存数据
        dishDtoList = (List<DishDto>) redisTemplate.opsForValue().get(key);

        //如果存在，直接返回，无需查询数据库
        if (dishDtoList != null){
            return R.success(dishDtoList);
        }

        //如果不存在，从数据库查询，并将查到的结果缓存到Redis
        //构建查询条件
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(dish.getCategoryId() != null, Dish::getCategoryId, dish.getCategoryId());
        queryWrapper.eq(Dish::getStatus, 1);
        if (dish.getName() != null) {
            queryWrapper.like(Dish::getName, dish.getName());
        }
        //添加排序条件
        queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);

        List<Dish> list = dishService.list(queryWrapper);

        //通过steam流的方式给分类名进行赋值
        dishDtoList = list.stream().map((item) -> {
            DishDto dishDto = new DishDto();

            //将item中属性拷贝到新建的Dto对象中
            BeanUtils.copyProperties(item, dishDto);

//            Long categoryId = item.getCategoryId(); //分类id
//            Category category = categoryService.getById(categoryId); //根据分类id查询分类对象
//            if (category != null) {
//                String categoryName = category.getName();   //返回分类名称
//                dishDto.setCategoryName(categoryName);  //将分类名称set到Dto对象中
//            }

            //当前菜品的id
            Long dishId = item.getId();
            //创建条件构造器LambdaQueryWrapper
            LambdaQueryWrapper<DishFlavor> lambdaQueryWrapper = new LambdaQueryWrapper<>();
            //增加判断条件，以id查询
            lambdaQueryWrapper.eq(DishFlavor::getDishId, dishId);
            //查询到口味信息的list集合
            List<DishFlavor> dishFlavorList = dishFlavorService.list(lambdaQueryWrapper);
            //将集合赋值给dishDto
            dishDto.setFlavors(dishFlavorList);

            return dishDto; //将Dto对象返回
        }).collect(Collectors.toList());

        //将查询到的菜品数据缓存到Redis
        redisTemplate.opsForValue().set(key,dishDtoList,60, TimeUnit.MINUTES);  //缓存时间60分钟

        return R.success(dishDtoList);
    }


}
