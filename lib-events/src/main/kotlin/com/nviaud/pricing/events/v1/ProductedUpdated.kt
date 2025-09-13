package com.nviaud.pricing.events.v1

data class ProductUpdated(
    val productId: String,

    val name: String? = null,
    val category: String? = null,
    val brand: String? = null,
)
