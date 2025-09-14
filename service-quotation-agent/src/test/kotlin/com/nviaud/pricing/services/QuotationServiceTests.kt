package com.nviaud.pricing.services

import com.nviaud.pricing.repositories.QuotationRepository
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.ai.chat.client.ChatClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cloud.stream.function.StreamBridge
import org.springframework.test.context.bean.override.mockito.MockitoBean
import java.math.BigDecimal

@SpringBootTest
class QuotationServiceTests {

    @MockitoBean
    lateinit var productService: ProductService

    @MockitoBean
    lateinit var quotationRepository: QuotationRepository

    @MockitoBean
    lateinit var chatClient: ChatClient

    @MockitoBean
    lateinit var streamBridge: StreamBridge

    @Autowired
    lateinit var quotationService: QuotationService

    @Test
    fun `convert JSON response to quotation data`() {
        val response = this::class.java.getResource("/response1.json")!!.readText()
        val quotationData = quotationService.convertToQuotationData(response)
        Assertions.assertThat(quotationData.quotationDate).isEqualTo("2025-09-01")
        Assertions.assertThat(quotationData.products).hasSize(2)

        val product1 = quotationData.products[0]
        Assertions.assertThat(product1.productBrand).isEqualTo("phenix")
        Assertions.assertThat(product1.productCategory).isEqualTo("fenêtre")
        Assertions.assertThat(product1.productName).isEqualTo("fenêtre pvc double vantaux")
        Assertions.assertThat(product1.productHeight).isEqualTo(1250)
        Assertions.assertThat(product1.productWidth).isEqualTo(1250)
        Assertions.assertThat(product1.productDepth).isNull()
        Assertions.assertThat(product1.productWeight).isNull()
        Assertions.assertThat(product1.quantity).isEqualTo(2)
        Assertions.assertThat(product1.unitPrice.compareTo(BigDecimal("925.58"))).isEqualTo(0)
        Assertions.assertThat(product1.totalPrice.compareTo(BigDecimal("1851.16"))).isEqualTo(0)
        Assertions.assertThat(product1.vat?.compareTo(BigDecimal("7.12"))).isEqualTo(0)

        val product2 = quotationData.products[1]
        Assertions.assertThat(product2.productBrand).isEqualTo("franciaflex")
        Assertions.assertThat(product2.productCategory).isEqualTo("volet")
        Assertions.assertThat(product2.productName).isEqualTo("volet roulant")
        Assertions.assertThat(product2.productHeight).isEqualTo(1500)
        Assertions.assertThat(product2.productWidth).isEqualTo(1350)
        Assertions.assertThat(product2.productDepth).isNull()
        Assertions.assertThat(product2.productWeight).isNull()
        Assertions.assertThat(product2.quantity).isEqualTo(1)
        Assertions.assertThat(product2.unitPrice.compareTo(BigDecimal("459.48"))).isEqualTo(0)
        Assertions.assertThat(product2.totalPrice.compareTo(BigDecimal("918.96"))).isEqualTo(0)
        Assertions.assertThat(product2.vat?.compareTo(BigDecimal("5.5"))).isEqualTo(0)
    }
}
