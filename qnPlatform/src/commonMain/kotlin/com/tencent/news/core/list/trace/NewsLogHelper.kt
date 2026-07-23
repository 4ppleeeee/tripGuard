package com.tencent.news.core.list.trace

import com.tencent.news.core.annotation.KmmInternalApi
import com.tencent.news.core.extension.ILogDoc
import com.tencent.news.core.extension.getNonNull
import com.tencent.news.core.isHarmonyPlatform
import com.tencent.news.core.platform.api.isDebug
import com.tencent.news.core.platform.qnFileLog
import com.tencent.news.core.platform.qnLogcat


// 【qnFeeds】
object NewsChannelLog : BaseBizLog("NChl")                              // 信息流（频道、列表 等）

// 【qnDetail】
object NewsDetailLog : BaseBizLog("NewsDetailLog")                      // 底层页
object MorningPostLog : BaseBizLog("MorningPost")                       // 早晚报
object EventLog : BaseBizLog("EventLog")                                 // 专题
object MixedLandingCloudReplaceLog : BaseBizLog("MixedLandingCloudReplace")      // 混合流落地页云重排

// 【qnAd】
object NewsGameLog : BaseBizLog("NewsGame")                             // 游戏
object AlphaVideoLog : BaseBizLog("AlphaVideo")                         // 透明视频

// 【qnMedia】
object NewsVideoLog : BaseBizLog("NewsVideo")                           // 视频
object Tab2CloudRerankLog : BaseBizLog("Tab2CloudRerank")               // tab2云重排
object LiveLog : BaseBizLog("NewsLive")                                 // 直播
object AudioServiceLog : BaseBizLog("NewsAudio", subTag = "Service")    // audioServiceLog
object NewsAudioLog : BaseBizLog("NewsRadio")                           // 音频
object AudioPodLog : BaseBizLog("AudioPod")                             // 播客
object AudioTabRadioLog : BaseBizLog("AudioTab3Radio")                  // 电台音频tab
object TTSLog : BaseBizLog("TTSLog")                                    // tts 断句

object SportLog : BaseBizLog("SportLog")                                // 体育相关

// 【qnUser】
object NewsFavoriteLog : BaseBizLog("NewsFavorite")                     // 收藏
object NewsHistoryLog : BaseBizLog("NewsHistory")                       // 历史
object NewsFollowLog : BaseBizLog("NewsFollow")                         // 关注
object NewsSubLog : BaseBizLog("NewsSub")                               // 订阅
object NewsLikeLog : BaseBizLog("NewsLike")                             // 点赞
object NewsSubscribeLog : BaseBizLog("NewsSubscribe")                   // 预约
object NewsLoginLog : BaseBizLog("NewsLogin")                           // 登录
object NewsEditorLog : BaseBizLog("NewsEditor")                         // AI声明内容日志
object InterfaceVoteLog : BaseBizLog("InterfaceVote")                   // 互动轻表态投票日志
object UserSponsorLog : BaseBizLog("UserSponsorLog")                    // 加热支持
object NewsPayLog : BaseBizLog("NewsPay")                               // 内容付费
object NewsAigcAudioLog : BaseBizLog("AigcAudio")                       // 新闻妹语音播放
object NewsAIGCLog : BaseBizLog("NewsAigc")                             // 新闻妹 AIGC
object MemberRankLog : BaseBizLog("NewsPay/MemberRank")                 // 付费-排行榜
object CoinCenterLog : BaseBizLog("CoinCenter")                         // 金币中心
object CheckInLog : BaseBizLog("CheckIn")                               // 积分签到
object MyHistoryLog : BaseBizLog("MyHistory")                           // 我的历史
object GaokaoLog : BaseBizLog("Gaokao")                                 // 高考（搜索结果页/卡片等）

// app公共日志：
object PushGuideFreqLog : BaseBizLog("PushGuideFreq")                   // PushGuide频控
object ComposeViewLog : BaseBizLog("ComposeView")                       // compose ui日志
object WebViewLog : BaseBizLog("WebView")                               // WebView/JSAPI日志
object NewsRouterLog : BaseBizLog("NewsRouter")                         // 路由日志
object AppStatusLog : BaseBizLog("AppStatus")                           // app状态开关等
object NetworkLog : BaseBizLog("NetworkRelay")                          // 网络缓存
object LottieLog : BaseBizLog("Lottie")                                 // Lottie动画日志
object AppLocationLog : BaseBizLog("AppLocation")                       // app定位日志
object NewsLruCache : BaseBizLog("NewsLruCache")                        // LruCache通用日志
object NewsJson : BaseBizLog("NewsJson")                                // Json解析通用日志
object NewsTimeLog : BaseBizLog("NewsTime")                             // 耗时埋点
object DayFreqLog : BaseBizLog("DayFreq")                               // 频控
object NotificationLog : BaseBizLog("Notification")                     // 通知日志
object BeaconLog : BaseBizLog("Beacon")                                 // 灯塔上报日志
object PopLog : BaseBizLog("Pop")                                       // 弹窗框架
object DateLog : BaseBizLog("Date")                                       // 日期日志
object EventBusLog : BaseBizLog("EventBus")                                // EventBus日志
object LifecycleLog : BaseBizLog("Lifecycle")                            // 生命周期日志
object IntentLog : BaseBizLog("Intent")                                   // 页面 NewIntent 事件流
object DemoLog : BaseBizLog("Demo")                                         // Demo日志
object BridgeLog : BaseBizLog("Bridge")                                    // 与宿主桥接层日志
object NewsSkinLog : BaseBizLog("NewsSkin")                                // 皮肤日志

// FIXME: AI 审查提示 hareLog属于业务模块功能，需迁移到业务模块。本页内容是否统一迁移？
object ShareLog : BaseBizLog("Share")                                   // 分享日志


@OptIn(KmmInternalApi::class)
open class BaseBizLog(val tag: String, val subTag: String = "") : ILogDoc {

    inline fun verbose(subTag: String, msg: () -> String) {
        if (isDebug()) {
            qnLogcat()?.logV("$tag/$subTag", msg())
        }
    }

    // 比较耗时的拼接用这个
    inline fun debug(subTag: String = this.subTag, msg: () -> String) {
        if (isDebug()) {
            qnLogcat()?.logI("$tag/$subTag", msg()) // 不细分d和i级别了，最常用的都是这个i的
        }
    }

    inline fun warn(subTag: String = this.subTag, msg: () -> String) {
        if (isDebug()) {
            qnLogcat()?.logW("$tag/$subTag", msg())
        }
    }

    fun fileLog(msg: String) {
        fileLog(subTag, msg)
    }

    fun fileLog(subTag: String = this.subTag, msg: String) {
        qnFileLog()?.logW("$tag/$subTag", msg)
    }

    fun error(msg: String, error: Throwable? = null) {
        error(subTag, msg, error)
    }

    fun error(subTag: String = this.subTag, msg: String, error: Throwable? = null) {
        if (isHarmonyPlatform()) {  // 鸿蒙平台不支持堆栈
            qnFileLog()?.logE("$tag/$subTag", msg, null)
        } else {
            qnFileLog()?.logE("$tag/$subTag", msg, error)
        }
    }

}

fun String?.trimLogChannel(): String = this?.replace("news_", "").getNonNull()
