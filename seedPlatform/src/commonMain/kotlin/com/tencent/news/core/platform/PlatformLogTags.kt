package com.tencent.news.core.platform

import com.tencent.news.core.list.trace.BaseBizLog

@PublishedApi
internal object PlatformJsonLog : BaseBizLog("Json")

internal object PlatformNetworkLog : BaseBizLog("NetworkRelay")

internal object PlatformBeaconLog : BaseBizLog("Beacon")
