package com.nviaud.pricing.services.dto

data class UpdateProduct (
    val id: Long,
    val name: String,
    val category: Long,
    val brand: String,
    val height: Int?,
    val width: Int?,
    val depth: Int?,
    val weight: Int?,
)