package com.shortener.ratelimit

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(
    properties = [
        "app.rate-limit.enabled=true",
        "app.rate-limit.capacity=3",
        "app.rate-limit.refill-tokens=3",
        "app.rate-limit.refill-period-seconds=3600"
    ]
)
class RateLimitIntegrationTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Test
    fun `link creation is throttled after the burst is exhausted`() {
        val body = objectMapper.writeValueAsString(mapOf("url" to "https://example.com"))

        repeat(3) {
            mockMvc.post("/api/v1/links") {
                contentType = MediaType.APPLICATION_JSON
                content = body
            }.andExpect { status { isCreated() } }
        }

        mockMvc.post("/api/v1/links") {
            contentType = MediaType.APPLICATION_JSON
            content = body
        }.andExpect { status { isTooManyRequests() } }
            .andExpect { header { exists("Retry-After") } }
            .andExpect { jsonPath("$.code") { value("RATE_LIMITED") } }
    }
}
