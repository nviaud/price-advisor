package com.nviaud.pricing.services.dto

data class PartialUpdateCategory (
    val id: Long,
    val name: PartialField<String>,
)
