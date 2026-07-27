package com.tencent.kmm.demo

import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

internal enum class TravelCategory(val displayName: String) {
    FOOD("吃喝"),
    FUN("玩乐"),
    SIGHTSEEING("景点"),
    SHOPPING("购物"),
    STAY("住宿"),
    TRANSPORT("交通"),
    GUIDE("攻略"),
    OTHER("其他");

    companion object {
        fun fromModelValue(value: String?): TravelCategory {
            val text = value.orEmpty().trim().lowercase()
            return when {
                text.contains("吃") || text.contains("喝") || text.contains("food") -> FOOD
                text == "eat" || text == "drink" -> FOOD
                text.contains("玩") || text.contains("娱乐") || text.contains("fun") -> FUN
                text == "play" || text == "entertainment" -> FUN
                text.contains("景") || text.contains("citywalk") || text.contains("sight") -> SIGHTSEEING
                text.contains("购") || text.contains("买") || text.contains("shop") -> SHOPPING
                text.contains("住") || text.contains("酒店") || text.contains("民宿") || text.contains("stay") -> STAY
                text.contains("交通") || text.contains("机票") || text.contains("高铁") || text.contains("transport") -> TRANSPORT
                text.contains("攻略") || text.contains("路线") || text.contains("guide") -> GUIDE
                else -> OTHER
            }
        }
    }
}

internal data class TravelInsight(
    val isTravelRelated: Boolean,
    val title: String,
    val destination: String,
    val category: TravelCategory,
    val tags: List<String>,
    val bodyText: String,
    val confidence: Double,
) {
    fun toJson(): JSONObject = JSONObject()
        .put("isTravelRelated", isTravelRelated)
        .put("title", title)
        .put("destination", destination)
        .put("category", category.name)
        .put("tags", JSONArray(tags))
        .put("bodyText", bodyText)
        .put("confidence", confidence)

    companion object {
        fun fromJson(json: JSONObject): TravelInsight =
            TravelInsight(
                isTravelRelated = json.optBoolean("isTravelRelated", false),
                title = json.optString("title").trim(),
                destination = json.optString("destination").trim(),
                category = runCatching { TravelCategory.valueOf(json.optString("category")) }
                    .getOrElse { TravelCategory.fromModelValue(json.optString("category")) },
                tags = json.optStringArray("tags"),
                bodyText = json.optString("bodyText").trim(),
                confidence = json.optDouble("confidence", 0.0).coerceIn(0.0, 1.0),
            )
    }
}

internal data class BackendCollectResult(
    val saved: Boolean,
    val reason: String?,
    val source: BackendSourceCard?,
)

internal data class BackendSourceCard(
    val sourceId: String,
    val title: String,
    val originalUrl: String?,
    val sourcePlatform: String?,
    val coverImageUrl: String?,
    val destination: String,
    val category: String,
    val locationName: String?,
    val tags: List<String>,
    val createdAt: String,
)

internal data class BackendRecommendResult(
    val answer: String,
    val usedSources: List<BackendUsedSource>,
)

internal data class BackendUsedSource(
    val sourceId: String,
    val title: String,
    val originalUrl: String?,
    val coverImageUrl: String?,
    val sourcePlatform: String?,
    val destination: String,
    val category: String,
    val tags: List<String>,
)

internal object TravelInsightAnalyzer {
    private const val MODEL = "gemma4:latest"
    private const val BACKEND_ANALYZE_ENDPOINT = "https://trip.aatroxli.site:1221/sources/analyze"
    private const val BACKEND_ANALYZE_IMAGE_ENDPOINT = "https://trip.aatroxli.site:1221/sources/analyze-image"
    private const val BACKEND_COLLECT_ENDPOINT = "https://trip.aatroxli.site:1221/sources/collect"
    private const val BACKEND_COLLECT_IMAGE_ENDPOINT = "https://trip.aatroxli.site:1221/sources/collect-image"
    private const val BACKEND_SOURCES_ENDPOINT = "https://trip.aatroxli.site:1221/sources"
    private const val BACKEND_RECOMMEND_ENDPOINT = "https://trip.aatroxli.site:1221/chat/recommend"
    private const val OLLAMA_CHAT_ENDPOINT = "http://127.0.0.1:11434/v1/chat/completions"

