package com.nviaud.pricing.idempotency

import jakarta.persistence.*
import java.time.Instant

/**
 * Tracks processed events to prevent duplicate processing (idempotency).
 * When an event is successfully processed, its ID is stored here.
 * Before processing an event, check if it already exists in this table.
 */
@Entity
@Table(
    name = "processed_events",
    indexes = [
        Index(name = "idx_processed_event_type", columnList = "eventType,processedAt")
    ]
)
class ProcessedEvent(
    /**
     * Unique event ID (typically CloudEvent ID or correlation ID)
     */
    @Id
    @Column(length = 255)
    val eventId: String,

    /**
     * Type of event processed (e.g., "pricing-quotation-submitted.v1")
     */
    @Column(nullable = false, length = 255)
    val eventType: String,

    /**
     * When the event was processed
     */
    @Column(nullable = false)
    val processedAt: Instant = Instant.now(),

    /**
     * Optional: store aggregate ID for easier debugging
     */
    @Column(length = 255)
    val aggregateId: String? = null,

    @Version
    val version: Long = 0
)
