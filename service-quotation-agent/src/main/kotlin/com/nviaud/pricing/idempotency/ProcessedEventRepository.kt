package com.nviaud.pricing.idempotency

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.time.Instant

@Repository
interface ProcessedEventRepository : JpaRepository<ProcessedEvent, String> {

    /**
     * Check if an event has already been processed.
     */
    fun existsByEventId(eventId: String): Boolean

    /**
     * Find processed events by type (for monitoring/debugging)
     */
    fun findByEventType(eventType: String): List<ProcessedEvent>

    /**
     * Delete old processed events (cleanup job).
     * Keep events for a retention period (e.g., 30 days) to handle late/duplicate deliveries.
     */
    @Modifying
    @Query("DELETE FROM ProcessedEvent e WHERE e.processedAt < :before")
    fun deleteProcessedEventsBefore(before: Instant): Int

    /**
     * Count processed events by type (for monitoring)
     */
    fun countByEventType(eventType: String): Long
}
