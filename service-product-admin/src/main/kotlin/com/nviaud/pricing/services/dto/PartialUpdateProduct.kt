package com.nviaud.pricing.services.dto

data class PartialUpdateProduct (
    val id: Long,
    val name: PartialField<String>,
    val brand: PartialField<String>,
    val category: PartialField<Long>,
    val height: PartialField<Int?>,
    val width: PartialField<Int?>,
    val depth: PartialField<Int?>,
    val weight: PartialField<Int?>,
)
