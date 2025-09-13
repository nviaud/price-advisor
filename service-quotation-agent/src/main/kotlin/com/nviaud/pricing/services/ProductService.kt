package com.nviaud.pricing.services

import com.nviaud.pricing.entities.Product
import com.nviaud.pricing.events.v1.ProductCreated
import com.nviaud.pricing.events.v1.ProductUpdated
import com.nviaud.pricing.repositories.ProductRepository
import org.slf4j.LoggerFactory
import org.springframework.cache.annotation.Cacheable
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Service
import java.util.function.Consumer

@Service
@Suppress("unused")
class ProductService(private val productRepository: ProductRepository) {

    private val logger = LoggerFactory.getLogger(QuotationService::class.java)

    @Bean
    fun onProductCreated(): Consumer<ProductCreated> {
        return Consumer { message ->
            println("Received message onProductCreated : $message")
            // Save the product to the database
            val product = Product().apply {
                name = message.name
                brand = message.brand
                category = message.category
            }
            productRepository.save(product)
        }
    }

    @Bean
    fun onProductUpdated(): Consumer<ProductUpdated> {
        return Consumer { message ->
            println("Received message onProductAggregated : $message")
            val productId = message.productId.toLong()
            productRepository.findById(productId)
                .ifPresentOrElse(
                    { product ->
                        message.name?.let { product.name = it }
                        message.brand?.let { product.brand = it }
                        message.category?.let { product.category = it }
                        productRepository.save(product)
                    },
                    {
                        throw NoSuchElementException("Product with id $productId not found")
                    }
                )
        }
    }

    fun findProductByName(name: String): Product? {
        // TODO implement not an exact match but a vectorial search inside an other service call product-requester. Scale this service highly because of synchronous calls
        return this.productRepository.findByName(name)
    }

    @Cacheable
    fun getAllProductNames(): List<String> {
        logger.info("Fetching all product names from the database")
        return this.productRepository.findAll()
            .mapNotNull { it.name }
            .distinct()
            .sorted()
    }

    @Cacheable
    fun getAllProductBrands(): List<String> {
        logger.info("Fetching all product brands from the database")
        return this.productRepository.findAll()
            .mapNotNull { it.brand }
            .distinct()
            .sorted()
    }

    @Cacheable
    fun getAllProductCategories(): List<String> {
        logger.info("Fetching all product categories from the database")
        return this.productRepository.findAll()
            .mapNotNull { it.category }
            .distinct()
            .sorted()
    }

}