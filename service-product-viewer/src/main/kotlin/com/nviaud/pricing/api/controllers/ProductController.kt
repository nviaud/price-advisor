package com.nviaud.pricing.api.controllers

import com.nviaud.pricing.api.models.ProductRequest
import com.nviaud.pricing.api.models.ProductResponse
import com.nviaud.pricing.api.models.PaginatedProductResponse
import com.nviaud.pricing.entities.Product
import com.nviaud.pricing.repositories.ProductRepository
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
import com.nviaud.pricing.repositories.ProductCategoryRepository


@RestController
@RequestMapping("/products")
@Suppress("unused")
class ProductController (
    private val productRepository: ProductRepository,
    private val productCategoryRepository: ProductCategoryRepository,
    private val productResponseAssembler: ProductResponseAssembler
) {
    // Get all products with pagination
    @GetMapping("/pages")
    fun getAllProductsWithPagination(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int
    ): ResponseEntity<PaginatedProductResponse> {
        val pageable: Pageable = PageRequest.of(page, size)
        val productsPage = productRepository.findAll(pageable)
        val response = productResponseAssembler.toResponsePage(productsPage)
        return ResponseEntity.ok(response)
    }

    // Get a product by ID
    @GetMapping("/{id}")
    fun getProductById(@PathVariable id: Long): ResponseEntity<ProductResponse> {
        val product = productRepository.findById(id)
        return if (product.isPresent) {
            ResponseEntity.ok(productResponseAssembler.toResponse(product.get()))
        } else {
            ResponseEntity.status(HttpStatus.NOT_FOUND).build()
        }
    }

    // Create product
    @PostMapping
    fun createProduct(@RequestBody productRequest: ProductRequest): ResponseEntity<ProductResponse> {
        val category = productCategoryRepository.findByName(productRequest.name)
            ?: return ResponseEntity.status(HttpStatus.BAD_REQUEST).build()

        val product = Product().apply {
            name = productRequest.name
            this.category = category
        }
        val savedProduct = productRepository.save(product)
        return ResponseEntity.status(HttpStatus.CREATED).body(productResponseAssembler.toResponse(savedProduct))
    }

    @PutMapping("/{id}")
    fun updateProduct(@PathVariable id: Long, @RequestBody productRequest: ProductRequest): ResponseEntity<ProductResponse> {
        val category = productCategoryRepository.findByName(productRequest.name)
            ?: return ResponseEntity.status(HttpStatus.BAD_REQUEST).build()

        val productOptional = productRepository.findById(id)
        if( !productOptional.isPresent) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build()
        }
        val product = productOptional.get()
        val productUpdated = product.apply {
            name = productRequest.name
            this.category = category
        }
        val productSaved = productRepository.save(productUpdated)
        return ResponseEntity.ok(productResponseAssembler.toResponse(productSaved))
    }

    @DeleteMapping("/{id}")
    fun deleteProduct(@PathVariable id: Long): ResponseEntity<Void> {
        return if (productRepository.existsById(id)) {
            productRepository.deleteById(id)
            ResponseEntity.noContent().build()
        } else {
            ResponseEntity.status(HttpStatus.NOT_FOUND).build()
        }
    }
}