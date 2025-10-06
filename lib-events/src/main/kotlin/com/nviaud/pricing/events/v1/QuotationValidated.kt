package com.nviaud.pricing.events.v1

import java.math.BigDecimal

/**
 * Published when a quotation is validated by an admin.
 */
data class QuotationValidated(
    val quotationId: String,
    val userId: String,
    val validationDate: String,
    val products: List<QuotationProduct> = emptyList(),
    val quotationDate: String? = null
)

data class QuotationProduct(
    val productBrand: String,
    val productName: String,
    val productCategory: String,
    val productHeight: Int? = null,
    val productWidth: Int? = null,
    val productDepth: Int? = null,
    val productWeight: Int? = null,
    val quantity: Int,
    val unitPrice: BigDecimal,
    val totalPrice: BigDecimal,
    val vat: BigDecimal? = null
)