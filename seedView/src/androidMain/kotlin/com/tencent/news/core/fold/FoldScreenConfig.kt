package com.tencent.news.core.fold

import com.tencent.news.core.extension.IConfigDoc
import com.tencent.news.core.platform.api.getShiplyFloat
import com.tencent.news.core.platform.api.getShiplyStringList

object FoldScreenConfig : IConfigDoc {

    // 获取目前已知Huawei的所有三折叠设备
    val getThreeFoldScreenDevices by lazy {
        getShiplyStringList("three_fold_screen_device") ?: emptyList()
    }

    // 获取Xiaomi的flip设备
    val getXiaomiFlipDevices by lazy {
        getShiplyStringList("xiaomi_flip_screen_device") ?: emptyList()
    }

    // 区分pad的参数值
    val getPadScreenParameter by lazy {
        getShiplyFloat("pad_screen_parameter", 8.0f)
    }
}