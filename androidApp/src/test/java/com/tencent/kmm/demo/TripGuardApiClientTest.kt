package com.tencent.kmm.demo

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import org.json.JSONObject

class TripGuardApiClientTest {

    @Test
    fun collectPayloadUsesBackendSourceContract() {
        val payload = TripGuardApiClient.buildCollectPayload(
            title = "东京表参道咖啡店",
            bodyText = "适合下午茶和拍照。",
            url = "https://example.com/note",
            sourcePlatform = "xhs",
        )

        assertEquals("text", payload.getString("input_type"))
        assertEquals("东京表参道咖啡店", payload.getString("title"))
        assertEquals("适合下午茶和拍照。", payload.getString("body_text"))
        assertEquals("https://example.com/note", payload.getString("url"))
        assertEquals("xhs", payload.getString("source_platform"))
    }

    @Test
    fun collectResponseDistinguishesSavedAndRejectedSources() {
        val saved = TripGuardApiClient.parseCollectResponse(
            JSONObject("""{"saved":true,"source":{"destination":"东京","category":"eat","normalized_tags":["咖啡","拍照好看"]}}"""),
        )
        val rejected = TripGuardApiClient.parseCollectResponse(
            JSONObject("""{"saved":false,"reason":"not travel related"}"""),
        )

        assertTrue(saved.saved)
        assertEquals("东京 · eat", saved.summary)
        assertEquals("东京", saved.destination)
        assertEquals("eat", saved.category)
        assertEquals(listOf("咖啡", "拍照好看"), saved.normalizedTags)
        assertFalse(rejected.saved)
        assertEquals("not travel related", rejected.summary)
        assertEquals(null, rejected.destination)
        assertEquals(emptyList(), rejected.normalizedTags)
    }

    @Test
    fun recommendationResponseReadsAnswer() {
        val result = TripGuardApiClient.parseRecommendationResponse(
            JSONObject("""{"answer":"建议去表参道下午茶","used_sources":[]}"""),
        )

        assertEquals("建议去表参道下午茶", result)
    }
}
