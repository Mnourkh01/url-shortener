package com.shortener.link.service

import com.shortener.cache.ClickCounter
import com.shortener.cache.LinkCache
import com.shortener.common.error.exceptions.ConflictException
import com.shortener.common.error.exceptions.NotFoundException
import com.shortener.config.AppProperties
import com.shortener.link.dto.CreateLinkRequest
import com.shortener.link.dto.DailyClicks
import com.shortener.link.dto.LinkResponse
import com.shortener.link.dto.LinkStatsResponse
import com.shortener.link.model.Link
import com.shortener.link.repository.ClickRepository
import com.shortener.link.repository.LinkRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime
import java.util.UUID

/** A code resolved to its target, carried on the redirect hot path. */
data class ResolvedLink(val id: UUID, val originalUrl: String)

@Service
@Transactional(readOnly = true)
class LinkService(
    private val linkRepository: LinkRepository,
    private val clickRepository: ClickRepository,
    private val cache: LinkCache,
    private val clickCounter: ClickCounter,
    private val codeGenerator: CodeGenerator,
    private val appProperties: AppProperties
) {
    private companion object {
        const val MAX_CODE_ATTEMPTS = 5
    }

    @Transactional
    fun create(request: CreateLinkRequest): LinkResponse {
        val url = request.url.trim()

        val code = request.alias?.let { alias ->
            if (linkRepository.existsByCode(alias)) {
                throw ConflictException("Alias '$alias' is already taken")
            }
            alias
        } ?: generateUniqueCode()

        val saved = linkRepository.save(Link(code = code, originalUrl = url))
        cache.put(code, encode(saved.id!!, url))

        return saved.toResponse()
    }

    /** Resolve a code to its target for redirecting; cache-aside via Redis. */
    fun resolve(code: String): ResolvedLink {
        cache.get(code)?.let { return decode(it) }

        val link = linkRepository.findByCode(code)
            ?: throw NotFoundException("Short link not found")

        cache.put(code, encode(link.id!!, link.originalUrl))
        return ResolvedLink(link.id!!, link.originalUrl)
    }

    fun stats(code: String): LinkStatsResponse {
        val link = linkRepository.findByCode(code)
            ?: throw NotFoundException("Short link not found")
        val id = link.id!!

        val cutoff = OffsetDateTime.now().minusDays(7).toLocalDate().atStartOfDay()
            .atOffset(OffsetDateTime.now().offset)

        val recent = clickRepository.findByLinkIdAndClickedAtAfter(id, cutoff)
        val byDay = recent.groupBy { it.clickedAt.toLocalDate() }
            .map { (date, clicks) -> DailyClicks(date, clicks.size.toLong()) }
            .sortedByDescending { it.date }

        return LinkStatsResponse(
            code = link.code,
            shortUrl = shortUrl(link.code),
            originalUrl = link.originalUrl,
            createdAt = link.createdAt,
            totalClicks = clickRepository.countByLinkId(id),
            liveClicks = clickCounter.get(code),
            last7Days = byDay
        )
    }

    @Transactional
    fun delete(code: String) {
        val link = linkRepository.findByCode(code)
            ?: throw NotFoundException("Short link not found")
        linkRepository.delete(link)
        cache.evict(code)
    }

    private fun generateUniqueCode(): String {
        repeat(MAX_CODE_ATTEMPTS) {
            val candidate = codeGenerator.generate()
            if (!linkRepository.existsByCode(candidate)) return candidate
        }
        throw IllegalStateException("Could not generate a unique code after $MAX_CODE_ATTEMPTS attempts")
    }

    private fun shortUrl(code: String) = "${appProperties.baseUrl.trimEnd('/')}/$code"

    private fun encode(id: UUID, url: String) = "$id\n$url"

    private fun decode(value: String): ResolvedLink {
        val i = value.indexOf('\n')
        return ResolvedLink(UUID.fromString(value.substring(0, i)), value.substring(i + 1))
    }

    private fun Link.toResponse() =
        LinkResponse(
            code = code,
            shortUrl = shortUrl(code),
            originalUrl = originalUrl,
            createdAt = createdAt
        )
}
