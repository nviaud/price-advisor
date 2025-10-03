package com.nviaud.pricing.services

import com.nviaud.pricing.entities.Quotation
import com.nviaud.pricing.entities.QuotationStatus
import com.nviaud.pricing.events.Events
import com.nviaud.pricing.events.v1.QuotationSubmitted
import com.nviaud.pricing.events.v1.QuotationUpdated
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
    private val quotationRepository: QuotationRepository,
    private val streamBridge: StreamBridge,
)  {

    private val logger = LoggerFactory.getLogger(QuotationService::class.java)

    fun findById(id: Long) = quotationRepository.findById(id).orElseThrow {
        NoSuchElementException("Quotation with id $id not found")
    }

    fun getQuotation(id: Long) = findById(id)

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

        val quotation = findById(message.quotationId.toLong())
        val mimeType = MimeTypeUtils.parseMimeType(quotation.resourceMimeType!!)
        val resource = ByteArrayResource(quotation.resourceData!!)
        val quotationData = quotationParserService.parseDataFromQuotation(mimeType, resource)
        quotation.status = QuotationStatus.WAITING_VALIDATION
        quotation.data = quotationData
        val savedQuotation = quotationRepository.save(quotation)
        logger.info("Quotation " + quotation.id + " updated with parsed data.")

        // Publish status update event
        publishQuotationUpdated(savedQuotation)
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
                val savedQuotation = quotationRepository.save(quotation)

                // Publish error status update event
                publishQuotationUpdated(savedQuotation)
            }
        }
    }

    fun validateQuotation(quotationId: Long, date: ZonedDateTime): Quotation {
        val quotation = findById(quotationId)
        if(quotation.status != QuotationStatus.WAITING_VALIDATION){
            throw IllegalStateException("Can't validate a quotation with status ${quotation.status}")
        }
        quotation.status = QuotationStatus.VALIDATED
        quotation.validationDate = date
        val savedQuotation = quotationRepository.save(quotation)

        // Publish status update event
        publishQuotationUpdated(savedQuotation)
        return savedQuotation
    }

    fun rejectQuotation(quotationId: Long, date: ZonedDateTime): Quotation {
        val quotation = findById(quotationId)
        if(quotation.status != QuotationStatus.WAITING_VALIDATION){
            throw IllegalStateException("Can't reject a quotation with status ${quotation.status}")
        }
        quotation.status = QuotationStatus.REJECTED
        quotation.validationDate = date
        val savedQuotation = quotationRepository.save(quotation)

        // Publish status update event
        publishQuotationUpdated(savedQuotation)
        return savedQuotation
    }

    private fun publishQuotationUpdated(quotation: Quotation) {
        val message = QuotationUpdated(
            quotationId = quotation.id!!.toString(),
            status = quotation.status!!.name,
            userId = quotation.userId!!
        )
        val sent = streamBridge.send(Events.QUOTATION_UPDATED_V1, message)
        if (!sent) {
            logger.error("Failed to send quotation updated message for quotationId: {}", quotation.id)
        } else {
            logger.debug("Published QuotationUpdated event for quotationId: {}, status: {}", quotation.id, quotation.status)
        }
    }
}

