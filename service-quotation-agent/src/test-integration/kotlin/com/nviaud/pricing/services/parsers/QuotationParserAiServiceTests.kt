package com.nviaud.pricing.services.parsers

import com.nviaud.pricing.extensions.MilvusTestExtension
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.ai.document.Document
import org.springframework.ai.vectorstore.SearchRequest
import org.springframework.ai.vectorstore.VectorStore
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.core.io.Resource
import org.springframework.util.MimeType
import java.math.BigDecimal


@ExtendWith(MilvusTestExtension::class)
@SpringBootTest
class QuotationParserAiServiceTests {

    @Autowired
    lateinit var quotationParserService: QuotationParserAiService

    @Autowired
    lateinit var vectorStore: VectorStore

    @Value("classpath:quotation.pdf")
    lateinit var quotationResource: Resource

    @Test
    fun `should create Quotation from file`() {

        vectorStore.add(
            listOf(
                Document("phenix fenêtre pvc double vantaux 1250x1250 for Spring",  mapOf("id" to "1")),
                Document("franciaflex volet roulant 1500x1350",  mapOf("id" to "2")),
                Document("spring is really a good framework",  mapOf("id" to "3", "author" to "john", "article_type" to "blog")),
                Document("I love spring framework",  mapOf("id" to "4", "author" to "jill", "article_type" to "blog")),
                Document("spring boot makes it easy to create stand-alone applications",  mapOf("id" to "5", "author" to "jack", "article_type" to "news")),
            )
        )

        val quotationData = quotationParserService.parseDataFromQuotation(MimeType.valueOf("application/pdf"), quotationResource)

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