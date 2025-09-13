package com.nviaud.pricing.api.controllers

import com.nviaud.pricing.api.models.ProductRequest
import com.nviaud.pricing.api.models.ProductResponse
import com.nviaud.pricing.api.models.PaginatedProductResponse
import org.springframework.http.ResponseEntity
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.PageRequest
import org.springframework.web.bind.annotation.RequestParam
import com.nviaud.pricing.api.assemblers.ProductResponseAssembler
import com.nviaud.pricing.services.CreateProduct
import com.nviaud.pricing.services.ProductService
import com.nviaud.pricing.services.UpdateProduct
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.PatchMapping


@RestController
@RequestMapping("/products")
@Suppress("unused")
class ProductController (
    private val productService: ProductService,
    private val productResponseAssembler: ProductResponseAssembler
) {
    // Get all products with pagination
    @GetMapping("/pages")
    fun getAllProductsWithPagination(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int
    ): ResponseEntity<PaginatedProductResponse> {
        val pageable: Pageable = PageRequest.of(page, size)
        val productsPage = productService.findAll(pageable)
        val response = productResponseAssembler.toResponsePage(productsPage)
        return ResponseEntity.ok(response)
    }

    // Get a product by ID
    @GetMapping("/{id}")
    fun getProductById(@PathVariable id: Long): ResponseEntity<ProductResponse> {
        productService.findById(id).let { product ->
            return if (product != null) {
                ResponseEntity.ok(productResponseAssembler.toResponse(product))
            } else {
                ResponseEntity.status(HttpStatus.NOT_FOUND).build()
            }
        }
    }

    // Create product
    @PostMapping
    fun createProduct(@Valid @RequestBody productRequest: ProductRequest): ResponseEntity<ProductResponse> {
        val productDto = CreateProduct(
            name = productRequest.name,
            brand = productRequest.brand,
            height = productRequest.height,
            width = productRequest.width,
            depth = productRequest.depth,
            weight = productRequest.weight,
            categoryName = productRequest.categoryName
        )
        val product = productService.create(productDto)
        return ResponseEntity.status(HttpStatus.CREATED).body(productResponseAssembler.toResponse(product))
    }

    @PutMapping("/{id}")
    fun updateProduct(@PathVariable id: Long, @Valid @RequestBody productRequest: ProductRequest): ResponseEntity<ProductResponse> {
        val productDto = UpdateProduct(
            id = id,
            name = productRequest.name,
            brand = productRequest.brand,
            height = productRequest.height,
            width = productRequest.width,
            depth = productRequest.depth,
            weight = productRequest.weight,
            categoryName = productRequest.categoryName
        )
        val product = productService.update(productDto)
        return ResponseEntity.ok(productResponseAssembler.toResponse(product))
    }

    @PatchMapping("/{id}")
    fun updateProductPartial(@PathVariable id: Long, @Valid @RequestBody productRequest: ProductRequest): ResponseEntity<ProductResponse> {
        TODO()
    }

    @DeleteMapping("/{id}")
    fun deleteProduct(@PathVariable id: Long): ResponseEntity<Void> {
        productService.delete(id)
        return ResponseEntity.noContent().build()
    }
}