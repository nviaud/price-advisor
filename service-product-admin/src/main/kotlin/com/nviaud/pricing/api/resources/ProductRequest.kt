package com.nviaud.pricing.api.resources

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

class ProductRequest(

    @field:NotBlank
    @field:Size(max = 255)
    val name: String,

    @field:NotBlank
    @field:Size(max = 255)
    val brand: String,

    @field:NotBlank
    @field:Size(max = 255)
    val category: Long,

    val height: Int? = null,
    val width: Int? = null,
    val depth: Int? = null,
    val weight: Int? = null,
)
