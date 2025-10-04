package com.nviaud.pricing.events

import com.nviaud.pricing.outbox.OutboxEventService
import io.opentelemetry.api.trace.Span
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.UUID

/**
 * Facade for publishing events using the Transactional Outbox pattern.
 * Simplifies business logic by hiding CloudEvents and Outbox complexity.
 *
 * Usage:
 * ```
 * eventPublisher.publish(
 *     eventType = Events.QUOTATION_SUBMITTED_V1,
 *     data = QuotationSubmitted(quotationId = "123"),
 *     aggregateType = "Quotation",
 *     aggregateId = "123"
 * )
 * ```
 */
@Component
class EventPublisher(
    private val outboxEventService: OutboxEventService,
    @Value("\${spring.application.name}") private val serviceName: String
) {

    private val logger = LoggerFactory.getLogger(EventPublisher::class.java)

    /**
     * Publishes an event using the Transactional Outbox pattern.
     * Event will be published asynchronously by OutboxEventPublisher.
     *
     * @param eventType The event type (e.g., Events.QUOTATION_SUBMITTED_V1)
     * @param data The event payload (domain event DTO)
     * @param aggregateType The type of aggregate (e.g., "Quotation", "Product")
     * @param aggregateId The ID of the aggregate
     * @param subject Optional CloudEvent subject (default: aggregateType/aggregateId)
     * @param source Optional CloudEvent source (default: urn:pricing:{serviceName})
     */
    fun <T> publish(
        eventType: String,
        data: T,
        aggregateType: String,
        aggregateId: String,
        subject: String? = null,
        source: String? = null
    ) {
        // Extract correlation ID from current trace context
        val correlationId = try {
            Span.current().spanContext.traceId
        } catch (e: Exception) {
            logger.warn("Failed to extract trace ID, generating new correlation ID", e)
            UUID.randomUUID().toString()
        }

        // Create CloudEvent with metadata
        val cloudEvent = CloudEventFactory.create(
            type = eventType,
            data = data,
            source = source ?: "urn:pricing:$serviceName",
            subject = subject ?: "$aggregateType/$aggregateId",
            correlationId = correlationId
        )

        // Store in outbox (same transaction as business logic)
        outboxEventService.storeEvent(
            cloudEvent = cloudEvent,
            aggregateType = aggregateType,
            aggregateId = aggregateId
        )

        logger.debug(
            "Event queued for publishing: type={}, aggregateId={}, correlationId={}",
            eventType,
            aggregateId,
            correlationId
        )
    }

    /**
     * Convenience method for publishing events without custom subject/source.
     */
    fun <T> publish(
        eventType: String,
        data: T,
        aggregateType: String,
        aggregateId: String
    ) {
        publish(
            eventType = eventType,
            data = data,
            aggregateType = aggregateType,
            aggregateId = aggregateId,
            subject = null,
            source = null
        )
    }
}
