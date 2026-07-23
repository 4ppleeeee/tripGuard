package com.tencent.news.core.compose.scaffold.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import com.tencent.kuikly.compose.ui.geometry.Offset
import com.tencent.kuikly.compose.ui.graphics.Brush
import com.tencent.kuikly.compose.ui.graphics.Brush.Companion.linearGradient
import com.tencent.kuikly.compose.ui.graphics.Color
import com.tencent.news.core.compose.scaffold.modifiers.changeAlpha

val LocalColorScheme = staticCompositionLocalOf { LightColorScheme }

@Immutable
data class AigcColorScheme(
    val prompt: Color,
    val prompt05: Color,
    val prompt25: Color,
    val bg: Color,
    val bgSendStart: Brush,
    val bgSendEnd: Brush,
    val lineCard: Color,
    val qaText: Color,
    val purple: Color,
    val purpleBg12: Color,
    val purpleButonBg: Color,
    val stroke: Color,
    val entryStart: Color,
    val entryEnd: Color,
    val streamStart: Color,
    val streamEnd: Color,
    val bottomBarBorder: Color,
    val bottomBarVoiceCancelBorder: Color,
    val bottomBarBottomMask: Brush,
    val bottomBarTopMask: Brush,
    val bottomBarShadowSpot: Color,
    val bottomBarShadowAmbient: Color,
    val yuanbaoGreen: Color,
    val yuanbaoGreen05: Color,
    val yuanbaoGreenBg: Color,
    val skinColor: Color? = null,  // 后台下发的皮肤色，为 null 表示未配置皮肤色
    val posterGrayBg: Color,  // 对话列表海报灰色背景
    val posterBlueBg: Color,  // 选中文本渐变海报蓝色背景
    val digitalBgMask: Brush,  // 数字人背景遮罩
    val inlineVideoBackground: Color,  // AI订阅内联视频播放器背景
    val inlineVideoCoverMask: Color,  // AI订阅内联视频封面遮罩
    val inlineVideoPlayIconBackground: Color,  // AI订阅内联视频播放按钮背景
    val inlineVideoMuteButtonBackground: Color,  // AI订阅内联视频静音按钮背景
)

@Immutable
data class CoinCenterColorScheme(
    val pageBackground: Color,                    // 金币中心页面底色
    val bgGradientStart: Color,                   // 金币中心头部背景渐变起始色
    val bgGradientMiddle: Color,                  // 金币中心头部背景渐变中间色
    val bgGradientEnd: Color,                     // 金币中心头部背景渐变结束色
    val primaryRed: Color,                        // 金币中心布局主红色
    val textPrimaryRed: Color,                    // 金币中心文本主红色
    val summaryPrimaryRed: Color,                 // 金币中心摘要卡主红色
    val summaryCardBackground: Color,             // 金币中心摘要卡背景
    val summaryCardBorder: Color,                 // 金币中心摘要卡边框
    val videoTaskPrimaryRed: Color,               // 金币中心视频任务主红色
    val videoTaskWarmBackground: Color,           // 金币中心视频任务暖色背景
    val videoTaskSecondaryBackground: Color,      // 金币中心视频任务次级背景
    val videoTaskEnvelopeUnavailableText: Color,  // 金币中心视频任务不可用红包文字色
    val checkInCardBackground: Color,             // 金币中心新用户签到卡背景
    val checkInPanelBackground: Color,            // 金币中心新用户签到奖励面板背景
    val checkInRewardPillBackground: Color,       // 金币中心新用户签到奖励数字底色
    val checkInRewardPillBorder: Color,           // 金币中心新用户签到奖励数字边框
    val checkInRewardText: Color,                 // 金币中心新用户签到奖励数字文字
    val checkInClaimedRewardPillBackground: Color, // 金币中心新用户签到已领取数字底色
    val checkInClaimedRewardPillBorder: Color,    // 金币中心新用户签到已领取数字边框
    val checkInClaimedRewardText: Color,          // 金币中心新用户签到已领取数字文字
    val checkInDayText: Color,                    // 金币中心新用户签到天数文字
    val checkInClaimedText: Color,                // 金币中心新用户签到已领取文字
    val checkInButtonBackground: Color,           // 金币中心新用户签到按钮底色
    val checkInButtonText: Color,                 // 金币中心新用户签到按钮文字
    val checkInSuccessDialogBackground: Color,    // 金币中心新用户签到成功弹窗背景
    val mainLaunchCheckInCardBackground: Color,   // 金币中心主启签到卡背景
    val mainLaunchCheckInPanelBackground: Color,  // 金币中心主启签到列表背景
    val mainLaunchCheckInDayItemBackground: Color, // 金币中心主启签到单日背景
    val mainLaunchCheckInTodayDayItemBackground: Color, // 金币中心主启签到当日单日背景
    val mainLaunchCheckInTitle: Color,            // 金币中心主启签到标题文字
    val mainLaunchCheckInTitleHighlight: Color,   // 金币中心主启签到标题高亮文字
    val mainLaunchCheckInDesc: Color,             // 金币中心主启签到说明文字
    val mainLaunchCheckInButtonBackground: Color, // 金币中心主启签到按钮底色
    val mainLaunchCheckInButtonText: Color,       // 金币中心主启签到按钮文字
    val mainLaunchGuideDialogBackground: Brush,   // 金币中心主启签到引导弹窗背景
    val mainLaunchTitleImageUrl: String,          // 金币中心主启签到标题图
    val mainLaunchHeaderIconUrl: String,          // 金币中心主启签到头图
    val mainLaunchUnsignedDayIconUrl: String,     // 金币中心主启签到未签到日间图
    val mainLaunchUnsignedIconUrl: String,        // 金币中心主启签到未签到当前主题图
    val mainLaunchDirectRewardBgUrl: String,      // 金币中心主启签到直领奖励弹窗背景
    val mainLaunchDoubleRewardBgUrl: String,      // 金币中心主启签到翻倍奖励弹窗背景
    val rewardDialogBackground: Color,            // 金币中心奖励弹窗背景
    val rewardDialogBorder: Color,                // 金币中心奖励弹窗边框
    val rewardDialogCream: Color,                 // 金币中心奖励弹窗浅金底色
    val rewardDialogGold: Color,                  // 金币中心奖励弹窗金色描边
    val rewardDialogTextRed: Color,               // 金币中心奖励弹窗红色文字
    val rewardDialogValueCream: Color,            // 金币中心奖励弹窗奖励数值文字
    val rewardDialogSubText: Color,               // 金币中心奖励弹窗辅助文字
    val rewardDialogTipsBackground: Color,        // 金币中心奖励弹窗提示背景
)

@Immutable
data class PointsCenterColorScheme(
    val dailyTaskTabsInactiveGradientTop: Color,  // 积分中心每日任务未选中 Tab 渐变顶部色
    val dailyTaskTabsInactiveGradientBottom: Color,  // 积分中心每日任务未选中 Tab 渐变底部色
)

@Immutable
data class AudioPodChannelColorScheme(
    val cardShadowColor: Color,  // 播客卡片底部阴影颜色
    val playIconColor: Color,    // 播放按钮图标颜色
    val listenLaterBg: Color,    // "我的稍后听"按钮背景色
    val playButtonBg: Color,     // 列表项播放按钮背景色
    val hotCategoryItemBg: Color,      // 热门分类item背景色
    val hotCategoryItemShadow: Color,  // 热门分类item阴影色
    val viewMoreBg: Color,             // "查看更多"组件背景色
    val viewMoreBorder: Color,         // "查看更多"组件边框颜色
    val viewMoreText: Color,           // "查看更多"组件字体颜色
)

@Immutable
data class SportColorScheme(
    val textPrimary: Color,         // 主要文字（纯白）
    val reservedBtnBg: Color,       // 已预约按钮背景 rgba(255,255,255,0.1)
    val reservedBtnText: Color,     // 已预约按钮文字 rgba(255,255,255,0.3)
    val textSecondary: Color,       // 描述/信源等辅助文字 rgba(255,255,255,0.5)
    val divider: Color,             // 分割线 rgba(255,255,255,0.12)
    val endedBtnBg: Color,          // 已结束/延期按钮背景 rgba(255,255,255,0.08)
    val highlightBtnBg: Color,      // 集锦/回放按钮背景 rgba(51,119,255,0.3)
    val liveRed: Color,             // 直播中文字/背景
)

