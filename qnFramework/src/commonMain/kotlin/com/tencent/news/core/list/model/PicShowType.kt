package com.tencent.news.core.list.model

import com.tencent.news.core.extension.IListEnumDoc


typealias KmmPicShowType = PicShowType

annotation class FeedsPicShowType(val picShowType: Int)

object PicShowType : IListEnumDoc {

    const val SINGLE_IMAGE = 0          // 单图模式
    const val TEXT_ONLY = 1             // 文字模式
    const val MULTI_IMAGE = 2           // 组图模式
    const val BIG_IMAGE = 3             // 大图模式
    const val BIG_VIDEO = 4             // 视频大图
    const val BIG_LIVE = 6              // 大图直播（原 直播+picShowType=3）
    const val CHANNEL_CHOICE_V1 = 24    // 品类精选，v1样式
    const val PAYMENT_CHANNEL_HOT_SELECTION = 31 // 会员频道-热点精选
    const val VIDEO_CHANNEL_V8 = 48     // 视频频道横版大图
    const val WEB_CELL = 108            // 新增通用H5 webcell
    const val HEADER_BAR_MESSAGE = 152  // 类似模块头形式信息条（关注冷启页面：为您推荐精选内容）

    const val EVENT_TIMELINE = 173      // 事件底层，事件脉络

    // ─── 长视频底层页：接口下发的 picShowType（与 Android com.tencent.news.config.PicShowType 完全对齐） ───
    const val TV_LONG_VIDEO_EPISODE = 174   // 长视频电视剧选集模块（对齐 Android LONG_VIDEO_TV_SERIES_CELL=174）
    const val LONG_VIDEO_PROGRAM_CELL = 175 // 长视频综艺/电影专辑列表模块(带封面图)（对齐 Android LONG_VIDEO_PROGRAM_CELL=175）
    const val LONG_VIDEO_DESC_CELL = 176    // 长视频简介cell（对齐 Android LONG_VIDEO_DESC_CELL=176，接口下发场景）
    const val TV_LONG_VIDEO_RECOMMEND = 177 // 长视频推荐模块/猜你想看（对齐 Android LONG_VIDEO_RECOMMEND_CELL=177）

    // ─── 长视频底层页：客户端本地构造的 picShowType（高位号段，避免与后端 174-180 区间冲突） ───
    const val LONG_VIDEO_DESC_LOCAL = 17601 // 长视频底层页简介cell(本地构造，KMM DataRepo 用 intro 拼装)
    const val LONG_VIDEO_COPYRIGHT  = 17602 // 长视频底层页版权声明(本地构造，"本内容来自腾讯视频")

    const val LONG_VIDEO_LIST_ITEM = 180 // 放映厅列表页  长视频 item
    const val FIX_TOP_IMAGE = 200       // 置顶文章-左文右图（高度缩减）
    const val FIX_TOP_TEXT = 201        // 置顶文章-纯文字无图（高度缩减）
    const val TOFU_BLOCK_CELL = 204     // 豆腐块样式，双列小图或者一大带双列小图

    const val IMAGE_BANNER_MODULE = 315    // 图片banner模块
    const val IMAGE_BANNER_CELL = 316      // 图片banner item

    const val LONG_VIDEO_VIP_BANNER = 393      // 腾讯视频会员banner
    const val TV_CATEGORY_ENTRY = 394           // 长视频频道分类入口卡片(articleType=570)
    const val LONG_VIDEO_VIP_BANNER_INTRO = 390 // 长视频底层页VIP开通banner(intro内)

    const val HOT_SPOT_V12 = 404             // 热点精选--长视频频道
    const val VIDEO_HISTORY_MODULE = 414     // 放映厅频道-最近在追(观影历史)模块
    const val LONG_IMAGE = 419                      // 长图
    const val TAG_MODULE_SINGLE_ROW = 422           // 首页分发-单列tag结构体

    const val EVENT_TIMELINE_TEXT_AND_PIC_CELL = 459  // 事件脉络173中左图右文的时间节点
    const val EVENT_TIMELINE_TEXT_VIDEO_CELL = 460     // 事件脉络173中视频的时间节点
    const val EVENT_TIMELINE_TEXT_ONLY_CELL = 461      // 事件脉络173中只有文字的时间节点
    const val CHANNEL_724_MORNING_POST = 462       // 724早报
    const val COLUMN_CATALOGUE_CELL = 481       // 带序号的单图卡片，用于【付费专栏/免费图文合集】目录列表 cell
    const val SINGLE_IMAGE_2: Int = 514         // 单图模式(2行和3行）
    const val UGC_DC_CARD = 517                 // ugc内容卡-双列样式

    const val EVENT_COMMENT = 580               // 事件底层，网友热议cell
    const val EVENT_QA = 583                    // 事件底层，问答cell
    const val CELL_SKIN_BIG_CARD_MODULE = 588   // 带皮肤大卡

