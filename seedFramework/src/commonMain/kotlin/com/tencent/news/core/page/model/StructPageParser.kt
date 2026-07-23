@file:Suppress("PropertyName")

package com.tencent.news.core.page.model

import com.tencent.news.core.extension.IKmmKeep
import com.tencent.news.core.extension.ResultCodeEx.JSON_PARSE_FAIL
import com.tencent.news.core.extension.getCurTimePassMillis
import com.tencent.news.core.extension.getCurTimestampMillis
import com.tencent.news.core.extension.unSafeDecode
import com.tencent.news.core.list.model.IKmmFeedsItem
import com.tencent.news.core.list.model.NewsFeedsSLO
import com.tencent.news.core.serializer.createBaseJson
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass


val StructWidgetJson by lazy {
    createBaseJson {

        // 【重要】根据 widget_type 字段解析 StructWidget 的不同子类
        classDiscriminator = "widget_type"

        // 【重要】后续新增的 widget 组件，都要在这里注册；否则多态解析会有遗漏
        serializersModule = SerializersModule {
            polymorphic(StructWidget::class) {
                // 【重要】文章列表组件
                subclass(NewsListWidget::class)
                subclass(AdListWidget::class)
                subclass(ItemCardWidget::class)

                // 页码组件（不进入 feeds 列表，承载分段/分页控制）
                subclass(IndexsWidget::class)
                subclass(IndexWidget::class)
                
                // 音频相关组件
                subclass(AudioRadioVerticalPagerWidget::class)

                // 容器相关组件
                subclass(StructPageWidget::class)

                subclass(CommonTitleBarWidget::class)

                subclass(LayersWidget::class)

                subclass(BottomBarWidget::class)
                subclass(IpBottomBarWidget::class)

                subclass(PagerWidget::class)
                subclass(RefreshIndicatorWidget::class)

                // 频道相关组件
                subclass(ChannelBarWidget::class)
                subclass(CatalogueWidget::class)
                subclass(ChannelWidget::class)

                // Header组件
                subclass(CommonHeaderWidget::class)
                subclass(ListHeaderWidget::class)
                subclass(VideoHeaderWidget::class)

                // 各种按钮组件
                subclass(TitleBtnWidget::class)
                subclass(SearchBtnWidget::class)
                subclass(FavoriteBtnWidget::class)
                subclass(FocusBtnWidget::class)
                subclass(InputBtnWidget::class)
                subclass(PublishBtnWidget::class)
                subclass(EmojiBtnWidget::class)
                subclass(CommentBtnWidget::class)
                subclass(ColumnPayBtnWidget::class)
                subclass(ColumnGiftBtnWidget::class)
                subclass(ShareBtnWidget::class)
                subclass(IpShareBtnWidget::class)
                subclass(HotSpotBtnWidget::class)
                subclass(AskBtnWidget::class)
                subclass(AudioBtnWidget::class)

                // 不认识的 widget_type 会走这里，如果不注册这个会抛 Polymorphic serializer was not found
                default {
                    DefaultStructWidget.serializer()
                }
            }

        }
    }
}


object StructWidgetParser {

    // 预热解析器，否则首次调用会有个200-300ms的耗时
    fun preload() {
        StructWidgetJson
    }

    // 给宿主留个口子，可以创建StructPageWidget子类
    fun StructPageWidget.parseStructPageWidgetJson(json: String?) {
        if (json.isNullOrBlank()) {
            return
        }

        val parseStart = getCurTimestampMillis()
        val resp = runCatching {
            StructWidgetJson.unSafeDecode<StructPageResponse>(json)
        }.getOrElse { error ->
            parseError = error
            retCode = JSON_PARSE_FAIL
            null
        } ?: return

        val parseCost = getCurTimePassMillis(parseStart)

        val respData = resp.data

        originNetData = respData
        retCode = resp.ret

        // 【重要】构建整个 widget 树
        val buildStart = getCurTimestampMillis()
        val layout = respData?.layout?.takeIf { !it.isEmpty() }
        buildPageWithLayout(respData?.widget_list, respData?.widget_group, layout)
        val buildCost = getCurTimePassMillis(buildStart)

        NewsFeedsSLO.mainLog(
            "StructPageWidget解析耗时：" +
                    "parseCost=${parseCost}, buildCost=${buildCost}, layout=${layout}"
        )
    }

}

@Serializable
private class StructPageResponse : IKmmKeep {
    var ret: Int = 0

    var data: StructPageResponseData? = null
}

// 接入层用json透传的layout参数，用这个model类进行解析，再抓换成StructPageWidget
@Serializable
private class StructPageResponseData : IKmmKeep {
    var widget_list: List<StructWidget>? = null

    var widget_group: Map<String, StructWidgetList>? = null

    @Serializable(StructPageLayoutSerializer::class)
    var layout: StructPageLayout? = null
}