@Immutable
data class GameColorScheme(
    val reserveTitle: Color,                     // 游戏预约标题颜色
    val checkInBgColor: Color,                   // 游戏签到背景色
    val miniGameHallTabBackground: Color,        // 小游戏大厅一级 Tab 背景色
    val miniGameHallTabText: Color,              // 小游戏大厅一级 Tab 文字色
    val miniGameHallTabActiveBackground: Color,  // 小游戏大厅一级 Tab 选中背景色
    val miniGameHallTabActiveText: Color,        // 小游戏大厅一级 Tab 选中文字色
    val calendarTitle: Color,                    // 游戏日历标题颜色
    val calendarDesc: Color,                     // 游戏日历描述颜色
    val calendarTagBg: Color,                    // 游戏日历标签背景色
    val calendarCardShadow: Color,               // 游戏日历卡片阴影色
    val calendarAvatarMask: Color,               // 游戏日历头像遮罩色（夜间模式下叠加）
    val categoryTabV2DescColor: Color,           // 游戏分类V2描述色
    val categoryTabV2MetaColor: Color,           // 游戏分类V2标签色
    val feedCardBg: Color,                       // 游戏资讯卡片背景色
    val feedCardShadow: Color,                   // 游戏资讯卡片阴影色
    val feedCardTitle: Color,                    // 游戏咨询卡片主标题
    val hangCardTitle: Color,                    // 游戏咨询卡片悬浮卡片标题
    val calendarBorder: Color,                   // 日历模块边框颜色
    val scoreText: Color,                        // 游戏评分文字颜色
    val skinNightMask: Color,                    // BonBon 换肤背景夜间遮罩色
    val skinBgGradientStart: Color,              // BonBon 换肤背景兜底渐变起始色
    val skinBgGradientEnd: Color,                // BonBon 换肤背景兜底渐变结束色
    val skinSignInPrimary: Color,                // BonBon 换肤签到主色
    val skinSignInSecondary: Color,              // BonBon 换肤签到浅色
    val skinZoneCardGradientMiddle: Color,       // BonBon 换肤游戏专区卡片背景渐变中段色
    val skinZoneCardGradientBottom: Color,       // BonBon 换肤游戏专区卡片背景渐变底部色
    val miniGameHallRecentPlayedGradientStart: Color, // 小游戏大厅最近在玩卡片背景渐变起始色
    val miniGameHallRecentPlayedGradientEnd: Color,   // 小游戏大厅最近在玩卡片背景渐变结束色
    val miniGameHallRecentPlayedBorder: Color,        // 小游戏大厅最近在玩卡片描边色
    val miniGameHallAddDesktopDescText: Color,       // 小游戏大厅添加桌面卡片描述文字色
)

@Immutable
data class AdColorScheme(
    val adMidArticleGameScoreColor: Color,   // 中插游戏评分分颜色
    val adMultiImageBgColor: Color,          // 微广多图背景色

    val adVipBannerBgColor: Color,
    val adVipBannerTipsBgColor: Color,
    val adVipBannerPTitleColor: Color,
    val adVipBannerPSubTitleColor: Color,
    val adVipBannerPPriceColor: Color,
    val adVipBannerBTitleColor: Color,

    // 微信小店标识色值
    val storeHaoDianColor: Color,            // 好店标色值
    val storeRGrey30Color: Color,            // R标 grey30（30%透明度）
    val storeRGrey55Color: Color,            // R标 grey55（55%透明度）

    // 618 小店券颜色
    val shop618Activity: Color = Color(0xFFFF6146),              // 618 文字主题色
    val shop618CouponRed: Color = Color(0xFFF64C30),             // 券金额/按钮主红色
    val shop618CouponRed60: Color = Color(0xFFF64C30).changeAlpha(0.6f), // 618 氛围橙色
    val shop618CouponLightBg: Color = Color(0xFFFFE6D6),         // 券浅色背景
    val shop618CouponDarkMask: Color = Color(0x99333333),        // 视频叠层暗色蒙层
    val shop618CouponGradientStart: Color = Color(0x4DF64C30),   // 618 大卡背景渐变起始色
    val shop618CouponGradientEnd: Color = Color(0x14FF915E),     // 618 大卡背景渐变结束色
    val shop618CouponInfoText: Color = Color(0xFFFA9D3B),        // 618 券信息文字色
    val commentStoreProductInfoStripBg: Color,                   // 评论小店商品信息条背景

    // 下载行业模板卡颜色
    val downloadCardInfoBg: Color = Color(0x2E000000),            // 小卡展开信息背景
    val downloadCardMetricText: Color = Color(0xFF3377FF),        // 下载量/评分文字
    val downloadCardTagText: Color = Color(0xFF999999),           // 标签文字
    val downloadCardTagBg: Color = Color(0x0D000000),             // 标签背景
    val downloadBigCardSeparator: Color = Color(0x0D000000),      // 下载大卡头部分隔线
    val downloadSmallCardTagBg: Color = Color(0x0DFFFFFF),        // 下载小卡标签背景
    val downloadSmallCardStarFilled: Color = Color(0xBFFFFFFF),   // iOS 小卡评分星级
    val downloadSmallCardStarEmpty: Color = Color(0x4DFFFFFF),    // iOS 小卡评分空星
    val downloadCardStarFilled: Color = Color(0xFF3377FF),        // iOS 大卡评分星级
    val downloadCardStarEmpty: Color = Color(0x4D3377FF),         // iOS 大卡评分空星

    // 游戏业务颜色
    val game: GameColorScheme,

    // 负反馈横向三点
    val fbHorThreePointColor: Color,

    )

data class UserColorScheme(
    val historyEditBtnDisabled: Color,
)

/**
 * 高考搜索结果页业务颜色（分数线卡片、考试时间卡片等共用）。
 */
