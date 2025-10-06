package com.nviaud.pricing.repositories

import com.nviaud.pricing.entities.ProcessedEvent
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ProcessedEventRepository : JpaRepository<ProcessedEvent, Long> {

    /**
     * Check if an event has already been processed
     */
    fun existsByEventId(eventId: String): Boolean
}
