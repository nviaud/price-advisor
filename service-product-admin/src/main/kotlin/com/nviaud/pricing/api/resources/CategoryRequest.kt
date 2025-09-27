package com.nviaud.pricing.api.resources

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

class CategoryRequest(

    @field:NotBlank
    @field:Size(max = 255)
    val name: String,

) : Request
