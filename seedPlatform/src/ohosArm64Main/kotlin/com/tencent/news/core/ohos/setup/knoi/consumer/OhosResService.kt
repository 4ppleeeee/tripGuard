package com.tencent.news.core.ohos.setup.knoi.consumer

import com.tencent.tmm.knoi.annotation.ServiceConsumer

val ohosResService: OhosResService = getOhosResServiceApi()

@ServiceConsumer
interface OhosResService {
    /**
     * 读取iconfont的映射关系
     */
    fun getIconFontMapping(): Map<String, String>
}