package com.shortener.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app")
data class AppProperties(
    /** Public base URL used to build the short links returned to clients. */
    val baseUrl: String = "http://localhost:8080",
    /** TTL (seconds) for cached code -> URL entries in Redis. */
    val cacheTtlSeconds: Long = 3600
)
