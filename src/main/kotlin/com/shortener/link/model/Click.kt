package com.shortener.link.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table
import java.time.OffsetDateTime
import java.util.UUID

@Entity
@Table(
    name = "clicks",
    indexes = [Index(name = "idx_clicks_link_id", columnList = "link_id")]
)
class Click(

    @Column(name = "link_id", nullable = false, updatable = false)
    var linkId: UUID,

    @Column(name = "referer", length = 512)
    var referer: String? = null,

    @Column(name = "user_agent", length = 512)
    var userAgent: String? = null,

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null,

    @Column(name = "clicked_at", nullable = false, updatable = false)
    var clickedAt: OffsetDateTime = OffsetDateTime.now()
)
