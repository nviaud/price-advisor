package com.nviaud.pricing.repositories

import com.nviaud.pricing.entities.ProductCategory
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.rest.core.annotation.RepositoryRestResource

@RepositoryRestResource(path = "categories")
interface ProductCategoryRepository : JpaRepository<ProductCategory, Long> {
    fun findByName(name: String): ProductCategory?
}