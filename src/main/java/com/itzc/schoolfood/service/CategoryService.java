package com.itzc.schoolfood.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itzc.schoolfood.entity.Category;

public interface CategoryService extends IService<Category> {

    public void remove(Long id);

}