    const val EVENT_BANNER_HEADER = 501         // 事件底层，简介头部，横滑banner
    const val EVENT_VIDEO_HEADER = 502          // 事件底层，简介头部，视频
    const val EVENT_MIX_HEADER = 503            // 事件底层，简介头部，标题加文字或秒懂卡

    const val NEWS_LIST_TAG_MODULE_V3 = 510;    // 【垂类】cell：tag展开模块

    const val EVENT_CP_LIST = 518               // cp聚合
    const val EVENT_TOP_CP_LIST = 520           // 优质创作者
    const val CELL_EVENT_COMMENT = 580          // 事件底层，网友热议cell
    const val CELL_EVENT_CARD_V3 = 581          // 老城区事件cell
    const val EVENT_IMGTEXT_HEADER = 586        // 事件底层，图文头部
    const val EVENT_MIAODONG_HEADER = 587       // 事件底层，秒懂头部
    const val CELL_EVENT_IP_HEADER = 590        // 事件IP底层头部cp
    const val CELL_EVENT_IP_SLIDE_MODULE = 591  // 事件IP底层横滑模块
    const val QA_DETAIL_CELL = 592              // 问答底层页cell

    const val HIPPY_724_HOT_LIST_A: Int = 703 // 724频道头部热榜hippy cell-3行样式
    const val HIPPY_724_HOT_LIST_B: Int = 704 // 724频道头部热榜hippy cell-4行样式
    const val HIPPY_724_HOT_LIST_C: Int = 705 // 724频道头部热榜hippy cell-5行样式
    const val LIVE_CARD_724: Int = 711        // 724频道头部直播卡片

    const val EVENT_MARQUEE_HEADER = 720        // 事件底层，横滑头部
    const val EVENT_TIMELINE_HEADER = 908       // 事件脉络底层头部
    const val EVENT_TIMELINE_STATUS_CELL = 9081 // 事件脉络状态cell（实时播报/未读提示）
    const val EVENT_TIMELINE_ABSTRACT_SWITCH_CELL = 9082 // 事件脉络摘要开关cell（无轮询）
    const val EVENT_TIMELINE_NODE_ABSTRACT = 9083 // 脉络摘要节点卡片（含展开/收起，端上自定义）
    const val EVENT_TIMELINE_DETAIL = 909       // 事件脉络底层页
    const val EVENT_HOT_QUESTION_HEADER = 910   // 热问大事件头部
    const val EVENT_TIMELINE_FAKE_HEADER = 911  // fakeheader 用于专题内事件脉络头8像素 适配间距和皮肤
    const val QA_CHANNEL_TIPS_V3 = 641          // 热问频道3.0 更新提示tips cell
    const val QA_CHANNEL_MOD_V3 = 642           // 热问频道3.0 外层item
    const val DISCOVERY_CARD_CELL: Int = 645    // 发现页通用卡片

    const val DETAIL_NEWS_RELATE_QUICK_QA = 614 // 相关推荐快问

    const val EVENT_SECTION_HEADER = 6000
    const val EVENT_SECTION_FOOTER = 6001
    const val EVENT_724_BREAKING_NEWS = 494     // 724tag快讯分发样式V2

    // 早报
    const val MORNING_POST_SECTION = 4701            // 本地类型，早报section
    const val TAG_MORNING_POST_VIDEO_CELL = 470      // 724类型早报TAG视频
    const val TAG_MORNING_POST_IMAGE_CELL = 471      // 724类型早报TAG大图
    const val TAG_MORNING_POST_TEXT_CELL = 472       // 724类型早报TAG无图
    const val CELL_MORNING_POST_RECOMMEND_CELL = 521 // 早报底层页尾部更多推荐
    const val CELL_AI_MORNING_POST_RECOMMEND_CELL = 524 // 早报底层页尾部AI早报
    const val CELL_AI_MORNING_POST_SUBSCRIBE_CELL = 525 // 早报底层页尾部AI订阅复合模块

    const val CELL_MORNING_POST_BANNER_CELL = 924   // 早报Banner卡片

    const val HOT_SPOT_MODE_SWITCH = 593        // 热点精选人工精选小条
    const val HOT_SPOT_EDITOR_PICK_CELL = 594   // 热点精选人工精选大图
    const val QA_NO_IMG = 595                   // 问答feed分发----无图模式
    const val QA_USER_CENTER = 596              // 问答个人中心分发
    const val QA_SINGLE_IMG = 597               // 问答feed分发---单图模式
    const val QA_MULTI_IMG = 598                // 问答feed分发---多图模式

    const val NO_MORE_EDITOR_RECOMMEND_CONTENT = 605    // 刷空条

