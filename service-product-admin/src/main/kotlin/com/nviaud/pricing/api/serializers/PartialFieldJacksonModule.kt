package com.nviaud.pricing.api.serializers

import com.fasterxml.jackson.databind.module.SimpleModule
import com.nviaud.pricing.services.PartialField

class PartialFieldJacksonModule : SimpleModule() {
    init {
        addSerializer(PartialField::class.java, PartialFieldSerializer())
        addDeserializer(PartialField::class.java, PartialFieldDeserializer())
    }
}
