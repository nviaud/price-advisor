package com.nviaud.pricing.events.v1

/**
 * Published when a quotation is rejected by an admin.
 */
data class QuotationRejected(
    val quotationId: String,
    val userId: String,
    val rejectionDate: String,
    val reason: String? = null
)