package com.shortener.link.repository

import com.shortener.link.model.Link
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface LinkRepository : JpaRepository<Link, UUID> {
    fun findByCode(code: String): Link?
    fun existsByCode(code: String): Boolean
}
