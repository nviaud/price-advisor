package com.nviaud.pricing.entities

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.math.BigDecimal

/**
 * Product entity representing a product in the system.
 * A product is an aggregation of quotations.
 */
@Entity
@Table(
    uniqueConstraints = [
        jakarta.persistence.UniqueConstraint(
            columnNames = ["name", "brand", "height", "width", "depth", "weight"]
        )
    ]
)
class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null
    var name: String? = null
    var brand: String? = null

    var height: Int? = null
    var width: Int? = null
    var depth: Int? = null
    var weight: Int? = null

    var averagePrice: BigDecimal? = null
    var minPrice: BigDecimal? = null
    var maxPrice: BigDecimal? = null
    var variance: BigDecimal? = null
    var numberOfQuotations: Int? = null

    @ManyToOne
    var category: ProductCategory? = null

}