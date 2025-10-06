package com.nviaud.pricing.idempotency

import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.time.temporal.ChronoUnit

/**
 * Service for ensuring idempotent event processing.
 * Use this to check if an event has already been processed before handling it.
 */
@Service
class IdempotencyService(
    private val processedEventRepository: ProcessedEventRepository
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * Checks if an event has already been processed.
     *
     * @param eventId Unique event ID (typically CloudEvent ID or correlation ID)
     * @return true if the event has already been processed, false otherwise
     */
    fun isProcessed(eventId: String): Boolean {
        return processedEventRepository.existsByEventId(eventId)
    }

    /**
     * Marks an event as processed.
     * Call this within the same transaction as the business logic to ensure atomicity.
     *
     * @param eventId Unique event ID
     * @param eventType Type of event (e.g., "pricing-quotation-submitted.v1")
     * @param aggregateId Optional aggregate ID for debugging
     */
    @Transactional
    fun markAsProcessed(eventId: String, eventType: String, aggregateId: String? = null) {
        if (processedEventRepository.existsById(eventId)) {
            logger.warn("Event already marked as processed: eventId={}", eventId)
            return
        }

        val processedEvent = ProcessedEvent(
            eventId = eventId,
            eventType = eventType,
            aggregateId = aggregateId
        )

        processedEventRepository.save(processedEvent)

        logger.debug("Marked event as processed: eventId={}, type={}", eventId, eventType)
    }

    /**
     * Processes an event with idempotency check.
     * Returns true if the event should be processed, false if it's a duplicate.
     *
     * Usage:
     * ```
     * if (!idempotencyService.ensureIdempotent(eventId, eventType, aggregateId)) {
     *     logger.warn("Duplicate event, skipping")
     *     return
     * }
     * // Process event...
     * ```
     */
    @Transactional
    fun ensureIdempotent(eventId: String, eventType: String, aggregateId: String? = null): Boolean {
        if (isProcessed(eventId)) {
            logger.warn("Duplicate event detected: eventId={}, type={}", eventId, eventType)
            return false
        }

        markAsProcessed(eventId, eventType, aggregateId)
        return true
    }

    /**
     * Cleans up old processed events.
     * Runs daily, keeps events for 30 days (longer than typical message broker retention).
     */
    @Scheduled(fixedDelay = 86400000) // 24 hours
    @Transactional
    fun cleanupOldProcessedEvents() {
        val retentionDays = 30L
        val before = Instant.now().minus(retentionDays, ChronoUnit.DAYS)
        val deleted = processedEventRepository.deleteProcessedEventsBefore(before)

        if (deleted > 0) {
            logger.info("Cleaned up {} processed events older than {} days", deleted, retentionDays)
        }
    }
}
