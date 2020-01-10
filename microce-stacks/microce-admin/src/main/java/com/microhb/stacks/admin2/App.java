package com.microce.stacks.admin2;

import de.codecentric.boot.admin.server.config.EnableAdminServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;


@EnableDiscoveryClient
@SpringBootApplication
@EnableAdminServer
public class App {

    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }
}
