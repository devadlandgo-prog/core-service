package com.landgo.coreservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableJpaAuditing
@EnableScheduling
@EnableAsync
// Force re-deployment
public class CoreServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(CoreServiceApplication.class, args);
    }
}
