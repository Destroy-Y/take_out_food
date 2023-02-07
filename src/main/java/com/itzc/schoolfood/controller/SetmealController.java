package com.itzc.schoolfood.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itzc.schoolfood.common.R;
import com.itzc.schoolfood.dto.DishDto;
import com.itzc.schoolfood.dto.SetmealDto;
import com.itzc.schoolfood.entity.Category;
import com.itzc.schoolfood.entity.Dish;
import com.itzc.schoolfood.entity.Setmeal;
import com.itzc.schoolfood.entity.SetmealDish;
import com.itzc.schoolfood.service.CategoryService;
import com.itzc.schoolfood.service.DishService;
import com.itzc.schoolfood.service.SetmealDishService;
import com.itzc.schoolfood.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 套餐
 */

@Slf4j
@RestController
@RequestMapping("/setmeal")
public class SetmealController {

    @Autowired
    private SetmealService setmealService;
    @Autowired
    private SetmealDishService setmealDishService;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private DishService dishService;

    /**
     * 新增套餐
     *
     * @param setmealDto
     * @return
     */
    @PostMapping
    public R<String> save(@RequestBody SetmealDto setmealDto) {
        log.info("套餐信息：{}", setmealDto);

        setmealService.saveWithDish(setmealDto);

        return R.success("新增套餐成功");
    }

    /**
     * 套餐分页查询
     *
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name) {
        Page<Setmeal> pageInfo = new Page<>(page, pageSize);
        Page<SetmealDto> dtoPage = new Page<>();

        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(name != null, Setmeal::getName, name);
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);

        setmealService.page(pageInfo, queryWrapper);

        //对象拷贝
        BeanUtils.copyProperties(pageInfo, dtoPage, "records");
        List<Setmeal> records = pageInfo.getRecords();
        List<SetmealDto> list = records.stream().map((item) -> {
            SetmealDto setmealDto = new SetmealDto();
            BeanUtils.copyProperties(item, setmealDto);  //对象拷贝
            //分类id
            Long categoryId = item.getCategoryId();
            //分类名称
            Category category = categoryService.getById(categoryId);
            if (category != null) {
                //分类名称
                String categoryName = category.getName();
                setmealDto.setCategoryName(categoryName);
            }
            return setmealDto;
        }).collect(Collectors.toList());

        dtoPage.setRecords(list);
        return R.success(dtoPage);
    }

    /**
     * 删除套餐功能
     *
     * @param ids
     * @return
     */
    @DeleteMapping
    public R<String> delete(@RequestParam List<Long> ids) {    //也可以直接用 Long[] ids 接收
        log.info("删除的ids:{}", ids);
        setmealService.removeWithDish(ids);
        return R.success("套餐数据删除成功");
    }

    /**
     * 根据id返回套餐信息
     *
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<SetmealDto> get(@PathVariable Long id) {
        SetmealDto setmealDto = setmealService.getByIdWithDish(id);
        return R.success(setmealDto);
    }

    /**
     * 修改套餐信息
     *
     * @param setmealDto
     * @return
     */
    @PutMapping
    public R<String> update(@RequestBody SetmealDto setmealDto) {
        log.info(String.valueOf(setmealDto));
        setmealService.updateWithDish(setmealDto);
        return R.success("修改套餐成功");
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
            Setmeal setmeal = setmealService.getById(ids[i]);
            if (setmeal.getStatus() == 0) {
                setmeal.setStatus(1);
                setmealService.updateById(setmeal);
            }
        }

        return R.success("启售状态修改成功");
    }

    /**
     * 批量停售
     *
     * @param ids
     * @return
     */
    @PostMapping("/status/0")
    public R<String> status0(Long[] ids) {
        log.info("批量停售，id为：{}", ids);
        for (int i = 0; i < ids.length; i++) {
            Setmeal setmeal = setmealService.getById(ids[i]);
            if (setmeal.getStatus() == 1) {
                setmeal.setStatus(0);
                setmealService.updateById(setmeal);
            }
        }
        return R.success("停售状态修改成功");
    }

    /**
     * 返回套餐集合
     * @param setmeal
     * @return
     */
    @GetMapping("/list")
    public R<List<Setmeal>> list(Setmeal setmeal) {
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(setmeal.getCategoryId() != null, Setmeal::getCategoryId, setmeal.getCategoryId());
        queryWrapper.eq(setmeal.getStatus() != null, Setmeal::getStatus, setmeal.getStatus());

        List<Setmeal> list = setmealService.list(queryWrapper);

        return R.success(list);
    }

    /**
     * 在客户界面，获取套餐内包含菜品
     * @param id
     * @return
     */
    @GetMapping("/dish/{id}")
    public R<List<DishDto>> getDish(@PathVariable Long id){

        LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SetmealDish::getSetmealId,id);
        List<SetmealDish> setmealDishList = setmealDishService.list(queryWrapper);  //通过条件获取该套餐包含菜品的setmealDish集合
        List<DishDto> dishDtoList = new ArrayList<>();  //创建新的DishDto集合用于返回数据
        for (SetmealDish setmealDish : setmealDishList) {   //遍历获取的setmealDish集合
            Long dishId = setmealDish.getDishId();  //获取菜品的id
            Dish dish = dishService.getById(dishId);    //通过id查询菜品信息
            DishDto dishDto = new DishDto();    //创建新的DishDto对象
            BeanUtils.copyProperties(dish,dishDto); //将dish的数据拷贝给dishDto
            dishDto.setCopies(setmealDish.getCopies()); //将套餐中包含该菜品的份数设置到dishDto中
            dishDtoList.add(dishDto);   //将该dishDto对象加入到返回的集合中
        }

        return R.success(dishDtoList);
    }


}
