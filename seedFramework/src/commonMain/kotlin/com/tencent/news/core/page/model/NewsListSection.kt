@file:Suppress("PrivatePropertyName")

package com.tencent.news.core.page.model

import com.tencent.news.core.extension.IKmmKeep
import kotlinx.serialization.Serializable


object SectionType {
    const val SECTION_NEW_TYPE = "1"    // ‘最新’模块（可以触发无限刷）
    const val WEIBO_COMMENT = "2"       // 动态话题
    const val THING_VERIFIER = "4"      // 查证家（已废弃）
    const val THING_RELATED = "5"       // 相关事件
    const val HOT_COMMENT = "7"         // 网友热议
    const val QA = "8"                  // 问答专区
    const val EVENT_TIMELINE = "9"      // 事件脉络（单tab时的时间轴样式，多tab换成 picShowType=577）

    // 非星辰下发的本地区块，号段从100+开始
    const val COLUMN_CATALOGUE = "101"  // 付费专栏-目录分区
}

annotation class SectionComponentType {
    companion object {
        const val CP_NEW = "CPNew"
        const val WEIBO_LIST = "WeiboList"
        const val NET_DISCUSSION = "NetDiscussion"
        const val THING_TRACE = "ThingTrace"
        const val LONG_IMAGE = "LongImage"
        const val SLIDING = "Sliding"
        const val TIME_SLIDER = "TimeSlider" // 横滑时间轴组件
    }
}

@Serializable // 【注意】这个类需要同时兼容Gson和ktx，不能用 @SerialName
class NewsListSection : IKmmKeep {

    var name: String = ""               // 分区唯一标识

    var section: String = ""            // 分区标题（标题只认section字段，有下发则显示标题；与目录的名称catalogue_name不强相关）

    var secondTitle: String = ""        // 副标题

    private var right_text: String = ""
    var rightText: String               // 分区标题右侧文案
        get() = right_text
        set(value) {
            right_text = value
        }

    var sectionIcon: String = ""        // 分区图标

    private var catalogue_name: String = ""
    var catalogueName: String           // 外显到导航目录上的文案（可能与分区标题不同）
        get() = catalogue_name
        set(value) {
            catalogue_name = value
        }

    private var section_ad_switch: Int = 1
    var sectionAdSwitch: Int            // 分区内是否可插入广告
        get() = section_ad_switch
        set(value) {
            section_ad_switch = value
        }

    private var first_num: Int = 0
    var firstNum: Int                    // 分区首屏展示的文章数量（剩余的需要点击footer展开才显示）
        get() = first_num
        set(value) {
            first_num = value
        }

    // SectionType
    var type: String = ""               // 分区的类型

    var component: String = ""          // 分区的类型（与type很类似）

    private var showtype: Int = 0
    var showType: Int
        get() = showtype
        set(value) {
            showtype = value
        }

    private var allcount: Int = 0
    var relateQuestionCount             // 网友热议讨论数、相关提问数 等等
        get() = allcount
        set(value) {
            allcount = value
        }

    // 不显示 1，细分割线 2，粗分割线 3，强制不显示 4，强制细分割线 5，强制粗分割线 6
    var top_sep_line_type = 0           // 分区顶部分割线

    var bottom_sep_line_type = 0        // 分区底部分割线

    private var word_size: Int = 0
    var wordSize                        // 分区标题字号，单位sp
        get() = word_size
        set(value) {
            word_size = value
        }

    var scheme: String = ""             // 分区头部跳转scheme

    private var module_type: Int = 0    // ios还有遗留逻辑，先留着
    var moduleType: Int
        get() = module_type
        set(value) {
            module_type = value
        }

    /**
     * 是否需要将该分区从主列表搬运到 header 区域展示
     * "1" 表示需要搬运到 header；其它值或为空表示保持在主列表
     */
    var move_to_header: String = ""

    fun shouldMoveToHeader(): Boolean = move_to_header == "1"


    fun getHeaderRightText(): String {
        if (SectionType.THING_VERIFIER == type && relateQuestionCount > 0) {
            return relateQuestionCount.toString() + "个相关提问"
        } else if (rightText.isNotEmpty()) {
            return rightText
        }
        return ""
    }

    fun getSubTitle(): String {
        return if (SectionType.HOT_COMMENT == type && relateQuestionCount > 0) {
            relateQuestionCount.toString()
        } else ""
    }

    fun isCpSection(): Boolean {
        return this.component.contains("CP", true)
    }

}
