package com.nviaud.pricing.services.dto

import com.nviaud.pricing.entities.Specifications

data class UpdateProduct (
    val id: Long,
    val name: String,
    val category: Long,
    val brand: String,
    val specifications: Specifications
)