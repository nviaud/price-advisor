package com.nviaud.pricing.api.models

class ProductResponse(
    val id: Long? = null,
    val name: String? = null,
    val brand: String? = null,
    val height: Int? = null,
    val width: Int? = null,
    val depth: Int? = null,
    val weight: Int? = null,
    val category: ProductCategoryResponse? = null,
)

