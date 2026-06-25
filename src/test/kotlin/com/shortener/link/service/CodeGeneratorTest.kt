package com.shortener.link.service

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

class CodeGeneratorTest {

    private val generator = CodeGenerator()

    @Test
    fun `generates a base62 code of the requested length`() {
        val code = generator.generate(7)
        assertThat(code).hasSize(7)
        assertThat(code).matches("[A-Za-z0-9]+")
    }

    @Test
    fun `generates different codes across calls`() {
        val codes = (1..50).map { generator.generate() }.toSet()
        // Collisions over 50 random 7-char base62 codes are astronomically unlikely.
        assertThat(codes).hasSize(50)
    }

    @Test
    fun `rejects a non-positive length`() {
        assertThatThrownBy { generator.generate(0) }
            .isInstanceOf(IllegalArgumentException::class.java)
    }
}
