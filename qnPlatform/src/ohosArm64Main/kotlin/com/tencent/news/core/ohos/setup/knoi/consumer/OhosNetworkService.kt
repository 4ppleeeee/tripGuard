package com.tencent.news.core.ohos.setup.knoi.consumer

import com.tencent.tmm.knoi.annotation.ServiceConsumer
import com.tencent.tmm.knoi.type.JSValue

val ohosNetworkService = getOhosNetworkServiceApi()

@ServiceConsumer
interface OhosNetworkService {

    fun getTotalRxBytes(): JSValue
    fun getTotalTxBytes(): JSValue
    fun subscribeNetState(onChange: (state: String) -> Unit): Unit

}