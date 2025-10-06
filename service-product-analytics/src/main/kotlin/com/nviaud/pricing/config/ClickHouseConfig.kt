package com.nviaud.pricing.config

import com.clickhouse.jdbc.ClickHouseDataSource
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.jdbc.core.JdbcTemplate
import java.util.*
import javax.sql.DataSource

@Configuration
class ClickHouseConfig {

    @Value("\${clickhouse.url}")
    private lateinit var url: String

    @Value("\${clickhouse.username}")
    private lateinit var username: String

    @Value("\${clickhouse.password}")
    private lateinit var password: String

    @Bean(name = ["clickHouseDataSource"])
    fun clickHouseDataSource(): DataSource {
        val properties = Properties().apply {
            setProperty("user", username)
            setProperty("password", password)
        }
        return ClickHouseDataSource(url, properties)
    }

    @Bean(name = ["clickHouseJdbcTemplate"])
    fun clickHouseJdbcTemplate(): JdbcTemplate {
        return JdbcTemplate(clickHouseDataSource())
    }
}
