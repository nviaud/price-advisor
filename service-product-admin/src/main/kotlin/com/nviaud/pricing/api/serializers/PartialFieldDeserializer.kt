package com.nviaud.pricing.api.serializers

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.nviaud.pricing.services.dto.PartialField

class PartialFieldDeserializer : JsonDeserializer<PartialField<Any>>() {
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): PartialField<Any> {
        val value = p.readValueAs(Any::class.java)
        return PartialField.of(value)
    }

    override fun getNullValue(ctxt: DeserializationContext?): PartialField<Any> {
        return PartialField.nullValue()
    }
}
