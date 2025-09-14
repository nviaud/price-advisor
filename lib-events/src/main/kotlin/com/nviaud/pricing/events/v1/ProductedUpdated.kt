package com.nviaud.pricing.events.v1

data class ProductUpdated(
    val productId: String,

    val name: String,
    val category: String,
    val brand: String,
)
