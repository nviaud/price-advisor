//package com.nviaud.pricing.services
//
//import com.nviaud.pricing.api.assemblers.CategoryResponseAssembler
//import com.nviaud.pricing.api.resources.CategoryRequest
//import com.nviaud.pricing.services.CategoryService
//import org.junit.jupiter.api.BeforeEach
//import org.junit.jupiter.api.Test
//import org.mockito.Mockito
//import org.mockito.MockitoAnnotations
//import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
//import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
//import org.springframework.security.test.context.support.WithMockUser
//import org.springframework.http.MediaType
//import org.springframework.test.web.servlet.MockMvc
//import org.springframework.beans.factory.annotation.Autowired
//import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
//import com.nviaud.pricing.api.controllers.CategoryController
//import com.nviaud.pricing.entities.Category
//import com.nviaud.pricing.services.dto.CreateCategory
//import org.mockito.kotlin.any
//import org.springframework.data.domain.PageImpl
//import org.springframework.data.domain.PageRequest
//import org.springframework.test.context.bean.override.mockito.MockitoBean
//import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
//import org.springframework.test.web.servlet.result.MockMvcResultMatchers
//import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
//import java.util.NoSuchElementException
//
//@WebMvcTest(CategoryController::class)
//@AutoConfigureMockMvc
//class CategoryServiceTest {
//
//    @Autowired
//    private lateinit var mockMvc: MockMvc
//
//    @MockitoBean
//    private lateinit var categoryService: CategoryService
//
//    @MockitoBean
//    private lateinit var categoryResponseAssembler: CategoryResponseAssembler
//
//    private val objectMapper = jacksonObjectMapper()
//
//    @BeforeEach
//    fun setup() {
//        MockitoAnnotations.openMocks(this)
//    }
//
//    @Test
//    @WithMockUser
//    fun `should return paginated categories`() {
//        val categories = PageImpl<Category>(listOf(), PageRequest.of(0, 10), 0)
//        Mockito.`when`(categoryService.findAll(any())).thenReturn(categories)
//
//        val requestBuilder = MockMvcRequestBuilders.get("/v1/categories")
//
//        mockMvc.perform(requestBuilder)
//            .andExpect(MockMvcResultMatchers.status().isOk)
//    }
//
//    @Test
//    @WithMockUser
//    fun `should return category by id`() {
//        val category = Category(). apply {
//            id = 1L
//            name = "Category"
//        }
//        Mockito.`when`(categoryService.findById(1L)).thenReturn(category)
//
//        val requestBuilder = MockMvcRequestBuilders.get("/v1/categories/1")
//            .with(csrf())
//        mockMvc.perform(requestBuilder)
//            .andExpect(MockMvcResultMatchers.status().isOk)
//    }
//
//    @Test
//    @WithMockUser(roles = ["ADMIN"])
//    fun `should create category`() {
//        val request = CategoryRequest("Category")
//        val dto = CreateCategory( "Category")
//        val category = Category(). apply {
//            id = 1L
//            name = "Category"
//        }
//        Mockito.`when`(categoryService.create(dto)).thenReturn(category)
//
//        val requestBuilder = MockMvcRequestBuilders.post("/v1/categories")
//            .with(csrf())
//            .contentType(MediaType.APPLICATION_JSON)
//            .content(objectMapper.writeValueAsString(request))
//
//        mockMvc.perform(requestBuilder)
//            .andExpect(MockMvcResultMatchers.status().isCreated)
//    }
//
//    @Test
//    @WithMockUser(roles = ["ADMIN"])
//    fun `should update category`() {
//        val request = CategoryRequest("Category")
//        val category = Category(). apply {
//            id = 1L
//            name = "Category"
//        }
//        Mockito.`when`(categoryService.update(any())).thenReturn(category)
//
//        val requestBuilder = MockMvcRequestBuilders.put("/v1/categories/1")
//            .with(csrf())
//            .contentType(MediaType.APPLICATION_JSON)
//            .content(objectMapper.writeValueAsString(request))
//
//        mockMvc.perform(requestBuilder)
//            .andExpect(MockMvcResultMatchers.status().isOk)
//    }
//
//    @Test
//    @WithMockUser(roles = ["ADMIN"])
//    fun `should delete category`() {
//        val category = Category(). apply {
//            id = 1L
//            name = "Category"
//        }
//        Mockito.`when`(categoryService.delete(1L)).thenReturn( category)
//
//        val requestBuilder = MockMvcRequestBuilders.delete("/v1/categories/1")
//            .with(csrf())
//
//        mockMvc.perform(requestBuilder)
//            .andExpect(MockMvcResultMatchers.status().isNoContent)
//    }
//
//    @Test
//    @WithMockUser
//    fun `should return 404 when category not found`() {
//        Mockito.`when`(categoryService.findById(999L)).thenThrow(NoSuchElementException())
//
//        val requestBuilder = MockMvcRequestBuilders.get("/v1/categories/999")
//
//        mockMvc.perform(requestBuilder)
//            .andExpect(MockMvcResultMatchers.status().isNotFound)
//    }
//}
