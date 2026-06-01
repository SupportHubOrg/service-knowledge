package com.supporthub.knowledge.messaging.outbox.core.config;

import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.provider.jdbctemplate.JdbcTemplateLockProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

/**
 * Outbox infrastructure beans. Provides the ShedLock {@link LockProvider} used by the scheduler;
 * {@code usingDbTime()} makes lock expiry rely on the database clock, which keeps the cluster lock
 * correct even when node clocks drift.
 */
@Configuration
public class OutboxConfiguration {

    @Bean
    LockProvider lockProvider(DataSource dataSource) {
        return new JdbcTemplateLockProvider(
                JdbcTemplateLockProvider.Configuration.builder()
                        .withJdbcTemplate(new JdbcTemplate(dataSource))
                        .usingDbTime()
                        .build()
        );
    }
}
