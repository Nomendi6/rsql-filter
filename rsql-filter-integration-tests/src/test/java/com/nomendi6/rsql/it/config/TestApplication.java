package com.nomendi6.rsql.it.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "com.nomendi6.rsql.it")
@EnableJpaRepositories(basePackages = "com.nomendi6.rsql.it.repository")
@EntityScan(basePackages = "com.nomendi6.rsql.it.domain")
public class TestApplication {
    public static void main(String[] args) {
        SpringApplication.run(TestApplication.class, args);
    }
}