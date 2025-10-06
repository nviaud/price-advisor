package com.nviaud.pricing.entities

import java.math.BigDecimal
import java.time.LocalDateTime

/**
 * ClickHouse table: product_price_events
 * Stores each individual price observation from validated quotations.
 * ClickHouse will handle aggregations (avg, variance, min, max) via queries.
 */
data class ProductPriceEvent(
    val quotationId: String,
    val productBrand: String,
    val productName: String,
    val productCategory: String,
    val unitPrice: BigDecimal,
    val eventTimestamp: LocalDateTime
)

/**
 * DTO for aggregated product analytics results from ClickHouse queries
 */
data class ProductAnalytics(
    val productBrand: String,
    val productName: String,
    val productCategory: String,
    val count: Long,
    val averagePrice: BigDecimal,
    val priceVariance: BigDecimal,
    val minPrice: BigDecimal,
    val maxPrice: BigDecimal
)
