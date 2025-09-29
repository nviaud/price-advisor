package com.nviaud.pricing.services.dto

import com.nviaud.pricing.entities.Specifications

data class PartialUpdateProduct (
    val id: Long,
    val name: PartialField<String>,
    val brand: PartialField<String>,
    val category: PartialField<Long>,
    val specifications: PartialField<Specifications>,
)
