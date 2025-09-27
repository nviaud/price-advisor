package com.nviaud.pricing.services

import com.nviaud.pricing.entities.Category
import com.nviaud.pricing.repositories.CategoryRepository
import com.nviaud.pricing.services.dto.CreateCategory
import com.nviaud.pricing.services.dto.PartialUpdateCategory
import com.nviaud.pricing.services.dto.UpdateCategory
import com.nviaud.pricing.services.exceptions.CategoryNotFoundException
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

@Service
class CategoryService(private val categoryRepository: CategoryRepository,) {

    private val logger = LoggerFactory.getLogger(javaClass)

    fun findAll(pageable: Pageable) = categoryRepository.findAll(pageable)

    fun findById(id: Long) = categoryRepository.findById(id).orElseThrow { CategoryNotFoundException("Category with id $id not found") } !!

    fun create(dto: CreateCategory): Category {
        val category = Category().apply {
            name = dto.name
        }
        return categoryRepository.save(category)
    }

    fun update(dto: UpdateCategory): Category {
        val category = this.findById(dto.id)
        category.apply {
            name = dto.name
        }
        return categoryRepository.save(category)
    }

    fun partialUpdate(dto: PartialUpdateCategory): Category {
        val category = this.findById(dto.id)

        category.apply {
            dto.name.takeIf { it.isSet }?.let { name = it.value }
        }

        return categoryRepository.save(category)
    }

    fun delete(id: Long): Category {
        val category = this.findById(id)
        categoryRepository.delete(category)
        return category
    }
}