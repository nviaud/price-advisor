package com.nviaud.pricing.outbox

import jakarta.persistence.*
import java.time.Instant

/**
 * Transactional Outbox pattern entity.
 * Stores events in the same transaction as business data, ensuring consistency.
 * A separate scheduler publishes events from the outbox to the message broker.
 */
@Entity
@Table(
    name = "outbox_events",
    indexes = [
        Index(name = "idx_outbox_published", columnList = "published,createdAt"),
        Index(name = "idx_outbox_aggregate", columnList = "aggregateType,aggregateId")
    ]
)
class OutboxEvent(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    /**
     * Type of aggregate (e.g., "Quotation", "Product")
     */
    @Column(nullable = false, length = 100)
    val aggregateType: String,

    /**
     * ID of the aggregate this event relates to
     */
    @Column(nullable = false, length = 255)
    val aggregateId: String,

    /**
     * Event type (CloudEvent type, e.g., "pricing-quotation-submitted.v1")
     */
    @Column(nullable = false, length = 255)
    val eventType: String,

    /**
     * CloudEvent payload as JSON
     */
    @Column(nullable = false, columnDefinition = "TEXT")
    val payload: String,

    /**
     * Correlation ID for distributed tracing
     */
    @Column(length = 255)
    val correlationId: String? = null,

    /**
     * Whether this event has been published to the message broker
     */
    @Column(nullable = false)
    var published: Boolean = false,

    /**
     * When the event was created
     */
    @Column(nullable = false)
    val createdAt: Instant = Instant.now(),

    /**
     * When the event was published (null if not yet published)
     */
    @Column
    var publishedAt: Instant? = null,

    /**
     * Number of publish attempts (for monitoring/debugging)
     */
    @Column(nullable = false)
    var publishAttempts: Int = 0,

    /**
     * Last error message if publish failed
     */
    @Column(columnDefinition = "TEXT")
    var lastError: String? = null,

    @Version
    val version: Long = 0
)
