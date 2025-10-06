package com.nviaud.pricing.controllers

import com.nviaud.pricing.entities.ProductAnalytics
import com.nviaud.pricing.services.ProductAnalyticsService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/analytics/products")
class ProductAnalyticsController(
    private val productAnalyticsService: ProductAnalyticsService
) {

    /**
     * Get analytics for all products
     */
    @GetMapping
    fun getAllAnalytics(): ResponseEntity<List<ProductAnalytics>> {
        val analytics = productAnalyticsService.getAllAnalytics()
        return ResponseEntity.ok(analytics)
    }

    /**
     * Get analytics for a specific product by brand, name, and category
     */
    @GetMapping("/product")
    fun getProductAnalytics(
        @RequestParam brand: String,
        @RequestParam name: String,
        @RequestParam category: String
    ): ResponseEntity<ProductAnalytics> {
        val analytics = productAnalyticsService.getAnalytics(brand, name, category)
        return if (analytics != null) {
            ResponseEntity.ok(analytics)
        } else {
            ResponseEntity.notFound().build()
        }
    }

    /**
     * Get analytics for all products by brand
     */
    @GetMapping("/by-brand/{brand}")
    fun getAnalyticsByBrand(@PathVariable brand: String): ResponseEntity<List<ProductAnalytics>> {
        val analytics = productAnalyticsService.getAnalyticsByBrand(brand)
        return ResponseEntity.ok(analytics)
    }

    /**
     * Get analytics for all products by category
     */
    @GetMapping("/by-category/{category}")
    fun getAnalyticsByCategory(@PathVariable category: String): ResponseEntity<List<ProductAnalytics>> {
        val analytics = productAnalyticsService.getAnalyticsByCategory(category)
        return ResponseEntity.ok(analytics)
    }
}
