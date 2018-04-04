package com.wxm;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan({"com.wxm","org.activiti"})
@MapperScan("com.wxm.mapper")
public class SpringBootWithActivitiApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringBootWithActivitiApplication.class, args);
	}
}
