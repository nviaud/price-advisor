package com.nviaud.pricing.api

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus

data class ErrorResponse(val error: String)

@ControllerAdvice
@Suppress("unused")
class GlobalExceptionHandler {

    @ExceptionHandler(NoSuchElementException::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun handleProductNotFound(ex: NoSuchElementException): ResponseEntity<Void> {
        return ResponseEntity.notFound().build()
    }

}