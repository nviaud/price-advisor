package com.nviaud.pricing.api.assemblers

import com.nviaud.pricing.api.models.ProductCategoryResponse
import com.nviaud.pricing.api.models.PaginatedProductResponse
import com.nviaud.pricing.api.models.ProductResponse
import com.nviaud.pricing.entities.Product
import org.springframework.data.domain.Page
import org.springframework.stereotype.Service

@Service
class ProductResponseAssembler {

    fun toResponse(product: Product): ProductResponse = ProductResponse(
        id = product.id,
        name = product.name,
        brand = product.brand,
        height = product.height,
        width = product.width,
        depth = product.depth,
        weight = product.weight,
        category = product.category ?.let {
            ProductCategoryResponse(
                id = it.id,
                name = it.name
            )
        }
    )

    fun toResponseList(products: List<Product>): List<ProductResponse> =
        products.map { toResponse(it) }

    fun toResponsePage(products: Page<Product>) =
        PaginatedProductResponse(
            content = toResponseList(products.content),
            page = products.number,
            size = products.size,
            totalElements = products.totalElements,
            totalPages = products.totalPages
        )
}