    fun analyze(rawText: String, metadata: ParsedMetadata): TravelInsight {
        return requestBackendInsight(rawText, metadata)
    }

    fun analyzeImage(imageFile: File): TravelInsight {
        val base64 = android.util.Base64.encodeToString(imageFile.readBytes(), android.util.Base64.NO_WRAP)
        return requestBackendImageInsight(base64)
    }

    fun collect(rawText: String, metadata: ParsedMetadata): BackendCollectResult {
        val fallbackTitle = metadata.title
            ?.takeIf { XhsShareParser.isUsefulParsedTitle(it) }
            ?: XhsShareParser.deriveTitle(rawText)
            ?: "未命名旅行资料"
        val fallbackBodyText = metadata.description
            ?.takeIf { it.isNotBlank() }
            ?: rawText
        val requestBody = JSONObject()
            .put("input_type", "url")
            .put("url", metadata.resolvedUrl)
            .put("title", fallbackTitle.ifBlank { "未命名旅行资料" })
            .put("body_text", fallbackBodyText.ifBlank { rawText })
            .put("source_platform", XhsShareParser.detectPlatform("${metadata.resolvedUrl} $rawText").apiValue)
            .put("cover_image_url", metadata.imageUrl)
            .toString()
        return parseBackendCollectJson(postJson(BACKEND_COLLECT_ENDPOINT, requestBody, readTimeoutMs = 120000))
    }

    fun collectImage(imageFile: File): BackendCollectResult {
        val base64 = android.util.Base64.encodeToString(imageFile.readBytes(), android.util.Base64.NO_WRAP)
        val requestBody = JSONObject()
            .put("input_type", "image")
            .put("image_base64", base64)
            .put("title_hint", "长图旅行资料")
            .put("source_platform", "image")
            .toString()
        return parseBackendCollectJson(postJson(BACKEND_COLLECT_IMAGE_ENDPOINT, requestBody, readTimeoutMs = 180000))
    }

    fun fetchSources(): List<BackendSourceCard> {
        val connection = (URL(BACKEND_SOURCES_ENDPOINT).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 8000
            readTimeout = 30000
        }
        try {
            val body = if (connection.responseCode in 200..299) {
                connection.inputStream.bufferedReader().use { it.readText() }
            } else {
                val error = connection.errorStream?.bufferedReader()?.use { it.readText() }.orEmpty()
                throw IllegalStateException("TripGuard backend list HTTP ${connection.responseCode}: ${error.take(120)}")
            }
            return parseBackendSourceListJson(body)
        } finally {
            connection.disconnect()
        }
    }

    fun recommend(message: String): BackendRecommendResult {
        val requestBody = JSONObject()
            .put("message", message)
            .put("limit", 8)
            .toString()
        return parseBackendRecommendJson(postJson(BACKEND_RECOMMEND_ENDPOINT, requestBody, readTimeoutMs = 120000))
    }

    private fun requestBackendInsight(rawText: String, metadata: ParsedMetadata): TravelInsight {
        val fallbackTitle = metadata.title
            ?.takeIf { XhsShareParser.isUsefulParsedTitle(it) }
            ?: XhsShareParser.deriveTitle(rawText)
            ?: "未命名旅行资料"
        val fallbackBodyText = metadata.description
            ?.takeIf { it.isNotBlank() }
            ?: rawText
        val requestBody = JSONObject()
            .put("input_type", "url")
            .put("url", metadata.resolvedUrl)
            .put("title", fallbackTitle.ifBlank { "未命名旅行资料" })
            .put("body_text", fallbackBodyText.ifBlank { rawText })
            .put("source_platform", XhsShareParser.detectPlatform("${metadata.resolvedUrl} $rawText").apiValue)
            .put("cover_image_url", metadata.imageUrl)
            .toString()

        val connection = (URL(BACKEND_ANALYZE_ENDPOINT).openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = 8000
            readTimeout = 60000
            doOutput = true
            setRequestProperty("Content-Type", "application/json")
        }
        try {
            connection.outputStream.use { it.write(requestBody.toByteArray(Charsets.UTF_8)) }
            val body = if (connection.responseCode in 200..299) {
                connection.inputStream.bufferedReader().use { it.readText() }
            } else {
                val error = connection.errorStream?.bufferedReader()?.use { it.readText() }.orEmpty()
                throw IllegalStateException("TripGuard backend HTTP ${connection.responseCode}: ${error.take(120)}")
            }
            return parseBackendAnalyzeJson(
                content = body,
                fallbackTitle = fallbackTitle,
                fallbackBodyText = fallbackBodyText,
            )
        } finally {
            connection.disconnect()
        }
    }

