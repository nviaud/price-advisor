package com.nviaud.pricing.repositories

import com.nviaud.pricing.entities.Quotation
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface QuotationRepository : JpaRepository<Quotation, Long> {

    /**
     * Efficient existence check to verify if a quotation belongs to a user.
     * Does not fetch the full entity - translates to SQL: SELECT EXISTS(...)
     */
    fun existsByIdAndUserId(id: Long, userId: String): Boolean
}
