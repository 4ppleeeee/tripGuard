package com.tencent.kmm.demo.setup

import com.tencent.beacon.event.open.BeaconEvent
import com.tencent.beacon.event.open.BeaconReport
import com.tencent.beacon.event.open.EventType
import com.tencent.kmm.demo.core.report.beacon.BeaconReporter
import com.tencent.kmm.demo.core.report.beacon.BeaconResult

/**
 * Android 平台 Beacon 上报实现
 *
 * 通过腾讯灯塔 Beacon SDK 进行实时事件上报。
 * 在 App 启动时注入到 [com.tencent.kmm.demo.core.report.beacon.BeaconWrapper]。
 */
class AndroidBeaconReporter : BeaconReporter {

    override fun report(eventCode: String, params: Map<String, String>): BeaconResult? {
        val event = BeaconEvent.builder()
            .withCode(eventCode)
            .withParams(params)
            .withType(EventType.REALTIME)
            .build()
        val reportResult = BeaconReport.getInstance().report(event) ?: return null
        return BeaconResult(
            reportResult.eventID,
            reportResult.errorCode,
            reportResult.errMsg.orEmpty()
        )
    }
}
