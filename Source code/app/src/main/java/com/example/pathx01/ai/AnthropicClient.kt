package com.example.pathx01.ai

import com.example.pathx01.BuildConfig
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor

class AnthropicClient(
    private val model: String = "claude-3-7-sonnet-2025-06-20",
    private val maxTokens: Int = 1024,
) {
    private val json = Json { ignoreUnknownKeys = true }

    private val client: OkHttpClient by lazy {
        val logging = HttpLoggingInterceptor()
        logging.level = HttpLoggingInterceptor.Level.BASIC
        OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()
    }

    suspend fun sendMessages(messages: List<ChatMessage>): String {
        val apiKey = BuildConfig.ANTHROPIC_API_KEY
        require(apiKey.isNotEmpty()) { "Anthropic API key is missing" }

        val body = MessageRequest(
            model = model,
            maxTokens = maxTokens,
            messages = messages.map { it.toAnthropic() }
        )
        val contentType = "application/json; charset=utf-8".toMediaType()
        val requestBody: RequestBody = json.encodeToString(body).toRequestBody(contentType)

        val request = Request.Builder()
            .url("https://api.anthropic.com/v1/messages")
            .post(requestBody)
            .addHeader("x-api-key", apiKey)
            .addHeader("anthropic-version", "2023-06-01")
            .addHeader("content-type", "application/json")
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                val err = response.body?.string() ?: response.message
                throw IllegalStateException("Anthropic error: ${response.code} ${err}")
            }
            val bodyStr = response.body?.string() ?: ""
            val parsed = json.decodeFromString<MessageResponse>(bodyStr)
            val first = parsed.content.firstOrNull()
            return first?.text ?: ""
        }
    }
}

@Serializable
data class ChatMessage(
    val role: String, // "user" or "assistant"
    val content: String
) {
    fun toAnthropic(): AnthropicMessage = AnthropicMessage(
        role = role,
        content = listOf(AnthropicTextBlock(text = content))
    )
}

@Serializable
private data class MessageRequest(
    val model: String,
    @SerialName("max_tokens") val maxTokens: Int,
    val messages: List<AnthropicMessage>
)

@Serializable
data class AnthropicMessage(
    val role: String,
    val content: List<AnthropicTextBlock>
)

@Serializable
data class AnthropicTextBlock(
    val type: String = "text",
    val text: String
)

@Serializable
private data class MessageResponse(
    val id: String? = null,
    val type: String? = null,
    val role: String? = null,
    val content: List<AnthropicResponseBlock>
)

@Serializable
private data class AnthropicResponseBlock(
    val type: String,
    val text: String = ""
)
