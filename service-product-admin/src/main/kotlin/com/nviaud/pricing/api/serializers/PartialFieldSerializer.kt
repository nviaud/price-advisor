package com.nviaud.pricing.api.serializers

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import com.nviaud.pricing.services.PartialField

class PartialFieldSerializer : JsonSerializer<PartialField<*>>() {
    override fun serialize(
        value: PartialField<*>,
        gen: JsonGenerator,
        serializers: SerializerProvider
    ) {
        gen.writeObject(value.value)
    }
}
