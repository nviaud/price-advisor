package com.nviaud.pricing.repositories

import com.nviaud.pricing.entities.Product
import org.springframework.data.jpa.repository.JpaRepository

interface ProductRepository: JpaRepository<Product, Long> {

    fun findByName(name: String): Product?

}