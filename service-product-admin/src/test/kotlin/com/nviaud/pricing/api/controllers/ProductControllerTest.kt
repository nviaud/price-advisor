package com.nviaud.pricing.api.controllers

import com.nviaud.pricing.api.resources.ProductRequest
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.test.context.jdbc.Sql

@SpringBootTest
@AutoConfigureMockMvc
@Sql(
    scripts = ["/sql/add-products.sql"],
    executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
)
@Sql(
    scripts = ["/sql/clean-database.sql"],
    executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD
)
class ProductControllerTest {

    private val logger = LoggerFactory.getLogger(javaClass)

    @Autowired
    private lateinit var mockMvc: MockMvc

    private val objectMapper = jacksonObjectMapper()

    @Test
    @WithMockUser
    fun `should return paginated products`() {
        val requestBuilder = get("/v1/products")
        mockMvc.perform(requestBuilder)
            .andExpect(status().isOk)
    }

    @Test
    @WithMockUser
    fun `should return product by id`() {
        val requestBuilder = get("/v1/products/1")
        mockMvc.perform(requestBuilder)
            .andExpect(status().isOk)
    }

    @Test
    @WithMockUser(roles = ["ADMIN"])
    fun `should create product`() {
        val request = ProductRequest("NewProduct", "NewBrand", 1L, 0,0,0,0)
        val requestBuilder = post("/v1/products")
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request))
        mockMvc.perform(requestBuilder)
            .andExpect(status().isCreated)
    }

    @Test
    @WithMockUser(roles = ["admin"])
    fun `should update product`() {
        val request = ProductRequest("UpdatedProduct", "UpdatedBrand", 1L, 12, 22, 32, 42)
        val requestBuilder = put("/v1/products/1")
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request))
        mockMvc.perform(requestBuilder)
            .andExpect(status().isOk)
    }

    @Test
    @WithMockUser(roles = ["admin"])
    fun `should delete product`() {
        val requestBuilder = delete("/v1/products/1")
            .with(csrf())
        mockMvc.perform(requestBuilder)
            .andExpect(status().isNoContent)
    }

    @Test
    @WithMockUser
    fun `should return 404 when product not found`() {
        val requestBuilder = get("/v1/products/999")
        mockMvc.perform(requestBuilder)
            .andExpect(status().isNotFound)
    }
}
