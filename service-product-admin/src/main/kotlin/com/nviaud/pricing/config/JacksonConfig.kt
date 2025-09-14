package com.nviaud.pricing.config

import com.fasterxml.jackson.databind.Module
import com.nviaud.pricing.api.serializers.PartialFieldJacksonModule
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@Suppress("unused")
class JacksonConfig {
    @Bean
    fun partialFieldJacksonModule(): Module = PartialFieldJacksonModule()
}

