package com.rajpatel.dynastytracker;

import com.rajpatel.dynastytracker.config.ValuationProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/** Spring Boot entry point for the dynasty roster value tracker API. */
@SpringBootApplication
@EnableConfigurationProperties(ValuationProperties.class)
public class DynastyTrackerApplication {

    /**
     * Starts the embedded server and Spring application context.
     * @param args standard JVM command-line arguments, passed through to Spring
     */
    public static void main(String[] args) {
        SpringApplication.run(DynastyTrackerApplication.class, args);
    }
}
