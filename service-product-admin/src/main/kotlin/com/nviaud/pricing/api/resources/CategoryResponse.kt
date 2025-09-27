package com.nviaud.pricing.api.resources

class CategoryResponse (
    override var id: Long,
    var name: String? = null
) : ResponseElement