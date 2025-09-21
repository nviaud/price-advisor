package com.nviaud.pricing.services

import com.nviaud.pricing.entities.Quotation
import com.nviaud.pricing.entities.QuotationStatus
import com.nviaud.pricing.events.Events
import com.nviaud.pricing.events.v1.QuotationSubmitted
import com.nviaud.pricing.repositories.QuotationRepository
import com.nviaud.pricing.services.parsers.QuotationParserService
import org.slf4j.LoggerFactory
import org.springframework.cloud.stream.function.StreamBridge
import org.springframework.context.annotation.Bean
import org.springframework.core.io.ByteArrayResource
import org.springframework.core.io.Resource
import org.springframework.messaging.support.ErrorMessage
import org.springframework.stereotype.Service
import org.springframework.util.MimeType
import org.springframework.util.MimeTypeUtils
import java.security.Principal
import java.time.ZonedDateTime
import java.util.function.Consumer


@Service
class QuotationService(
    private val quotationParserService: QuotationParserService,
    private val productService: ProductService,
    private val quotationRepository: QuotationRepository,
    private val streamBridge: StreamBridge,
)  {

    private val logger = LoggerFactory.getLogger(QuotationService::class.java)

    fun submitQuotation(
        principal: Principal?,
        mimeType: MimeType,
        resource: Resource,
        currentDate: ZonedDateTime
    ): Quotation {
        // Validate mime type
        when (mimeType) {
            MimeTypeUtils.IMAGE_PNG -> logger.debug("Processing PNG quotation")
            MimeTypeUtils.IMAGE_JPEG -> logger.debug("Processing JPEG quotation")
            MimeType("application","pdf") -> logger.debug("Processing PDF quotation")
            else -> throw IllegalArgumentException("Unsupported mime type: $mimeType")
        }
        val quotation = Quotation().apply {
            userId = principal?.name
            submissionDate = currentDate
            status = QuotationStatus.SUBMITTED
            resourceMimeType = mimeType.toString()
            resourceFilename = resource.filename
            resourceData = resource.inputStream.readAllBytes()
        }

        val savedQuotation = quotationRepository.save(quotation)
        val message = QuotationSubmitted(
            quotationId = savedQuotation.id!!.toString(),
        )
        val sent = streamBridge.send(Events.QUOTATION_SUBMITTED_V1, message)
        if (!sent) {
            logger.error("Failed to send quotation submitted message for quotationId: {}", savedQuotation.id)
            throw RuntimeException("Failed to send message")
        }
        return savedQuotation
    }


    @Bean
    fun onQuotationSubmitted(): Consumer<QuotationSubmitted> = Consumer { message ->
        logger.info("Received QuotationSubmittedMessage: $message")
        val quotation = quotationRepository.findById(message.quotationId.toLong()).orElseThrow {
            NoSuchElementException("Quotation with id ${message.quotationId} not found")
        }

        val mimeType = MimeTypeUtils.parseMimeType(quotation.resourceMimeType!!)
        val resource = ByteArrayResource(quotation.resourceData!!)
        val quotationData = quotationParserService.parseDataFromQuotation(mimeType, resource)
        quotation.status = QuotationStatus.WAITING_VALIDATION
        quotation.data = quotationData
        quotationRepository.save(quotation)
        logger.info("Quotation " + quotation.id + " updated with parsed data.")

    }

    @Bean
    fun onQuotationSubmittedError(): Consumer<ErrorMessage> {
        return Consumer { error ->
            run {
                logger.error("Error processing QuotationSubmitted message", error.payload)
                val message:QuotationSubmitted = error.originalMessage?.payload as QuotationSubmitted
                val quotation = quotationRepository.findById(message.quotationId.toLong()).orElseThrow {
                    NoSuchElementException("Quotation with id ${message.quotationId} not found")
                }
                quotation.status = QuotationStatus.ERROR
                quotationRepository.save(quotation)
            }
        }
    }

}
