package com.project.frame;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.PropertySource;

@SpringBootApplication
@MapperScan("com.project.frame.mapper")    // *mapper.java的所在包路径
@PropertySource(value = {"classpath:/application-service.properties", "classpath:/application-app.properties"}, encoding = "utf-8") // 属性配置文件路径
public class FrameApplication {

    public static void main(String[] args) {
        SpringApplication.run(FrameApplication.class, args);
    }

}
