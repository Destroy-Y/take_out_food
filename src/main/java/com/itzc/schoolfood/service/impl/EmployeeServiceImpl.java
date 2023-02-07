package com.itzc.schoolfood.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itzc.schoolfood.entity.Employee;
import com.itzc.schoolfood.mapper.EmployeeMapper;
import com.itzc.schoolfood.service.EmployeeService;
import org.springframework.stereotype.Service;

@Service
public class EmployeeServiceImpl extends ServiceImpl<EmployeeMapper,Employee> implements EmployeeService {
}
