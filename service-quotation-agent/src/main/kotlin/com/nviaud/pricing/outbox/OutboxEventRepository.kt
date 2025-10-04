package com.nviaud.pricing.outbox

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.time.Instant

@Repository
interface OutboxEventRepository : JpaRepository<OutboxEvent, Long> {

    /**
     * Find unpublished events, ordered by creation time.
     * Limits to prevent processing too many at once.
     */
    @Query("""
        SELECT e FROM OutboxEvent e
        WHERE e.published = false
        ORDER BY e.createdAt ASC
    """)
    fun findUnpublishedEvents(limit: Int = 100): List<OutboxEvent>

    /**
     * Find published events older than retention period for cleanup.
     */
    fun findByPublishedTrueAndPublishedAtBefore(before: Instant): List<OutboxEvent>

    /**
     * Delete old published events (cleanup job)
     */
    @Modifying
    @Query("DELETE FROM OutboxEvent e WHERE e.published = true AND e.publishedAt < :before")
    fun deletePublishedEventsBefore(before: Instant): Int

    /**
     * Count unpublished events for monitoring
     */
    fun countByPublishedFalse(): Long
}
