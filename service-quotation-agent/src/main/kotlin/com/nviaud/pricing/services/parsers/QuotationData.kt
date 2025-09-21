package com.nviaud.pricing.services.parsers

import java.math.BigDecimal

data class QuotationData(
    val products: List<QuotationDataProduct>,
    val quotationDate: String,
)

data class QuotationDataProduct (
    val productBrand: String,
    val productName: String,
    val productCategory: String,
    var productHeight: Int? = null,
    var productWidth: Int? = null,
    var productDepth: Int? = null,
    var productWeight: Int? = null,
    val quantity: Int,
    val unitPrice: BigDecimal,
    val totalPrice: BigDecimal,
    val vat: BigDecimal? = null,
)
