package com.shortener.common.error

import java.time.OffsetDateTime

data class ErrorResponse(
    val timestamp: OffsetDateTime,
    val status: Int,
    val error: String,
    val code: String,
    val message: String,
    val path: String,
    val details: List<FieldErrorResponse>? = null
)
