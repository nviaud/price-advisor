package com.nviaud.pricing.api.assemblers

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
        averagePrice = product.averagePrice,
        minPrice = product.minPrice,
        maxPrice = product.maxPrice,
        variance = product.variance
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