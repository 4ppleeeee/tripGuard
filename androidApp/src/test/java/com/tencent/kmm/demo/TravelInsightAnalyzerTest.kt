package com.tencent.kmm.demo

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TravelInsightAnalyzerTest {

    @Test
    fun parseInsightExtractsStructuredTravelFields() {
        val insight = TravelInsightAnalyzer.parseInsightJson(
            """
            {
              "isTravelRelated": true,
              "title": "上海武康路咖啡路线",
              "destination": "上海",
              "category": "吃喝",
              "tags": ["咖啡", "甜品", "citywalk"],
              "bodyText": "武康路和外滩的咖啡 citywalk 路线。",
              "confidence": 0.86
            }
            """.trimIndent(),
        )

        assertTrue(insight.isTravelRelated)
        assertEquals("上海武康路咖啡路线", insight.title)
        assertEquals("上海", insight.destination)
        assertEquals(TravelCategory.FOOD, insight.category)
        assertEquals(listOf("咖啡", "甜品", "citywalk"), insight.tags)
        assertEquals("武康路和外滩的咖啡 citywalk 路线。", insight.bodyText)
        assertEquals(0.86, insight.confidence)
    }

    @Test
    fun parseInsightTreatsNonTravelAsRejected() {
        val insight = TravelInsightAnalyzer.parseInsightJson(
            """{"isTravelRelated":false,"title":"","destination":"","category":"其他","tags":[],"bodyText":"这是一条编程资料。","confidence":0.91}""",
        )

        assertFalse(insight.isTravelRelated)
        assertEquals(TravelCategory.OTHER, insight.category)
    }

    @Test
    fun parseBackendAnalyzeResponseMapsSnakeCaseFields() {
        val insight = TravelInsightAnalyzer.parseBackendAnalyzeJson(
            content = """
            {
              "is_travel_related": true,
              "destination": "东京",
              "category": "eat",
              "location_name": "表参道",
              "normalized_tags": ["拍照好看", "甜品"],
              "raw_tags": ["松饼"],
              "confidence": 0.82
            }
            """.trimIndent(),
            fallbackTitle = "东京表参道超好吃的舒芙蕾松饼",
            fallbackBodyText = "这家店在表参道附近，很出片。",
        )

        assertTrue(insight.isTravelRelated)
        assertEquals("东京表参道超好吃的舒芙蕾松饼", insight.title)
        assertEquals("东京", insight.destination)
        assertEquals(TravelCategory.FOOD, insight.category)
        assertEquals(listOf("拍照好看", "甜品", "松饼", "表参道"), insight.tags)
        assertEquals("这家店在表参道附近，很出片。", insight.bodyText)
        assertEquals(0.82, insight.confidence)
    }

    @Test
    fun buildPromptIncludesParsedMetadataAndRawShareText() {
        val prompt = TravelInsightAnalyzer.buildPrompt(
            rawText = "周末上海咖啡路线 http://xhslink.com/a/test",
            metadata = ParsedMetadata(
                resolvedUrl = "https://www.xiaohongshu.com/explore/abc",
                title = "上海周末 citywalk",
                description = "武康路、外滩、咖啡店路线。",
                imageUrl = "https://example.com/cover.jpg",
            ),
        )

        assertTrue(prompt.contains("上海周末 citywalk"))
        assertTrue(prompt.contains("武康路、外滩、咖啡店路线。"))
        assertTrue(prompt.contains("周末上海咖啡路线"))
        assertTrue(prompt.contains("isTravelRelated"))
        assertTrue(prompt.contains("bodyText"))
        assertTrue(prompt.contains("tags"))
    }
}
