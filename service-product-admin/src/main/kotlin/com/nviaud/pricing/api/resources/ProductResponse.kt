package com.nviaud.pricing.api.resources

class ProductResponse(
    override val id: Long,
    val name: String? = null,
    val brand: String? = null,
    val height: Int? = null,
    val width: Int? = null,
    val depth: Int? = null,
    val weight: Int? = null,
    val category: CategoryResponse? = null,
) : ResponseElement

