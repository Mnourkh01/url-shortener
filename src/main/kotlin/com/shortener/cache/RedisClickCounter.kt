package com.shortener.cache

import org.springframework.context.annotation.Profile
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Component

@Component
@Profile("!test")
class RedisClickCounter(
    private val redis: StringRedisTemplate
) : ClickCounter {

    private fun key(code: String) = "clicks:$code"

    override fun increment(code: String): Long =
        redis.opsForValue().increment(key(code)) ?: 0

    override fun get(code: String): Long =
        redis.opsForValue().get(key(code))?.toLongOrNull() ?: 0
}
