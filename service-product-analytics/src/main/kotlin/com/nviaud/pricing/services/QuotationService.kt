package com.nviaud.pricing.services

import com.nviaud.pricing.entities.ProcessedEvent
import com.nviaud.pricing.repositories.ProcessedEventRepository
import com.nviaud.pricing.events.v1.QuotationValidated
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.messaging.Message
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.ZonedDateTime
import java.util.function.Consumer

@Service
@Suppress("unused")
class QuotationService(
    private val productAnalyticsService: ProductAnalyticsService,
    private val processedEventRepository: ProcessedEventRepository
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    @Bean
    fun onQuotationValidated(): Consumer<Message<QuotationValidated>> {
        return Consumer { message ->
            val quotationValidated = message.payload
            val eventId = message.headers["id"] as? String ?: message.headers["ce_id"] as? String

            logger.info(
                "Received QuotationValidated event: quotationId={}, eventId={}, productsCount={}",
                quotationValidated.quotationId, eventId, quotationValidated.products.size
            )

            // Idempotency check
            if (eventId != null && processedEventRepository.existsByEventId(eventId)) {
                logger.info("Event already processed, skipping: eventId={}", eventId)
                return@Consumer
            }

            processQuotationValidated(quotationValidated, eventId)
        }
    }

    @Transactional
    fun processQuotationValidated(quotationValidated: QuotationValidated, eventId: String?) {
        try {
            // Process each product in the quotation from event data
            quotationValidated.products.forEach { product ->
                productAnalyticsService.recordPriceEvent(
                    quotationId = quotationValidated.quotationId,
                    productBrand = product.productBrand,
                    productName = product.productName,
                    productCategory = product.productCategory,
                    unitPrice = product.unitPrice
                )
            }

            // Mark event as processed (idempotency)
            if (eventId != null) {
                val processedEvent = ProcessedEvent().apply {
                    this.eventId = eventId
                    this.eventType = "QuotationValidated"
                    this.processedAt = ZonedDateTime.now()
                }
                processedEventRepository.save(processedEvent)
            }

            logger.info(
                "Successfully processed analytics for validated quotation: quotationId={}, productsCount={}",
                quotationValidated.quotationId, quotationValidated.products.size
            )
        } catch (e: Exception) {
            logger.error("Error processing quotation analytics: quotationId={}", quotationValidated.quotationId, e)
            throw e
        }
    }
}

