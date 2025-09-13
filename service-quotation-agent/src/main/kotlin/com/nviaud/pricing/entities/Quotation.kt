package com.nviaud.pricing.entities

import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.ManyToOne
import jakarta.persistence.Entity
import java.math.BigDecimal
import java.time.OffsetDateTime
import java.time.ZonedDateTime

/**
 * Quotation entity representing a price quotation for a product.
 * A quotation is linked to a single product.
 */
@Entity
class Quotation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

}
