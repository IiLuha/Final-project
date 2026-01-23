package com.itdev.finalproject;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class EventManagerApplication {

    public static void main(String[] args) {
        var run = SpringApplication.run(EventManagerApplication.class, args);
    }

}