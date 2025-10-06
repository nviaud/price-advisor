package com.nviaud.pricing.config

import net.javacrumbs.shedlock.core.LockProvider
import net.javacrumbs.shedlock.provider.jdbctemplate.JdbcTemplateLockProvider
import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.scheduling.annotation.EnableScheduling
import javax.sql.DataSource

/**
 * ShedLock configuration for distributed scheduled task locking.
 *
 * Ensures that scheduled tasks (like outbox cleanup) run only once across
 * multiple service instances. Uses PostgreSQL to coordinate locks.
 *
 * Configuration properties (application.properties):
 * - shedlock.defaults.lock-at-most-for: Maximum lock duration (default: 10m)
 * - shedlock.defaults.lock-at-least-for: Minimum lock duration (default: 5s)
 * - shedlock.table-name: Database table name (default: shedlock)
 */
@Configuration
@EnableScheduling
@EnableSchedulerLock(defaultLockAtMostFor = "\${shedlock.defaults.lock-at-most-for:10m}")
class ShedLockConfig {

    @Value("\${shedlock.table-name:shedlock}")
    private lateinit var tableName: String

    /**
     * Creates a JDBC-based lock provider using PostgreSQL.
     * ShedLock will create the lock table automatically if it doesn't exist.
     */
    @Bean
    fun lockProvider(dataSource: DataSource): LockProvider {
        return JdbcTemplateLockProvider(
            JdbcTemplateLockProvider.Configuration.builder()
                .withTableName(tableName)
                .withJdbcTemplate(JdbcTemplate(dataSource))
                .usingDbTime() // Use database time for lock expiration consistency
                .build()
        )
    }
}
