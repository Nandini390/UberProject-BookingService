package org.example.uberbookingservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;


@SpringBootApplication
@EnableDiscoveryClient
@EnableJpaAuditing
@EnableAsync
@EnableScheduling
@EntityScan(basePackages = {"org.example.uberprojectentityservice", "org.example.uberbookingservice"})
public class UberBookingServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(UberBookingServiceApplication.class, args);
    }

}