@Immutable
data class GaokaoColorScheme(
    val tabSelectedBg: Color,                  // 分数线卡片：选中 Tab 背景
    val tabSelectedText: Color,                // 分数线卡片：选中 Tab 文字色
    val gaokaoBlueText: Color,                 // 高考蓝色文字（日夜统一 #4A83F9），如未选中 Tab / 时间卡片标题等
    val primaryText: Color,                    // 高考业务主要文字（日 #333333 / 夜 #DEDEDE）
    val secondaryText: Color,                  // 高考业务次要文字（日夜统一 #999999）
    val inactiveText: Color,                   // 高考业务未选中/兜底文字（日 #999999 / 夜 #5C5C5C）
    val chipBg: Color,                         // 高考筛选/未选中胶囊背景（日 #F7F7F7 / 夜 #363636）
    val filterChipText: Color,                 // 高考筛选栏文字（日 #333333 / 夜 #999999）
    val pickerSelectedText: Color,             // 通用下拉选择器：选中态文字 / 勾选图标（日夜统一 #4675F2）
    val pickerDivider: Color,                  // 通用下拉选择器：分割线（日 #F1F1F1 / 夜 #333333）
    val pickerCloseIcon: Color,                // 通用下拉选择器：关闭图标（日夜统一 #A1A1A1）
    val placeholderText: Color,                // 高考空/错误占位描述（日夜统一 #5C5C5C）

    // 通用展开/收起按钮（[com.tencent.news.core.compose.search.gaokao.common.GaokaoExpandToggle]）
    val expandToggleCollapsedText: Color,      // 收起态文字：日 #1C75E7 / 夜 #3377FF
    val expandToggleCollapsedArrow: Color,     // 收起态箭头：日 #83ACE0 / 夜 #3377FF
    val expandToggleExpandedArrow: Color,      // 展开态箭头：日夜统一 #AEAEAE

    // 高考卡片体系通用「弱化灰文本」色（日夜统一 #949494），覆盖：
    //  - 展开/收起按钮展开态文字（[com.tencent.news.core.compose.search.gaokao.common.GaokaoExpandToggle]）
    //  - 一分一段位次卡片标签 / 输入框 placeholder 等次要提示文字
    val gaokaoMutedText: Color,

    // 考试时间卡片：阶段 Tab / 内容区
    val stageTabSelectedText: Color,           // 阶段 Tab 选中文字（日 #FFFFFF / 夜 #DEDEDE）
    val examContentText: Color,                // 考试时间日期/时间/科目（日 #333333 / 夜 #848484）
    val examDivider: Color,                    // 考试时间日期块分割线（日 #EBEBEB / 夜 #3A3A3A）

    // 考试时间卡片：入口按钮 Icon 底托色（日 #B6D7FF / 夜 #6C87BD）
    val entryIconBackdrop: Color,
    val entryButtonBg: Color,                  // 入口按钮底层背景

    // 考试时间卡片：阶段时间轴激活态渐变（日 #4A83F9 -> #9DBCFD / 夜 #4A83F9 -> #446ABA）
    val stageTimelineGradientStart: Color,
    val stageTimelineGradientEnd: Color,

    // 考试时间卡片：省份 Chip 文字色（日 #323232 / 夜 #DEDEDE），箭头复用 expandToggleExpandedArrow（#AEAEAE）
    val provinceChipText: Color,

    // 真题卡片：试卷类型副标题文字色（日 #949494 / 夜 #5C5C5C）
    val paperTypeText: Color,

    // 真题范文卡片（[com.tencent.news.core.compose.search.gaokao.zhenti.component.FanwenCard]）
    val fanwenCardBorder: Color,               // 1px 边框：日 #F1F4FF / 夜 #455B7B
    val fanwenCardGradientTop: Color,          // 卡片背景渐变上：日 #F8F9FF / 夜 #363636 (alpha 100%)
    val fanwenCardGradientBottom: Color,       // 卡片背景渐变下：日 #FFFFFF / 夜 #363636 (alpha 0%)
    val fanwenDetailText: Color,               // 详情文字：日 #333333 / 夜 #999999

    // 一分一段折线图（[com.tencent.news.core.compose.search.gaokao.scoreline.component.ScoreLineChart]）
    val scoreQueryButtonBg: Color,             // 查询按钮启用态背景（日 #4A83F9 / 夜 #3377FF）
    val scoreInfoCardBg: Color,                // 位次卡片背景（日 #F4F9FF / 夜 #273240）
    val scoreInfoCardBorder: Color,            // 位次卡片描边（日 #D3E6FF / 夜 #5C5C5C）
    val scoreLineAxisLabel: Color,             // X/Y 轴刻度（日 #949494 / 夜 #696969）
    val scoreLineGridLine: Color,              // Y 轴横向网格线（0.4px）：日 #EBEBEB / 夜 #5C5C5C
    val scoreLineStrokeTop: Color,             // 折线描边渐变-顶部：日夜统一 #328BFC
    val scoreLineStrokeBottom: Color,          // 折线描边渐变-底部：日夜统一 #BEDBFF
    val scoreLineCursor: Color,                // 定位竖虚线（1px）：日夜统一 #4395FF
    val scoreLineFillTop: Color,               // 折线下方填充渐变-顶部：日 #64B4FF / 夜 #64B4FF
    val scoreLineFillBottom: Color,            // 折线下方填充渐变-底部：日 #FFFFFF / 夜 #1F1F1F
    val previousYearMetaText: Color,           // 历史同位次标题/年份（日 #949494 / 夜 #A9A9A9）
    val provincialFooterIcon: Color,           // 省控线底部备注图标（日 #949494 / 夜 #5C5C5C）
    val subjectPrimaryText: Color,             // 真题科目名/按钮启用态文字（日 #5C5C5C / 夜 #DEDEDE）
    val subjectViewAllText: Color,             // 真题「查看全部」文字（日夜统一 #1C75E7）
)

@Immutable
data class ColorScheme(
    // 标准蓝
    val bNormal: Color,
    val bLight: Color,
    val bMiddle: Color,
    // 灰蓝色-按钮不可用等场景
    val bGray: Color,
    val redNormal: Color,
    val orangeNormal: Color,
    val redBg: Color,
    val assistantText: Color,
    val blueNormal: Color,
    val bgContentPreference: Color,
    val backIconColor: Color,
    val brownNormal: Color,
    // 蓝紫色
    val purpleNormal: Color,
    // 赞助选中背景色（日间：bLight，夜间：#3377FF）
    val sponsorSelectBg: Color,
    // 标准红
    val rNormal: Color,
    // 字体颜色
    val t1: Color,
    val t2: Color,
    val t3: Color,
    val t6: Color,
    val t3DarkAlpha04: Color,
    val t4: Color,
    val t5: Color,
    val tlink: Color,
    // 黄色系
    val yNormal: Color,

    // 背景颜色
    val bgPage: Color,
    val bgBlock: Color,
    val picDefaultColor: Color,
    val panelBgBlock: Color,
    val panelBgPage: Color,
    val bgCard: Color,
    val bgPageGrey: Color,
    val assistantBg: Color,
    val assistantBgPurple: Color,
    val assistantBgGradient: Brush,
    val bgAssistantSendStart: Brush,
    val bgAssistantSendEnd: Brush,
    val buttonBlock: Color,
    val bgBottomSheetGrey: Color,
    val bgSnackBar: Color,
    val audioPodSelectBgBrush: Brush,
    val audioPodShadowColor: Color,
    // 字体推荐气泡阴影色
    val fontRecommendTipShadowColor: Color,
    val audioPodNormalBgColor: Color,
    // 播客频道业务颜色
    val audioPodChannel: AudioPodChannelColorScheme,

    // 分割线
    val lineFine: Color,
    val lineWide: Color,
    val lineStroke: Color,
    val lineWideVideo: Color,
    val lineInside: Color,
    val transparent: Color,

    // 25%透明度的阴影
    val shadow50: Color,
    val shadow40: Color,
    val shadow25: Color,
    val shadow8: Color,
    val shadow6: Color,
    val shadowImage: Brush,

    // 关闭按钮背景色渐变
    val closeBackground: Color,
    // 纯白色透明度为70%的颜色值
    val white70: Color,
    val white80: Color,
    // 日夜间均为纯白色 #FFFFFF的固定颜色（用于需要不跟随主题变化的描边/指示等）
    val whiteFixed: Color,

    // 热问大事件横滑 左滑查看更多
    val hotAskMore: Color,
    val verticalShadow: Brush,
    val adMiniGameBg: Color,
    val sponsorCardBg: Brush,
    val sponsorProcessBg: Color,
    // 语音波形背景色
    val whiteText: Color,

    // 内容付费颜色配置
    val paymentColorScheme: PaymentColorScheme,

    // 图文合集头部渐变背景色
    val collectionHeaderIconGradientColor1: Color,
    val collectionHeaderIconGradientColor2: Color,
    val greenNormal: Color,

    // 签到相关颜色
    val checkInBgGradientStart: Color,
    val checkInBgGradientEnd: Color,
    val checkInContentBgGradientStart: Color,
    val checkInContentBgGradientEnd: Color,
    val checkInTitleText: Color,
    val checkInAccentText: Color,
    val checkInButtonGradient: Brush,
    val checkInDashedLine: Color,
    val checkInCoinText: Color,
    val checkInCoinTextChecked: Color,
    val mask50: Color,
    val mask60: Color,

    // 微信小店相关颜色
    val oran70: Color,

    val bgGameCard: Color,
    val bgGameShadow: Color,

    // AIGC 业务颜色
    val aigc: AigcColorScheme,

    // 金币中心 Hippy 迁移业务颜色
    val coinCenter: CoinCenterColorScheme,

    // 积分中心业务颜色
    val pointsCenter: PointsCenterColorScheme,

    // 体育赛事颜色
    val sport: SportColorScheme,

    // 广告业务颜色
    val ad: AdColorScheme,

    // 音频已读标题颜色
    val audioReadTitleColor: Color,

    // 用户相关颜色
    val user: UserColorScheme,

    // 高考搜索业务颜色（分数线卡片、考试时间卡片等共用）
    val gaokao: GaokaoColorScheme,
)

