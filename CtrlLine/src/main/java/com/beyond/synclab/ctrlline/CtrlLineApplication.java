package com.beyond.synclab.ctrlline;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CtrlLineApplication {

    public static void main(String[] args) {
        SpringApplication.run(CtrlLineApplication.class, args);
    }

}
