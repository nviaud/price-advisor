package com.nviaud.pricing.api.resources

interface Response

interface ResponseElement : Response {
    val id: Long
}

interface PaginatedResponse<T> : Response where T : ResponseElement{
    val content: List<T>
    val page: Int
    val size: Int
    val totalElements: Long
    val totalPages: Int
}