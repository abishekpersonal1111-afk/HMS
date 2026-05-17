package com.hms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.CommandLineRunner;
import com.hms.service.UserService;

@SpringBootApplication
public class HmsApplication extends SpringBootServletInitializer {

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(HmsApplication.class);
    }

    @Bean
    public CommandLineRunner initAdmin(UserService userService) {
        return args -> {
            userService.ensureAdminApproved();
        };
    }

    public static void main(String[] args) {
        SpringApplication.run(HmsApplication.class, args);
    }
}

