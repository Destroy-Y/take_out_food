package com.itzc.schoolfood.dto;

import com.itzc.schoolfood.entity.OrderDetail;
import com.itzc.schoolfood.entity.Orders;
import lombok.Data;
import java.util.List;

@Data
public class OrdersDto extends Orders {

    private String userName;

    private String phone;

    private String address;

    private String consignee;

    private List<OrderDetail> orderDetails;
	
}
