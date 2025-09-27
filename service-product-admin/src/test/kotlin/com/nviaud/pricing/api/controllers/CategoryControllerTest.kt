package com.nviaud.pricing.api.controllers

import com.nviaud.pricing.api.resources.CategoryRequest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.MockitoAnnotations
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.beans.factory.annotation.Autowired
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.test.context.jdbc.Sql

@SpringBootTest
@AutoConfigureMockMvc
@Sql(
    scripts = ["/sql/add-categories.sql"],
    executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
)
@Sql(
    scripts = ["/sql/clean-database.sql"],
    executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD
)
class CategoryControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    private val objectMapper = jacksonObjectMapper()

    @BeforeEach
    fun setup() {
        MockitoAnnotations.openMocks(this)
    }

    @Test
    @WithMockUser
    fun `should return paginated categories`() {
        val requestBuilder = MockMvcRequestBuilders.get("/v1/categories")

        mockMvc.perform(requestBuilder)
            .andExpect(MockMvcResultMatchers.status().isOk)
    }

    @Test
    @WithMockUser
    fun `should return category by id`() {
        val requestBuilder = MockMvcRequestBuilders.get("/v1/categories/1")
            .with(csrf())
        mockMvc.perform(requestBuilder)
            .andExpect(MockMvcResultMatchers.status().isOk)
    }

    @Test
    @WithMockUser(roles = ["ADMIN"])
    fun `should create category`() {
        val request = CategoryRequest("NewCategory")

        val requestBuilder = MockMvcRequestBuilders.post("/v1/categories")
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request))

        mockMvc.perform(requestBuilder)
            .andExpect(MockMvcResultMatchers.status().isCreated)
    }

    @Test
    @WithMockUser(roles = ["ADMIN"])
    fun `should update category`() {
        val request = CategoryRequest("Category")

        val requestBuilder = MockMvcRequestBuilders.put("/v1/categories/1")
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request))

        mockMvc.perform(requestBuilder)
            .andExpect(MockMvcResultMatchers.status().isOk)
    }

    @Test
    @WithMockUser(roles = ["ADMIN"])
    fun `should delete category`() {
        val requestBuilder = MockMvcRequestBuilders.delete("/v1/categories/1")
            .with(csrf())

        mockMvc.perform(requestBuilder)
            .andExpect(MockMvcResultMatchers.status().isNoContent)
    }

    @Test
    @WithMockUser
    fun `should return 404 when category not found`() {
        val requestBuilder = MockMvcRequestBuilders.get("/v1/categories/999")

        mockMvc.perform(requestBuilder)
            .andExpect(MockMvcResultMatchers.status().isNotFound)
    }
}
