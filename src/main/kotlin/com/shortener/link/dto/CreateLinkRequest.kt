package com.shortener.link.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

data class CreateLinkRequest(
    @field:NotBlank(message = "URL is required")
    @field:Size(max = 2048, message = "URL must be at most 2048 characters")
    @field:Pattern(
        regexp = "^https?://.+",
        message = "URL must start with http:// or https://"
    )
    val url: String,

    /** Optional custom alias. Alphanumeric, dash and underscore, 3-32 chars. */
    @field:Pattern(
        regexp = "^[A-Za-z0-9_-]{3,32}$",
        message = "Alias must be 3-32 chars: letters, digits, '-' or '_'"
    )
    val alias: String? = null
)
