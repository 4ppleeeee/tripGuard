package com.tencent.news.core.list.trace

import com.tencent.news.core.extension.getNonNull

object NewsChannelLog : BaseBizLog("NChl")
object ComposeViewLog : BaseBizLog("ComposeView")
object AppLocationLog : BaseBizLog("AppLocation")
object DayFreqLog : BaseBizLog("DayFreq")
object LifecycleLog : BaseBizLog("Lifecycle")
object PopLog : BaseBizLog("Pop")
object AppStatusLog : BaseBizLog("AppStatus")
object ShareLog : BaseBizLog("Share")
object AlphaVideoLog : BaseBizLog("AlphaVideo")
object NewsAIGCLog : BaseBizLog("NewsAigc")

fun String?.trimLogChannel(): String = this?.replace("news_", "").getNonNull()
