package com.coderzxh;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

/** 
 * @Description: 指定项目为springboot，由此类当作程序入口，自动装配 web 依赖的环境
 * @author zxh
 *
 */
@SpringBootApplication
@MapperScan("com.coderzxh.persistence.mapper")
@EnableCaching
@Slf4j
public class SpringbootApplication{
	public static void main(String[] args) {
        SpringApplication.run(SpringbootApplication.class, args);
    }

}
