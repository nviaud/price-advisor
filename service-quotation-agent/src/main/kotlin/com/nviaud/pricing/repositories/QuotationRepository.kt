package com.nviaud.pricing.repositories

import com.nviaud.pricing.entities.Quotation
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface QuotationRepository : JpaRepository<Quotation, Long>
