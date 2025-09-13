package com.nviaud.pricing.api.models

import jakarta.validation.constraints.NotBlank
import java.util.Optional

class ProductRequest(

    @field:NotBlank
    val name: String,

    @field:NotBlank
    val brand: String,

    val height: Int? = null,
    val width: Int? = null,
    val depth: Int? = null,
    val weight: Int? = null,
    val categoryName: String? = null,
)