val LightColorScheme = ColorScheme(
    bNormal = Color(0xFF3377FF),
    bLight = Color(0xFFEBF5FF),
    bMiddle = Color(0xFF505DE5),
    bGray = Color(0xFFC7D5FB),
    redNormal = Color(0xFFFF0055),
    orangeNormal = Color(0xFFFF8A00),
    redBg = Color(0xFFD81306),
    assistantText = Color(0xFF999999),
    blueNormal = Color(0xFF3377FF),
    bgContentPreference = Color(0xFFEFF6FF),
    backIconColor = Color(0xFF333333),
    brownNormal = Color(0xFFC44F1F),
    purpleNormal = Color(0xFF505DE5),
    sponsorSelectBg = Color(0xFFEBF5FF), // 日间使用 bLight
    t1 = Color(0xFF333333),
    t2 = Color(0xFF5C5C5C),
    t3 = Color(0xFF999999),
    t6 = Color(0x4D999999),
    t3DarkAlpha04 = Color(0x66999999),
    t4 = Color(0xFFFFFFFF),
    t5 = Color(0xBFFFFFFF),
    tlink = Color(0xFF3377FF),
    yNormal = Color(0xFFFFB700),
    bgPage = Color(0xFFFFFFFF),
    audioPodNormalBgColor = Color(0xFFFFFFFF),
    audioPodChannel = AudioPodChannelColorScheme(
        cardShadowColor = Color(0x26000000),  // 黑色15%透明度
        playIconColor = Color(0xFFFFFFFF),
        listenLaterBg = Color(0xFFF7F7F7),  // 日间：#F7F7F7
        playButtonBg = Color(0xFFEBF5FF),   // 日间：#EBF5FF
        hotCategoryItemBg = Color(0xFFFFFFFF),      // 日间：白色
        hotCategoryItemShadow = Color(0x263377FF),  // 日间：RGBA(51,119,255,0.15)
        viewMoreBg = Color(0xFFF7F7F7),             // 日间：#F7F7F7
        viewMoreBorder = Color(0xFFE5E5E5),         // 日间：#E5E5E5
        viewMoreText = Color(0xFF999999),            // 日间：#999999
    ),
    bgBlock = Color(0xFFF7F7F7),
    panelBgBlock = Color(0xFFF7F7F7),
    panelBgPage = Color(0xFFFFFFFF),
    bgCard = Color(0xFFFFFFFF),
    bgPageGrey = Color(0xFFF5F5F5),
    assistantBg = Color(0xFF212144),
    assistantBgPurple = Color(0xFF12132B),
    assistantBgGradient = linearGradient(
        colorStops = arrayOf(
            0f to Color(0xFF464488), // 0%
            0.3f to Color(0xFF12132B), // 30%
            0.71f to Color(0xFF12132B), // 71%
            1f to Color(0xFF12132B)  // 100%
        ),
        start = Offset(0.0f, 0f), end = Offset(0f, Float.POSITIVE_INFINITY)
    ),
    bgAssistantSendStart = Brush.verticalGradient(listOf(Color(0xFF12132B), Color(0x0012132B))),
    bgAssistantSendEnd = Brush.verticalGradient(listOf(Color(0x0012132B), Color(0xFF12132B))),
    audioPodSelectBgBrush = Brush.verticalGradient(
        listOf(Color(0xFFEBF5FF), Color(0xFFFFFFFF)),
        endY = 0.3f
    ),
    audioPodShadowColor = Color(0xFF3377FF),
    fontRecommendTipShadowColor = Color(0x140080FF),
    buttonBlock = Color(0xFFF7F7F7),
    bgBottomSheetGrey = Color(0xFFF5F5F5),
    bgSnackBar = Color(0xFFFFFFFF),
    lineFine = Color(0XFFF0F0F0),
    lineWide = Color(0XFFF5F5F5),
    lineStroke = Color(0XFFE6E6E6),
    lineWideVideo = Color(0xFFE6E6E6),
    lineInside = Color(0xFFE6E6E6),
    transparent = Color(0x00FFFFFF),
    shadow50 = Color.Black.changeAlpha(0.5f),
    shadow40 = Color(0x66000000),
    shadow25 = Color(0x40000000),
    shadow8 = Color(0x14000000),
    shadow6 = Color(0x0F000000),
    shadowImage = Brush.verticalGradient(colors = listOf(Color(0x00FFFFFF), Color(0x00FFFFFF))),
    closeBackground = Color(0x0A000000),
    white70 = Color(0xB2FFFFFF),
    white80 = Color(0xCCFFFFFF),
    whiteFixed = Color(0xFFFFFFFF),
    hotAskMore = Color(0xFF53586B),
    verticalShadow = Brush.verticalGradient(colors = listOf(Color(0x1A000000), Color(0x00000000))),
    adMiniGameBg = Color(0x99333333),
    sponsorCardBg = Brush.verticalGradient(colors = listOf(Color(0x14FF0055), Color(0x00FFFFFF))),
    sponsorProcessBg = Color(0xFFFFE3E3),
    whiteText = Color(0xFFFFFFFF),
    paymentColorScheme = LightPaymentColorScheme,
    collectionHeaderIconGradientColor1 = Color(0xFFD5D5D5),
    collectionHeaderIconGradientColor2 = Color(0x66D5D5D5),
    greenNormal = Color(0xFF00AA70),

    // 签到相关颜色 - 日间
    checkInBgGradientStart = Color(0xFFFFDABA),
    checkInBgGradientEnd = Color(0xFFFFF4F4),
    checkInContentBgGradientStart = Color(0xFFFFF2F2),
    checkInContentBgGradientEnd = Color(0xFFFFF8F8),
    checkInTitleText = Color(0xFF282828),
    checkInAccentText = Color(0xFFC44F1F),
    checkInButtonGradient = Brush.linearGradient(
        colors = listOf(
            Color(0xFFFEA755),
            Color(0xFFFF7658),
            Color(0xFFFF5934)
        ),
        start = Offset(0f, 0.5f),
        end = Offset(1f, 0.5f)
    ),
    checkInDashedLine = Color(0xFFEDB8AB),
    checkInCoinText = Color(0xFFB24D04),
    checkInCoinTextChecked = Color(0xFF333333),
    picDefaultColor = Color(0xFFEAEAEA),
    mask50 = Color(0x80000000),
    mask60 = Color(0x99000000),
    bgGameCard = Color(0xFFFFFFFF),
    oran70 = Color(0xFF3617B3),
    bgGameShadow = Color(0x0F666666),
    audioReadTitleColor = Color(0xFF999999),
    rNormal = Color(0xFFFF0055),

    aigc = AigcColorScheme(
        prompt = Color(0xFF505DE5),
        prompt05 = Color(0x0D505DE5),
        prompt25 = Color(0x40505DE5),
        bg = Color(0xFFF2F6FF),
        bgSendStart = Brush.verticalGradient(listOf(Color(0xFFF2F6FF), Color(0x00F2F6FF))),
        bgSendEnd = Brush.verticalGradient(listOf(Color(0x00F2F6FF), Color(0xFFF2F6FF))),
        lineCard = Color(0x9999994D),
        qaText = Color(0xFF505DE5),
        purple = Color(0xFF505DE5),
        purpleBg12 = Color(0x1F505DE5),
        purpleButonBg = Color(0xFF505DE5),
        stroke = Color(0xFFE9EBFF),
        entryStart = Color(0xFFF7F2FF),
        entryEnd = Color(0xFFF2F7FF),
        streamStart = Color(0xFFD6F2E0),
        streamEnd = Color(0xFFF5F5F5),
        bottomBarBorder = Color(0xFF45AD60),
        bottomBarVoiceCancelBorder = Color(0x66FF0055),
        bottomBarBottomMask = Brush.verticalGradient(
            colors = listOf(
                Color(0xFFFFFFFF),
                Color(0xFFF6FFFE)
            )
        ),
        bottomBarTopMask = Brush.linearGradient(
            colors = listOf(
                Color(0x00FFFFFF),
                Color(0x33A8F0D1)
            )
        ),
        bottomBarShadowSpot = Color(0xFF999999),
        bottomBarShadowAmbient = Color(0xFF999999),
        yuanbaoGreen = Color(0xFF45AD60),
        yuanbaoGreen05 = Color(0x0D45AD60),
        yuanbaoGreenBg = Color(0xFFEDFAF0),
        posterGrayBg = Color(0xFFF6F6F6),  // 对话列表海报灰色背景
        posterBlueBg = Color(0xFF167BFF),   // 选中文本渐变海报蓝色背景
        digitalBgMask = Brush.verticalGradient(
            colors = listOf(
                Color(0x00123338),
                Color(0x66123338),
                Color(0xCC123338),
                Color(0xFF10393F),
                Color(0xFF0D3035)
            ),
            startY = 0f,
            endY = Float.POSITIVE_INFINITY
        ),
        inlineVideoBackground = Color(0xFF000000),
        inlineVideoCoverMask = Color(0x4D000000),
        inlineVideoPlayIconBackground = Color(0x73000000),
        inlineVideoMuteButtonBackground = Color(0x4D333333)
    ),

    coinCenter = CoinCenterColorScheme(
        pageBackground = Color(0xFFEAEAEA),
        bgGradientStart = Color(0xFFFF1B1B),
        bgGradientMiddle = Color(0xFFFFDDD5),
        bgGradientEnd = Color(0xFFEAEAEA),
        primaryRed = Color(0xFFFF2810),
        textPrimaryRed = Color(0xFFFF2810),
        summaryPrimaryRed = Color(0xFFFF2810),
        summaryCardBackground = Color(0xB3FFFFFF),
        summaryCardBorder = Color(0xFFFFFFFF),
        videoTaskPrimaryRed = Color(0xFFFF2810),
        videoTaskWarmBackground = Color(0xFFFFF0EF),
        videoTaskSecondaryBackground = Color(0xFFFFF2F2),
        videoTaskEnvelopeUnavailableText = Color(0xFFFFFFFF),
        checkInCardBackground = Color(0xFFFFF0EF),
        checkInPanelBackground = Color(0xFFFFFFFF),
        checkInRewardPillBackground = Color(0xFFFFF0EF),
        checkInRewardPillBorder = Color(0xFFFFB3B3),
        checkInRewardText = Color(0xFFFF2810),
        checkInClaimedRewardPillBackground = Color(0xFFFF2810),
        checkInClaimedRewardPillBorder = Color(0xFFFFF0EF),
        checkInClaimedRewardText = Color(0xFFFFFFFF),
        checkInDayText = Color(0xFF5C5C5C),
        checkInClaimedText = Color(0xFFFF2810),
        checkInButtonBackground = Color(0xFFFF2810),
        checkInButtonText = Color(0xFFFFFFFF),
        checkInSuccessDialogBackground = Color(0xD92B2B2B),
        mainLaunchCheckInCardBackground = Color(0xFFFFF0EF),
        mainLaunchCheckInPanelBackground = Color(0xFFFFFFFF),
        mainLaunchCheckInDayItemBackground = Color(0xFFF7F7F7),
        mainLaunchCheckInTodayDayItemBackground = Color(0x1AFF7749),
        mainLaunchCheckInTitle = Color(0xFF333333),
        mainLaunchCheckInTitleHighlight = Color(0xFFFF2810),
        mainLaunchCheckInDesc = Color(0xFF5C5C5C),
        mainLaunchCheckInButtonBackground = Color(0xFFFF2810),
        mainLaunchCheckInButtonText = Color(0xFFFFFFFF),
        mainLaunchGuideDialogBackground = linearGradient(
            colorStops = arrayOf(
                0f to Color(0xFFFFBFB7),
                0.20f to Color(0xFFFFECE6),
                0.67f to Color(0xFFFFF6F4),
                1f to Color(0xFFFFD5C7),
            ),
            start = Offset(0f, 0f),
            end = Offset(0f, Float.POSITIVE_INFINITY)
        ),
        mainLaunchTitleImageUrl = "https://inews.gtimg.com/newsapp_bt/0/0710175526235_7222/0",
        mainLaunchHeaderIconUrl = "https://inews.gtimg.com/newsapp_bt/0/0706133741525_1139/0",
        mainLaunchUnsignedDayIconUrl = "https://inews.gtimg.com/newsapp_bt/0/0706152714287_7915/0",
        mainLaunchUnsignedIconUrl = "https://inews.gtimg.com/newsapp_bt/0/0706152714287_7915/0",
        mainLaunchDirectRewardBgUrl = "https://inews.gtimg.com/newsapp_bt/0/0706210813449_2583/0",
        mainLaunchDoubleRewardBgUrl = "https://inews.gtimg.com/newsapp_bt/0/070621112182_8668/0",
        rewardDialogBackground = Color(0xFFFF3D3D),
        rewardDialogBorder = Color(0xFFFFD6A8),
        rewardDialogCream = Color(0xFFFFF3D1),
        rewardDialogGold = Color(0xFFFFD06A),
        rewardDialogTextRed = Color(0xFFE8482E),
        rewardDialogValueCream = Color(0xFFFFF2D3),
        rewardDialogSubText = Color(0xCCFFFFFF),
        rewardDialogTipsBackground = Color.White
    ),

    pointsCenter = PointsCenterColorScheme(
        dailyTaskTabsInactiveGradientTop = Color(0xFFFFDDC0),
        dailyTaskTabsInactiveGradientBottom = Color(0xFFFFFFFF),
    ),

    ad = AdColorScheme(
        adMidArticleGameScoreColor = Color(0xFFFF7D3E),
        adMultiImageBgColor = Color(0xFF8A8A8A),
        adVipBannerBgColor = Color(0xFFF5E9CE),
        adVipBannerTipsBgColor = Color(0xFFD81306),
        adVipBannerPTitleColor = Color(0xFF333333),
        adVipBannerPSubTitleColor = Color(0xFF999999),
        adVipBannerPPriceColor = Color(0xFF9A5D11),
        adVipBannerBTitleColor = Color(0xFF000000),
        storeHaoDianColor = Color(0xFFE0B584),        // 好店标 日间 #E0B584
        storeRGrey30Color = Color(0x4D000000),        // R标 日间 #000000 30%
        storeRGrey55Color = Color(0x8C000000),        // R标 日间 #000000 55%

        shop618Activity = Color(0xFFFF6146),          // 618 文字主题色
        shop618CouponRed = Color(0xFFF64C30),         // 618 券主红色
        shop618CouponRed60 = Color(0xFFF64C30).changeAlpha(0.6f),      // 618 券主红色 0.6透明度
        shop618CouponLightBg = Color(0xFFFFE6D6),     // 618 券浅色背景
        shop618CouponDarkMask = Color(0x99333333),    // 618 视频叠层暗色蒙层
        shop618CouponGradientStart = Color(0x4DF64C30), // 618 大卡背景渐变起始色
        shop618CouponGradientEnd = Color(0x14FF915E), // 618 大卡背景渐变结束色
        shop618CouponInfoText = Color(0xFFFA9D3B),    // 618 券信息文字色
        commentStoreProductInfoStripBg = Color(0xFFF5F5F5),

        fbHorThreePointColor = Color(0xFF666666),
        game = GameColorScheme(
            reserveTitle = Color(0xFF222222),
            checkInBgColor = Color(0XFFF0F0F0),
            miniGameHallTabBackground = Color(0xFFFFFFFF),
            miniGameHallTabText = Color(0xFF333333),
            miniGameHallTabActiveBackground = Color(0xFF333333),
            miniGameHallTabActiveText = Color(0xFFE5F831),
            calendarTitle = Color(0xFF333333),    // 游戏日历标题 - 日间
            calendarDesc = Color(0xFF999999),     // 游戏日历描述 - 日间
            calendarTagBg = Color(0xFFE69100),    // 游戏日历标签背景 - 日间 #E69100
            calendarCardShadow = Color(0x40B3B3B3), // 游戏日历卡片阴影 - 日间 #B3B3B3 25%
            calendarAvatarMask = Color.Transparent, // 游戏日历头像遮罩 - 日间透明
            categoryTabV2DescColor = Color(0xFF666666),
            categoryTabV2MetaColor = Color(0xFFF8A33B),
            feedCardShadow = Color(0x40B3B3B3),
            calendarBorder = Color(0xC2FFFFFF),
            hangCardTitle = Color(0xFF5C5C5C),
            feedCardTitle = Color(0xFF333333),
            feedCardBg = Color(0xFFFFFFFF),
            scoreText = Color(0xFFFF8A38),
            skinNightMask = Color(0x661F1F1F),
            skinBgGradientStart = Color(0xFFFFC176),
            skinBgGradientEnd = Color(0x00FFC176),
            skinSignInPrimary = Color(0xFFFF3F61),
            skinSignInSecondary = Color(0xFFFFE4EA),
            skinZoneCardGradientMiddle = Color(0x1A814229),
            skinZoneCardGradientBottom = Color(0x1A814229),
            miniGameHallRecentPlayedGradientStart = Color(0xDEFFF0E1),
            miniGameHallRecentPlayedGradientEnd = Color(0xDEFFFFFF),
            miniGameHallRecentPlayedBorder = Color(0xFFFFE7DD),
            miniGameHallAddDesktopDescText = Color(0xFF333333),
        ),
    ),

    sport = SportColorScheme(
        textPrimary = Color.White,
        reservedBtnBg = Color(255, 255, 255, 26),
        reservedBtnText = Color(255, 255, 255, 77),
        textSecondary = Color(255, 255, 255, 128),
        divider = Color(255, 255, 255, 31),
        endedBtnBg = Color(255, 255, 255, 20),
        highlightBtnBg = Color(51, 119, 255, 77),
        liveRed = Color(0xFFFF0055),
    ),

    user = UserColorScheme(
        historyEditBtnDisabled = Color(0xFFB2B2B2),
    ),

    gaokao = GaokaoColorScheme(
        tabSelectedBg = Color(0xFFDFE9FF),       // 日间选中背景
        tabSelectedText = Color(0xFF5C5C5C),     // 日间选中文字
        gaokaoBlueText = Color(0xFF4A83F9),      // 日夜统一蓝色文字
        primaryText = Color(0xFF333333),         // 日间高考主要文字
        secondaryText = Color(0xFF999999),       // 日间高考次要文字
        inactiveText = Color(0xFF999999),        // 日间高考未选中/兜底文字
        chipBg = Color(0xFFF7F7F7),              // 日间筛选/未选中胶囊背景
        filterChipText = Color(0xFF333333),      // 日间筛选栏文字
        pickerSelectedText = Color(0xFF4675F2),  // 日夜统一下拉选择器选中态
        pickerDivider = Color(0xFFF1F1F1),       // 日间下拉选择器分割线
        pickerCloseIcon = Color(0xFFA1A1A1),     // 日夜统一下拉选择器关闭图标
        placeholderText = Color(0xFF5C5C5C),     // 日夜统一空/错误占位描述
        expandToggleCollapsedText = Color(0xFF1C75E7),
        expandToggleCollapsedArrow = Color(0xFF83ACE0),
        expandToggleExpandedArrow = Color(0xFFAEAEAE),
        gaokaoMutedText = Color(0xFF949494),     // 日夜统一弱化灰文本
        stageTabSelectedText = Color(0xFFFFFFFF), // 日间阶段 Tab 选中文字
        examContentText = Color(0xFF333333),     // 日间考试日期/时间/科目文字
        examDivider = Color(0xFFEBEBEB),         // 日间考试日期块分割线
        entryIconBackdrop = Color(0xFFB6D7FF),     // 日间入口 Icon 底托
        entryButtonBg = Color(0xFFF4F9FF),       // 日夜统一入口按钮底层背景
        stageTimelineGradientStart = Color(0xFF4A83F9), // 日间阶段时间轴渐变起点
        stageTimelineGradientEnd = Color(0xFF9DBCFD),   // 日间阶段时间轴渐变终点
        provinceChipText = Color(0xFF323232),      // 日间省份 Chip 文字
        paperTypeText = Color(0xFF949494),         // 日间真题试卷类型副标题
        fanwenCardBorder = Color(0xFFF1F4FF),       // 日间范文卡片边框
        fanwenCardGradientTop = Color(0xFFF8F9FF),  // 日间范文卡片渐变上
        fanwenCardGradientBottom = Color(0xFFFFFFFF), // 日间范文卡片渐变下
        fanwenDetailText = Color(0xFF333333),       // 日间范文卡片详情文字
        scoreQueryButtonBg = Color(0xFF4A83F9),     // 日间查询按钮启用态背景
        scoreInfoCardBg = Color(0xFFF4F9FF),        // 日间位次卡片背景
        scoreInfoCardBorder = Color(0xFFD3E6FF),    // 日间位次卡片描边
        scoreLineAxisLabel = Color(0xFF949494),     // 日间折线图 X/Y 轴刻度
        scoreLineGridLine = Color(0xFFEBEBEB),       // 日间一分一段折线图网格横线
        scoreLineStrokeTop = Color(0xFF328BFC),      // 日间折线描边渐变-顶部
        scoreLineStrokeBottom = Color(0xFFBEDBFF),   // 日间折线描边渐变-底部
        scoreLineCursor = Color(0xFF4395FF),         // 日间定位竖虚线
        scoreLineFillTop = Color(0xFF64B4FF),        // 日间折线下方填充渐变-顶部
        scoreLineFillBottom = Color(0xFFFFFFFF),     // 日间折线下方填充渐变-底部
        previousYearMetaText = Color(0xFF949494),    // 日间历史同位次标题/年份
        provincialFooterIcon = Color(0xFF949494),    // 日间省控线底部备注图标
        subjectPrimaryText = Color(0xFF5C5C5C),      // 日间真题科目名/按钮启用态文字
        subjectViewAllText = Color(0xFF1C75E7),      // 日夜统一真题查看全部文字
    ),
)

