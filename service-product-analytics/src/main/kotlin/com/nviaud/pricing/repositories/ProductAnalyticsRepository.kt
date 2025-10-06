package com.nviaud.pricing.repositories

import com.nviaud.pricing.entities.ProductAnalytics
import com.nviaud.pricing.entities.ProductPriceEvent
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository
import java.math.BigDecimal

@Repository
class ProductAnalyticsRepository(
    @Qualifier("clickHouseJdbcTemplate")
    private val clickHouseJdbcTemplate: JdbcTemplate
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * Initialize ClickHouse table schema
     */
    fun initializeSchema() {
        val createTableSQL = """
            CREATE TABLE IF NOT EXISTS product_price_events (
                quotation_id String,
                product_brand String,
                product_name String,
                product_category String,
                unit_price Decimal(19, 2),
                event_timestamp DateTime
            ) ENGINE = MergeTree()
            ORDER BY (product_brand, product_name, product_category, event_timestamp)
        """.trimIndent()

        try {
            clickHouseJdbcTemplate.execute(createTableSQL)
            logger.info("ClickHouse table 'product_price_events' initialized")
        } catch (e: Exception) {
            logger.error("Failed to initialize ClickHouse schema", e)
            throw e
        }
    }

    /**
     * Insert a price event into ClickHouse
     */
    fun insertPriceEvent(event: ProductPriceEvent) {
        val sql = """
            INSERT INTO product_price_events (
                quotation_id, product_brand, product_name, product_category,
                unit_price, event_timestamp
            ) VALUES (?, ?, ?, ?, ?, ?)
        """.trimIndent()

        clickHouseJdbcTemplate.update(
            sql,
            event.quotationId,
            event.productBrand,
            event.productName,
            event.productCategory,
            event.unitPrice,
            event.eventTimestamp
        )

        logger.debug(
            "Inserted price event: quotation={}, product={}/{}/{}, price={}",
            event.quotationId, event.productBrand, event.productName,
            event.productCategory, event.unitPrice
        )
    }

    /**
     * Get aggregated analytics for a specific product
     */
    fun getAnalytics(brand: String, name: String, category: String): ProductAnalytics? {
        val sql = """
            SELECT
                product_brand,
                product_name,
                product_category,
                count() as count,
                avg(unit_price) as average_price,
                varPop(unit_price) as price_variance,
                min(unit_price) as min_price,
                max(unit_price) as max_price
            FROM product_price_events
            WHERE product_brand = ?
              AND product_name = ?
              AND product_category = ?
            GROUP BY product_brand, product_name, product_category
        """.trimIndent()

        return clickHouseJdbcTemplate.query(sql, { rs, _ ->
            ProductAnalytics(
                productBrand = rs.getString("product_brand"),
                productName = rs.getString("product_name"),
                productCategory = rs.getString("product_category"),
                count = rs.getLong("count"),
                averagePrice = rs.getBigDecimal("average_price"),
                priceVariance = rs.getBigDecimal("price_variance"),
                minPrice = rs.getBigDecimal("min_price"),
                maxPrice = rs.getBigDecimal("max_price")
            )
        }, brand, name, category).firstOrNull()
    }

    /**
     * Get analytics for all products by brand
     */
    fun getAnalyticsByBrand(brand: String): List<ProductAnalytics> {
        val sql = """
            SELECT
                product_brand,
                product_name,
                product_category,
                count() as count,
                avg(unit_price) as average_price,
                varPop(unit_price) as price_variance,
                min(unit_price) as min_price,
                max(unit_price) as max_price
            FROM product_price_events
            WHERE product_brand = ?
            GROUP BY product_brand, product_name, product_category
        """.trimIndent()

        return clickHouseJdbcTemplate.query(sql, { rs, _ ->
            ProductAnalytics(
                productBrand = rs.getString("product_brand"),
                productName = rs.getString("product_name"),
                productCategory = rs.getString("product_category"),
                count = rs.getLong("count"),
                averagePrice = rs.getBigDecimal("average_price"),
                priceVariance = rs.getBigDecimal("price_variance"),
                minPrice = rs.getBigDecimal("min_price"),
                maxPrice = rs.getBigDecimal("max_price")
            )
        }, brand)
    }

    /**
     * Get analytics for all products by category
     */
    fun getAnalyticsByCategory(category: String): List<ProductAnalytics> {
        val sql = """
            SELECT
                product_brand,
                product_name,
                product_category,
                count() as count,
                avg(unit_price) as average_price,
                varPop(unit_price) as price_variance,
                min(unit_price) as min_price,
                max(unit_price) as max_price
            FROM product_price_events
            WHERE product_category = ?
            GROUP BY product_brand, product_name, product_category
        """.trimIndent()

        return clickHouseJdbcTemplate.query(sql, { rs, _ ->
            ProductAnalytics(
                productBrand = rs.getString("product_brand"),
                productName = rs.getString("product_name"),
                productCategory = rs.getString("product_category"),
                count = rs.getLong("count"),
                averagePrice = rs.getBigDecimal("average_price"),
                priceVariance = rs.getBigDecimal("price_variance"),
                minPrice = rs.getBigDecimal("min_price"),
                maxPrice = rs.getBigDecimal("max_price")
            )
        }, category)
    }

    /**
     * Get analytics for all products
     */
    fun getAllAnalytics(): List<ProductAnalytics> {
        val sql = """
            SELECT
                product_brand,
                product_name,
                product_category,
                count() as count,
                avg(unit_price) as average_price,
                varPop(unit_price) as price_variance,
                min(unit_price) as min_price,
                max(unit_price) as max_price
            FROM product_price_events
            GROUP BY product_brand, product_name, product_category
        """.trimIndent()

        return clickHouseJdbcTemplate.query(sql) { rs, _ ->
            ProductAnalytics(
                productBrand = rs.getString("product_brand"),
                productName = rs.getString("product_name"),
                productCategory = rs.getString("product_category"),
                count = rs.getLong("count"),
                averagePrice = rs.getBigDecimal("average_price"),
                priceVariance = rs.getBigDecimal("price_variance"),
                minPrice = rs.getBigDecimal("min_price"),
                maxPrice = rs.getBigDecimal("max_price")
            )
        }
    }
}
