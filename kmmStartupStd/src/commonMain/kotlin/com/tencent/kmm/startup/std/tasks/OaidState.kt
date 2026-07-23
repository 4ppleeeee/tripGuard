package com.tencent.kmm.startup.std.tasks

/**
 * 启动阶段产出的设备 OAID 缓存。
 *
 * 通过 VendorManager（qmsp-oaid2 SDK）获取并缓存 OAID。
 * 业务侧通过此 State 拉取设备 OAID，避免直接依赖平台 SDK。
 */
object OaidState {
    var oaid: String = ""
        private set

    fun update(oaid: String) {
        this.oaid = oaid
    }
}