    const val MIXED_LANDING_VIDEO_WX_HOR_SCROLL_STYLE = 643        // 混合流首条同步微信插件内容 - 横滑样式
    const val MIXED_LANDING_VIDEO_WX_LIST_EXPAND_STYLE = 644       // 混合流首条同步微信插件内容 - 列表展开样式
    const val CP_HOME_PAGE_COLUMN_CELL = 650    // 用户主页专栏cell
    const val PAYMENT_CHANNEL_MY_ASSETS = 654      // 会员频道-我的资产
    const val PAYMENT_CHANNEL_MUST_SEE = 655       // 会员频道-必看热门
    const val PAYMENT_CHANNEL_USER_DYNAMIC = 656   // 会员频道-用户最近动态
    const val PAYMENT_CHANNEL_ARTICLE_HOT_LIST = 660   // 会员频道-文章热榜
    const val PAYMENT_CHANNEL_CP_HOT_LIST = 661        // 会员频道-作者热榜
    const val PAYMENT_CHANNEL_COLUMN_HOT_LIST = 662    // 会员频道-专栏热榜
    const val PAYMENT_CHANNEL_INFO_FEED_TITLE = 669    // 会员频道-信息流标题
    const val CHECK_IN_CELL = 690            // 积分签到cell
    const val CELL_CCTV_HOT_SPOT = 850                  // 央视频频道热点精选
    const val CELL_CCTV_SINGLE_IMAGE = 851              // 央视频单图
    const val CELL_CCTV_RECOMMEND_MORE_PROGRAM = 852    // 央视频更多精彩节目模块

    const val COMMENT_EVENT_HEADER_CELL = 902       // 评论专题头部
    const val COMMENT_EVENT_CELL = 903              // 评论
    const val COMMENT_REPLY_BOTTOM_BAR_CELL = 904   // 评论展开更多
    const val COMMENT_RELATE_ARTICLE_CELL = 905     // 评论原文
    const val COMMENT_DIV_CELL = 906                // 评论分割线

    const val CELL_724_SECTION = 2020               // 事件脉络section头部（‘实时播报’、‘来龙去脉’等）
    const val COLLECTION_SECTION_HEADER = 2021      // 图文合集section头部（'目录'）
    const val CP_COLUMN_CELL = 2024     // 多tab 专栏cell

    // 7000 号段给游戏cell使用；对照 @GameModuleType
    const val GAME_BASE = 7000
    const val GAME_BROADCAST_PIC = 7001         // 轮播图（3 兜底映射成这个）
    const val GAME_HORIZONTAL_LIST = 7002       // 横划模块（24、39、40 都映射成这个）
    const val GAME_GIFT_LIST = 7005             // 福利礼包
    const val GAME_EDIT_LIST = 7009             // 精编列表
    const val GAME_ARTICLE_HOLDER = 7010        // 文章列表占位模块
    const val GAME_GRID_NAVIGATION = 7034       // 宫格导航板块
    const val GAME_CENTER_RANKING = 7037        // 游戏中心排行榜
    const val GAME_CATEGORIES_HEADER = 70431    // 游戏分类头部模块
    const val GAME_CATEGORIES = 7043            // 游戏分类模块
    const val GAME_RETURN_LIST = 7046           // 回流模块
    const val GAME_CALENDAR_LIST = 7047         // 游戏日历模块
    const val GAME_MODULE_TAIL = 7049           // 游戏中心信息流头部，用于分割游戏和信息流
    const val GAME_FEEDS = 7050                 // 游戏中心信息流
    const val GAME_TOP_MODEL = 7051             // 游戏中心顶部专区
    const val GAME_CHECK_IN = 7052              // 游戏签到模块
    const val GAME_CATEGORY_TAB_V2 = 7053       // 游戏分类Tab模块（协议预研）
    const val GAME_SKIN_COMBO = 7054            // 游戏中心顶部换肤组合模块

    // 新版小游戏大厅首页 cell，客户端本地构造
    const val MINIGAME_HALL_USER_GREETING = 7060     // 小游戏大厅用户欢迎区
    const val MINIGAME_HALL_ADD_DESKTOP = 7061       // 小游戏大厅添加到桌面
    const val MINIGAME_HALL_RECENT_PLAYED = 7062     // 小游戏大厅最近在玩
    const val MINIGAME_HALL_THREE_ROW_GROUP_LIST = 7063 // 小游戏大厅三行分组列表
    const val MINIGAME_HALL_RANK_LIST = 7064         // 小游戏大厅排行榜卡片
    const val MINIGAME_HALL_GAME_INFO_ROW = 7065     // 小游戏大厅小游戏信息横条
    const val MINIGAME_HALL_MEDIA_CAROUSEL = 7066    // 小游戏大厅媒体轮播
    const val MINIGAME_HALL_COMMON_HEADER = 7067     // 小游戏大厅通用头部模块
    const val MINIGAME_HALL_BANNER = 7068            // 小游戏大厅 banner

