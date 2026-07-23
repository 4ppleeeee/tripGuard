package com.tencent.kmm.demo

import kotlin.test.Test
import kotlin.test.assertEquals

class XhsNoteMetadataParserTest {

    @Test
    fun parseInitialStateExtractsNoteCardFields() {
        val html = """
            <html>
              <head><title>小红书</title></head>
              <body>
                <script>
                  window.__INITIAL_STATE__={
                    "note": {
                      "noteDetailMap": {
                        "66abc": {
                          "note": {
                            "noteId": "66abc",
                            "title": "三亚亲子旅行路线",
                            "desc": "海边酒店、免税店和椰梦长廊安排。",
                            "imageList": [
                              {
                                "urlDefault": "http://sns-webpic-qc.xhscdn.com/202407/abc/notes_pre_post/cover-token!nd_whgt34_webp_wm_1"
                              }
                            ]
                          }
                        }
                      }
                    }
                  }
                </script>
              </body>
            </html>
        """.trimIndent()

        val metadata = XhsNoteMetadataParser.parse("https://www.xiaohongshu.com/explore/66abc", html)

        assertEquals("三亚亲子旅行路线", metadata?.title)
        assertEquals("海边酒店、免税店和椰梦长廊安排。", metadata?.description)
        assertEquals("http://sns-webpic-qc.xhscdn.com/202407/abc/notes_pre_post/cover-token!nd_whgt34_webp_wm_1", metadata?.imageUrl)
    }

    @Test
    fun parseMobileInitialStateExtractsNoteDataFieldsWithUndefinedAssets() {
        val html = """
            <html>
              <body>
                <script>
                  window.__INITIAL_STATE__={
                    "jsAssetsList": undefined,
                    "noteData": {
                      "data": {
                        "noteData": {
                          "noteId": "6a56469d00000000110167c8",
                          "title": "上海周末 citywalk",
                          "desc": "外滩、武康路和咖啡店路线。",
                          "imageList": [
                            {
                              "url": "https://sns-webpic-qc.xhscdn.com/mobile-cover"
                            }
                          ]
                        }
                      }
                    }
                  }
                </script>
              </body>
            </html>
        """.trimIndent()

        val metadata = XhsNoteMetadataParser.parse(
            "https://www.xiaohongshu.com/discovery/item/6a56469d00000000110167c8",
            html,
        )

        assertEquals("上海周末 citywalk", metadata?.title)
        assertEquals("外滩、武康路和咖啡店路线。", metadata?.description)
        assertEquals("https://sns-webpic-qc.xhscdn.com/mobile-cover", metadata?.imageUrl)
    }

    @Test
    fun parseFallsBackToMobileDomDescriptionWhenStateDescriptionIsMissing() {
        val html = """
            <html>
              <body>
                <div class="author-desc-content">
                  <span class="note-desc-text-opt">
                    <span>最近很多人都迷上了一款躲猫猫游戏。</span>
                    <br>
                    <span>我也入坑了，真的很有趣。</span>
                  </span>
                </div>
                <script>
                  window.__INITIAL_STATE__={
                    "noteData": {
                      "data": {
                        "noteData": {
                          "noteId": "6a56469d00000000110167c8",
                          "title": "果然，ai时代创意才是王道",
                          "imageList": [
                            {
                              "url": "https://sns-webpic-qc.xhscdn.com/mobile-cover"
                            }
                          ]
                        }
                      }
                    }
                  }
                </script>
              </body>
            </html>
        """.trimIndent()

        val metadata = XhsNoteMetadataParser.parse(
            "https://www.xiaohongshu.com/discovery/item/6a56469d00000000110167c8",
            html,
        )

        assertEquals("最近很多人都迷上了一款躲猫猫游戏。\n我也入坑了，真的很有趣。", metadata?.description)
    }
}
