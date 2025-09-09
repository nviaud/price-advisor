package com.nviaud.pricing.api.models

import java.math.BigDecimal

class ProductResponse(
    val id: Long? = null,
    val name: String? = null,
    val averagePrice: BigDecimal? = null,
    val minPrice: BigDecimal? = null,
    val maxPrice: BigDecimal? = null,
    val variance: BigDecimal? = null
)

