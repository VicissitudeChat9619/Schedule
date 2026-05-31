package com.myschedule;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MyscheduleApplication {

    public static void main(String[] args) {
        SpringApplication.run(MyscheduleApplication.class, args);
    }
}
