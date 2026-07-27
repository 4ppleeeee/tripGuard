package com.tencent.kmm.demo

import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import org.json.JSONObject

internal object TripGuardApiClient {
    const val DEFAULT_BASE_URL = "https://trip.aatroxli.site:1221"

    data class CollectResult(
        val saved: Boolean,
        val summary: String,
        val destination: String? = null,
        val category: String? = null,
        val normalizedTags: List<String> = emptyList(),
    )

    fun collectSource(
        title: String,
        bodyText: String,
        url: String,
        sourcePlatform: String,
        baseUrl: String = DEFAULT_BASE_URL,
    ): CollectResult = parseCollectResponse(
        postJson(
            baseUrl = baseUrl,
            path = "/sources/collect",
            body = buildCollectPayload(title, bodyText, url, sourcePlatform),
        ),
    )

    fun recommend(
        message: String,
        baseUrl: String = DEFAULT_BASE_URL,
    ): String = parseRecommendationResponse(
        postJson(
            baseUrl = baseUrl,
            path = "/chat/recommend",
            body = JSONObject().put("message", message),
        ),
    )

    internal fun buildCollectPayload(
        title: String,
        bodyText: String,
        url: String,
        sourcePlatform: String,
    ): JSONObject = JSONObject()
        .put("input_type", "text")
        .put("title", title)
        .put("body_text", bodyText)
        .put("url", url)
        .put("source_platform", sourcePlatform)

    internal fun parseCollectResponse(response: JSONObject): CollectResult {
        val saved = response.optBoolean("saved")
        if (!saved) {
            return CollectResult(
                saved = false,
                summary = response.optString("reason").ifBlank { "AI 未收录这条内容" },
            )
        }
        val source = response.optJSONObject("source")
        val destination = source?.optString("destination").orEmpty()
        val category = source?.optString("category").orEmpty()
        val normalizedTags = source?.optJSONArray("normalized_tags")?.let { tags ->
            buildList {
                for (index in 0 until tags.length()) {
                    tags.optString(index).takeIf { it.isNotBlank() }?.let(::add)
                }
            }
        }.orEmpty()
        val summary = listOf(destination, category)
            .filter { it.isNotBlank() }
            .joinToString(" · ")
            .ifBlank { "AI 已收录这条旅行资料" }
        return CollectResult(
            saved = true,
            summary = summary,
            destination = destination.takeIf { it.isNotBlank() },
            category = category.takeIf { it.isNotBlank() },
            normalizedTags = normalizedTags,
        )
    }

    internal fun parseRecommendationResponse(response: JSONObject): String =
        response.optString("answer").ifBlank { "AI 暂时没有生成推荐" }

    private fun postJson(baseUrl: String, path: String, body: JSONObject): JSONObject {
        val connection = (URL(baseUrl.trimEnd('/') + path).openConnection() as HttpURLConnection).apply {
            connectTimeout = CONNECT_TIMEOUT_MS
            readTimeout = READ_TIMEOUT_MS
            requestMethod = "POST"
            doOutput = true
            setRequestProperty("Accept", "application/json")
            setRequestProperty("Content-Type", "application/json; charset=UTF-8")
        }
        try {
            connection.outputStream.bufferedWriter(Charsets.UTF_8).use { writer ->
                writer.write(body.toString())
            }
            val status = connection.responseCode
            val stream = if (status in 200..299) connection.inputStream else connection.errorStream
            val responseText = stream?.bufferedReader(Charsets.UTF_8).use { it?.readText().orEmpty() }
            if (status !in 200..299) {
                throw IOException("TripGuard API HTTP $status: ${responseText.take(240)}")
            }
            return JSONObject(responseText)
        } finally {
            connection.disconnect()
        }
    }

    private const val CONNECT_TIMEOUT_MS = 10_000
    private const val READ_TIMEOUT_MS = 120_000
}
