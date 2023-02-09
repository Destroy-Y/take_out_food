package com.itzc.schoolfood;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Slf4j
@SpringBootApplication
@ServletComponentScan //扫描过滤器等注解
@EnableTransactionManagement //开启事务注解支持
@EnableCaching //Spring Cache 的注解方式缓存功能
public class SchoolFoodApplication {
    public static void main(String[] args) {
        SpringApplication.run(SchoolFoodApplication.class,args);
        log.info("项目启动成功......");
    }


}
