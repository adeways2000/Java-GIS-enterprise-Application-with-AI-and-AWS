package com.adeprogramming.javagis.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Database configuration for the JavaGIS application.
 * Configures PostgreSQL with PostGIS extension for geospatial capabilities.
 */
@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = "com.adeprogramming.javagis.repository")

@EnableJpaAuditing
public class DatabaseConfig {

    /**
     * Configure the data source properties.
     */
    @Bean
    @ConfigurationProperties("spring.datasource")
    public DataSourceProperties dataSourceProperties() {
        return new DataSourceProperties();
    }

    /**
     * Configure the data source.
     */
    @Bean
    @ConfigurationProperties("spring.datasource.hikari")
    public DataSource dataSource(DataSourceProperties properties) {
        return properties.initializeDataSourceBuilder().build();
    }
}
