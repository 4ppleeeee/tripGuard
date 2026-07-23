package com.tencent.news.core.platform.i18n

import com.tencent.news.core.platform.api.IAppI18n
import com.tencent.news.core.platform.api.appI18n

/**
 * 文案描述协议，ViewModel 只产出"要显示什么"，不产出最终字符串。
 *
 * - [Res]    → 引用 Native 本地化资源，支持参数化
 * - [Plural] → 引用 Native 复数资源，由 Native 决定复数规则
 * - [Raw]    → 直接使用原始字符串（服务端下发、用户输入等）
 */
sealed interface UiText {

    /**
     * 引用 Native 本地化资源
     *
     * @param key      资源 key，对应 [StringKey] 的 name
     * @param args     格式化参数，按顺序替换 Native 资源中的占位符
     * @param fallback key 无法解析时的回退文案
     */
    data class Res(
        val key: StringKey,
        val args: List<Any?> = emptyList(),
        val fallback: String? = null,
    ) : UiText

    /**
     * 引用 Native 复数资源
     *
     * @param key      资源 key
     * @param count    数量，用于 Native 侧复数规则判定
     * @param args     格式化参数（通常包含 count）
     * @param fallback key 无法解析时的回退文案
     */
    data class Plural(
        val key: StringKey,
        val count: Int,
        val args: List<Any?> = emptyList(),
        val fallback: String? = null,
    ) : UiText

    /**
     * 直接使用原始字符串，不经过 Native 本地化解析
     */
    data class Raw(val value: String) : UiText
}

/**
 * 本地化资源 key 枚举。
 *
 * KMM 侧的 key name 应与 Native 资源文件中的 key 保持一致：
 * - iOS:    `COMMON_CANCEL = "取消";` in Localizable.strings
 * - Android: `<string name="COMMON_CANCEL">取消</string>` in strings.xml
 *
 * [zhDefault] 是简体中文兜底文案，宿主未注入 [IAppI18n] 或未在资源文件中配置
 * 对应 key 时，会回落到该文案，避免页面直接显示 key 名。
 *
 * 占位符约定：[zhDefault] 使用 `%s`、`%d` 等 Java 风格占位符，与 Android strings.xml 一致。
 *
 * 新增 key 时：
 * 1. 必须在此处提供 [zhDefault]（构造器强制要求）。
 * 2. 如该 key 需要在 iOS 多语言中切换，需同步更新 iOS 的 Localizable.strings。
 */
enum class StringKey(val zhDefault: String) {
    // 通用按钮
    COMMON_CANCEL("取消"),
    COMMON_CONFIRM("确认"),
    COMMON_RETRY("重试"),
    COMMON_SKIP("跳过"),
    COMMON_SELECT_ALL("全选"),
    COMMON_DESELECT_ALL("反选"),
    COMMON_DELETE("删除"),
    COMMON_EDIT("编辑"),
    COMMON_SEARCH("搜索"),

    // 通用列表标签
    COMMON_FEED_LABEL_COMMENT_COUNT("%s评"),
    COMMON_FEED_LABEL_READ_COUNT("%s阅读"),
    COMMON_FEED_LABEL_APPROVE_COUNT("%s赞同"),
    COMMON_FEED_LABEL_PROGRESS_COUNT("%s进展"),
    COMMON_FEED_LABEL_LISTEN_COUNT("%s人听过"),
    COMMON_FEED_LABEL_VIDEO_COUNT("%s视频"),

    // 通用时间与日期
    COMMON_DATE_TODAY("今天"),
    COMMON_DATE_YESTERDAY("昨天"),
    COMMON_DATE_DAY_BEFORE_YESTERDAY("前天"),
    COMMON_DATE_MONTH_DAY("%s月%s日"),
    COMMON_DATE_YEAR_MONTH_DAY("%s年%s月%s日"),

    // 登录
    LOGIN_SIGN_IN_IMMEDIATELY("立即登录"),
    LOGIN_SIGNING_IN("登录中"),
    LOGIN_OTHER_ACCOUNT("其它账号登录"),
    LOGIN_LAST_SIGN_IN("上次登录"),

    // 列表状态
    LIST_LOADING("加载中..."),
    LIST_LOAD_FAILED("加载失败"),
    LIST_NO_MORE("没有更多了"),
    LIST_EMPTY("暂无数据"),

    // 第三方链接
    THIRD_PARTY_LEAVE_TITLE("离开提示"),
    THIRD_PARTY_LEAVE_DESC("即将离开腾讯新闻，前往第三方链接"),
    THIRD_PARTY_LEAVE_SKIP("取消"),
    THIRD_PARTY_CONTINUE("继续"),