    private fun requestBackendImageInsight(imageBase64: String): TravelInsight {
        val requestBody = JSONObject()
            .put("input_type", "image")
            .put("image_base64", imageBase64)
            .put("title_hint", "长图旅行资料")
            .put("source_platform", "image")
            .toString()

        val connection = (URL(BACKEND_ANALYZE_IMAGE_ENDPOINT).openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = 10000
            readTimeout = 180000
            doOutput = true
            setRequestProperty("Content-Type", "application/json")
        }
        try {
            connection.outputStream.use { it.write(requestBody.toByteArray(Charsets.UTF_8)) }
            val body = if (connection.responseCode in 200..299) {
                connection.inputStream.bufferedReader().use { it.readText() }
            } else {
                val error = connection.errorStream?.bufferedReader()?.use { it.readText() }.orEmpty()
                throw IllegalStateException("TripGuard backend image HTTP ${connection.responseCode}: ${error.take(120)}")
            }
            return parseBackendAnalyzeJson(
                content = body,
                fallbackTitle = "长图旅行资料",
                fallbackBodyText = "",
            )
        } finally {
            connection.disconnect()
        }
    }

    private fun requestInsight(userMessage: JSONObject): TravelInsight {
        val requestBody = JSONObject()
            .put("model", MODEL)
            .put("stream", false)
            .put(
                "messages",
                JSONArray()
                    .put(
                        JSONObject()
                            .put("role", "system")
                            .put("content", SYSTEM_PROMPT),
                    )
                    .put(userMessage),
            )
            .toString()

        val connection = (URL(OLLAMA_CHAT_ENDPOINT).openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = 5000
            readTimeout = 45000
            doOutput = true
            setRequestProperty("Content-Type", "application/json")
        }
        try {
            connection.outputStream.use { it.write(requestBody.toByteArray(Charsets.UTF_8)) }
            val body = if (connection.responseCode in 200..299) {
                connection.inputStream.bufferedReader().use { it.readText() }
            } else {
                val error = connection.errorStream?.bufferedReader()?.use { it.readText() }.orEmpty()
                throw IllegalStateException("Ollama HTTP ${connection.responseCode}: ${error.take(120)}")
            }
            val content = JSONObject(body)
                .getJSONArray("choices")
                .getJSONObject(0)
                .getJSONObject("message")
                .getString("content")
            return parseInsightJson(content)
        } finally {
            connection.disconnect()
        }
    }

    fun buildPrompt(rawText: String, metadata: ParsedMetadata): String =
        """
        请判断下面这条收藏是否和旅行相关。

        旅行相关包括：目的地、景点、路线、攻略、酒店住宿、交通、吃喝、玩乐、购物、展览、市集、城市体验。
        明显无关包括：纯技术文章、泛娱乐八卦、金融、无目的地的普通商品、和出行无关的生活记录。

        只返回 JSON，不要 Markdown，不要解释：
        {
          "isTravelRelated": true,
          "title": "原标题，必须来自原文或图片中最像标题的文本",
          "destination": "目的地，无法判断则为空字符串",
          "category": "吃喝|玩乐|景点|购物|住宿|交通|攻略|其他",
          "tags": ["3 到 6 个标签，可包含口味、菜系、玩法、人群、地点关键词"],
          "bodyText": "识别或解析到的正文全文，只用于入库，不用于卡片展示",
          "confidence": 0.0
        }

        原始分享:
        ${rawText.take(1800)}

        解析标题:
        ${metadata.title.orEmpty().take(300)}

        解析正文:
        ${metadata.description.orEmpty().take(2500)}

        原文链接:
        ${metadata.resolvedUrl}
        """.trimIndent()

