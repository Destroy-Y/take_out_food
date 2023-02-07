package com.itzc.schoolfood.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itzc.schoolfood.common.BaseContext;
import com.itzc.schoolfood.common.R;
import com.itzc.schoolfood.dto.OrdersDto;
import com.itzc.schoolfood.entity.OrderDetail;
import com.itzc.schoolfood.entity.Orders;
import com.itzc.schoolfood.entity.ShoppingCart;
import com.itzc.schoolfood.service.OrderDetailService;
import com.itzc.schoolfood.service.OrderService;
import com.itzc.schoolfood.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 订单
 */

@RequestMapping("/order")
@RestController
@Slf4j
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderDetailService orderDetailService;

    @Autowired
    private ShoppingCartService shoppingCartService;

    /**
     * 用户下单
     *
     * @param orders
     * @return
     */
    @PostMapping("/submit")
    public R<String> submit(@RequestBody Orders orders) {
        log.info("订单数据：{}", orders);
        orderService.submit(orders);

        return R.success("下单成功");

    }

    /**
     * 查询后台分页数据
     * @param page
     * @param pageSize
     * @param number
     * @param beginTime
     * @param endTime
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String number, String beginTime, String endTime) {
        //分页构造器
        Page<Orders> pageInfo = new Page<>(page, pageSize);

        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(number != null, Orders::getNumber, number);
        queryWrapper.ge(beginTime != null, Orders::getOrderTime, beginTime);
        queryWrapper.le(beginTime != null, Orders::getOrderTime, endTime);


        orderService.page(pageInfo, queryWrapper);

        return R.success(pageInfo);
    }

    /**
     * 修改订单状态
     * @param map
     * @return
     */
    @PutMapping
    public R<String> update(@RequestBody Map<String,String> map){

        Long id = Long.valueOf(map.get("id"));
        Integer status = Integer.valueOf(map.get("status"));

        Orders orders = orderService.getById(id);
        //订单状态 1待付款，2待派送，3已派送，4已完成，5已取消
        orders.setStatus(Integer.valueOf(status));   //设置订单状态的值
        orderService.updateById(orders);
        return R.success("订单状态更新成功");
    }

//    /**
//     * 客户端 个人主页 最近订单
//     * @param page
//     * @param pageSize
//     * @return
//     */
//    @GetMapping("/userPage")
//    public R<Page> userPage(int page, int pageSize) {
//        //分页构造器
//        Page<Orders> pageInfo = new Page<>(page, pageSize);
//
//        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();
//        queryWrapper.orderByDesc(Orders::getOrderTime); //按时间排序订单
//        queryWrapper.eq(Orders::getUserId, BaseContext.getCurrentId());
//
//        orderService.page(pageInfo, queryWrapper);
//
//        return R.success(pageInfo);
//    }

    @GetMapping("/userPage")
    public R<Page> page(int page, int pageSize) {
        //获取当前id
        Long userId = BaseContext.getCurrentId();
        Page<Orders> pageInfo = new Page<>(page, pageSize);
        Page<OrdersDto> ordersDtoPage = new Page<>(page, pageSize);
        //条件构造器
        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();
        //查询当前用户id订单数据
        queryWrapper.eq(userId != null, Orders::getUserId, userId);
        //按时间降序排序
        queryWrapper.orderByDesc(Orders::getOrderTime);
        orderService.page(pageInfo, queryWrapper);
        List<OrdersDto> list = pageInfo.getRecords().stream().map((item) -> {
            OrdersDto ordersDto = new OrdersDto();
            //获取orderId,然后根据这个id，去orderDetail表中查数据
            Long orderId = item.getId();
            LambdaQueryWrapper<OrderDetail> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(OrderDetail::getOrderId, orderId);
            List<OrderDetail> details = orderDetailService.list(wrapper);
            BeanUtils.copyProperties(item, ordersDto);
            //之后set一下属性
            ordersDto.setOrderDetails(details);
            return ordersDto;
        }).collect(Collectors.toList());
        BeanUtils.copyProperties(pageInfo, ordersDtoPage, "records");
        ordersDtoPage.setRecords(list);
        //日志输出看一下
        log.info("list:{}", list);
        return R.success(ordersDtoPage);
    }

    /**
     * 再来一单
     * @param map
     * @return
     */
    @PostMapping("/again")
    public R<String> again(@RequestBody Map<String,String> map){
        //获取order_id
        Long orderId = Long.valueOf(map.get("id"));
        //条件构造器
        LambdaQueryWrapper<OrderDetail> queryWrapper = new LambdaQueryWrapper<>();
        //查询订单的口味细节数据
        queryWrapper.eq(OrderDetail::getOrderId,orderId);
        List<OrderDetail> details = orderDetailService.list(queryWrapper);
        //获取用户id，待会需要set操作
        Long userId = BaseContext.getCurrentId();
        List<ShoppingCart> shoppingCarts = details.stream().map((item) ->{
            ShoppingCart shoppingCart = new ShoppingCart();
            //Copy对应属性值
            BeanUtils.copyProperties(item,shoppingCart);
            //设置一下userId
            shoppingCart.setUserId(userId);
            //设置一下创建时间为当前时间
            shoppingCart.setCreateTime(LocalDateTime.now());
            return shoppingCart;
        }).collect(Collectors.toList());
        //加入购物车
        shoppingCartService.saveBatch(shoppingCarts);
        return R.success("喜欢吃就再来一单吖~");
    }


}
