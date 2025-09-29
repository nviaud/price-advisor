package com.nviaud.pricing.api.assemblers

import com.nviaud.pricing.api.resources.PaginatedProductResponse
import com.nviaud.pricing.api.resources.ProductResponse
import com.nviaud.pricing.entities.Product
import org.springframework.stereotype.Service

/**
 * Assembles ProductResponse objects from Product entities.
 * Fetches associated category data.
 */
@Service
class ProductResponseAssembler(
    private val categoryResponseAssembler: CategoryResponseAssembler
): ResponseAssembler<Product, ProductResponse, PaginatedProductResponse>() {

    override fun toResponse(t: Product) = ProductResponse(
        id = t.id!!,
        name = t.name,
        brand = t.brand,
        height = t.specifications?.height,
        width = t.specifications?.width,
        depth = t.specifications?.depth,
        weight = t.specifications?.weight,
        category = t.category?.let { categoryResponseAssembler.toResponse(it) }
    )

    override fun instantiatePaginatedResponse(
        content: List<ProductResponse>,
        number: Int,
        size: Int,
        totalElements: Long,
        totalPages: Int
    ) = PaginatedProductResponse(
        content = content,
        page = number,
        size = size,
        totalElements = totalElements,
        totalPages = totalPages
    )

}