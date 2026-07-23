@file:Suppress("PropertyName")

package com.tencent.news.core.page.model

import com.tencent.news.core.extension.IKmmKeep
import com.tencent.news.core.list.model.IKmmFeedsItem
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 电台垂直分页滑动组件
 * 用于实现类似抖音的上下滑动切换效果
 * 
 * 注意：此Widget仅负责数据管理，UI渲染由StructPageWidget2框架自动处理
 */
@Serializable
@SerialName(QnCoreStructWidgetType.AUDIO_RADIO_VERTICAL_PAGER)
class AudioRadioVerticalPagerWidget : StructWidget(), IFeedsItemWidget {

    var data: AudioRadioVerticalPagerWidgetData? = null
    
    var action: AudioRadioVerticalPagerWidgetAction? = null

    override fun getWidgetType() = QnCoreStructWidgetType.AUDIO_RADIO_VERTICAL_PAGER

    override fun getItemWidgets(): List<IKmmFeedsItem> {
        return data?.radioList ?: emptyList()
    }
}

/**
 * 电台垂直分页组件数据
 */
@Serializable
data class AudioRadioVerticalPagerWidgetData(
    var radioList: MutableList<IKmmFeedsItem> = mutableListOf()
) : StructWidgetData() {
    companion object {
        fun create(radioList: List<IKmmFeedsItem>): AudioRadioVerticalPagerWidgetData {
            return AudioRadioVerticalPagerWidgetData(
                radioList = radioList.toMutableList()
            )
        }
    }
}

/**
 * 电台垂直分页组件行为配置
 */
@Serializable
data class AudioRadioVerticalPagerWidgetAction(
    var initialPage: Int = 0,              // 初始显示的页面索引
    var beyondViewportPageCount: Int = 1,  // 预加载前后各N页
    var userScrollEnabled: Boolean = true  // 是否允许用户滑动
) : IKmmKeep
