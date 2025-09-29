package com.nviaud.pricing.services

import com.nviaud.pricing.events.v1.ProductCreated
import com.nviaud.pricing.events.v1.ProductUpdated
import org.slf4j.LoggerFactory
import org.springframework.ai.document.Document
import org.springframework.ai.vectorstore.VectorStore
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Service
import java.util.function.Consumer

@Service
@Suppress("unused")
class ProductService(
    private val vectorStore: VectorStore
) {

    private val logger = LoggerFactory.getLogger(QuotationService::class.java)

    @Bean
    fun onProductCreated(): Consumer<ProductCreated> {
        return Consumer { message ->
            println("Received message onProductCreated : $message")
            vectorStore.add(
                listOf(
                    Document(
                        "Product $message.name of brand ${message.brand} in category ${message.category}",
                        mapOf("productId" to message.productId)
                    )
                )
            )
        }
    }

    @Bean
    fun onProductUpdated(): Consumer<ProductUpdated> {
        return Consumer { message ->
            println("Received message onProductAggregated : $message")
            //            vectorStore.delete(
//
//            )
            vectorStore.add(
                listOf(
                    Document(
                        "Product $message.name of brand ${message.brand} in category ${message.category}",
                        mapOf("productId" to message.productId)
                    )
                )
            )
        }
    }

}