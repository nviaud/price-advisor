package com.nviaud.pricing.services

class PartialField<T>(
    val value: T?,
    val isSet: Boolean = false
) {

    companion object {
        fun <T> of(value: T): PartialField<T> = PartialField(value, true)
        fun <T> unset(): PartialField<T> = PartialField(null, false)
        fun <T> nullValue(): PartialField<T> = PartialField(null, true)
    }

}