package com.nviaud.pricing.api.resources

class PaginatedCategoryResponse (
    override val content: List<CategoryResponse>,
    override val page: Int,
    override val size: Int,
    override val totalElements: Long,
    override val totalPages: Int
) : PaginatedResponse<CategoryResponse>
