package com.tencent.news.core.platform

import platform.UIKit.UIDevice

/**
 * 设备类型枚举
 */
enum class DeviceType(val value: Int) {
    PHONE(0),  // 手机
    PAD(1),    // 平板
    UNKNOWN(-1) // 未知设备类型
}

/**
 * 判断当前设备是否为iPad
 */
fun isPad(): Boolean {
    return UIDevice.currentDevice.userInterfaceIdiom.toInt() == DeviceType.PAD.value
}