    // 用户中心（Tab4）
    USER_CENTER_MESSAGE("消息"),
    USER_CENTER_COMMON_FUNCTIONS("常用功能"),
    USER_CENTER_MORE_FUNCTIONS("更多功能"),
    USER_CENTER_GO_CHECK("去看看"),
    USER_CENTER_POINTS("积分"),

    // 我的历史
    MY_HISTORY_TITLE("我的历史"),
    MY_HISTORY_TAB_FAVORITE("收藏"),
    MY_HISTORY_TAB_BROWSE_HISTORY("浏览历史"),
    MY_HISTORY_TAB_LIKED("已赞"),
    MY_HISTORY_TAB_PUSH("推送"),
    MY_HISTORY_TAB_UNDERLINE("划线"),
    MY_HISTORY_PUSH_OPTIMIZE("推送优化"),
    MY_HISTORY_PUSH_OPTIMIZE_ACTION("优化"),
    MY_HISTORY_PUSH_EDIT_GUIDE("选择不感兴趣内容，同时会为你优化后续推送"),
    MY_HISTORY_LABEL_WECHAT_ACCOUNT("微信公众号"),
    MY_HISTORY_PUSH_GROUP_TITLE("%s推送了%s篇文章"),
    MY_HISTORY_LOAD_FINISH("已显示全部内容"),
    MY_HISTORY_PUSH_LOAD_FINISH("已显示最近两天全部推送内容"),
    MY_HISTORY_EMPTY_FAVORITE("暂无收藏\n点击星星收藏你喜欢的内容"),
    MY_HISTORY_EMPTY_FAVORITE_NO_LOGIN("登录查看收藏内容"),
    MY_HISTORY_EMPTY_LIKED("暂无喜欢的内容\n点赞后你喜欢的内容会出现在这里"),
    MY_HISTORY_EMPTY_LIKED_NO_LOGIN("登录查看点赞内容"),
    MY_HISTORY_EMPTY_PUSH("暂无推送"),
    MY_HISTORY_EMPTY_UNDERLINE("暂无划线内容"),
    MY_HISTORY_EMPTY_UNDERLINE_NO_LOGIN("登录查看划线内容"),
    MY_HISTORY_EMPTY_BROWSE("暂无阅读历史\n看过的内容都会在这里出现"),
    MY_HISTORY_EMPTY_AUDIO("暂无收听记录"),
    MY_HISTORY_EMPTY_SEARCH_CONTENT("暂无内容"),
    MY_HISTORY_EMPTY_BUTTON_NEWS_LOOK("去要闻频道看看"),
    MY_HISTORY_EMPTY_BUTTON_NEWS_LISTEN("去要闻频道听听"),
    MY_HISTORY_PUSH_ENABLE("开启推送"),
    MY_HISTORY_PUSH_ENABLE_GUIDE("第一时间获知你关心的重大新闻"),
    MY_HISTORY_TOAST_PUSH_OPTIMIZED("已优化%s条推送"),
    MY_HISTORY_TOAST_ALL_OPTIMIZED("已优化全部内容"),
    MY_HISTORY_TOAST_FEEDBACK("已反馈"),
    MY_HISTORY_TOAST_NO_PLAYABLE_CONTENT("暂无可播放内容"),
    MY_HISTORY_LABEL_COLUMN_PURCHASED("已购专栏"),

    // 图文详情
    DETAIL_AI_AUDIO_ENTRANCE("生成AI播客"),
    ;

    val key: String get() = name

    companion object {
        /**
         * 根据 key 字符串查询其简体中文兜底文案。找不到返回 null。
         */
        fun zhDefaultOf(key: String): String? =
            entries.firstOrNull { it.name == key }?.zhDefault
    }
}

/**
 * 便捷构造方法：无参数 Res
 */
fun StringKey.res(vararg args: Any?, fallback: String? = null): UiText.Res =
    UiText.Res(this, args.toList(), fallback)

/**
 * 便捷构造方法：Plural
 */
fun StringKey.plural(count: Int, vararg args: Any?, fallback: String? = null): UiText.Plural =
    UiText.Plural(this, count, args.toList(), fallback)

/**
 * 将 [UiText] 解析为最终字符串。
 *
 * @param i18n 用于解析的 i18n 实例，默认使用 [appI18n()]
 */
fun UiText.resolve(i18n: IAppI18n = appI18n()): String = when (this) {
    is UiText.Res -> i18n.resolve(key.key, args, fallback)
    is UiText.Plural -> i18n.resolvePlural(key.key, count, args, fallback)
    is UiText.Raw -> value
}
