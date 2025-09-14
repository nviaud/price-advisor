package com.nviaud.pricing.api.assemblers

import com.nviaud.pricing.api.resources.PaginatedQuotationResponse
import com.nviaud.pricing.api.resources.QuotationResponse
import com.nviaud.pricing.entities.Quotation
import org.springframework.data.domain.Page
import org.springframework.stereotype.Service

@Service
class QuotationResponseAssembler {

    fun toResponse(quotation: Quotation): QuotationResponse = QuotationResponse(
        id = quotation.id,
    )

    fun toResponseList(quotations: List<Quotation>): List<QuotationResponse> =
        quotations.map { toResponse(it) }

    fun toResponsePage(quotations: Page<Quotation>) =
        PaginatedQuotationResponse(
            content = toResponseList(quotations.content),
            page = quotations.number,
            size = quotations.size,
            totalElements = quotations.totalElements,
            totalPages = quotations.totalPages
        )
}