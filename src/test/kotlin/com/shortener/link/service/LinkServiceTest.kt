package com.shortener.link.service

import com.shortener.cache.ClickCounter
import com.shortener.cache.LinkCache
import com.shortener.common.error.exceptions.ConflictException
import com.shortener.common.error.exceptions.NotFoundException
import com.shortener.config.AppProperties
import com.shortener.link.dto.CreateLinkRequest
import com.shortener.link.model.Link
import com.shortener.link.repository.ClickRepository
import com.shortener.link.repository.LinkRepository
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.UUID

class LinkServiceTest {

    private val linkRepository = mock<LinkRepository>()
    private val clickRepository = mock<ClickRepository>()
    private val cache = mock<LinkCache>()
    private val clickCounter = mock<ClickCounter>()
    private val codeGenerator = mock<CodeGenerator>()
    private val appProperties = AppProperties(baseUrl = "http://sho.rt", cacheTtlSeconds = 3600)

    private val service = LinkService(
        linkRepository, clickRepository, cache, clickCounter, codeGenerator, appProperties
    )

    private fun savedLink(code: String, url: String) =
        Link(code = code, originalUrl = url, id = UUID.randomUUID())

    @Test
    fun `create with a free alias uses it and caches the mapping`() {
        whenever(linkRepository.existsByCode("promo")).doReturn(false)
        whenever(linkRepository.save(any<Link>())).doReturn(savedLink("promo", "https://example.com"))

        val res = service.create(CreateLinkRequest(url = "https://example.com", alias = "promo"))

        assertThat(res.code).isEqualTo("promo")
        assertThat(res.shortUrl).isEqualTo("http://sho.rt/promo")
        verify(cache).put(eqCode("promo"), any())
    }

    @Test
    fun `create with a taken alias is rejected`() {
        whenever(linkRepository.existsByCode("taken")).doReturn(true)

        assertThatThrownBy {
            service.create(CreateLinkRequest(url = "https://example.com", alias = "taken"))
        }.isInstanceOf(ConflictException::class.java)

        verify(linkRepository, never()).save(any())
    }

    @Test
    fun `create without alias generates a unique code`() {
        whenever(codeGenerator.generate()).doReturn("abc1234")
        whenever(linkRepository.existsByCode("abc1234")).doReturn(false)
        whenever(linkRepository.save(any<Link>())).doReturn(savedLink("abc1234", "https://example.com"))

        val res = service.create(CreateLinkRequest(url = "https://example.com"))

        assertThat(res.code).isEqualTo("abc1234")
    }

    @Test
    fun `resolve returns from cache without hitting the database`() {
        val id = UUID.randomUUID()
        whenever(cache.get("hit")).doReturn("$id\nhttps://cached.example")

        val resolved = service.resolve("hit")

        assertThat(resolved.id).isEqualTo(id)
        assertThat(resolved.originalUrl).isEqualTo("https://cached.example")
        verify(linkRepository, never()).findByCode(any())
    }

    @Test
    fun `resolve falls back to the database and populates the cache on a miss`() {
        whenever(cache.get("miss")).doReturn(null)
        whenever(linkRepository.findByCode("miss")).doReturn(savedLink("miss", "https://db.example"))

        val resolved = service.resolve("miss")

        assertThat(resolved.originalUrl).isEqualTo("https://db.example")
        verify(cache).put(eqCode("miss"), any())
    }

    @Test
    fun `resolve throws when the code is unknown`() {
        whenever(cache.get("nope")).doReturn(null)
        whenever(linkRepository.findByCode("nope")).doReturn(null)

        assertThatThrownBy { service.resolve("nope") }
            .isInstanceOf(NotFoundException::class.java)
    }

    @Test
    fun `delete throws when the code is unknown`() {
        whenever(linkRepository.findByCode("ghost")).doReturn(null)

        assertThatThrownBy { service.delete("ghost") }
            .isInstanceOf(NotFoundException::class.java)
    }

    // Mockito-kotlin matcher helper for the String code argument.
    private fun eqCode(value: String): String = org.mockito.kotlin.eq(value)
}
