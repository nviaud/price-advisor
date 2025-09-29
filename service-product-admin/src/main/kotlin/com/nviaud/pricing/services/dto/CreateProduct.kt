package com.nviaud.pricing.services.dto

import com.nviaud.pricing.entities.Specifications

data class CreateProduct (
    val name: String,
    val brand: String,
    val category: Long,
    val specifications: Specifications
)