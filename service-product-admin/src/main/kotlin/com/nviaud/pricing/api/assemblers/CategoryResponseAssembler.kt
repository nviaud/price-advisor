package com.nviaud.pricing.api.assemblers

import com.nviaud.pricing.api.resources.CategoryResponse
import com.nviaud.pricing.api.resources.PaginatedCategoryResponse
import com.nviaud.pricing.entities.Category
import org.springframework.stereotype.Service

/**
 * Assembles CategoryResponse objects from Product entities.
 */
@Service
class CategoryResponseAssembler: ResponseAssembler<Category, CategoryResponse, PaginatedCategoryResponse>() {

    override fun toResponse(t: Category) = CategoryResponse(
        id = t.id!!,
        name = t.name,
    )

    override fun instantiatePaginatedResponse(
        content: List<CategoryResponse>,
        number: Int,
        size: Int,
        totalElements: Long,
        totalPages: Int
    ) = PaginatedCategoryResponse(
        content = content,
        page = number,
        size = size,
        totalElements = totalElements,
        totalPages = totalPages
    )

}