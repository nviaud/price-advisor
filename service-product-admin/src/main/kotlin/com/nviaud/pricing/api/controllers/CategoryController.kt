package com.nviaud.pricing.api.controllers

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
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.PatchMapping
import com.nviaud.pricing.api.annotations.PublicEndpoint
import com.nviaud.pricing.api.assemblers.CategoryResponseAssembler
import com.nviaud.pricing.api.resources.CategoryRequest
import com.nviaud.pricing.api.resources.CategoryResponse
import com.nviaud.pricing.api.resources.PaginatedCategoryResponse
import com.nviaud.pricing.services.CategoryService
import com.nviaud.pricing.services.dto.CreateCategory
import com.nviaud.pricing.services.dto.UpdateCategory
import org.springframework.http.MediaType


@RestController
@RequestMapping("/v1/categories")
@Suppress("unused")
class CategoryController (
    private val categoryService: CategoryService,
    private val categoryResponseAssembler: CategoryResponseAssembler
) {

    // Get all categories with pagination
    @PublicEndpoint
    @GetMapping
    fun getAllCategoryWithPagination(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int
    ): ResponseEntity<PaginatedCategoryResponse> {
        val pageable: Pageable = PageRequest.of(page, size)
        val categoryPage = categoryService.findAll(pageable)
        val response = categoryResponseAssembler.toResponsePage(categoryPage)
        return ResponseEntity.ok(response)
    }

    // Get a category by ID
    @PublicEndpoint
    @GetMapping("/{id}")
    fun getCategoryById(@PathVariable id: Long): ResponseEntity<CategoryResponse> {
        val category = categoryService.findById(id)
        return ResponseEntity.ok(categoryResponseAssembler.toResponse(category))
    }

    // Create category
    @PostMapping
    fun createCategory(@Valid @RequestBody categoryRequest: CategoryRequest): ResponseEntity<CategoryResponse> {
        val categoryDto = CreateCategory(
            name = categoryRequest.name,
        )
        val product = categoryService.create(categoryDto)
        return ResponseEntity.status(HttpStatus.CREATED).body(categoryResponseAssembler.toResponse(product))
    }

    // Update category
    @PutMapping("/{id}")
    fun updateCategory(@PathVariable id: Long, @Valid @RequestBody categoryRequest: CategoryRequest): ResponseEntity<CategoryResponse> {
        val categoryDto = UpdateCategory(
            id = id,
            name = categoryRequest.name,
        )
        val product = categoryService.update(categoryDto)
        return ResponseEntity.ok(categoryResponseAssembler.toResponse(product))
    }

    // Partial update category
    @PatchMapping("/{id}")
    fun updateProductPartial(@PathVariable id: Long, @Valid @RequestBody patchRequest: CategoryRequest): ResponseEntity<CategoryResponse> {
        TODO()
    }

    // Delete category
    @DeleteMapping("/{id}")
    fun deleteCategory(@PathVariable id: Long): ResponseEntity<Void> {
        categoryService.delete(id)
        return ResponseEntity.noContent().build()
    }
}