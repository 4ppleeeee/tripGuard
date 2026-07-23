package com.tencent.news.core.tads.constants


const val INVALID_NUM = -1

object AdParams {
    // slot参数：
    const val SLOT = "slot"                 // 关于广告位的核心参数，请求时都统一放在一个slot节点下
    const val CHID = "chid"                 // 【重要】广告三元组-渠道（新闻是2）
    const val LOID = "loid"                 // 【重要】广告三元组-广告位
    const val CHANNEL = "channel"           // 【重要】广告三元组-频道

    const val REFRESH_TYPE = "refresh_type" // 【重要】刷新方式（首刷、顶刷、底刷）
    const val CUR = "cur"                   // 【重要】当前列表总个数（含广告）（注意：计数基于getUIBlockSum）
    const val SEQ = "seq"                   // 【重要】列表中已插入广告的位置，逗号分隔的（例如：5,10,15）
    const val SEQ_LOID = "seq_loid"
    const val SEQ_NATIVE = "seq_native"     // 【重要】原生广告槽位标识  @AdNativePosType
    const val BRUSH_NUM = "brush_num"       // 列表刷次

    const val ARTICLE_ID = "article_id"                 // 【重要】文章cmsid（影响定向投放）
    const val ARTICLE_CLOSE_AD = "article_close_ad"     // 【重要】文章是否关闭广告
    const val VID = "vid"                               // 有视频的场景，带上vid（影响定向投放）
    const val TAG_ID = "rcm_tag_id"                     // 带tag的场景，带上tag_id（影响定向投放）
    const val MEDIA_ID = "media_id"                     // 有cp的场景，带上media_id（影响定向投放）

    // 文章一级分类（对应业务接入层 news_category_id）
    const val ARTICLE_FIRST_CATEGORY = "article_first_category"

    // 文章二级分类（对应业务接入层 news_sub_category_id）
    const val ARTICLE_SECOND_CATEGORY = "article_second_category"

    const val ORDERS_INFO = "orders_info"               // 【重要】新鲜度排重，订单信息
    const val CURRENT_ROT = "current_rot"               // 【重要】新鲜度排重，订单下标，配合orders_info用

    const val CURRENT_NEWS_LIST = "current_newslist"
    const val CURRENT_VID_LIST = "current_vidlist"
    const val FEEDBACK_CUR = "feedbackCur"              // 【重要】影响混排分页位置，与cur相关
    const val FEEDBACK_NEWS_ID = "feedbackNewsId"       // 【重要】影响混排


    // 长视频相关（影响投放定向）：
    const val LONG_VIDEO_TITLE = "title"
    const val LONG_VIDEO_ACTORS = "actors"
    const val LONG_VIDEO_CATEGORIES = "categorys"
    const val IS_VIP = "is_vip"
    const val VIP_START_DATE = "vip_start_date"
    const val VIP_END_DATE = "vip_end_date"
    const val VIP_LEVEL = "vip_level"


    // 落地页相关：
    const val IS_V_LAND_PAGE = "is_v_land_page"         // 【重要】是否是视频落地页（影响广告位置策略）
    const val TIME_ON_PAGE = "time_on_page"             // 当前页面停留时间 单位ms（算法侧试用）


    // 频控与投放定向：
    const val LOID_WATCH_COUNT = "loid_watch_count"         // 单频道，广告位曝光次数
    const val LOID_WATCH_COUNT_ALL = "loid_watch_count_all" // 全局，广告位曝光次数
    const val IS_LOCAL = "islocal"                          // 是否是地方站
    const val TODAY_FIRST_VIEW = "first_view"               // 是否是今天首次请求（1-是首次，0-不是）
    const val FEEDS_LAUNCH_TYPE = "feeds_launch_type"       // 信息流第一刷启动方式（2-冷启，1-热启，oneshot使用）
    const val RESET_EXIST_SEQ = "reset_exist_seq"           // 同一session_id下，截止上一次刷新，全部广告位置序列
    const val RECOVERY_RESET = "recovery_reset"             // 是否处于'上次看到这里'模式（要闻用）
    const val IS_RESET = "is_reset"                         // 是否30分钟自动reset


    // 版本与端能力：
    const val MOBSTR = "mobstr"                         // 【重要】客户端业务公参（加密后的字符串）
    const val ADTYPE = "adtype"     // 【重要】广告的轮播方式，绝大部分是0（其他如：闪屏预选单、品牌献礼预拉取等）
    const val PF = "pf"                                 // 【重要】平台类型（取值：aphone iphone）
    const val VER = "ver"                               // 【重要】客户端版本（格式：7.2.70）
    const val APPVERSION = "appversion"                 // 【重要】商业化版本号（是个日期格式，例如：231020）
    const val WXVERSION = "wxversion"                   // 手机上的微信客户端版本号
    const val WXOPENSDK_VERSION = "wxopensdk_version"   // 新闻里微信sdk的版本号（固定值）

    @Deprecated("sharpP已废弃")
    const val IS_SUPPORT_SHARPP = "is_support_sharpP"   // 是否支持sharpP解码
    const val IS_SUPPORT_WEBP = "is_support_webP"       // 是否支持webP解码

