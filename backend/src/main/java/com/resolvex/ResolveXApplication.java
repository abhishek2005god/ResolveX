package com.resolvex;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ResolveXApplication {

    public static void main(String[] args) {
        SpringApplication.run(ResolveXApplication.class, args);
    }
}
