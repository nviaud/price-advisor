package com.nviaud.pricing.repositories

import com.nviaud.pricing.entities.Quotation
import org.springframework.data.jpa.repository.JpaRepository

interface QuotationRepository: JpaRepository<Quotation, Long> {

}