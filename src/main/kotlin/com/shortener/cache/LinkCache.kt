package com.shortener.cache

/**
 * Cache-aside store for resolved `code -> originalUrl` lookups on the redirect
 * hot path. Backed by Redis in running environments and by an in-memory map in
 * tests, so the test suite needs no Redis.
 */
interface LinkCache {
    fun get(code: String): String?
    fun put(code: String, url: String)
    fun evict(code: String)
}
