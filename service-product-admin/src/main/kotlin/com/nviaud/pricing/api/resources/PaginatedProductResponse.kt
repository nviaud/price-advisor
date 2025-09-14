package com.nviaud.pricing.api.resources

class PaginatedProductResponse(
    val content: List<ProductResponse>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int
)

