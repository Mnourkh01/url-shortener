package com.shortener.link.repository

import com.shortener.link.model.Click
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.OffsetDateTime
import java.util.UUID

@Repository
interface ClickRepository : JpaRepository<Click, UUID> {
    fun countByLinkId(linkId: UUID): Long
    fun findByLinkIdAndClickedAtAfter(linkId: UUID, cutoff: OffsetDateTime): List<Click>
}