    const val USER_HEADER_INFO = 7100           // 用户个人页头部-头像cell
    const val USER_HEADER_DESC = 7101           // 用户个人页头部-简介cell
    const val USER_HEADER_INTERACTION = 7102    // 用户个人页头部-互动数cell
    const val USER_HEADER_TABS = 7103           // 用户个人页头部-多tab子页面cell

    const val MY_HISTORY_DATE_HEADER = 7200     // 浏览历史日期分组头部

    const val QA_CHANNEL_CELL_V3 = 64201        // 热问频道3.0 答案cell
    const val QA_CHANNEL_TAIL_V3 = 64202        // 热问频道3.0 尾部查看更多cell
    const val CP_SELECT_CELL = 70000            // 【创服】CP列表 cell
    const val CP_SEARCH_EMPTY = 70001           // 【创服】CP搜索空状态 cell
    const val CP_RECOMMEND_LIST_CELL = 929       // 【创服】CP推荐列表 cell
    const val COLUMN_INTRO_CELL = 90093         // 【付费专栏】简介 cell

    // 以下废弃，都改为vm模式了：
    const val SPONSOR_DETAIL_CONTENT_CELL = 90094   // 支持加热-展示内容cell
    const val SPONSOR_PLAN_CELL = 90095             // 支持加热-套餐选择cell
    const val SPONSOR_COMMENT_SELECT_CELL = 90096   // 支持加热-评论选择cell
    const val SPONSOR_PIC_BANNER_CELL = 90097       // 支持加热-文字轮播+背景cell

    const val AI_QA_LIST_CELL = 90098               // 问答AI 列表cell
    const val AI_QA_LIST_TAIL_AIGC_ENTRY = 90099    // 问答AI 列表尾部AIGC入口

    const val ANNOUNCE_BAR = 90100                  // 频道公告Bar
    const val EVENT_NO_COMMENT_PLACEHOLDER = 99001  // 评论专题沙发占位cell
    const val HOT_SPOT_AI_AGGREGATION_CELL = 917    // 热点精选AI cell
    const val EVENT_BIG_AI_IMAGE_CELL = 919    // 专题大图AI cell

    // 高考搜索工具卡片
    const val GAOKAO_TIME      = 816    // 高考日程卡片
    const val GAOKAO_SCORELINE = 817    // 分数线 / 一分一段卡片
    const val GAOKAO_ZHENTI    = 818    // 高考真题（范文 / 真题）卡片
    const val AUDIO_POD_CHANNEL_LIST_CARD = 631          //  播客频道列表卡片
    const val AUDIO_POD_CHANNEL_LATEST_LIST_CARD = 632  //  播客频道最新列表卡片
    const val AUDIO_POD_CHANNEL_CARD = 637              //  播客频道横滑卡片
    const val AUDIO_POD_CHANNEL_REC_PODCAST_CARD = 633  //  播客频道推荐播客卡片
    const val AUDIO_POD_CHANNEL_LATEST_PODCAST_CARD = 634  //  播客频道最新播客标题卡片
    const val AUDIO_POD_CHANNEL_HOT_CATEGORY_CARD = 638 //  播客频道热门分类卡片
    const val AUDIO_POD_CAST_DETAIL_ITEM_CARD = 90101   //  播客详情页卡片
    const val AUDIO_RADIO_CHANNEL_BANNER = 694 // 电台广场顶部频道Banner
    const val AUDIO_RADIO_CHILDREN_CHANNEL_CARD = 695 // 电台儿童频道UI卡片
    const val CELL_TECH_CALENDAR = 803 // 日历
    const val CELL_TECH_CALENDAR_MARQUEE = 804 // 日历 轮播
    const val EVENT_HORIZONTAL_TIME_SLIDER = 912 // 专题页水平滑动时间轴

    // QA问答专题 cell（详见 doc/业务/detail/问答专题/）
    const val CELL_QA_EVENT_LIST = 617                       // 问答专题主列表问题卡
    const val CELL_QA_EVENT_HEADER_MAIN_PIC = 618            // 头部大卡-带头图
    const val CELL_QA_EVENT_HEADER_MAIN_NO_PIC = 619         // 头部大卡-无头图
    const val CELL_QA_EVENT_HEADER_RECOMMEND = 620           // 推荐热问横滑模块
    const val CELL_QA_EVENT_HEADER_TEACHER_ASK = 621         // 名师答疑横滑模块
    const val CELL_QA_EVENT_HEADER_RECOMMEND_CELL = 622      // 推荐热问子卡
    const val CELL_QA_EVENT_HEADER_TEACHER_INNER_CELL = 623  // 名师答疑子卡
    // 注：910 EVENT_HOT_QUESTION_HEADER 已存在，不再重复
}
