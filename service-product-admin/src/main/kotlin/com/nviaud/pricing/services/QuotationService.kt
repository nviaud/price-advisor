package com.nviaud.pricing.services

import com.nviaud.pricing.events.v1.QuotationSubmitted
import org.springframework.context.annotation.Bean
import org.springframework.messaging.support.ErrorMessage
import org.springframework.stereotype.Service
import java.util.function.Consumer

@Service
@Suppress("unused")
class QuotationService {

    @Bean
    fun onQuotationSucceeded(): Consumer<QuotationSubmitted> {
        return Consumer { message ->
            println("Received message onQuotationSucceeded : $message")
            throw IllegalArgumentException("Simulated exception for testing purposes")
        }
    }

    @Bean
    fun onQuotationSucceededError(): Consumer<ErrorMessage> {
        return Consumer { error ->
            println("Error occurred: " + error.payload.message)
        }
    }

    @Bean
    fun onProductAggregated(): Consumer<String> {
        return Consumer { message ->
            println("Received message onProductAggregated : $message")
        }
    }

    @Bean
    fun onProductAggregatedError():  Consumer<ErrorMessage> {
        return Consumer { error ->
            println("Error occurred: " + error.payload.message)
        }
    }


}
