package com.shortener.config

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * Per-client rate limit applied to link creation. Defaults: a burst of
 * [capacity] requests, refilling [refillTokens] every [refillPeriodSeconds].
 */
@ConfigurationProperties(prefix = "app.rate-limit")
data class RateLimitProperties(
    val enabled: Boolean = true,
    val capacity: Long = 20,
    val refillTokens: Long = 20,
    val refillPeriodSeconds: Long = 60
)
