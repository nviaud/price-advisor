package com.nviaud.pricing.api.resources

class PaginatedQuotationResponse(
    val content: List<QuotationResponse>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int
)

