package com.shortener.cache

import com.shortener.config.AppProperties
import org.springframework.context.annotation.Profile
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Component
import java.time.Duration

@Component
@Profile("!test")
class RedisLinkCache(
    private val redis: StringRedisTemplate,
    private val appProperties: AppProperties
) : LinkCache {

    private fun key(code: String) = "url:$code"

    override fun get(code: String): String? =
        redis.opsForValue().get(key(code))

    override fun put(code: String, url: String) {
        redis.opsForValue().set(key(code), url, Duration.ofSeconds(appProperties.cacheTtlSeconds))
    }

    override fun evict(code: String) {
        redis.delete(key(code))
    }
}
