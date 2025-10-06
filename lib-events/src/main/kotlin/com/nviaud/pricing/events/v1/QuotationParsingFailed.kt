package com.nviaud.pricing.events.v1

/**
 * Published when quotation parsing (AI extraction) fails.
 * The quotation is marked as ERROR status.
 */
data class QuotationParsingFailed(
    val quotationId: String,
    val userId: String,
    val errorMessage: String? = null
)