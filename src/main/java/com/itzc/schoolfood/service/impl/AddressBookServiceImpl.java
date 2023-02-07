package com.itzc.schoolfood.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itzc.schoolfood.entity.AddressBook;
import com.itzc.schoolfood.mapper.AddressBookMapper;
import com.itzc.schoolfood.service.AddressBookService;
import org.springframework.stereotype.Service;

@Service
public class AddressBookServiceImpl extends ServiceImpl<AddressBookMapper, AddressBook> implements AddressBookService {
}
