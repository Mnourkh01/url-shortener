package com.shortener.ratelimit

import com.shortener.common.error.ErrorCode
import com.shortener.common.error.ErrorResponse
import com.shortener.config.RateLimitProperties
import com.fasterxml.jackson.databind.ObjectMapper
import com.github.benmanes.caffeine.cache.Caffeine
import io.github.bucket4j.Bucket
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.time.Duration
import java.time.OffsetDateTime
import java.util.concurrent.TimeUnit

/**
 * Per-client token-bucket rate limit on link creation. Buckets live in a
 * Caffeine cache keyed by client IP with idle eviction, so the map cannot grow
 * unbounded. Exhaustion returns the standard error envelope with HTTP 429.
 */
@Component
class RateLimitFilter(
    private val properties: RateLimitProperties,
    private val objectMapper: ObjectMapper
) : OncePerRequestFilter() {

    private companion object {
        const val CREATE_PATH = "/api/v1/links"
    }

    private val buckets = Caffeine.newBuilder()
        .expireAfterAccess(Duration.ofMinutes(10))
        .maximumSize(100_000)
        .build<String, Bucket> { newBucket() }

    private fun newBucket(): Bucket =
        Bucket.builder()
            .addLimit { limit ->
                limit.capacity(properties.capacity)
                    .refillGreedy(properties.refillTokens, Duration.ofSeconds(properties.refillPeriodSeconds))
            }
            .build()

    override fun shouldNotFilter(request: HttpServletRequest): Boolean {
        if (!properties.enabled) return true
        // Only throttle link creation (POST /api/v1/links).
        return !(request.method == HttpMethod.POST.name() && request.requestURI == CREATE_PATH)
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val bucket = buckets.get(clientIp(request))!!
        val probe = bucket.tryConsumeAndReturnRemaining(1)

        if (probe.isConsumed) {
            response.setHeader("X-RateLimit-Remaining", probe.remainingTokens.toString())
            filterChain.doFilter(request, response)
            return
        }

        val retryAfter = TimeUnit.NANOSECONDS.toSeconds(probe.nanosToWaitForRefill).coerceAtLeast(1)
        writeTooManyRequests(request, response, retryAfter)
    }

    private fun clientIp(request: HttpServletRequest): String {
        val forwarded = request.getHeader("X-Forwarded-For")
        if (!forwarded.isNullOrBlank()) return forwarded.split(",").first().trim()
        return request.remoteAddr ?: "unknown"
    }

    private fun writeTooManyRequests(
        request: HttpServletRequest,
        response: HttpServletResponse,
        retryAfterSeconds: Long
    ) {
        if (response.isCommitted) return
        val status = ErrorCode.RATE_LIMITED.httpStatus
        val body = ErrorResponse(
            timestamp = OffsetDateTime.now(),
            status = status.value(),
            error = status.reasonPhrase,
            code = ErrorCode.RATE_LIMITED.name,
            message = "Too many requests. Please retry later.",
            path = request.requestURI,
            details = null
        )
        response.status = status.value()
        response.setHeader("Retry-After", retryAfterSeconds.toString())
        response.contentType = MediaType.APPLICATION_JSON_VALUE
        response.characterEncoding = "UTF-8"
        objectMapper.writeValue(response.writer, body)
    }
}
