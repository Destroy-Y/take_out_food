package com.itzc.schoolfood.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.itzc.schoolfood.entity.AddressBook;
import com.itzc.schoolfood.entity.Orders;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OrderMapper extends BaseMapper<Orders> {
}