    // 设备屏幕相关
    const val DEVICE_MODEL_TYPE = "device_model_type"   // 设备类型
    const val DEVICE_SCREEN_TOTAL_COUNT = "device_screen_total_count"   // 设备屏幕总数量

    const val SUPPORT_QUICK_JUMP = "support_quick_jump" // android独有：是否支持快应用
    const val APP_CHANNEL = "app_channel"               // android独有：渠道号
    const val IS_RDM = "is_rdm"                         // ios独有：是否是RDM包
    const val QAID_INFO = "qaid_info"                   // ios独有：qaid

    const val DYNAMIC_JS_BUNDLE_VERSION = "js_bundle_version" // 双端-动态化-js bundle 版本
    const val DYNAMIC_SDK_VERSION = "dsdk_version"            // 双端-动态化- sdk 版本
    const val DYNAMIC_MOSAIC_JS_BUNDLE_VERSION = "mosaic_js_bundle_version" // 双端-动态化 - mosaic
    const val STYLE_PRELOAD_TEMPLATE_IDS = "style_preload_template_ids" // 双端-动态化 - 模版ID列表

    // 启动相关：
    const val LAUNCH = "launch"                         // 【重要】外部拉起方法
    const val START_EXTRAS = "startextras"              // 【重要】外部拉起透传参数
    const val PRE_TRACE_ID = "pre_trace_id"             // 【重要】外部拉起透传链路id（来自startExtras）
    const val LAUNCH_TIMESTAMP = "launchTimestamp"      // 冷热启动时间
    const val SSP_PARAM = "ssp_param"                   // 机器猫扫码截单，透传字段
    const val WUID = "wuid"                             // 微信插件拉起时的uid 给ams用的
    const val ATTRI_DEVICE_INFO = "attri_device_info"   // 闪屏sdk透传参数（jsonObj格式）
    const val SESSION_ID = "session_id" // sessionId（口径为大同sdk提供的dt_ussn，与业务侧口径一致）


    // 端智能相关：
    const val AMS_TRACE_ID = "ams_traceid"  // 按刷次的trace（对应ssp给到CarService的pre_get_request_id）
    const val RE_PULL_TYPE = "repull_type"              // 端智能拉取类型：1（一阶段拉取信息流），2（二阶段换单）
    const val CAR_SDK_INFO = "car_sdk_info"             // sdk本地模型信息
    const val APP_PKG_NAME = "app_pkg_name"             // 客户端包名
    const val RE_PULL_LOC_INFO = "repull_loc_info"      // 二阶段换单时，回传旧订单数据


    // 奥运挂件相关：
    const val OLYMPIC_POINTS = "push_info"              // 奥运挂件点位信息

    // 应用session内回传数据（混排模型算法使用；参数都放根节点 report_info 下面）：
    const val SN_APP_STAY_TIME = "app_stay_time"        // app使用时长
    const val SN_REFRESH_COUNT = "refresh_count"        // 所在频道&场景刷新请求次数
    const val SN_AD_REAL_EXP = "ad_exp_count"           // 所在频道&场景已有效曝光的广告个数
    const val SN_CONTENT_REAL_EXP = "content_exp_count" // 所在频道&场景已浏览的内容有效曝光

    const val SOURCE_ENTRANCE_ID = "source_entrance_id" // 竖版场景请求广告时携带来源（tab2、沉浸式）

    const val VIDEO_AUTO_PLAY = "video_auto_play"       // 信息流请求时，视频是否自动播
    const val SEARCH_WORD = "search_word"               // 搜索词定向

    const val EXT = "ext"                               // 广告公参

    // 频道双列
    const val CHANNEL_SHOW_TYPE = "channel_show_type"   // 固定样式，用于区分发现频道和要闻频道
    const val LIST_LAYOUT_TYPE = "list_layout_type"     // 动态样式字段 在请求时明确当前列表展示样式为单列还是双列
    const val SKIP_HOT_BRIEF_MODULE = "skip_hot_brief_module" // 要闻首刷命中“跳过置顶+热点精选”时透传给广告侧


    // params
    const val IS_INSTALLED: String = "isinstalled"      // 落地页应用直达广告需要拼接上是否安装参数-key

    const val CONTENT_INSERT_AD_CNT: String = "content_insert_ad_cnt"   // loid98请求广告条数

    const val NEWS_SCS = "news_scs" // 反作弊

    const val WX_MINI_GAME_PLAYABLE = "wechat_playable"        // wx试玩小游戏是否初始化成功

    // ========== PCAD特征入模需求新增参数（全局计数，仅 loid=1,33 场景）==========
    // 【数据口径】仅 loid=1、loid=33 两个场景参与计数，技术是全局的加在一起不用区分
    const val PCAD_CONTENT_EXP_COUNT = "content_imp_num"           // session内：内容曝光数
    const val PCAD_AD_REAL_EXP_COUNT = "ad_real_imp_num"           // session内：广告真实曝光数
    const val PCAD_AD_ORIGIN_EXP_COUNT = "ad_raw_imp_num"       // session内：广告原始曝光数
    const val PCAD_RECENT_MINUTE_BROWSE_SPEED = "browsing_speed_per_min"  // 最近一分钟浏览速度（内容+广告有效曝光数）
    const val PCAD_TODAY_ENTER_TIMES = "session_entries"         // 今日进入信息流session数量

}
