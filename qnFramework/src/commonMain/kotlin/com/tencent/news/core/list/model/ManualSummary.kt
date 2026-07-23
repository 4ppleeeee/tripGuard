package com.tencent.news.core.list.model

import com.tencent.news.core.extension.IKmmKeep
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 人工摘要数据（picShowType=9083 节点用）
 *
 * form 取值与样式对应：
 * - "1" 纯文（无序号无圆点）
 * - "2" 带序号（数字圆圈）
 * - "3" 圆点
 * - "4" 图片
 */
@Serializable
class ManualSummary : IKmmKeep {
    /** 摘要类型：1长文本 / 2带序号 / 3不带序号 / 4图片 */
    var form: String = ""

    /** 摘要文本数组（form=1 时取第一个） */
    @SerialName("text_list")
    var text_list: List<String>? = null

    /** 图片链接（form=4 时使用） */
    @SerialName("image_url")
    var image_url: String = ""

    /** 封面图链接 */
    @SerialName("image_cover_url")
    var image_cover_url: String = ""

    /** 图片宽高比（不是封面图，是图片本体的宽高比） */
    @SerialName("image_ratio_new")
    var image_ratio_new: Float = 0f

    /** 来源文案描述 */
    @SerialName("source_description")
    var source_description: String = ""

    /** 折叠态最大展示总行数，<= 0 表示不折叠 */
    @SerialName("show_num")
    var show_num: Int = 0

    /** 业务代码使用的 camelCase 访问入口，兼容 Gson 直接写入 snake_case 字段。 */
    val textList: List<String>?
        get() = text_list

    val imageUrl: String
        get() = image_url

    val imageCoverUrl: String
        get() = image_cover_url

    val imageRatioNew: Float
        get() = image_ratio_new

    val sourceDescription: String
        get() = source_description

    val showNum: Int
        get() = show_num
}
