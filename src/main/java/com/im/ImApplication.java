package com.im;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ImApplication {
    public static void main(String[] args) {
        SpringApplication.run(ImApplication.class, args);
        System.out.println("========================================");
        System.out.println("IM system started successfully");
        System.out.println("Open it from the server IP, domain, or public tunnel URL");
        System.out.println("Frontend and WebSocket use the current browser host automatically");
        System.out.println("WebSocket path: /im");
        System.out.println("========================================");
    }
}
