package com.itzc.schoolfood.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itzc.schoolfood.common.BaseContext;
import com.itzc.schoolfood.common.CustomException;
import com.itzc.schoolfood.entity.*;
import com.itzc.schoolfood.mapper.OrderMapper;
import com.itzc.schoolfood.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper,Orders> implements OrderService {

    @Autowired
    private ShoppingCartService shoppingCartService;

    @Autowired
    private UserService userService;

    @Autowired
    private AddressBookService addressBookService;

    @Autowired
    private OrderDetailService orderDetailService;

    /**
     * 用户下单
     * @param orders
     */
    @Override
    @Transactional
    public void submit(Orders orders) {
        //获得当前用户id
        Long userId = BaseContext.getCurrentId();

        //查询当前用户的购物车数据
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId,userId);    //通过用户id查询购物车内容
        List<ShoppingCart> shoppingCarts = shoppingCartService.list(queryWrapper);
        if (shoppingCarts == null || shoppingCarts.size() == 0){
            throw new CustomException("购物车为空，不能下单");    //如果为空，购物车抛出异常
        }

        //查询用户数据
        User user = userService.getById(userId);

        //查询地址
        Long addressBookId = orders.getAddressBookId();
        AddressBook addressBook = addressBookService.getById(addressBookId);
        if (addressBook == null){
            throw new CustomException("用户地址信息有误，不能下单");
        }

        //向订单表插入数据，一条

        long orderId = IdWorker.getId(); //自动生成id，作为订单号

        AtomicInteger amount = new AtomicInteger(0);    //原子操作方法，用于计算总金额，设置初始值为0

        List<OrderDetail> orderDetails = shoppingCarts.stream().map((item) -> {
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setOrderId(orderId);    //自动生成的订单号
            orderDetail.setNumber(item.getNumber());    //菜品份数
            orderDetail.setDishFlavor(item.getDishFlavor());
            orderDetail.setDishId(item.getDishId());
            orderDetail.setSetmealId(item.getSetmealId());
            orderDetail.setName(item.getName());
            orderDetail.setImage(item.getImage());
            orderDetail.setAmount(item.getAmount());    //菜品单价
            amount.addAndGet(item.getAmount().multiply(new BigDecimal(item.getNumber())).intValue());   //单价*份数计算金额
            return orderDetail;
        }).collect(Collectors.toList());

        orders.setId(orderId);
        orders.setOrderTime(LocalDateTime.now());
        orders.setCheckoutTime(LocalDateTime.now());
        orders.setStatus(2);
        orders.setAmount(new BigDecimal(amount.get())); //BigDecimal(amount。get()) : 创建一个具有amount所指定整数值的对象
        orders.setUserId(userId);   //用户id
        orders.setNumber(String.valueOf(orderId));  //订单号
        orders.setUserName(user.getName()); //用户姓名
        orders.setConsignee(addressBook.getConsignee());    //设置收货人姓名
        orders.setPhone(addressBook.getPhone());    //收货人手机号
        orders.setAddress(  //设置收货地址，将省，市，区等拼接
                (addressBook.getProvinceName() == null ? "" : addressBook.getProvinceName()) +
                (addressBook.getCityName() == null ? "" : addressBook.getCityName()) +
                (addressBook.getDistrictName() == null ? "" : addressBook.getDistrictName()) +
                (addressBook.getDetail() == null ? "" : addressBook.getDetail())
        );

        this.save(orders);

        //向订单明细表插入数据，多条
        orderDetailService.saveBatch(orderDetails);

        //清空购物车
        shoppingCartService.remove(queryWrapper);

    }
}
