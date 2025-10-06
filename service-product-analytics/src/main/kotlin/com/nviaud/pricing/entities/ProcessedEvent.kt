package com.nviaud.pricing.entities

import jakarta.persistence.*
import java.time.ZonedDateTime

/**
 * Entity for tracking processed events to ensure idempotency.
 * Prevents duplicate processing of the same event.
 */
@Entity
@Table(
    name = "processed_events",
    indexes = [Index(name = "idx_event_id", columnList = "eventId", unique = true)]
)
class ProcessedEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    /**
     * Unique identifier of the processed event (CloudEvent ID)
     */
    @Column(nullable = false, unique = true)
    var eventId: String? = null

    /**
     * Type of the event that was processed
     */
    @Column(nullable = false)
    var eventType: String? = null

    /**
     * Timestamp when the event was processed
     */
    @Column(nullable = false)
    var processedAt: ZonedDateTime? = null
}
