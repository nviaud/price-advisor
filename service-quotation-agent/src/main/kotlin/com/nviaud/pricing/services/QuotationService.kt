package com.nviaud.pricing.services

import com.nviaud.pricing.entities.Quotation
import com.nviaud.pricing.entities.QuotationStatus
import com.nviaud.pricing.events.EventPublisher
import com.nviaud.pricing.events.Topics
import com.nviaud.pricing.events.v1.*
import com.nviaud.pricing.repositories.QuotationRepository
import com.nviaud.pricing.services.parsers.QuotationParserService
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.core.io.ByteArrayResource
import org.springframework.core.io.Resource
import org.springframework.messaging.support.ErrorMessage
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.util.MimeType
import org.springframework.util.MimeTypeUtils
import java.security.Principal
import java.time.ZonedDateTime
import java.util.function.Consumer


@Service
class QuotationService(
    private val quotationParserService: QuotationParserService,
    private val quotationRepository: QuotationRepository,
    private val eventPublisher: EventPublisher,
)  {

    private val logger = LoggerFactory.getLogger(javaClass)

    fun findById(id: Long) = quotationRepository.findById(id).orElseThrow {
        NoSuchElementException("Quotation with id $id not found")
    }

    fun getQuotation(id: Long) = findById(id)

    @Transactional
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

        // Publish event (uses Outbox pattern internally)
        eventPublisher.publish(
            eventType = Topics.QUOTATION_SUBMITTED_V1,
            data = QuotationSubmitted(quotationId = savedQuotation.id!!.toString()),
            aggregateType = "Quotation",
            aggregateId = savedQuotation.id!!.toString()
        )

        logger.info("Quotation submitted: id={}, userId={}", savedQuotation.id, savedQuotation.userId)

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

        // Publish parsing completed event
        publishQuotationParsingCompleted(savedQuotation)
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

                // Publish parsing failed event
                publishQuotationParsingFailed(savedQuotation, error.payload.message)
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

        // Publish validation event
        publishQuotationValidated(savedQuotation)
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

        // Publish rejection event
        publishQuotationRejected(savedQuotation)
        return savedQuotation
    }

    private fun publishQuotationParsingCompleted(quotation: Quotation) {
        val eventProducts = mapQuotationProducts(quotation)

        eventPublisher.publish(
            eventType = Topics.QUOTATION_PARSING_COMPLETED_V1,
            data = QuotationParsingCompleted(
                quotationId = quotation.id!!.toString(),
                userId = quotation.userId!!,
                products = eventProducts,
                quotationDate = quotation.data?.quotationDate
            ),
            aggregateType = "Quotation",
            aggregateId = quotation.id!!.toString()
        )
        logger.debug("QuotationParsingCompleted event queued: quotationId={}, productsCount={}",
            quotation.id, eventProducts.size)
    }

    private fun publishQuotationParsingFailed(quotation: Quotation, errorMessage: String?) {
        eventPublisher.publish(
            eventType = Topics.QUOTATION_PARSING_FAILED_V1,
            data = QuotationParsingFailed(
                quotationId = quotation.id!!.toString(),
                userId = quotation.userId!!,
                errorMessage = errorMessage
            ),
            aggregateType = "Quotation",
            aggregateId = quotation.id!!.toString()
        )
        logger.debug("QuotationParsingFailed event queued: quotationId={}", quotation.id)
    }

    private fun publishQuotationValidated(quotation: Quotation) {
        val eventProducts = mapQuotationProducts(quotation)

        eventPublisher.publish(
            eventType = Topics.QUOTATION_VALIDATED_V1,
            data = QuotationValidated(
                quotationId = quotation.id!!.toString(),
                userId = quotation.userId!!,
                validationDate = quotation.validationDate!!.toString(),
                products = eventProducts,
                quotationDate = quotation.data?.quotationDate
            ),
            aggregateType = "Quotation",
            aggregateId = quotation.id!!.toString()
        )
        logger.debug("QuotationValidated event queued: quotationId={}, productsCount={}",
            quotation.id, eventProducts.size)
    }

    private fun publishQuotationRejected(quotation: Quotation) {
        eventPublisher.publish(
            eventType = Topics.QUOTATION_REJECTED_V1,
            data = QuotationRejected(
                quotationId = quotation.id!!.toString(),
                userId = quotation.userId!!,
                rejectionDate = quotation.validationDate!!.toString(),
                reason = null  // Can be extended to include rejection reason
            ),
            aggregateType = "Quotation",
            aggregateId = quotation.id!!.toString()
        )
        logger.debug("QuotationRejected event queued: quotationId={}", quotation.id)
    }

    private fun mapQuotationProducts(quotation: Quotation): List<QuotationProduct> {
        return quotation.data?.products?.map { product ->
            QuotationProduct(
                productBrand = product.productBrand,
                productName = product.productName,
                productCategory = product.productCategory,
                productHeight = product.productHeight,
                productWidth = product.productWidth,
                productDepth = product.productDepth,
                productWeight = product.productWeight,
                quantity = product.quantity,
                unitPrice = product.unitPrice,
                totalPrice = product.totalPrice,
                vat = product.vat
            )
        } ?: emptyList()
    }
}

