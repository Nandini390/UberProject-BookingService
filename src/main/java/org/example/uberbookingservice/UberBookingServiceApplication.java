package org.example.uberbookingservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;


@SpringBootApplication
@EnableJpaAuditing
@EntityScan("org.example.uberprojectentityservice")
public class UberBookingServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(UberBookingServiceApplication.class, args);
    }

}