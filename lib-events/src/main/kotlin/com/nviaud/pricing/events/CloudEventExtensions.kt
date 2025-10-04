package com.nviaud.pricing.events

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.cloudevents.CloudEvent
import io.cloudevents.core.builder.CloudEventBuilder
import io.cloudevents.jackson.JsonFormat
import java.net.URI
import java.time.OffsetDateTime
import java.util.UUID

/**
 * Utility functions for creating CloudEvents from domain events.
 */
object CloudEventFactory {

    private const val DEFAULT_SOURCE = "urn:pricing:events"
    private val objectMapper: ObjectMapper = jacksonObjectMapper()

    /**
     * Creates a CloudEvent from a domain event payload.
     *
     * @param type The event type (e.g., "pricing-quotation-submitted.v1")
     * @param data The event payload
     * @param source The source system (default: "urn:pricing:events")
     * @param subject Optional subject (e.g., quotationId, productId)
     * @param correlationId Optional correlation ID for tracing
     * @param dataContentType Content type of the data (default: "application/json")
     */
    fun <T> create(
        type: String,
        data: T,
        source: String = DEFAULT_SOURCE,
        subject: String? = null,
        correlationId: String? = null,
        dataContentType: String = "application/json"
    ): CloudEvent {
        // Serialize data to JSON bytes
        val jsonBytes = objectMapper.writeValueAsBytes(data)

        val builder = CloudEventBuilder.v1()
            .withId(UUID.randomUUID().toString())
            .withType(type)
            .withSource(URI.create(source))
            .withTime(OffsetDateTime.now())
            .withDataContentType(dataContentType)
            .withData(jsonBytes)

        subject?.let { builder.withSubject(it) }
        correlationId?.let { builder.withExtension("correlationid", it) }

        return builder.build()
    }

    /**
     * Extracts the correlation ID from a CloudEvent.
     */
    fun CloudEvent.getCorrelationId(): String? {
        return this.getExtension("correlationid") as? String
    }

    /**
     * Extracts typed data from a CloudEvent.
     */
    inline fun <reified T> CloudEvent.getData(): T? {
        val data = this.data ?: return null
        // Create a new ObjectMapper instance for inline function
        val mapper = jacksonObjectMapper()
        return mapper.readValue(data.toBytes(), T::class.java)
    }
}
