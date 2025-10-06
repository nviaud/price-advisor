package com.nviaud.pricing.services.parsers

import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.core.io.Resource
import org.springframework.stereotype.Service
import org.springframework.util.MimeType
import java.math.BigDecimal


@Service
@ConditionalOnProperty(name = ["application.services.quotation-parser.stub.enabled"], havingValue = "true")
class QuotationParserStubService : QuotationParserService {

    private val logger = LoggerFactory.getLogger(javaClass)

    override fun parseDataFromQuotation(
        mimeType: MimeType,
        input: Resource
    ): QuotationData {
        if (!input.exists()) {
            throw IllegalArgumentException("Input resource does not exist")
        }
        if (!input.isReadable) {
            throw IllegalArgumentException("Input resource is not readable")
        }
        logger.debug("Generating stub quotation data")

        // Wait and return a fake response
        Thread.sleep(10000)
        return QuotationData(
            quotationDate = "2025-09-01",
            products = listOf(
                QuotationDataProduct(
                    productBrand = "phenix",
                    productCategory = "fenêtre",
                    productName = "fenêtre pvc double vantaux",
                    productHeight = 1250,
                    productWidth = 1250,
                    quantity = 2,
                    unitPrice = BigDecimal("925.58"),
                    totalPrice = BigDecimal("1851.16"),
                    vat = BigDecimal("7.12")
                ),
                QuotationDataProduct(
                    productBrand = "franciaflex",
                    productCategory = "volet",
                    productName = "volet roulant",
                    productHeight = 1500,
                    productWidth = 1350,
                    quantity = 1,
                    unitPrice = BigDecimal("799.00"),
                    totalPrice = BigDecimal("799.00"),
                    vat = BigDecimal("5.58")
                )
            )
        )
    }

}
