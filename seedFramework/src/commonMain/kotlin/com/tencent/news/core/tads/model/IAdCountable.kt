package com.tencent.news.core.tads.model

import com.tencent.news.core.platform.getCurTimeMillis

// 广告倒计时能力接口，提供倒计时相关的基础能力
interface IAdCountable {

    fun countdownTime(): Long // 倒计时时间（毫秒）

    fun fetchedTimestamp(): Long // 获取时间戳（客户端本地赋值）

    // 获取当前剩余时间
    fun getCurrentRestTime(): Long {
        return countdownTime() - (getCurTimeMillis() - fetchedTimestamp())
    }
}
