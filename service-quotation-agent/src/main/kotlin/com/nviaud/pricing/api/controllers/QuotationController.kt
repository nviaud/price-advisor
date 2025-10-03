package com.nviaud.pricing.api.controllers

import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import com.nviaud.pricing.api.assemblers.QuotationResponseAssembler
import com.nviaud.pricing.entities.Quotation
import com.nviaud.pricing.services.QuotationService
import com.nviaud.pricing.services.QuotationStreamService
import org.springframework.http.MediaType
import reactor.core.publisher.Flux
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.util.MimeTypeUtils
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.multipart.MultipartFile
import java.security.Principal
import java.time.ZonedDateTime


@RestController
@RequestMapping("/quotations")
@Suppress("unused")
class QuotationController (
    private val quotationService: QuotationService,
    private val quotationStreamService: QuotationStreamService,
    private val quotationResponseAssembler: QuotationResponseAssembler
) {

    @PostMapping(
        consumes = [MediaType.MULTIPART_FORM_DATA_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun submitQuotation(
        principal: Principal?,
        @RequestPart("file") file: MultipartFile
    ): Quotation {
        val contentType = file.contentType
        if (contentType.isNullOrBlank()) {
            throw IllegalArgumentException("Bad params: file content type is missing or empty")
        }
        val mimeType = MimeTypeUtils.parseMimeType(contentType)
        val resource = file.resource
        val now = ZonedDateTime.now()
        return quotationService.submitQuotation(principal, mimeType, resource, now)
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasPermission(#id, 'Quotation', 'read')")
    fun getQuotation(@PathVariable id: Long): Quotation {
        return quotationService.getQuotation(id)
    }

    @GetMapping("/{id}/stream", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    @PreAuthorize("hasPermission(#id, 'Quotation', 'read')")
    fun streamQuotation(@PathVariable id: Long): Flux<Quotation> {
        return quotationStreamService.streamQuotationUpdates(id)
    }

    @PostMapping("/{id}/validate")
    @PreAuthorize("hasPermission(#id, 'Quotation', 'write')")
    fun validateQuotation(
        principal: Principal?,
        @PathVariable id: Long
    ): Quotation{
        val now = ZonedDateTime.now()
        return quotationService.validateQuotation(id, now)
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasPermission(#id, 'Quotation', 'write')")
    fun rejectQuotation(
        principal: Principal?,
        @PathVariable id: Long
    ): Quotation{
        val now = ZonedDateTime.now()
        return quotationService.rejectQuotation(id, now)
    }

}