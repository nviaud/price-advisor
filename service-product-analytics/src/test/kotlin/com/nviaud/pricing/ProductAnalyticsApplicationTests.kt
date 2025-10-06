package com.nviaud.pricing

import com.nviaud.pricing.config.ClickHouseConfig
import com.nviaud.pricing.services.ProductAnalyticsService
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.jdbc.core.JdbcTemplate
import org.mockito.Mockito

@SpringBootTest
class ProductAnalyticsApplicationTests {

	@TestConfiguration
	class TestConfig {
		@Bean
		@Primary
		fun clickHouseJdbcTemplate(): JdbcTemplate {
			return Mockito.mock(JdbcTemplate::class.java)
		}
	}

	@Test
	fun contextLoads() {
	}

}
