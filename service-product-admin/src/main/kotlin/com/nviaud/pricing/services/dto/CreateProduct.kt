package com.nviaud.pricing.services.dto

data class CreateProduct (
    val name: String,
    val brand: String,
    val category: Long,
    val height: Int?,
    val width: Int?,
    val depth: Int?,
    val weight: Int?,
)