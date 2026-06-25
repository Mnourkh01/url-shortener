package com.shortener.link

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class LinkFlowIntegrationTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    private fun createBody(url: String, alias: String? = null): String {
        val map = if (alias != null) mapOf("url" to url, "alias" to alias) else mapOf("url" to url)
        return objectMapper.writeValueAsString(map)
    }

    private fun create(url: String, alias: String? = null): JsonNode {
        val body = mockMvc.post("/api/v1/links") {
            contentType = MediaType.APPLICATION_JSON
            content = createBody(url, alias)
        }.andReturn().response.contentAsString
        return objectMapper.readTree(body)
    }

    @Test
    fun `create returns 201 with a short url`() {
        mockMvc.post("/api/v1/links") {
            contentType = MediaType.APPLICATION_JSON
            content = createBody("https://example.com/page")
        }.andExpect { status { isCreated() } }
            .andExpect { jsonPath("$.code") { exists() } }
            .andExpect { jsonPath("$.shortUrl") { exists() } }
            .andExpect { jsonPath("$.originalUrl") { value("https://example.com/page") } }
    }

    @Test
    fun `create honors a custom alias and rejects a duplicate`() {
        create("https://example.com", alias = "my-alias")

        mockMvc.post("/api/v1/links") {
            contentType = MediaType.APPLICATION_JSON
            content = createBody("https://example.com", alias = "my-alias")
        }.andExpect { status { isConflict() } }
    }

    @Test
    fun `invalid url is rejected with 422`() {
        mockMvc.post("/api/v1/links") {
            contentType = MediaType.APPLICATION_JSON
            content = createBody("not-a-url")
        }.andExpect { status { isUnprocessableEntity() } }
            .andExpect { jsonPath("$.code") { value("VALIDATION_ERROR") } }
            .andExpect { jsonPath("$.details") { isNotEmpty() } }
    }

    @Test
    fun `visiting a short code redirects and records a click`() {
        val code = create("https://example.com/target", alias = "go-target").get("code").asText()

        mockMvc.get("/$code")
            .andExpect { status { isFound() } }
            .andExpect { header { string("Location", "https://example.com/target") } }

        mockMvc.get("/api/v1/links/$code")
            .andExpect { status { isOk() } }
            .andExpect { jsonPath("$.totalClicks") { value(1) } }
            .andExpect { jsonPath("$.liveClicks") { value(1) } }
    }

    @Test
    fun `unknown code returns 404`() {
        mockMvc.get("/doesnotexist")
            .andExpect { status { isNotFound() } }
    }

    @Test
    fun `delete removes the link`() {
        val code = create("https://example.com/del", alias = "del-me").get("code").asText()

        mockMvc.delete("/api/v1/links/$code")
            .andExpect { status { isNoContent() } }

        mockMvc.get("/api/v1/links/$code")
            .andExpect { status { isNotFound() } }
    }
}
