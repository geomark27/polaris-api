package com.azenticsys.polaris.config;

import com.azenticsys.polaris.config.multitenancy.TenantAwareDataSource;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

/**
 * Define el DataSource principal como TenantAwareDataSource, que envuelve HikariCP
 * y aplica SET search_path en cada getConnection() según el TenantContext activo.
 *
 * Al definir un @Primary DataSource bean, Spring Boot no auto-configura otro
 * (gracias a @ConditionalOnMissingBean en DataSourceAutoConfiguration).
 */
@Configuration
public class DataSourceConfig {

    @Value("${spring.datasource.url}")
    private String jdbcUrl;

    @Value("${spring.datasource.username}")
    private String username;

    @Value("${spring.datasource.password}")
    private String password;

    @Value("${spring.datasource.driver-class-name:org.postgresql.Driver}")
    private String driverClassName;

    @Bean
    @Primary
    public DataSource dataSource() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(jdbcUrl);
        config.setUsername(username);
        config.setPassword(password);
        config.setDriverClassName(driverClassName);
        return new TenantAwareDataSource(new HikariDataSource(config));
    }
}
