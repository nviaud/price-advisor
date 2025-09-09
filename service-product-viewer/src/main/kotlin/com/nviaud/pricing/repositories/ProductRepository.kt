package com.nviaud.pricing.repositories

import com.nviaud.pricing.entities.Product
import org.springframework.data.jpa.repository.JpaRepository

interface ProductRepository : JpaRepository<Product, Long> {
    fun findByNameAndBrandAndHeightAndWidthAndDepthAndWeight(
        name: String,
        brand: String,
        height: Int?,
        width: Int?,
        depth: Int?,
        weight: Int?
    ): Product?
}