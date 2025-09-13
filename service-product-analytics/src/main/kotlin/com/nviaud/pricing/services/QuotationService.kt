package com.nviaud.pricing.services

import com.nviaud.pricing.events.v1.QuotationSubmitted
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Service
import java.util.function.Consumer

@Service
@Suppress("unused")
class QuotationService {

    @Bean
    fun onQuotationSubmitted(): Consumer<QuotationSubmitted> {
        return Consumer { message ->
            println("Received message: $message")
        }
    }
}

