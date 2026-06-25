package com.shortener.cache

/**
 * Fast, atomic live click counter per code. Redis `INCR` in running
 * environments; an in-memory counter in tests.
 */
interface ClickCounter {
    fun increment(code: String): Long
    fun get(code: String): Long
}
