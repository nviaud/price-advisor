package com.nviaud.pricing.events.v1

import java.math.BigDecimal

/**
 * Published when quotation parsing (AI extraction) is completed successfully.
 * The quotation is now in WAITING_VALIDATION status.
 */
data class QuotationParsingCompleted(
    val quotationId: String,
    val userId: String,
    val products: List<QuotationProduct> = emptyList(),
    val quotationDate: String? = null
)