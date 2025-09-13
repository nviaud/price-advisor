package com.nviaud.pricing.api.controllers

import com.nviaud.pricing.api.models.QuotationRequest
import com.nviaud.pricing.api.models.QuotationResponse
import org.springframework.http.ResponseEntity
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import com.nviaud.pricing.api.assemblers.QuotationResponseAssembler
import com.nviaud.pricing.services.QuotationService


@RestController
@RequestMapping("/quotations")
@Suppress("unused")
class QuotationController (
    private val quotationService: QuotationService,
    private val quotationResponseAssembler: QuotationResponseAssembler
) {

    @PostMapping()
    fun submitQuotation(
        @RequestBody quotationRequest: QuotationRequest
    ): ResponseEntity<QuotationResponse> {
        quotationService.submitQuotation(
            quotationRequest.content,
        )
        return ResponseEntity.status(HttpStatus.ACCEPTED).build()
    }

}