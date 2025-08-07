package com.adeprogramming.javagis;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main application class for the JavaGIS Enterprise Application.
 * This application provides geospatial capabilities, STAC catalog, and AI workflow automation.
 */
@SpringBootApplication
@EnableScheduling
public class JavaGisApplication {

    public static void main(String[] args) {
        SpringApplication.run(JavaGisApplication.class, args);
    }
}