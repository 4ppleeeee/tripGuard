package com.tencent.news.core.list.model

import com.tencent.news.core.annotation.OnlyUseForSerializable
import com.tencent.news.core.platform.QnKmmModelConvert
import com.tencent.news.core.setup.GlobalModelSerializerFactory
import kotlinx.serialization.Serializable

typealias IItemLabel = IFeedsItemLabel // 优化命名

@Suppress("AnnotationOnSeparateLine")
@OnlyUseForSerializable
typealias QnItemLabel = @Serializable(IFeedsItemLabel.QnSerializer::class) IItemLabel

// type 枚举值
object FeedsLabelType {
    const val NONE = 0                    // 无
    const val TEXT = 1                    // 文字
    const val IMAGE = 2                   // 图片
    const val SUBSCRIBE = 3               // "已关注"标签占位
    const val HOT_DISCUSS = 4             // 热评
    const val NEW_MISSION = 5             // 新手任务
    const val PUSH_OVER = 6               // 大V热推
    const val TEXT_AND_AVATAR = 8         // 文字+用户头像
    const val TEXT_AND_IMAGE = 9          // 文字+图片
    const val MUST_SEE = 10               // "必看"标签占位
    const val REWARD = 11                 // "悬赏"标签占位
    const val AVATAR_ANIMATE = 12         // 头像轮播
    const val VIDEO_PLAY_COUNT = 13       // 视频播放数
    const val FOCUS_COUNT = 14            // 关注数
    const val CARE_V_LIKE = 15            // 大V点赞
    const val CARE_RECOMMAND = 16         // 推荐理由
    const val TEXT_AND_ARROW = 17         // 文字+箭头样式
    const val TEXT_TAG = 18               // 文字+纯色背景（边距圆角不同于1）
    const val TEXT_AND_ICON = 19          // icon+文字+纯色背景（边距圆角不同于9）
    const val OM_CARD = 20                // 类似TextAndAvatar，头像更小
    const val BACKGROUND_IMG = 21         // 标签带背景图片
    const val BREAK_724 = 23              // 724"突发"标签占位（22 Android已占用）
    const val CP_AUTH = 24                // CP认证信息
    const val JOIN_CP_VIP = 25            // 开通CP专享
    const val WECHAT_PUBLIC = 26          // 微信公众号文章
    const val CP_COLUMN = 27              // 付费专栏
    const val CP_ARTICLE_READABLE_STATE = 28 // 付费内容可读状态（免费/可看/不可看）
    const val HOT_EVENT_TAG = 29          // 热点精选标志信息
    const val SERIOUS = 1000              // "较真"标签占位
    const val CARE_FEATURED_TAG = 1001    // 关心精选标签（可点击）
    const val CARE_FEATURED_TAG2 = 1002   // 关心精选标签（不可点击）
    const val FOCUS_GUIDE = 2001          // 视频播放x秒后引导关注
}

object FeedsLabelTypeName {
    const val TYPE_NAME_SOURCE = "source"       // 来源
    const val TYPE_NAME_QIEHAO = "qiehao"       // 企鹅号
}

interface IFeedsItemLabel {

    var word: String

    // 标签类型，枚举值参考 Type 伴生对象
    var type: Int

    var nightColor: String?

    var color: String?

    var textNightColor: String?

    var textColor: String?

    var backgroundNightColor: String?

    var backgroundColor: String?

    var typeName: String?

    var show_scene: Int

    var imgUrl: String?
    var nightImgUrl: String?
    var imgWidth: Int
    var imgHeight: Int

    val lightBgColor get() = backgroundColor?.takeIf { it.isNotEmpty() } ?: ""
    val darkBgColor get() = backgroundNightColor?.takeIf { it.isNotEmpty() } ?: ""

    val lightTextColor get() = textColor ?: "#999999"
    val darkTextColor get() = textNightColor ?: "#696969"

    object QnSerializer : QnCompatSerializer<IItemLabel>(
        qnParser = { QnKmmModelConvert.itemLabelParser },
        kmmSerializer = { GlobalModelSerializerFactory.getDefault() }
    )

    companion object : IQnInterfaceCreator<IItemLabel> {
        override fun defaultSerializer() = QnSerializer

        fun create(word: String): IItemLabel = IItemLabel.new {
            this.type = FeedsLabelType.TEXT
            this.word = word
        }
    }
}