    fun buildImagePrompt(): String =
        """
        请识别这张长图是否和旅行相关，并提炼入库卡片字段。

        规则：
        1. 标题必须使用图片里的原标题；如果没有明确标题，使用图片里最像标题的一行文字。
        2. destination 和 category 必须尽力给出；无法给出时判定 isTravelRelated=false。
        3. 不要生成摘要。
        4. bodyText 填入识别到的正文全文。
        5. tags 给 3 到 6 个，优先包含口味、菜系、玩法、人群、商圈、景点等。

        只返回 JSON，不要 Markdown，不要解释：
        {
          "isTravelRelated": true,
          "title": "原标题",
          "destination": "目的地",
          "category": "吃喝|玩乐|景点|购物|住宿|交通|攻略|其他",
          "tags": ["咖啡", "甜品", "citywalk"],
          "bodyText": "识别出的正文全文",
          "confidence": 0.0
        }
        """.trimIndent()

    fun parseBackendAnalyzeJson(
        content: String,
        fallbackTitle: String,
        fallbackBodyText: String,
    ): TravelInsight {
        val json = JSONObject(extractJsonObject(content))
        val locationName = json.optString("location_name").trim()
        val modelTitle = json.optString("title").trim().takeUnless { it.isBlank() || it == "null" }
        val modelBodyText = json.optString("body_text").trim().takeUnless { it.isBlank() || it == "null" }
        val tags = buildList {
            addAll(json.optStringArray("normalized_tags"))
            addAll(json.optStringArray("raw_tags"))
            if (locationName.isNotBlank() && locationName != "null") {
                add(locationName)
            }
        }.distinct().take(6)
        return TravelInsight(
            isTravelRelated = json.optBoolean("is_travel_related", false) &&
                isSpecificDestination(json.optString("destination")) &&
                json.optString("category").trim().lowercase() != "unknown",
            title = modelTitle ?: fallbackTitle.trim(),
            destination = json.optString("destination").trim().takeUnless { it == "null" }.orEmpty(),
            category = TravelCategory.fromModelValue(json.optString("category")),
            tags = tags,
            bodyText = modelBodyText ?: fallbackBodyText.trim(),
            confidence = json.optDouble("confidence", 0.0).coerceIn(0.0, 1.0),
        )
    }

    fun parseBackendCollectJson(content: String): BackendCollectResult {
        val json = JSONObject(extractJsonObject(content))
        return BackendCollectResult(
            saved = json.optBoolean("saved", false),
            reason = json.optNullableString("reason"),
            source = json.optJSONObject("source")?.let(::parseBackendSourceCardJson),
        )
    }

    fun parseBackendSourceListJson(content: String): List<BackendSourceCard> {
        val array = JSONObject(extractJsonObject(content)).optJSONArray("items") ?: return emptyList()
        return buildList {
            for (index in 0 until array.length()) {
                array.optJSONObject(index)?.let { add(parseBackendSourceCardJson(it)) }
            }
        }
    }

    fun parseBackendSourceCardJson(json: JSONObject): BackendSourceCard {
        val locationName = json.optNullableString("location_name")
        val tags = buildList {
            addAll(json.optStringArray("normalized_tags"))
            addAll(json.optStringArray("raw_tags"))
            if (!locationName.isNullOrBlank()) {
                add(locationName)
            }
        }.distinct().take(6)
        return BackendSourceCard(
            sourceId = json.getString("source_id"),
            title = json.optString("title").trim(),
            originalUrl = json.optNullableString("original_url"),
            sourcePlatform = json.optNullableString("source_platform"),
            coverImageUrl = json.optNullableString("cover_image_url"),
            destination = json.optString("destination").trim(),
            category = json.optString("category").trim(),
            locationName = locationName,
            tags = tags,
            createdAt = json.optString("created_at").trim(),
        )
    }

