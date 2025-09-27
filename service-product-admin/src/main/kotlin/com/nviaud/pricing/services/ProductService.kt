package com.nviaud.pricing.services

import com.nviaud.pricing.entities.Product
import com.nviaud.pricing.events.Events
import com.nviaud.pricing.events.v1.ProductCreated
import com.nviaud.pricing.events.v1.ProductUpdated
import com.nviaud.pricing.services.exceptions.ProductNotFoundException
import com.nviaud.pricing.services.exceptions.CategoryParametersException
import com.nviaud.pricing.repositories.ProductRepository
import com.nviaud.pricing.repositories.CategoryRepository
import com.nviaud.pricing.services.dto.CreateProduct
import com.nviaud.pricing.services.dto.PartialUpdateProduct
import com.nviaud.pricing.services.dto.UpdateProduct
import jakarta.transaction.Transactional
import org.springframework.cloud.stream.function.StreamBridge
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.slf4j.LoggerFactory

@Service
//@RepositoryEventHandler(Product::class)
class ProductService(
    private val productRepository: ProductRepository,
    private val categoryRepository: CategoryRepository,
    private val streamBridge: StreamBridge,
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    fun findAll(pageable: Pageable) = productRepository.findAll(pageable)

    fun findById(id: Long) = productRepository.findById(id).orElseThrow { ProductNotFoundException("Product with id $id not found") } !!

    private fun findCategoryById(id: Long) = categoryRepository.findById(id).orElseThrow { CategoryParametersException ("Category with name $id not found") }

    @Transactional
    fun create(dto: CreateProduct): Product {
        val product = Product().apply {
            name = dto.name
            brand = dto.brand
            height = dto.height
            width = dto.width
            depth = dto.depth
            weight = dto.weight
            category = findCategoryById(dto.category)
        }
        val savedProduct = productRepository.save(product)
        onProductCreated(savedProduct)
        return savedProduct
    }

    @Transactional
    fun update(dto: UpdateProduct): Product {
        val product = this.findById(dto.id)
        product.apply {
            name = dto.name
            brand = dto.brand
            height = dto.height
            width = dto.width
            depth = dto.depth
            weight = dto.weight
            category = findCategoryById(dto.category)
        }
        val savedProduct = productRepository.save(product)
        onProductUpdated(savedProduct)
        return savedProduct
    }

    @Transactional
    fun partialUpdate(dto: PartialUpdateProduct): Product {
        val product = this.findById(dto.id)

        product.apply {
            dto.name.takeIf { it.isSet }?.let { name = it.value }
            dto.brand.takeIf { it.isSet }?.let { brand = it.value }
            dto.height.takeIf { it.isSet }?.let { height = it.value }
            dto.width.takeIf { it.isSet }?.let { width = it.value }
            dto.depth.takeIf { it.isSet }?.let { depth = it.value }
            dto.weight.takeIf { it.isSet }?.let { weight = it.value }
            dto.category.takeIf { it.isSet }?.let {
                category = findCategoryById(it.value !!)
            }
        }

        val savedProduct = productRepository.save(product)
        onProductUpdated(savedProduct)
        return savedProduct
    }

    @Transactional
    fun delete(id: Long): Product {
        val product = this.findById(id)
        productRepository.delete(product)
        onProductDeleted(product)
        return product
    }

//    @HandleAfterCreate
    fun onProductCreated(product: Product) {
        val event = ProductCreated(
            productId = product.id.toString(),
            name = product.name!!,
            brand = product.brand!!,
            category = product.category!!.name!!
        )
        logger.info("Sending ProductCreated event: $event")
        streamBridge.send(Events.PRODUCT_CREATED_V1, event)
    }

//    @HandleAfterSave
    fun onProductUpdated(product: Product) {
        val event = ProductUpdated(
            productId = product.id.toString(),
            name = product.name!!,
            brand = product.brand!!,
            category = product.category!!.name!!
        )
        logger.info("Sending ProductUpdated event: $event")
        streamBridge.send(Events.PRODUCT_UPDATED_V1, event)
    }


//    @HandleAfterDelete
    fun onProductDeleted(product: Product) {
        TODO()
    }

}

