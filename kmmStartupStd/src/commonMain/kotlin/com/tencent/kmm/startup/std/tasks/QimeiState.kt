package com.tencent.kmm.startup.std.tasks

/**
 * 启动阶段产出的 Qimei 结果缓存。
 *
 * 业务侧通过 provider 拉取这里的值，避免直接依赖平台 SDK。
 */
object QimeiState {
    var qimei: String = ""
        private set

    var qimei36: String = ""
        private set

    fun update(result: QimeiInitResult) {
        qimei = result.qimei
        qimei36 = result.qimei36
    }
}
