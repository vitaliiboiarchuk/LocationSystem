package com.example.locationsystem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@EnableAspectJAutoProxy
public class LocationSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(LocationSystemApplication.class, args);
    }

}
