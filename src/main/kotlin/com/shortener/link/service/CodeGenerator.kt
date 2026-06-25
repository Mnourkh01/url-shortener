package com.shortener.link.service

import org.springframework.stereotype.Component
import java.security.SecureRandom

/** Generates random base62 short codes. */
@Component
class CodeGenerator {

    private companion object {
        const val ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
        const val DEFAULT_LENGTH = 7
    }

    private val random = SecureRandom()

    fun generate(length: Int = DEFAULT_LENGTH): String {
        require(length > 0) { "length must be positive" }
        val sb = StringBuilder(length)
        repeat(length) {
            sb.append(ALPHABET[random.nextInt(ALPHABET.length)])
        }
        return sb.toString()
    }
}
