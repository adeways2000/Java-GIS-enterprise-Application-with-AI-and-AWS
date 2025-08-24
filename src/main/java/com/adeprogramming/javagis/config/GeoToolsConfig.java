package com.adeprogramming.javagis.config;

import lombok.extern.slf4j.Slf4j;
import org.geotools.referencing.CRS;
import org.geotools.util.factory.Hints;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

/**
 * Configuration for GeoTools library integration.
 * Sets up the necessary components for geospatial operations.
 */
@Configuration
@Slf4j
public class GeoToolsConfig {

    /**
     * Initialize GeoTools configuration.
     * This ensures proper setup of coordinate reference systems and other GeoTools components.
     */
    @PostConstruct
    public void init() {
        try {
            log.info("Initializing GeoTools configuration...");

            // Set system-wide hints for GeoTools
            System.setProperty("org.geotools.referencing.forceXY", "true");

            // Configure axis order handling
            Hints.putSystemDefault(Hints.FORCE_LONGITUDE_FIRST_AXIS_ORDER, Boolean.TRUE);
            Hints.putSystemDefault(Hints.FORCE_AXIS_ORDER_HONORING, "http");

            // Set additional system properties for better GeoTools performance
            System.setProperty("org.geotools.shapefile.datetime", "true");
            System.setProperty("org.geotools.shapefile.ng", "true");

            log.info("GeoTools configuration initialized successfully");
        } catch (Exception e) {
            log.error("Error initializing GeoTools configuration: {}", e.getMessage(), e);
        }
    }

    /**
     * Configure the default coordinate reference system (CRS) for the application.
     * Using EPSG:4326 (WGS84) as the default CRS.
     */
    @Bean
    public CoordinateReferenceSystem defaultCRS() {
        try {
            log.info("Creating default CRS (EPSG:4326)...");

            // Return WGS84 as default CRS
            CoordinateReferenceSystem crs = CRS.decode("EPSG:4326", true);

            log.info("Default CRS created successfully: {}", crs.getName());
            return crs;
        } catch (FactoryException e) {
            log.error("Error creating default CRS: {}", e.getMessage(), e);

            // Fallback: try to create a basic geographic CRS
            try {
                log.warn("Attempting to create fallback CRS...");
                return CRS.decode("EPSG:4326");
            } catch (FactoryException fallbackException) {
                log.error("Failed to create fallback CRS: {}", fallbackException.getMessage(), fallbackException);
                throw new RuntimeException("Unable to create default coordinate reference system", fallbackException);
            }
        }
    }

    /**
     * Configure a UTM CRS bean for projected operations.
     * This provides a projected coordinate system for area and distance calculations.
     */
    @Bean
    public CoordinateReferenceSystem utmCRS() {
        try {
            // Use UTM Zone 33N (EPSG:32633) as a default projected CRS
            // This covers central Europe and is suitable for many applications
            CoordinateReferenceSystem utmCrs = CRS.decode("EPSG:32633", true);
            log.info("UTM CRS created successfully: {}", utmCrs.getName());
            return utmCrs;
        } catch (FactoryException e) {
            log.warn("Could not create UTM CRS, using Web Mercator as fallback: {}", e.getMessage());
            try {
                // Fallback to Web Mercator (EPSG:3857)
                return CRS.decode("EPSG:3857", true);
            } catch (FactoryException fallbackException) {
                log.error("Failed to create any projected CRS: {}", fallbackException.getMessage(), fallbackException);
                return null;
            }
        }
    }

    /**
     * Check if GeoTools is properly configured.
     */
    @Bean
    public Boolean geoToolsHealthCheck() {
        try {
            // Test basic GeoTools functionality
            CoordinateReferenceSystem testCrs = CRS.decode("EPSG:4326");
            boolean isValid = testCrs != null && testCrs.getName() != null;

            log.info("GeoTools health check: {}", isValid ? "PASSED" : "FAILED");
            return isValid;
        } catch (Exception e) {
            log.error("GeoTools health check failed: {}", e.getMessage(), e);
            return false;
        }
    }
}
