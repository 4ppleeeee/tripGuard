@file:Suppress("MaxLineLength")

package com.tencent.news.core.pop

import com.tencent.news.core.extension.IKmmKeep
import com.tencent.news.core.extension.safeDecode
import com.tencent.news.core.platform.api.getShiplyConfig
import com.tencent.news.core.router.contants.ComposeViewKey
import com.tencent.news.core.serializer.KtJson
import kotlinx.serialization.Serializable

/**
 * 双端公用弹窗优先级（数字越大的，优先级越高）
 *
 * - 结构:(id, priority)
 * - 隐私弹窗、升级弹窗理论上优先级应当高于其他普通弹窗
 */
// 【新闻客户端弹窗梳理】https://doc.weixin.qq.com/sheet/e3_ANgAJQbbACk0CXloa9kQwu07BhAu6?scode=AJEAIQdfAAo3Wn2iJvANgAJQbbACk&tab=BB08J2
// todo 【开发规范】新增弹窗时注意：
//  1. 与产品敲定优先级排序
//  2. 新增枚举值后，注释里要贴上示意图url（可以从tapd的图片里直接复制一个）
//  3. 广告新增枚举，需要带有AD_前缀
//  warn:每个弹窗标志唯一一个弹窗
enum class PopType(
    private val defaultPriority: Int,
    val implType: PopImplType = PopNativeImplType(),
    val triggerType: TriggerType = TriggerType.DEFAULT
) : IKmmKeep {
    PRIVACY_DIALOG(9999999),                // 隐私弹窗
    EXIT_VISIT_MODE_BTN(9999999),           // 仅浏览模式底部按钮
    PRIVACY_SURVEY_DIALOG(9999998),         // 仅浏览模式调研弹窗 https://tapd.woa.com/tapd_fe/10045201/story/detail/1010045201127803811
    CONTENT_TAB_DIALOG(9981),               // 图文侧边目录弹窗
    CAST_DEVICE_DIALOG(9991),               // 投屏弹窗
    VIDEO_PRIVACY_DIALOG(9990),             // 视频隐私弹窗
    CAST_DIALOG(9980),                      // 投屏弹窗
    YUN_GAME_LOGIN_DIALOG(9700),            // 云游戏登陆弹窗
    FORCE_UPDATE_DIALOG(9600),              // 强制升级弹窗
    UPDATE_DIALOG(9500),                    // 升级弹窗，应该优先级高于普通弹窗
    VERSION_UPDATE_DIALOG(9490),            // 新版本升级提示弹窗，https://tapd.woa.com/tapd_fe/10045201/story/detail/1010045201128884581
    LOGIN_PRIVACY_DIALOG(9450),             // 登录隐私弹窗
    BAN_COMMENT_DIALOG(9430),               // 评论禁言弹窗 https://wdoc-76491.picgzc.qpic.cn/MTY4ODg1NTkyNjI4NDI5NA_443179_EJ0E0VxD8Lw5buhM_1725433980?w=727&h=512
    REFERENCE_ARTICLE_DIALOG(9420),         // 参考文章弹窗 https://www.figma.com/design/j4vy3mJIzaPmb7TnKUiFQH/%E4%B8%93%E9%A2%98-%E7%83%AD%E7%82%B9%E7%B2%BE%E9%80%89-AI%E6%A8%A1%E5%9D%97?node-id=396-699&t=zUyRKkGUWuKCn6kn-0
    EXTREME_WEATHER_DIALOG(9410),           // 极端天气弹窗 https://tapd.woa.com/tfl/captures/2025-10/tapd_10045201_base64_1761289450_291.png
    AD_GAME_RESERVE_DIALOG(9402),              // 游戏预约弹窗
    AD_GAME_SUB_DIALOG(9401, triggerType = TriggerType.USER_OPERATE),                  // 云游戏功能介绍弹窗
    AD_GAME_DIALOG(9400),                      // 文底云游戏弹窗，不会自动弹
    FULL_SCREEN_LOADING_DIALOG(9395),           // 全屏loading弹窗
    PAYMENT_LOADING_DIALOG(9391),               // 付费loading弹窗
    RIGHTS_THANKS_LETTER_DIALOG(
        9390,
        implType = PopComposeImplType(ComposeViewKey.Pay.VIP_THANKS_LETTER_DIALOG),
        triggerType = TriggerType.USER_OPERATE
    ),                                          // 会员/专栏购买后感谢信弹窗
    RIGHTS_REMINDER_PAY_ERROR_DIALOG(9380),     // 支付异常
    RIGHTS_REMINDER_PAY_CANCEL_DIALOG(9370),    // 支付取消
    RIGHTS_REMINDER_PAY_SUCCESS_DIALOG(9360),   // 支付成功
    RIGHTS_REMINDER_PAY_PRIVACY_DIALOG(9350),   // 支付权限隐私弹窗
    RIGHTS_REMINDER_PAY_DIALOG(9340),           // 付费会员支付弹窗
    LIVE_SWITCH_STREAM_DIALOG(9340),            // 专栏支付弹窗(支付类弹窗优先级降低)
    COLUMN_PAYMENT_DISCOUNT_DIALOG(9331),       // 专栏折扣弹窗(支付类弹窗优先级降低)
    COLUMN_PAYMENT_RECHARGE_DIALOG(9330),       // 专栏支付弹窗(支付类弹窗优先级降低)
    COLUMN_PAYMENT_CHOOSE_DIALOG(9320),         // 专栏支付弹窗(支付类弹窗优先级降低)
    COLUMN_PAYMENT_INFO_DIALOG(9310),           // 专栏支付弹窗(支付类弹窗优先级降低)
    COLUMN_GIFT_CLAIM_DIALOG(
        9311,
        implType = PopComposeImplType(ComposeViewKey.Pay.COLUMN_GIFT_CLAIM_DIALOG),
        triggerType = TriggerType.USER_OPERATE
    ),                                          // 专栏免费领取弹窗（用户进入专栏页自动弹出）
    COLUMN_DETAIL_DIALOG(
        9309,
        implType = PopComposeImplType(ComposeViewKey.Column.COLUMN_DIALOG),
        triggerType = TriggerType.USER_OPERATE
    ),                                          // 专栏详情弹窗（半屏浮层），用户点击触发
    TAB2_COLD_START_SETTING_DIALOG(9306),       // 冷启动进入tab2引导切换默认主页弹窗，https://file.tapd.woa.com/compress/compress_img/1400/tapd_10045201_base64_1747191378_835.png?src=/tfl/captures/2025-05/tapd_10045201_base64_1747191378_835.png
    MEDAL_LOGIN_BOTTOM_TIP(9305),               // 勋章领取引导登录弹窗，https://news-bz-1258344701.shiply-cdn.qq.com/reshub/qqnews_ios/commonfile/formal/20250305165933/Production/medal_login_popup.png
    AI_ERROR_WORD_DIALOG(9301),                 // 错别字划词交互弹窗
    ERROR_WORD_DIALOG(9300),                    // 划词后点错别字交互弹窗
    AIGC_FEEDBACK_DIALOG(9290),                 // 新闻妹点踩反馈弹窗
    NICK_SETTING_DIALOG(9280),                  // 设置昵称弹窗
    PUSH_RETAIN_DIALOG(9210),                   // push挽留弹窗
    NOTIFY_GUIDE_DIALOG(9200),                  // 通知次数用户授权弹窗
    UNINSTALL_OPT_FONT_SIZE_DIALOG(9190),       // 卸载挽留字号调整弹窗
    COMMENT_VOTE_DIALOG(9180),                  // 评论投票弹窗
    INTEREST_SELECT_DIALOG(9010),               // 用户兴趣选择弹窗
    REDPACKET_DIALOG(9003),                     // 红包弹窗
    PORTRAIT_FRAME_DIALOG(9002),                // 个人头像框弹窗
    LIVE_BACKGROUND_PLAY_DIALOG(9001),          // 直播后台播放提示弹窗
    ACTIVITY_DIALOG(9000),                      // 老的升级弹窗和活动弹窗
    VISIT_MODE_GOLDEN_COIN_PRIVACY_DIALOG(8800), // 仅浏览金币弹窗,https://tapd.woa.com/tapd_fe/10045201/story/detail/1010045201133291552
    VISIT_MODE_BONUS_DIALOG(8700),              // 仅浏览福利弹窗,https://tapd.woa.com/tapd_fe/10045201/story/detail/1010045201125099815
    AD_PAUSE_DIALOG(8500),                      // 下载挽留弹窗
    CHANNEL_FEATURE_DIALOG(8200),               // 频道功能迁移弹窗 https://tapd.woa.com/tapd_fe/10045201/story/detail/1010045201129719961
    CHANGE_LOCAL_CHANNEL_DIALOG(8010),          // 切地方站弹窗
    HOT_TRACK_DIALOG(8005),                     // 热点追踪弹窗 https://tapd.woa.com/tapd_fe/10045201/story/detail/1010045201131693731
    HOT_TRACK_FOCUS_DIALOG(8004),               // 热点追踪关注成功弹窗 https://tapd.woa.com/tapd_fe/10045201/story/detail/1010045201132188613
    HOT_TRACK_PUSH_TIPS(8003),                  // 图文push落地页热点追踪通用大弹窗, https://tapd.woa.com/tapd_fe/10045201/story/detail/1010045201135052385
    V2_ARTICLE_FONT_SIZE_ADJUST_GUIDE(8002),    // v2版小字文章引导调大字号弹窗, https://news-bz-1258344701.shiply-cdn.qq.com/reshub/qqnews_ios/commonfile/formal/20250305172003/Production/v2_font_size_adjust.png
    LITTLE_HELPER_DIALOG(8001),                 // 添加小助手弹窗, https://news-bz-1258344701.shiply-cdn.qq.com/reshub/qqnews_android/commonfile/formal/20241209145156/Production/little_helper.png
    CLEAN_CACHE_DIALOG(8000),                   // 清缓存弹窗
    USER_GROWTH_KOULING_FULLSCREEN(7901),       // 口令烟花弹窗
    USER_GROWTH_KOULING_MIDDLE(7900),           // 口令任务金弹窗
    AD_ONESHOT_BROKEN(7890),                    // 广告 oneshot 破窗
    AD_CSHOT(7890),                             // 广告 oneshot 蒙太奇破窗
    AD_ONESHOT(7880),                           // 广告 oneshot 尾帧回缩动效
    AD_SUPER_DIALOG(7850),                      // 广告超级蒙层 全屏
    AD_COMPOSE_SUPER_DIALOG(
        7850,
        PopComposeImplType(ComposeViewKey.Ad.Dialog.SUPER_MASK)
    ),                                                         // 广告超级蒙层 全屏
    AD_BRAND_GIFT(7840),                        // 广告 品牌献礼 全屏
    AD_BOTTOM_FLOAT(7801),                      // 广告 底浮追光 底部
    AD_FOLLOW_U(7800),                          // 广告 followU 右下
    AD_FULL_SCREEN_DIALOG(7770),                // 广告 push，图文底层页全屏弹窗
    AD_HIGHLIGHT_PENDANT(7760),                 // 高光挂件
    AD_OLYMPIC_PENDANT(7750),                   // 奥运挂件
    LOGIN_EXPIRED_BOTTOM_DIALOG(7703),          // 过期重新登录
    USER_GROWTH_BOTTOM(7702),                   // 增长底部弹窗
    USER_GROWTH_BOTTOM_AUTO_DISMISS(7701),      // 增长底部自动隐藏弹窗
    USER_GROWTH_BOTTOM_MINI_BAR(7700),          // 增长右下角弹窗
    LANDING_COIN_MIX_VIDEO_POP(7622),           // 金币任务引导主启动混合流弹窗 --story=135052659 【落地页】落地页主启签到任务
    LANDING_COIN_MIX_VIDEO_RIGHT_BOTTOM(7621),  // 金币任务引导主启动混合流右下角挂件
    LANDING_COIN_MAIN_BOTTOM(7620),             // 金币任务引导首页底部小条
    PUSH_SWITCH_BOTTOM_DIALOG(7610),            // 引导开启push开关的弹窗
    PUSH_DIALOG_BOTTOM(7600),                   // 疫情push底部bar弹窗
    CONTINUE_CHECK_IN_DIALOG(7595),             // 继续签到引导弹窗
    USER_INTEGRAL_CHECK_IN_DIALOG(7590),        // 要闻增加签到领积分弹窗
    USER_POINTS_TASK_CENTER_DIALOG(
        7589,
        implType = PopComposeImplType(ComposeViewKey.User.POINTS_TASK_CENTER_POPUP_DIALOG),
        triggerType = TriggerType.USER_OPERATE
    ),                                          // 积分任务中心任务结果弹窗 https://www.figma.com/design/c4P0cbywW0SjezIDhvvzp3/%E7%A7%AF%E5%88%86%E4%B8%AD%E5%BF%83?node-id=60-985
    USER_SURVEY(7585),                          // 用户调研弹窗，https://file.tapd.woa.com/compress/compress_img/700/tapd_10045201_base64_1727416943_387.png?src=/tfl/captures/2024-09/tapd_10045201_base64_1727416943_387.png
    OM_DECLARATION_DIALOG_BOTTOM(7590),         // 企鹅号自主声明弹窗（比如是否是AI生成）
    OM_DECLARATION_DIALOG_SELECT_SOURCE(7591),  // 企鹅号自主声明弹窗: 选择来源
    USER_GROWTH_FULLSCREEN(7501),               // 增长全屏弹窗
    USER_GROWTH_MIDDLE(7500),                   // 增长middle弹窗
    PERSONALIZED_RULE_DIALOG(7400),             // 个性化定推弹窗
    COMMENT_ENV_TIP_DIALOG(7200),               // 首次评论弹窗
    LIVE_START_TIP(7010),                       // 直播开播提示
    LAST_READ_TIP(7000),                        // 上次阅读提示
    READ_MORE_TIP(6990),                        // 广告遮挡时的阅读引导提示
    BOTTOM_BAR_TIP(6980),                       // 底部个性小条
    OM_STATE_BOTTOM_DIALOG(6010),               // 企鹅号居底弹窗样式
    LONG_VIDEO_PRIVACY_DIALOG(6000),            // 长视频隐私协议
    OM_STATE_DIALOG(5990),                      // 新闻号登录注册状态提醒弹窗
    VIP_MOVE_CHANNEL_DIALOG_BOTTOM(5980),       // 开通Vip后提示前移频道
    HOT_DIALOG(5900),                           // 热点弹窗
    SPORT_VIP_EXPIRED_TIP(5800),                // 体育会员到期提醒
    VIP_EXPIRED_TIP(5750),                      // 会员到期提醒
    PAY_COLUMN_FREE_CLAIM_HOME_TIP(5745),       // 付费专栏免费领取首页弹窗，https://tapd.woa.com/tapd_fe/10045201/story/detail/1010045201134291266
    VIP_COLUMN_UPDATE_TIP(5740),                // '会员专区'更新提醒
    PAY_COLUMN_UPDATE_TIP(5739),                // '付费专栏'更新提醒：https://tapd.woa.com/tfl/captures/2024-10/tapd_10045201_base64_1728469072_632.png
    IP_VIDEO_APPOINTMENT(5730),                 // 出品预约
    LONG_VIDEO_APPOINTMENT(5720),               // 长视频, 放映厅预约
    LIVE_VIDEO_APPOINTMENT(5710),               // 直播预约
    EARTHQUAKE_REMINDER_GUIDE(5707),            // 地震提醒
    TAB4_INTERACT_TIP(5706),                    // Tab4互动提醒弹窗（点赞，回复等）https://file.tapd.woa.com/compress/compress_img/700/tapd_10045201_base64_1741241733_505.png?src=/tfl/captures/2025-03/tapd_10045201_base64_1741241733_505.png
    WEATHER_REMINDER_GUIDE(5705),               // 天气提醒
    OPERATION_CONFIG_PENDANT(5702),             // 运营配置挂件（普通挂件+弹窗挂件）https://tapd.woa.com/tapd_fe/10045201/story/detail/1010045201132761501

    CHANNEL_SUBSCRIPTION(5700),                 // 频道订阅
    CHANNEL_SUBSCRIPTION_TIPS(5600),            // 首页频道订阅
    LONG_VIDEO_START_TIP(5500),

    COMPOSE_BRIDGE_DIALOG(3000),                // compose弹窗桥接native弹窗
    PRAISE_DIALOG(3000, triggerType = TriggerType.USER_OPERATE),   // 好评弹窗
    H5_POP_UP_DIALOG(2000),
    PLUGIN_DOWNLOAD_TIPS(1500),                 // 插件下载小条
    CLOUD_GAME_TRY_PLAY_END(1100),
    CLOUD_GAME_EXPIRE_DIALOG(1050),
    CITY_SELECT_IOS(1001),                      // 城市选择ios弹窗
    AD_YUN_GAME_WAIT_LOADING(1000),                // 云游戏loading
    HOMETOWN_GUIDE(1000), // 城市选择【【安卓】【图文底层&tab2&沉浸式】用户家乡信息收集[7850/2D跨迭代]】https://tapd.woa.com/tapd_fe/10045201/story/detail/1010045201129170671
    LOCATION_APP_PERMISSION(1000),              // 端内位置权限授权弹窗
    COMMON_CHANNEL_DIALOG_BOTTOM(999),          // 通用兜底弹窗
    AD_GAME_DOWNLOAD_BOTTOM_BAR(900),           // 首页游戏小条下载提醒
    AD_APPOINTMENT_BOTTOM_BAR(801),             // 首页广告预约小条
    AD_APPOINTMENT_CARD(800),                   // 首页广告预约大卡
    PICK_CARE_DIALOG(709),                      // 精选标签弹窗
    AD_SHORT_CUT_DIALOG(708),                   // 小游戏关闭挽留弹窗
    SENSITIVE_CARE_DIALOG(707),                 // 敏感词弹窗
    PUSH_FEED_BACK_DIALOG(706),                 // 发文弹窗
    CO_CREATOR_DIALOG(705),                     // “梦幻联动”发布
    GOT_RED_FLOWER_DIALOG(704),                 // 获取小红花弹窗
    RED_FLOWER_DIALOG(703),                     // 小红花弹窗
    TAG_724_FILTER_DIALOG(702),                 // 724筛选弹窗
    QR_CODE_LOGIN_DIALOG(701),                  // 扫码登陆弹窗
    REC_CHANNEL_DIALOG(700),                    // 运营推荐弹窗
    NO_INTEREST_BOTTOM_DIALOG(699),             // tab2 不感兴趣弹窗
    FAST_SPEED_DIALOG(698),                     // tab2 倍速弹窗
    DEBUG_CARE_VIDEO_DIALOG(697),
    AIGC_HOT_SPOT_UPDATE_DIALOG(696),           // 新闻妹热点更新提示条 https://news-bz-1258344701.shiply-cdn.qq.com/reshub/qqnews_android/commonfile/formal/20241031110037/Production/111.png
    AIGC_MORNING_POST_DIALOG(695),              // 用户私人定制新闻日报 https://file.tapd.woa.com/compress/compress_img/1400/tapd_10045201_base64_1745233567_754.png?src=/tfl/captures/2025-04/tapd_10045201_base64_1745233567_754.png

    AD_MINI_GAME_EXIT_RETAIN_DIALOG(
        691,
        triggerType = TriggerType.USER_OPERATE
    ),                                                        // 系统返回键退出 app 小游戏挽留, https://tapd.woa.com/tapd_fe/10045201/story/detail/1010045201135411703

    GREETING_EXIT_DIALOG(
        690,
        triggerType = TriggerType.USER_OPERATE
    ),                                                         // 退出 toast 优化为问候语, https://tapd.woa.com/tapd_fe/10045201/story/detail/1010045201135039749
    VIDEO_WIFI_DIALOG(680),                     // 视频wifi
    MEDAL_GAINED_DIALOG(670),                   // 提醒获得新勋章，例如：Tab4 https://news-bz-1258344701.shiply-cdn.qq.com/reshub/qqnews_android/commonfile/formal/20250313192307/Production/Screenshot_20250313_192123_com_tencent_news_SplashActivity.jpg
    TAB4_CREATOR_ACHIEVEMENT_POSTER_DIALOG(
        668,
        implType = PopComposeImplType(ComposeViewKey.User.CREATOR_ACHIEVEMENT_POSTER_DIALOG)
    ),                                          // tab4 作者成就海报弹窗（优先级：广告 > 勋章 > 成就海报）；期望 PopTaskBuilder.dismissSelfByHigherPriority=true，由接入层在 toComposeDialog 平台实现处补齐 https://tapd.woa.com/tapd_fe/10045201/story/detail/1010045201132823977 协议 https://iwiki.woa.com/p/4020324532
    DESKTOP_SHORT_CUT_DIALOG(667),              // 快捷方式弹窗 https://tapd.woa.com/tapd_fe/10045201/story/detail/1010045201126062178
    CHANNEL_DESKTOP_SHORT_CUT_DIALOG(666),      // 二级频道快捷方式弹窗  https://tapd.woa.com/tapd_fe/10045201/story/detail/1010045201126062178
    CHANNEL_SHARE_DIALOG(665),                  // 频道分享弹窗   https://tapd.woa.com/tapd_fe/10045201/story/detail/1010045201128834050
    RED_FLOWER_AVATAR_FRAME_DIALOG(650),        // 小红花头像框  https://tapd.woa.com/tapd_fe/10045201/story/detail/1010045201126075691
    CP_MEMBERAREA_DIALOG(100),                  // 会员专区半浮层弹窗
    AD_APPOINTMENT_DIALOG(0),                   // 广告预约弹窗 https://tapd.woa.com/tapd_fe/10161211/story/detail/1010161211873562601
    AD_GAME_RESERVE_PANEL(0),                   // 游戏预约弹窗 https://tapd.woa.com/tapd_fe/10045201/story/detail/1010045201124827810
    AD_ARTICLE_BOTTOM_NFT(0),                   // 文章底浮广告 https://tapd.tencent.com/tapd_fe/10045201/story/detail/1010045201883453823

    COLLECTION_DIALOG(
        0,
        implType = PopComposeImplType(ComposeViewKey.Collection.COLLECTION_DIALOG),
        triggerType = TriggerType.USER_OPERATE
    ),
    AD_GAME_CANCEL_RESERVE_PANEL(
        0,
        implType = PopComposeImplType(ComposeViewKey.Ad.Game.CANCEL_RESERVE_DIALOG),
        triggerType = TriggerType.USER_OPERATE
    ),                                          // 游戏预约取消弹窗 https://tapd.woa.com/tapd_fe/10045201/story/detail/1010045201124827810
    QUICKLY_UPDATE_INSTALL_FAIL(
        0,
        triggerType = TriggerType.USER_OPERATE
    ),                                          // 极速弹窗挽留弹窗 https://tapd.woa.com/tapd_fe/10045201/story/detail/1010045201124443357
    MEDAL_LOGIN_GUIDE_POP_VIEW(0),              // 勋章领取引导弹窗 https://tapd.woa.com/tapd_fe/10045201/story/detail/1010045201121956673
    OPEN_PERSONAL_RECOMMEND_TIP(0),
    OPEN_PERSONAL_RECOMMEND_TIP_SUB_MENU(0),
    PLUGIN_DOWNLOAD_BOTTOM_BAR(0),              // 插件下载弹窗
    AI_DECLARATION_DIALOG(
        0,
        implType = PopComposeImplType(ComposeViewKey.Editor.DETAIL)
    ),                                          // 【创服】AI声明弹窗 https://tapd.woa.com/tapd_fe/10045201/story/detail/1010045201126455629
    SELECT_COLLECTION_DIALOG(
        0,
        implType = PopComposeImplType(ComposeViewKey.Editor.SELECT_COLLECTION)
    ),                                           // 【创服】合集选择弹窗 https://tapd.woa.com/tapd_fe/10045201/story/detail/1010045201132068354
    AI_AVATAR_PAGE(
        0,
        implType = PopComposeImplType(ComposeViewKey.Aigc.AVATAR_PAGE)
    ),                                          // ai头像弹窗 https://tapd.woa.com/tapd_fe/10045201/story/detail/1010045201122428860
    VOICE_INPUT_DIALOG(
        0,
        implType = PopComposeImplType(ComposeViewKey.Voice.VOICE_INPUT)
    ),                                          // 【【安卓】增加语音搜索入口】https://tapd.woa.com/tapd_fe/10045201/story/detail/1010045201132140340
    DEBUG_CHANNEL(0),
    PLAY_LIST_DIALOG(
        0,
        implType = PopComposeImplType(ComposeViewKey.Audio.PLAY_LIST_DIALOG)
    ),                                          // 播放列表弹窗 https://tapd.woa.com/tapd_fe/10045201/story/detail/1010045201125450455
    SPONSOR_DETAIL(
        0,
        implType = PopComposeImplType(ComposeViewKey.Sponsor.DETAIL)
    ),                                          // 内容加热弹窗 https://tapd.woa.com/tapd_fe/10045201/story/detail/1010045201123881496
    AI_AUDIO_AGREE_DIALOG(
        0,
        implType = PopComposeImplType(ComposeViewKey.Audio.AI_AUDIO_AGREE_DIALOG)
    ),                                          // 腾讯新闻AI播客服务协议弹窗 https://tapd.woa.com/tapd_fe/10045201/story/detail/1010045201125450455
    EYE_PROTECTION_MODE_DIALOG(0),              // 护眼模式弹窗 https://tapd.woa.com/tapd_fe/10045201/story/detail/1010045201121464612
    REVOKE_PHONE_DIALOG(0),
    VIDEO_ERROR_FEEDBACK_DIALOG(0),             // tab2 视频反馈入口 https://tapd.woa.com/tapd_fe/10045201/story/detail/1010045201121280536
    CHANGE_TIMBRE(
        0,
        implType = PopComposeImplType(ComposeViewKey.ChangeTimbre.PAGE)
    ),                                          // 音色弹窗 https://tapd.woa.com/tapd_fe/10045201/story/detail/1010045201123633580
    MEDAL_GAINED_BOTTOM_TIP(0),                 // 【登录】勋章获取弹窗 https://tapd.woa.com/tapd_fe/10045201/story/detail/1010045201121956673
    WX_LANDING_ARTICLE_DIALOG(0),               // 微信同步内容弹窗 https://tapd.woa.com/tapd_fe/10045201/story/detail/1010045201117114701
    // 微信插件快速退出挽留弹窗
    // https://tapd.woa.com/tapd_fe/10045201/bug/detail/1010045201159489395
    WX_RETAIN_ARTICLE_DIALOG(0),
    QA_CONTINUE_EDIT_GUIDE(0),                  // 【客户端】首页继续编辑引导 https://tapd.woa.com/tapd_fe/10045201/story/detail/1010045201115743755
    CITY_SELECT(
        0,
        implType = PopComposeImplType(ComposeViewKey.Location.CITY_SELECT)
    ),                                          // 城市选择【【安卓】【图文底层&tab2&沉浸式】用户家乡信息收集[7850/2D跨迭代]】https://tapd.woa.com/tapd_fe/10045201/story/detail/1010045201129170671
    AD_GAME_NOTIFICATION_DIALOG(
        0,
        implType = PopComposeImplType(
            ComposeViewKey.Ad.Game.NOTIFICATION_DIALOG,
            ComposeImplLevel.OVERLAY
        )
    ),                                          // 游戏通知弹窗（要闻首页底部通知卡片）
    DEMO_DIALOG(0),                             // 用于demo测试弹窗逻辑是否正常
    TEST_COMPOSE_VIEW_DIALOG(
        0,
        implType = PopComposeImplType(
            ComposeViewKey.Dialog.DEMO_VIEW_DIALOG,
            ComposeImplLevel.OVERLAY
        )
    );                                          // 测试 Overlay 级别 Compose 弹窗

    // todo 【开发规范】再次提醒！新增弹窗时注意：
    //  1. 与产品敲定优先级排序
    //  2. 新增枚举值后，注释里要贴上示意图url（可以从tapd的图片里直接复制一个）

    fun getPriority(isLocal: Boolean = true): Int {
        if (isLocal) {
            return defaultPriority
        }
        return RemotePopConfig.typeList?.find {
            it.type == name
        }?.priority ?: defaultPriority
    }
}

@Serializable
internal data class RemotePopType(
    val type: String = "",
    val priority: Int = 0,
) : IKmmKeep

internal object RemotePopConfig {
    val typeList by lazy {
        KtJson.safeDecode<List<RemotePopType>>(getShiplyConfig("pop_dialog_config"))
    }
}
