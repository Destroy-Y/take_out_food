package com.itzc.schoolfood.dto;


import com.itzc.schoolfood.entity.Setmeal;
import com.itzc.schoolfood.entity.SetmealDish;
import lombok.Data;
import java.util.List;

@Data
public class SetmealDto extends Setmeal {

    private List<SetmealDish> setmealDishes;

    private String categoryName;
}
