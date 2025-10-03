package com.nviaud.pricing.events.v1

data class QuotationUpdated(
    val quotationId: String,
    val status: String,
    val userId: String
)
