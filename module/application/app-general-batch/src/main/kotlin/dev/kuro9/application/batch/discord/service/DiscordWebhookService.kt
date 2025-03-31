package dev.kuro9.application.batch.discord.service

import dev.kuro9.application.batch.discord.DiscordWebhookPayload
import dev.kuro9.multiplatform.common.network.httpClient
import dev.kuro9.multiplatform.common.serialization.minifyJson
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.resources.post
import io.ktor.client.request.setBody
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import org.springframework.stereotype.Service

@Service
class DiscordWebhookService {
    private val httpClient = httpClient {
        install(ContentNegotiation) {
            json(minifyJson)
        }
        expectSuccess = true

        defaultRequest {
            contentType(ContentType.Application.Json)
        }
    }

    suspend fun sendWebhook(url: String, payload: DiscordWebhookPayload) {
        httpClient.post(url) { setBody(payload) }
    }
}