package com.rajpatel.dynastytracker;

import com.rajpatel.dynastytracker.config.ValuationProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(ValuationProperties.class)
public class DynastyTrackerApplication {

    public static void main(String[] args) {
        SpringApplication.run(DynastyTrackerApplication.class, args);
    }
}
