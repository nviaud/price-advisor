package com.nviaud.pricing.repositories

import com.nviaud.pricing.entities.ProductCategory
import org.springframework.data.jpa.repository.JpaRepository

interface ProductCategoryRepository : JpaRepository<ProductCategory, Long> {
    fun findByName(name: String): ProductCategory?
}