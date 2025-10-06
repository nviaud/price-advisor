package com.nviaud.pricing.outbox

import com.fasterxml.jackson.databind.ObjectMapper
import io.cloudevents.CloudEvent
import io.cloudevents.jackson.JsonFormat
import io.opentelemetry.api.trace.Span
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * Service for storing events in the transactional outbox.
 * Use this instead of publishing directly via StreamBridge to ensure
 * at-least-once delivery and consistency with database transactions.
 */
@Service
class OutboxEventService(
    private val outboxRepository: OutboxEventRepository,
    private val objectMapper: ObjectMapper
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * Stores a CloudEvent in the outbox within the current transaction.
     * The event will be published asynchronously by OutboxEventPublisher.
     *
     * @param cloudEvent The CloudEvent to store
     * @param aggregateType The type of aggregate (e.g., "Quotation", "Product")
     * @param aggregateId The ID of the aggregate
     */
    @Transactional
    fun storeEvent(
        cloudEvent: CloudEvent,
        aggregateType: String,
        aggregateId: String
    ) {
        // Serialize CloudEvent to JSON
        val payload = String(JsonFormat().serialize(cloudEvent))

        // Extract correlation ID from CloudEvent extensions or current trace
        val correlationId = cloudEvent.getExtension("correlationid") as? String
            ?: Span.current().spanContext.traceId

        val outboxEvent = OutboxEvent(
            aggregateType = aggregateType,
            aggregateId = aggregateId,
            eventType = cloudEvent.type,
            payload = payload,
            correlationId = correlationId
        )

        outboxRepository.save(outboxEvent)

        logger.debug(
            "Stored event in outbox: type={}, aggregateId={}, correlationId={}",
            cloudEvent.type,
            aggregateId,
            correlationId
        )
    }
}
