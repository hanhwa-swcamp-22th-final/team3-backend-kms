package com.ohgiraffers.team3backendkms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients(basePackages = "com.ohgiraffers.team3backendkms.infrastructure.client.feign")
public class Team3BackendKmsApplication {

    public static void main(String[] args) {
        SpringApplication.run(Team3BackendKmsApplication.class, args);
    }

}
