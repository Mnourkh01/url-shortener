package com.shortener.link.service

import com.shortener.cache.ClickCounter
import com.shortener.link.model.Click
import com.shortener.link.repository.ClickRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class ClickService(
    private val clickRepository: ClickRepository,
    private val clickCounter: ClickCounter
) {
    /**
     * Record a click: bump the fast Redis counter and persist a row for
     * time-series analytics. Long headers are truncated to the column width.
     */
    @Transactional
    fun record(code: String, linkId: UUID, referer: String?, userAgent: String?) {
        clickCounter.increment(code)
        clickRepository.save(
            Click(
                linkId = linkId,
                referer = referer?.take(512),
                userAgent = userAgent?.take(512)
            )
        )
    }
}
