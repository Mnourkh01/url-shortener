package com.shortener.link.controller

import com.shortener.link.dto.CreateLinkRequest
import com.shortener.link.dto.LinkResponse
import com.shortener.link.dto.LinkStatsResponse
import com.shortener.link.service.LinkService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/links")
class LinkController(
    private val linkService: LinkService
) {

    @PostMapping(consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun create(@Valid @RequestBody request: CreateLinkRequest): ResponseEntity<LinkResponse> =
        ResponseEntity.status(HttpStatus.CREATED).body(linkService.create(request))

    @GetMapping("/{code}")
    fun stats(@PathVariable code: String): ResponseEntity<LinkStatsResponse> =
        ResponseEntity.ok(linkService.stats(code))

    @DeleteMapping("/{code}")
    fun delete(@PathVariable code: String): ResponseEntity<Void> {
        linkService.delete(code)
        return ResponseEntity.noContent().build()
    }
}
