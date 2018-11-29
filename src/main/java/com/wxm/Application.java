package com.wxm;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.embedded.ConfigurableEmbeddedServletContainer;
import org.springframework.boot.context.embedded.EmbeddedServletContainerCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan({"com.wxm","org.activiti"})
@MapperScan("com.wxm.mapper")
public class Application {
	@Bean
	public EmbeddedServletContainerCustomizer containerCustomizer(){
		return new EmbeddedServletContainerCustomizer() {
			 @Override
			 public void customize(ConfigurableEmbeddedServletContainer container) {
			 	container.setSessionTimeout(3600);//单位为S 
			 }
		};
	}

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}
}
