package com.shortener.link.dto

import java.time.OffsetDateTime

data class LinkResponse(
    val code: String,
    val shortUrl: String,
    val originalUrl: String,
    val createdAt: OffsetDateTime
)
