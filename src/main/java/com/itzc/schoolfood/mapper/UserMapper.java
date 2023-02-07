package com.itzc.schoolfood.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.itzc.schoolfood.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
