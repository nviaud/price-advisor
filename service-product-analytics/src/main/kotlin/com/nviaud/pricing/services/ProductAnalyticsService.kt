package com.nviaud.pricing.services

import com.nviaud.pricing.entities.ProductAnalytics
import com.nviaud.pricing.entities.ProductPriceEvent
import com.nviaud.pricing.repositories.ProductAnalyticsRepository
import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.LocalDateTime

@Service
class ProductAnalyticsService(
    private val productAnalyticsRepository: ProductAnalyticsRepository
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * Initialize ClickHouse schema on startup
     */
    @PostConstruct
    fun init() {
        logger.info("Initializing ClickHouse schema for product analytics")
        productAnalyticsRepository.initializeSchema()
    }

    /**
     * Record a price event for a product from a validated quotation.
     * ClickHouse will compute analytics on-the-fly via aggregation queries.
     */
    fun recordPriceEvent(
        quotationId: String,
        productBrand: String,
        productName: String,
        productCategory: String,
        unitPrice: BigDecimal
    ) {
        val event = ProductPriceEvent(
            quotationId = quotationId,
            productBrand = productBrand,
            productName = productName,
            productCategory = productCategory,
            unitPrice = unitPrice,
            eventTimestamp = LocalDateTime.now()
        )

        productAnalyticsRepository.insertPriceEvent(event)

        logger.info(
            "Recorded price event: quotation={}, product={}/{}/{}, price={}",
            quotationId, productBrand, productName, productCategory, unitPrice
        )
    }

    /**
     * Get analytics for a specific product
     */
    fun getAnalytics(productBrand: String, productName: String, productCategory: String): ProductAnalytics? {
        return productAnalyticsRepository.getAnalytics(productBrand, productName, productCategory)
    }

    /**
     * Get all analytics for products by brand
     */
    fun getAnalyticsByBrand(brand: String): List<ProductAnalytics> {
        return productAnalyticsRepository.getAnalyticsByBrand(brand)
    }

    /**
     * Get all analytics for products by category
     */
    fun getAnalyticsByCategory(category: String): List<ProductAnalytics> {
        return productAnalyticsRepository.getAnalyticsByCategory(category)
    }

    /**
     * Get all analytics
     */
    fun getAllAnalytics(): List<ProductAnalytics> {
        return productAnalyticsRepository.getAllAnalytics()
    }
}
