package com.shortener.common.error

data class FieldErrorResponse(
    val field: String,
    val message: String
)
