package com.shortener.cache

import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

@Component
@Profile("test")
class InMemoryClickCounter : ClickCounter {
    private val counters = ConcurrentHashMap<String, AtomicLong>()

    override fun increment(code: String): Long =
        counters.computeIfAbsent(code) { AtomicLong() }.incrementAndGet()

    override fun get(code: String): Long =
        counters[code]?.get() ?: 0
}
