package com.itzc.schoolfood.dto;

import com.itzc.schoolfood.entity.Dish;
import com.itzc.schoolfood.entity.DishFlavor;
import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
public class DishDto extends Dish {

    //菜品所对应的口味数据
    private List<DishFlavor> flavors = new ArrayList<>();

    private String categoryName;

    private Integer copies;
}
