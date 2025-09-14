package com.nviaud.pricing.repositories

import com.nviaud.pricing.entities.Product
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.rest.core.annotation.RepositoryRestResource

@RepositoryRestResource
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