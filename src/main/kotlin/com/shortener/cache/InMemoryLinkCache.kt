package com.shortener.cache

import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap

@Component
@Profile("test")
class InMemoryLinkCache : LinkCache {
    private val store = ConcurrentHashMap<String, String>()

    override fun get(code: String): String? = store[code]
    override fun put(code: String, url: String) { store[code] = url }
    override fun evict(code: String) { store.remove(code) }
}
