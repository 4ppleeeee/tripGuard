@file:Suppress("PropertyName", "VariableNaming")

package com.tencent.news.core.page.model

import com.tencent.news.core.app.constants.IconFont
import com.tencent.news.core.dt.constants.DtElementId
import com.tencent.news.core.extension.IKmmKeep
import com.tencent.news.core.list.vm.IClickVM
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient


object StructBtnStyleId {

    // 【常规分享按钮】图标在上、文字在下
    const val COMMON_SHARE_WITH_BOTTOM_NAME = "common_share_with_bottom_name"
    const val COMMON_TAG_SHARE_WITH_BOTTOM_NAME = "common_tag_share_with_bottom_name"
    const val COMMON_TAG_SHARE_WITHOUT_BOTTOM_NAME = "event_share_without_bottom_name"

    // 【常规收藏按钮】图标在上、文字在下
    const val COMMON_FAVORITE_WITH_BOTTOM_NAME = "common_favorite_with_bottom_name"
    const val COMMON_TAG_FAVORITE_WITH_BOTTOM_NAME = "common_tag_favorite_with_bottom_name"

}

// 按钮样式布局类型
@Serializable
object StructBtnStyleShowType {
    const val NO_TEXT = 0               // 无文字
    const val RIGHT_TOP_TEXT = 1        // 文字在右上角
    const val RIGHT_TEXT = 2            // 文字在图标右侧
    const val BOTTOM_TEXT = 3           // 文字在图标底部
    const val DIY = 4   // 需自定义layout xml 的特殊ShowType，这种showType只支持更换icon
}

@Serializable
abstract class StructBtnWidget<Data : StructBtnWidgetData> : StructWidget(), IKmmKeep {
    var ui: StructActionBtnWidgetUI? = null

    abstract val data: Data?
}

@Serializable
class StructActionBtnWidgetUI : IKmmKeep {
    var btn_style: BtnStyle? = null
    var showBtnBg: Boolean = false
    var borderRadius: Int? = null
    var showShareInNative: Boolean = false

    companion object {
        fun createWithStyleId(styleId: String): StructActionBtnWidgetUI {
            return StructActionBtnWidgetUI().apply {
                btn_style = BtnStyle().apply {
                    style_id = styleId
                }
            }
        }
    }
}

@Serializable
open class BtnStyle : IKmmKeep {
    var style_id: String = ""

//    "id": "common_share_with_bottom_name",
//    "opType": 4,
//    "resType": 1,
//    "resWidth": 24,
//    "iconfontConfig": {
//        "iconCode": "share_regular",
//        "iconColor": "#1f1f1f",
//        "nightIconColor": "#d9d9d9",
//        "iconSize": 24
//    },
//    "showType": 3,
//    "textFontSize": 12,
//    "textColor": "#1f1f1f",
//    "textNightColor": "#d9d9d9"
}

@Serializable
open class StructBtnWidgetData(
    var icon: StructImage? = null,      // 后台目前有下发size使用

    var iconFont: IconFont? = null,     // iconFont图标（端上预留，后端暂未下发）

    var lottie: StructLottie? = null,   // lottie图标（端上预留，后端暂未下发）

    var btnText: StructText? = null,    // 按钮文案（端上预留，后端暂未下发）

    @Transient
    var clickVM: IClickVM? = null,

    @Transient
    var dtEid: DtElementId? = null,     // 大同埋点eid

    @Transient
    var dtElementParams: Map<String, Any>? = null, // 大同埋点参数

    @Transient
    var dtEnableExposure: Boolean = false, // 是否开启大同曝光

    @Transient
    var contentDescription: String? = null,

    var textAlignment: StructTextAlignment = StructTextAlignment.TOP,   // 按钮文案的排列方式

    var forceUseCustomTextColor: Boolean = false,

    var hasRedDot: Boolean = false,
) : StructWidgetData(), IKmmKeep
