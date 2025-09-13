package com.nviaud.pricing.entities

import jakarta.persistence.Column
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id

/**
 * Product entity representing a light version of a product in the system.
 * This entity is simplified for the quotation agent service and is not the source of truth.
 */
class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @Column(unique = true)
    var name: String? = null

    var category: String? = null
    var brand: String? = null

}
