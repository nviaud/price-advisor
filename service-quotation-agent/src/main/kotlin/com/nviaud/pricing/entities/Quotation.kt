package com.nviaud.pricing.entities

import com.nviaud.pricing.services.parsers.QuotationData
import jakarta.persistence.Basic
import jakarta.persistence.Column
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Entity
import jakarta.persistence.Enumerated
import jakarta.persistence.EnumType
import jakarta.persistence.FetchType
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.time.ZonedDateTime
import jakarta.persistence.Lob

/**
 * Quotation entity representing a price quotation for a product.
 * A quotation is linked to a single product.
 *
 */
@Entity
class Quotation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    var userId: String? = null

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "json")
    var data: QuotationData? = null

    @Enumerated(EnumType.STRING)
    var status: QuotationStatus? = null

    var submissionDate: ZonedDateTime? = null
    var validationDate: ZonedDateTime? = null
    var rejectionDate: ZonedDateTime? = null

    @Lob
    @Basic(fetch= FetchType.LAZY)
    var resourceData: ByteArray? = null

    var resourceFilename: String? = null

    var resourceMimeType: String? = null

}

enum class QuotationStatus {
    SUBMITTED,  // When a user submit the quotation
    ERROR,     // When an error occurs during the processing of the quotation
    WAITING_VALIDATION, // When the system has extracted data and wait for user validation
    VALIDATED, // When the user has validated the extracted data
    REJECTED // When the user has rejected the extracted data
}