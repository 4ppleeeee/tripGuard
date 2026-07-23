package com.tencent.news.core.ohos.setup

import com.tencent.news.core.platform.QnPlatformLogic
import com.tencent.news.core.platform.api.IEvent
import com.tencent.news.core.platform.api.IEventBus
import com.tencent.news.core.platform.api.appEventFlow
import com.tencent.news.core.platform.qnLogcat

/**
 * 注入鸿蒙端 IEventBus 实现。
 *
 * 事件分发策略：
 * 1. 【KMP 内部订阅者】通过 `appEventFlow().emit(event)` 广播，任何 Kotlin 侧调用方
 *    都可以通过 `appEventFlow().collect { }` / `safeCollect<T>(scope) { }` 订阅同一个事件流。
 * 2. 【ArkTS 宿主订阅者】如果后续业务需要把事件透传到 ArkTS 侧（例如广告视频 feedback 移除），
 *    可参考腾讯新闻 `OhosAppEventBus` 的实现：
 *      - 让宿主通过 knoi @KNCallback 注入一个 "post(eventJson: String)" 接口；
 *      - 这里在 post() 中判断 exportEventName 非空时调用 `event.exportJson()` 并转发给宿主。
 *    当前项目暂无此业务诉求，先保留 Kotlin 侧的事件闭环即可。
 *
 * 注入后，业务层 `appEventBus().post(event)` 不再走 commonMain 的 DefaultEventBus 空实现，
 * 订阅方能真正收到事件。
 */
fun setupOhosAppEventBus() {
    QnPlatformLogic.eventBus = OhosAppEventBus
}

private object OhosAppEventBus : IEventBus {

    private const val TAG = "OhosAppEventBus"

    override fun post(event: IEvent) {
        qnLogcat()?.logI(TAG, "post event: ${event::class.simpleName}")
        appEventFlow().emit(event)
    }

    override fun postSticky(event: IEvent) {
        // 当前 MutableSharedFlow(replay=0) 不支持真正意义上的 sticky 语义；
        // 鸿蒙端业务暂无订阅"过去事件"的需求，按普通 post 处理，保持三端行为一致。
        qnLogcat()?.logI(TAG, "postSticky event: ${event::class.simpleName}")
        appEventFlow().emit(event)
    }
}
