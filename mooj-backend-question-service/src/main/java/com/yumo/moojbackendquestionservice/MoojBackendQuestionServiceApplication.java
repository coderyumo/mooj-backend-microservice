package com.yumo.moojbackendquestionservice;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("com.yumo.moojbackendquestionservice.mapper")
@EnableScheduling
@EnableAspectJAutoProxy(proxyTargetClass = true, exposeProxy = true)
@ComponentScan("com.yumo")
@EnableDiscoveryClient
@EnableFeignClients(basePackages = {"com.yumo.moojbackendserviceclient.service"})
public class MoojBackendQuestionServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(MoojBackendQuestionServiceApplication.class, args);
    }

}
