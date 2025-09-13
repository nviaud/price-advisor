package com.nviaud.pricing.events.v1

import java.math.BigDecimal

data class QuotationSubmitted(
    val quotationId: String,
    val products: List<QuotationProduct>,
)


data class QuotationProduct(
    val productId: String,
    val price: BigDecimal
)