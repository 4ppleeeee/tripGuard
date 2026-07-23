package com.tencent.news.qnchannel.api


annotation class RedDotType {
    companion object {
        const val ANY = -1 // 特殊标识，代表任意红点
        const val NONE = 0 // 无红点
        const val DOT = 1 // 普通小红点
        const val BUBBLE = 2 // 气泡
        const val ANIM = 3 // lottie动画遮罩
        const val LIVE_724 = 4  // 724 live
    }
}