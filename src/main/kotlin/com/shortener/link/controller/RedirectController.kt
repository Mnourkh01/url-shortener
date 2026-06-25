package com.shortener.link.controller

import com.shortener.link.service.ClickService
import com.shortener.link.service.LinkService
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import java.net.URI

@RestController
class RedirectController(
    private val linkService: LinkService,
    private val clickService: ClickService
) {

    // Redirect hot path. The code is constrained to the alias charset so this
    // mapping never captures paths like /swagger-ui.html or /actuator (which
    // contain characters outside the charset or have extra segments).
    @GetMapping("/{code:[A-Za-z0-9_-]+}")
    fun redirect(
        @PathVariable code: String,
        request: HttpServletRequest
    ): ResponseEntity<Void> {
        val resolved = linkService.resolve(code)

        clickService.record(
            code = code,
            linkId = resolved.id,
            referer = request.getHeader("Referer"),
            userAgent = request.getHeader("User-Agent")
        )

        return ResponseEntity
            .status(HttpStatus.FOUND)
            .location(URI.create(resolved.originalUrl))
            .build()
    }
}
