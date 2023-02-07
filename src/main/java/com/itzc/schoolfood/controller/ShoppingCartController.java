package com.itzc.schoolfood.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.itzc.schoolfood.common.BaseContext;
import com.itzc.schoolfood.common.R;
import com.itzc.schoolfood.entity.OrderDetail;
import com.itzc.schoolfood.entity.ShoppingCart;
import com.itzc.schoolfood.service.OrderDetailService;
import com.itzc.schoolfood.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;


/**
 * 购物车
 */

@RestController
@RequestMapping("/shoppingCart")
@Slf4j
public class ShoppingCartController {

    @Autowired
    private ShoppingCartService shoppingCartService;


    /**
     * 添加购物车
     *
     * @param shoppingCart
     * @return
     */
    @PostMapping("/add")
    public R<ShoppingCart> add(@RequestBody ShoppingCart shoppingCart) {
        log.info("购物车数据:{}", shoppingCart);

        //设置用户id
        Long currentId = BaseContext.getCurrentId();
        shoppingCart.setUserId(currentId);

        //查询当前菜品或者套餐是否在购物车中
        Long dishId = shoppingCart.getDishId();
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId, currentId);

        if (dishId != null) {
            //添加到购物车的是菜品
            queryWrapper.eq(ShoppingCart::getDishId, dishId);
        } else {
            //添加到购物车的是套餐
            queryWrapper.eq(ShoppingCart::getSetmealId, shoppingCart.getSetmealId());
        }

        ShoppingCart cartServiceOne = shoppingCartService.getOne(queryWrapper);

        if (cartServiceOne != null) {
            //如果已存在口味相同的，就在原来的数量上+1
            Integer number = cartServiceOne.getNumber();
            cartServiceOne.setNumber(number + 1);
            shoppingCartService.updateById(cartServiceOne);
        }else{
            //如果不存在，则添加到购物车
            shoppingCart.setNumber(1);  //设置初始值为1，不设置也可以，数据库默认为1
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCartService.save(shoppingCart);

            cartServiceOne = shoppingCart;  //为了方便返回数据
        }

        return R.success(cartServiceOne);
    }

    /**
     * 菜品数量减少
     *
     * @param shoppingCart
     * @return
     */
    @PostMapping("/sub")
    public R<ShoppingCart> sub(@RequestBody ShoppingCart shoppingCart) {
        log.info("要减少的菜品id:{}", shoppingCart);

        ShoppingCart returnShoppingCart = new ShoppingCart();

        Long dishId = shoppingCart.getDishId();
        //判断是否为菜品
        if (dishId != null){
            //查询购物车中该菜品的数量
            LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(ShoppingCart::getDishId,dishId);

            ShoppingCart cartServiceOne = shoppingCartService.getOne(queryWrapper);
            Integer number = cartServiceOne.getNumber();    //获取菜品数量
            if (number > 1){
                cartServiceOne.setNumber(number - 1);
                shoppingCartService.updateById(cartServiceOne);
            }else {
                cartServiceOne.setNumber(number - 1);
                shoppingCartService.remove(queryWrapper);
            }
            returnShoppingCart = cartServiceOne;
        }else{
            //否则为套餐
            Long setmealId = shoppingCart.getSetmealId();
            //查询购物车中该套餐的数量
            LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(ShoppingCart::getSetmealId,setmealId);

            ShoppingCart cartServiceOne = shoppingCartService.getOne(queryWrapper);
            Integer number = cartServiceOne.getNumber();    //获取套餐数量
            if (number > 1){
                cartServiceOne.setNumber(number - 1);
                shoppingCartService.updateById(cartServiceOne);
            }else {
                cartServiceOne.setNumber(number - 1);
                shoppingCartService.remove(queryWrapper);
            }
            returnShoppingCart = cartServiceOne;
        }

        return R.success(returnShoppingCart);
    }

    /**
     * 查看购物车
     * @return
     */
    @GetMapping("/list")
    public R<List<ShoppingCart>> list(){
        log.info("查看购物车");
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId,BaseContext.getCurrentId());
        queryWrapper.orderByAsc(ShoppingCart::getCreateTime);

        List<ShoppingCart> list = shoppingCartService.list(queryWrapper);

        return R.success(list);
    }

    /**
     * 清空购物车
     * @return
     */
    @DeleteMapping("/clean")
    public R<String> clean(){
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId,BaseContext.getCurrentId());
        shoppingCartService.remove(queryWrapper);
        return R.success("清除购物车成功");
    }


}
