package com.nviaud.pricing.api.assemblers

import com.nviaud.pricing.api.resources.PaginatedResponse
import com.nviaud.pricing.api.resources.ResponseElement
import org.springframework.data.domain.Page

abstract class ResponseAssembler<T, R, P> where T : Any, R : ResponseElement, P : PaginatedResponse<R> {
    abstract fun toResponse(t: T): R
    abstract fun instantiatePaginatedResponse(
        content: List<R>,
        number: Int,
        size: Int,
        totalElements: Long,
        totalPages: Int
    ): P

    fun toResponseList(t: List<T>) =
        t.map { toResponse(it) }

    fun toResponsePage(t: Page<T>): P =
        instantiatePaginatedResponse(
            toResponseList(t.content),
            t.number,
            t.size,
            t.totalElements,
            t.totalPages
        )
}