package com.nviaud.pricing.services

import com.nviaud.pricing.entities.Product
import com.nviaud.pricing.repositories.ProductRepository
import com.nviaud.pricing.repositories.ProductCategoryRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

@Service
class ProductService(
    private val productRepository: ProductRepository,
    private val productCategoryRepository: ProductCategoryRepository
) {

fun findById(id: Long): Product? {
        return productRepository.findById(id).orElse(null)
    }

    fun findAll(pageable: Pageable): Page<Product> {
        return productRepository.findAll(pageable)
    }

    fun create(dto: CreateProduct): Product {
        val product = Product().apply {
            name = dto.name
            brand = dto.brand
            height = dto.height
            width = dto.width
            depth = dto.depth
            weight = dto.weight
            category = dto.categoryName ?.let {
                productCategoryRepository.findByName(it)
                    ?: throw IllegalArgumentException("Category with name $it not found")
            }
        }
        return productRepository.save(product)
    }

    fun update(dto: UpdateProduct, partial: Boolean = false): Product {
        val product = productRepository.findById(dto.id).orElseThrow {
            NoSuchElementException("Product with id ${dto.id} not found")
        }
        product.apply {
            name = dto.name
            brand = dto.brand
            height = dto.height
            width = dto.width
            depth = dto.depth
            weight = dto.weight
            category = dto.categoryName?.let {
                productCategoryRepository.findByName(it)
                    ?: throw IllegalArgumentException("Category with name $it not found")
            }
        }
        return productRepository.save(product)
    }

    fun partialUpdate(dto: PartialUpdateProduct): Product {
        val product = productRepository.findById(dto.id).orElseThrow {
            NoSuchElementException("Product with id ${dto.id} not found")
        }

        product.apply {
            dto.name?.let { name = it }
            dto.brand?.let { brand = it }
            dto.height?.let { height = it }
            dto.width?.let { width = it }
            dto.depth?.let { depth = it }
            dto.weight?.let { weight = it }
            dto.categoryName?.let {
                category = productCategoryRepository.findByName(it)
                    ?: throw IllegalArgumentException("Category with name $it not found")
            }
        }

        return productRepository.save(product)
    }

    fun delete(id: Long): Product {
        val product = productRepository.findById(id).orElseThrow { NoSuchElementException("Product with id $id not found") }
        productRepository.delete(product)
        return product
    }

}

data class CreateProduct (
    val name: String,
    val brand: String,
    val height: Int?,
    val width: Int?,
    val depth: Int?,
    val weight: Int?,
    val categoryName: String?
)

data class UpdateProduct (
    val id: Long,
    val name: String,
    val brand: String,
    val height: Int?,
    val width: Int?,
    val depth: Int?,
    val weight: Int?,
    val categoryName: String?
)

data class PartialUpdateProduct (
    val id: Long,
    val name: String?,
    val brand: String?,
    val height: Int?,
    val width: Int?,
    val depth: Int?,
    val weight: Int?,
    val categoryName: String?
)
