package com.nviaud.pricing.api.validation

import com.nviaud.pricing.services.PartialField
import jakarta.validation.valueextraction.ExtractedValue
import jakarta.validation.valueextraction.ValueExtractor

class PartialFieldValueExtractor : ValueExtractor<PartialField<@ExtractedValue Any>> {
    override fun extractValues(
        originalValue: PartialField<Any>?,
        receiver: ValueExtractor.ValueReceiver
    ) {
        if (originalValue != null && originalValue.isSet) {
            receiver.value(null, originalValue.value)
        }
    }
}
