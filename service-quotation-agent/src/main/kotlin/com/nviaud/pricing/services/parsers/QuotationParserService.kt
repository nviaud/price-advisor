package com.nviaud.pricing.services.parsers

import org.springframework.core.io.Resource
import org.springframework.util.MimeType

interface QuotationParserService {
    fun parseDataFromQuotation(mimeType: MimeType, input: Resource): QuotationData
}
