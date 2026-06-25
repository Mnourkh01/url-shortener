package com.shortener.link.dto

import java.time.LocalDate
import java.time.OffsetDateTime

data class LinkStatsResponse(
    val code: String,
    val shortUrl: String,
    val originalUrl: String,
    val createdAt: OffsetDateTime,
    /** Authoritative total from Postgres. */
    val totalClicks: Long,
    /** Live counter from Redis (may briefly lead the DB total). */
    val liveClicks: Long,
    /** Click counts per day for the last 7 days, most recent first. */
    val last7Days: List<DailyClicks>
)

data class DailyClicks(
    val date: LocalDate,
    val clicks: Long
)