    fun parseBackendRecommendJson(content: String): BackendRecommendResult {
        val json = JSONObject(extractJsonObject(content))
        val usedArray = json.optJSONArray("used_sources") ?: JSONArray()
        val usedSources = buildList {
            for (index in 0 until usedArray.length()) {
                val item = usedArray.optJSONObject(index) ?: continue
                add(
                    BackendUsedSource(
                        sourceId = item.getString("source_id"),
                        title = item.optString("title").trim(),
                        originalUrl = item.optNullableString("original_url"),
                        coverImageUrl = item.optNullableString("cover_image_url"),
                        sourcePlatform = item.optNullableString("source_platform"),
                        destination = item.optString("destination").trim(),
                        category = item.optString("category").trim(),
                        tags = item.optStringArray("normalized_tags"),
                    ),
                )
            }
        }
        return BackendRecommendResult(
            answer = json.optString("answer").trim(),
            usedSources = usedSources,
        )
    }

    fun parseInsightJson(content: String): TravelInsight {
        val json = JSONObject(extractJsonObject(content))
        return TravelInsight(
            isTravelRelated = json.optBoolean("isTravelRelated", false),
            title = json.optString("title").trim(),
            destination = json.optString("destination").trim(),
            category = TravelCategory.fromModelValue(json.optString("category")),
            tags = json.optStringArray("tags"),
            bodyText = json.optString("bodyText").trim(),
            confidence = json.optDouble("confidence", 0.0).coerceIn(0.0, 1.0),
        )
    }

    private fun extractJsonObject(content: String): String {
        val trimmed = content.trim()
            .removePrefix("```json")
            .removePrefix("```")
            .removeSuffix("```")
            .trim()
        val start = trimmed.indexOf('{')
        val end = trimmed.lastIndexOf('}')
        require(start >= 0 && end > start) { "模型没有返回 JSON" }
        return trimmed.substring(start, end + 1)
    }

    private fun isSpecificDestination(value: String): Boolean {
        return value.trim().lowercase() !in setOf("", "未知", "unknown", "null", "none", "不确定", "无法判断")
    }

    private fun postJson(url: String, requestBody: String, readTimeoutMs: Int): String {
        val connection = (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = 10000
            readTimeout = readTimeoutMs
            doOutput = true
            setRequestProperty("Content-Type", "application/json")
        }
        try {
            connection.outputStream.use { it.write(requestBody.toByteArray(Charsets.UTF_8)) }
            return if (connection.responseCode in 200..299) {
                connection.inputStream.bufferedReader().use { it.readText() }
            } else {
                val error = connection.errorStream?.bufferedReader()?.use { it.readText() }.orEmpty()
                throw IllegalStateException("TripGuard backend HTTP ${connection.responseCode}: ${error.take(120)}")
            }
        } finally {
            connection.disconnect()
        }
    }

    private const val SYSTEM_PROMPT =
        "你是旅行知识库的入库审核和卡片提炼助手。你必须只输出一个合法 JSON 对象。"
}

private val SourcePlatform.apiValue: String
    get() = when (this) {
        SourcePlatform.XIAOHONGSHU -> "xhs"
        SourcePlatform.MAFENGWO -> "mafengwo"
        SourcePlatform.WEB -> "web"
    }

private fun JSONObject.optStringArray(name: String): List<String> {
    val array = optJSONArray(name) ?: return emptyList()
    return buildList {
        for (index in 0 until array.length()) {
            val value = array.optString(index).trim()
            if (value.isNotBlank()) {
                add(value)
            }
        }
    }
}

private fun JSONObject.optNullableString(name: String): String? =
    if (has(name) && !isNull(name)) optString(name).trim().takeIf { it.isNotBlank() && it != "null" } else null