val DarkColorScheme = ColorScheme(
    bNormal = Color(0xFF3377FF),
    bLight = Color(0xFF273240),
    bMiddle = Color(0xFF7780D9),
    bGray = Color(0xFF223F62),
    redNormal = Color(0xFFD90048),
    orangeNormal = Color(0xFFd97500),
    redBg = Color(0xFFB31005),
    assistantText = Color(0xFF696969),
    blueNormal = Color(0xFF3377FF),
    bgContentPreference = Color(0xFF242629),
    backIconColor = Color(0xFFFFFFFF),
    brownNormal = Color(0xFFC44F1F),
    purpleNormal = Color(0xFF505DE5),
    sponsorSelectBg = Color(0xFF273240),
    t1 = Color(0xFFE6E6E6),
    t2 = Color(0xFFA9A9A9),
    t3 = Color(0xFF696969),
    t6 = Color(0x4D999999),
    t3DarkAlpha04 = Color(0x66696969),
    t4 = Color(0xFFE6E6E6),
    t5 = Color(0xBFE6E6E6),
    tlink = Color(0xFF2B65D9),
    yNormal = Color(0xFFD99B00),
    bgPage = Color(0xFF1f1f1f),
    audioPodNormalBgColor = Color(0xFF2B2B2B),
    audioPodChannel = AudioPodChannelColorScheme(
        cardShadowColor = Color(0x00000000),
        playIconColor = Color(0xFFD9D9D9),
        listenLaterBg = Color(0xFF2B2B2B),  // 夜间：#2B2B2B
        playButtonBg = Color(0xFF273240),   // 夜间：#273240
        hotCategoryItemBg = Color(0xFF2B2B2B),      // 夜间：#2B2B2B
        hotCategoryItemShadow = Color(0x1A000000),   // 夜间：RGBA(0,0,0,0.1)
        viewMoreBg = Color(0xFF2B2B2B),              // 夜间：#2B2B2B
        viewMoreBorder = Color(0xFF3D3D3D),          // 夜间：#3D3D3D
        viewMoreText = Color(0xFF696969),             // 夜间：#696969
    ),
    bgBlock = Color(0xFF2B2B2B),
    panelBgBlock = Color(0xEE121212),
    panelBgPage = Color(0xFF1f1f1f),
    bgCard = Color(0xFF1F1F1F),
    bgPageGrey = Color(0xFF121212),
    assistantBg = Color(0xFF272633),
    assistantBgPurple = Color(0xFF12132B),
    assistantBgGradient = linearGradient(
        colorStops = arrayOf(
            0f to Color(0xFF464488), // 0%
            0.3f to Color(0xFF12132B), // 30%
            0.71f to Color(0xFF12132B), // 71%
            1f to Color(0xFF12132B)  // 100%
        ),
        start = Offset(0.0f, 0f), end = Offset(0f, Float.POSITIVE_INFINITY)
    ),
    bgAssistantSendStart = Brush.verticalGradient(listOf(Color(0xFF12132B), Color(0x0012132B))),
    bgAssistantSendEnd = Brush.verticalGradient(listOf(Color(0x0012132B), Color(0xFF12132B))),
    audioPodSelectBgBrush = Brush.verticalGradient(
        listOf(Color(0xFF273240), Color(0xD41F1F1F)),
        endY = 0.3f
    ),
    audioPodShadowColor = Color(0x1A000000),
    fontRecommendTipShadowColor = Color(0x140080FF),
    buttonBlock = Color(0xFF2B2B2B),
    bgBottomSheetGrey = Color(0xFF1F1F1F),
    bgSnackBar = Color(0xFF2B2B2B),
    lineFine = Color(0xFF292929),
    lineWide = Color(0xFF121212),
    lineStroke = Color(0xFF303030),
    lineWideVideo = Color(0xFF121212),
    lineInside = Color(0xFF3D3D3D),
    transparent = Color(0x00FFFFFF),
    shadow50 = Color.Black.changeAlpha(0.5f),
    shadow40 = Color(0x66000000),
    shadow25 = Color(0x40000000),
    shadow8 = Color(0x14000000),
    shadow6 = Color(0x0F000000),
    shadowImage = Brush.verticalGradient(
        colors = listOf(
            Color(0x99303030), Color(0xAA303030), Color(0xFF262626)
        )
    ),
    closeBackground = Color(0x0AFFFFFF),
    white70 = Color(0xB2FFFFFF),
    white80 = Color(0xCCFFFFFF),
    whiteFixed = Color(0xFFFFFFFF),
    hotAskMore = Color(0xFF53586B),
    verticalShadow = Brush.verticalGradient(colors = listOf(Color(0x1A1F1F1F), Color(0x001F1F1F))),
    adMiniGameBg = Color(0x99333333),
    sponsorCardBg = Brush.verticalGradient(colors = listOf(Color(0x14D90048), Color(0x001F1F1F))),
    sponsorProcessBg = Color(0xFF332929),
    whiteText = Color(0xFFD9D9D9),
    paymentColorScheme = DarkPaymentColorScheme,
    collectionHeaderIconGradientColor1 = Color(0xFF444444),
    collectionHeaderIconGradientColor2 = Color(0x66444444),

    greenNormal = Color(0xFF008557),

    // 签到相关颜色 - 夜间
    checkInBgGradientStart = Color(0xFF3E2208),
    checkInBgGradientEnd = Color(0xFF1F1F1F),
    checkInContentBgGradientStart = Color(0xFF2B1005),
    checkInContentBgGradientEnd = Color(0xFF231A16),
    checkInTitleText = Color(0xFFD9D9D9),
    checkInAccentText = Color(0xFFC26A46),
    checkInButtonGradient = Brush.linearGradient(
        colors = listOf(
            Color(0xFFFEA755),
            Color(0xFFFF7658),
            Color(0xFFFF5934)
        ),
        start = Offset(0f, 0.5f),
        end = Offset(1f, 0.5f)
    ),
    checkInDashedLine = Color(0xFFEDB8AB),
    checkInCoinText = Color(0xFFB24D04),
    checkInCoinTextChecked = Color(0xFF333333),
    picDefaultColor = Color(0xFF1A1B1C),
    mask50 = Color(0x80000000),
    mask60 = Color(0x99000000),
    bgGameCard = Color(0xFF2A2A2A),
    oran70 = Color(0xFF3617B3),
    bgGameShadow = Color(0x0F666666),
    audioReadTitleColor = Color(0xFF696969),
    rNormal = Color(0xFFD90048),
    aigc = AigcColorScheme(
        prompt = Color(0xFF7780D9),
        prompt05 = Color(0x0D7780D9),
        prompt25 = Color(0x407780D9),
        bg = Color(0xFF121212),
        bgSendStart = Brush.verticalGradient(listOf(Color(0xFF121212), Color(0x00121212))),
        bgSendEnd = Brush.verticalGradient(listOf(Color(0x00121212), Color(0xFF121212))),
        lineCard = Color(0x9999994D),
        qaText = Color(0xFF505DE5),
        purple = Color(0xFF505DE5),
        purpleBg12 = Color(0x1F5564FF),
        purpleButonBg = Color(0xFF5564FF),
        stroke = Color(0xFF2F2D3D),
        entryStart = Color(0xFF26222C),
        entryEnd = Color(0xFF22262C),
        streamStart = Color(0xFF102E16),
        streamEnd = Color(0xFF121212),
        bottomBarBorder = Color(0xFF45AD60),
        bottomBarVoiceCancelBorder = Color(0x66FF0055),
        bottomBarBottomMask = Brush.verticalGradient(
            colors = listOf(
                Color(0xFF1F1F1F),
                Color(0x4D1B2E26)
            )
        ),
        bottomBarTopMask = Brush.verticalGradient(
            colors = listOf(
                Color(0x00000000),
                Color(0x00000000)
            )
        ),
        bottomBarShadowSpot = Color(0xFF111111),
        bottomBarShadowAmbient = Color(0xFF111111),
        yuanbaoGreen = Color(0xFF45AD60),
        yuanbaoGreen05 = Color(0x0D45AD60),
        yuanbaoGreenBg = Color(0xFF15331C),
        posterGrayBg = Color(0xFFF6F6F6),  // 对话列表海报灰色背景
        posterBlueBg = Color(0xFF167BFF),   // 选中文本渐变海报蓝色背景
        digitalBgMask = Brush.verticalGradient(
            colors = listOf(
                Color(0x00123338),
                Color(0x66123338),
                Color(0xCC123338),
                Color(0xFF10393F),
                Color(0xFF0D3035)
            ),
            startY = 0f,
            endY = Float.POSITIVE_INFINITY
        ),
        inlineVideoBackground = Color(0xFF000000),
        inlineVideoCoverMask = Color(0x4D000000),
        inlineVideoPlayIconBackground = Color(0x73000000),
        inlineVideoMuteButtonBackground = Color(0x4D333333)
    ),

    coinCenter = CoinCenterColorScheme(
        pageBackground = Color(0xFF161616),
        bgGradientStart = Color(0xFF950A0A),
        bgGradientMiddle = Color(0xFF751513),
        bgGradientEnd = Color(0xFF161616),
        primaryRed = Color(0xFFBC1E21),
        textPrimaryRed = Color(0xFFEA4245),
        summaryPrimaryRed = Color(0xFFEA4245),
        summaryCardBackground = Color(0x4D000000),
        summaryCardBorder = Color(0x33FFFFFF),
        videoTaskPrimaryRed = Color(0xFFBC1E21),
        videoTaskWarmBackground = Color(0xFF3B1B14),
        videoTaskSecondaryBackground = Color(0xFF332929),
        videoTaskEnvelopeUnavailableText = Color(0xBFE6E6E6),
        checkInCardBackground = Color(0xFF3B1B14),
        checkInPanelBackground = Color(0xFF1F1F1F),
        checkInRewardPillBackground = Color(0xFF3B1B14),
        checkInRewardPillBorder = Color(0x33BC1E21),
        checkInRewardText = Color(0xFFEA4245),
        checkInClaimedRewardPillBackground = Color(0xFFBC1E21),
        checkInClaimedRewardPillBorder = Color(0x33FFF0EF),
        checkInClaimedRewardText = Color(0xFFFFFFFF),
        checkInDayText = Color(0xFFA9A9A9),
        checkInClaimedText = Color(0xFFEA4245),
        checkInButtonBackground = Color(0xFFBC1E21),
        checkInButtonText = Color(0xFFE6E6E6),
        checkInSuccessDialogBackground = Color(0xD92B2B2B),
        mainLaunchCheckInCardBackground = Color(0xFF3B1B14),
        mainLaunchCheckInPanelBackground = Color(0xFF1F1F1F),
        mainLaunchCheckInDayItemBackground = Color(0xB32B2B2B),
        mainLaunchCheckInTodayDayItemBackground = Color(0x1AFF7749),
        mainLaunchCheckInTitle = Color(0xFFE6E6E6),
        mainLaunchCheckInTitleHighlight = Color(0xFFBC1E21),
        mainLaunchCheckInDesc = Color(0xFFA9A9A9),
        mainLaunchCheckInButtonBackground = Color(0xFFBC1E21),
        mainLaunchCheckInButtonText = Color(0xFFE6E6E6),
        mainLaunchGuideDialogBackground = linearGradient(
            colorStops = arrayOf(
                0f to Color(0xFF4B2B28),
                0.2954f to Color(0xFF2B2B2B),
                0.8194f to Color(0xFF2B2B2B),
                1f to Color(0xFF322321),
            ),
            start = Offset(0f, 0f),
            end = Offset(0f, Float.POSITIVE_INFINITY)
        ),
        mainLaunchTitleImageUrl = "https://inews.gtimg.com/newsapp_bt/0/0710180546227_1881/0",
        mainLaunchHeaderIconUrl = "https://inews.gtimg.com/newsapp_bt/0/0709170113227_8667/0",
        mainLaunchUnsignedDayIconUrl = "https://inews.gtimg.com/newsapp_bt/0/0706152714287_7915/0",
        mainLaunchUnsignedIconUrl = "https://inews.gtimg.com/newsapp_bt/0/0709170620562_3336/0",
        mainLaunchDirectRewardBgUrl = "https://inews.gtimg.com/newsapp_bt/0/0709171405976_4794/0",
        mainLaunchDoubleRewardBgUrl = "https://inews.gtimg.com/newsapp_bt/0/0709171405998_4816/0",
        rewardDialogBackground = Color(0xFFFF3D3D),
        rewardDialogBorder = Color(0xFFFFD6A8),
        rewardDialogCream = Color(0xFFFFF3D1),
        rewardDialogGold = Color(0xFFFFD06A),
        rewardDialogTextRed = Color(0xFFE8482E),
        rewardDialogValueCream = Color(0xFFFFF2D3),
        rewardDialogSubText = Color(0xCCFFFFFF),
        rewardDialogTipsBackground = Color.White
    ),

    pointsCenter = PointsCenterColorScheme(
        dailyTaskTabsInactiveGradientTop = Color(0xFF3D352F),
        dailyTaskTabsInactiveGradientBottom = Color(0xFF2B2B2B),
    ),

    ad = AdColorScheme(
        adMidArticleGameScoreColor = Color(0xFFD96A35),
        adMultiImageBgColor = Color(0xFF8A8A8A),
        adVipBannerBgColor = Color(0xFFD0C6AF),
        adVipBannerTipsBgColor = Color(0xFFA32618),
        adVipBannerPTitleColor = Color(0xFF2B2B2B),
        adVipBannerPSubTitleColor = Color(0xFF828282),
        adVipBannerPPriceColor = Color(0xFF834F0E),
        adVipBannerBTitleColor = Color(0xFFD9D9D9),
        storeHaoDianColor = Color(0xFFBA966E),       // 好店标 夜间 #BA966E
        storeRGrey30Color = Color(0x4DFFFFFF),        // R标 夜间 #FFFFFF 30%
        storeRGrey55Color = Color(0x80FFFFFF),        // R标 夜间 #FFFFFF 50%

        commentStoreProductInfoStripBg = Color(0xFF2B2B2B),

        fbHorThreePointColor = Color(0xFF666666),

        game = GameColorScheme(
            reserveTitle = Color(0xFFFFFFFF),
            checkInBgColor = Color(0xFF262626),
            miniGameHallTabBackground = Color(0x26FFFFFF),
            miniGameHallTabText = Color(0xE6FFFFFF),
            miniGameHallTabActiveBackground = Color(0xFFE6E6E6),
            miniGameHallTabActiveText = Color(0xFF333333),
            calendarTitle = Color(0xFFD9D9D9),    // 游戏日历标题 - 夜间
            calendarDesc = Color(0xFF5C5C5C),     // 游戏日历描述 - 夜间
            calendarTagBg = Color(0xFFB97700),    // 游戏日历标签背景 - 夜间 #E69100 叠加 20% 黑色
            calendarCardShadow = Color(0x401F1F1F), // 游戏日历卡片阴影 - 夜间 #1F1F1F 25%
            calendarAvatarMask = Color.Black.copy(alpha = 0.2f), // 游戏日历头像遮罩 - 夜间
            categoryTabV2DescColor = Color(0xFFA9A9A9),
            categoryTabV2MetaColor = Color(0xFFD97500),
            feedCardShadow = Color(0x40000000),
            calendarBorder = Color(0x331F1F1F),
            hangCardTitle = Color(0xFF696969),
            feedCardTitle = Color(0xFFD9D9D9),
            feedCardBg = Color(0xFF262626),
            scoreText = Color(0xFFD97500),
            skinNightMask = Color(0x661F1F1F),
            skinBgGradientStart = Color(0xFFB97700),
            skinBgGradientEnd = Color(0x001F1F1F),
            skinSignInPrimary = Color(0xFFD90048),
            skinSignInSecondary = Color(0xFF332329),
            skinZoneCardGradientMiddle = Color(0xFF332329),
            skinZoneCardGradientBottom = Color(0xFF4A2A1A),
            miniGameHallRecentPlayedGradientStart = Color(0xE64E3E2D),
            miniGameHallRecentPlayedGradientEnd = Color(0xFF1F1F1F),
            miniGameHallRecentPlayedBorder = Color(0xFF4D4D4D),
            miniGameHallAddDesktopDescText = Color(0xFFD9D9D9),
        ),
    ),

    sport = SportColorScheme(
        textPrimary = Color.White,
        reservedBtnBg = Color(255, 255, 255, 26),
        reservedBtnText = Color(255, 255, 255, 77),
        textSecondary = Color(255, 255, 255, 128),
        divider = Color(255, 255, 255, 31),
        endedBtnBg = Color(255, 255, 255, 20),
        highlightBtnBg = Color(51, 119, 255, 77),
        liveRed = Color(0xFFFF0055),
    ),

    user = UserColorScheme(
        historyEditBtnDisabled = Color(0xFF4C4C4C),
    ),

    gaokao = GaokaoColorScheme(
        tabSelectedBg = Color(0xFF455B7B),       // 夜间选中背景
        tabSelectedText = Color(0xFF6C90C5),     // 夜间选中文字
        gaokaoBlueText = Color(0xFF4A83F9),      // 日夜统一蓝色文字
        primaryText = Color(0xFFDEDEDE),         // 夜间高考主要文字
        secondaryText = Color(0xFF999999),       // 夜间高考次要文字
        inactiveText = Color(0xFF5C5C5C),        // 夜间高考未选中/兜底文字
        chipBg = Color(0xFF363636),              // 夜间筛选/未选中胶囊背景
        filterChipText = Color(0xFF999999),      // 夜间筛选栏文字
        pickerSelectedText = Color(0xFF4675F2),  // 日夜统一下拉选择器选中态
        pickerDivider = Color(0xFF333333),       // 夜间下拉选择器分割线
        pickerCloseIcon = Color(0xFFA1A1A1),     // 日夜统一下拉选择器关闭图标
        placeholderText = Color(0xFF5C5C5C),     // 日夜统一空/错误占位描述
        expandToggleCollapsedText = Color(0xFF3377FF),
        expandToggleCollapsedArrow = Color(0xFF3377FF),
        expandToggleExpandedArrow = Color(0xFFAEAEAE),
        gaokaoMutedText = Color(0xFF949494),     // 日夜统一弱化灰文本
        stageTabSelectedText = Color(0xFFDEDEDE), // 夜间阶段 Tab 选中文字
        examContentText = Color(0xFF848484),     // 夜间考试日期/时间/科目文字
        examDivider = Color(0xFF3A3A3A),         // 夜间考试日期块分割线
        entryIconBackdrop = Color(0xFF6C87BD),     // 夜间入口 Icon 底托
        entryButtonBg = Color(0xFF3A3F44),       // 夜间入口按钮底层背景
        stageTimelineGradientStart = Color(0xFF4A83F9), // 夜间阶段时间轴渐变起点
        stageTimelineGradientEnd = Color(0xFF446ABA),   // 夜间阶段时间轴渐变终点
        provinceChipText = Color(0xFFDEDEDE),      // 夜间省份 Chip 文字
        paperTypeText = Color(0xFF5C5C5C),         // 夜间真题试卷类型副标题
        fanwenCardBorder = Color(0xFF455B7B),       // 夜间范文卡片边框
        fanwenCardGradientTop = Color(0xFF363636),  // 夜间范文卡片渐变上 (alpha 100%)
        fanwenCardGradientBottom = Color(0x00363636), // 夜间范文卡片渐变下 (alpha 0%)
        fanwenDetailText = Color(0xFF999999),       // 夜间范文卡片详情文字
        scoreQueryButtonBg = Color(0xFF3377FF),     // 夜间查询按钮启用态背景
        scoreInfoCardBg = Color(0xFF363636),        // 夜间位次卡片背景
        scoreInfoCardBorder = Color(0xFF5C5C5C),    // 夜间位次卡片描边
        scoreLineAxisLabel = Color(0xFF696969),     // 夜间折线图 X/Y 轴刻度
        scoreLineGridLine = Color(0xFF5C5C5C),       // 夜间一分一段折线图网格横线
        scoreLineStrokeTop = Color(0xFF328BFC),      // 夜间折线描边渐变-顶部
        scoreLineStrokeBottom = Color(0xFFBEDBFF),   // 夜间折线描边渐变-底部
        scoreLineCursor = Color(0xFF4395FF),         // 夜间定位竖虚线
        scoreLineFillTop = Color(0xFF64B4FF),        // 夜间折线下方填充渐变-顶部
        scoreLineFillBottom = Color(0xFF1F1F1F),     // 夜间折线下方填充渐变-底部
        previousYearMetaText = Color(0xFFA9A9A9),    // 夜间历史同位次标题/年份
        provincialFooterIcon = Color(0xFF5C5C5C),    // 夜间省控线底部备注图标
        subjectPrimaryText = Color(0xFFDEDEDE),      // 夜间真题科目名/按钮启用态文字
        subjectViewAllText = Color(0xFF1C75E7),      // 日夜统一真题查看全部文字
    ),
)
