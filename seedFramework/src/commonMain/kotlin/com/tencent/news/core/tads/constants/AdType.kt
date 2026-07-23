package com.tencent.news.core.tads.constants


/**
 * 广告轮播方式（可以理解为广告请求的类型）；
 * 对应广告请求里的 [adtype] 参数
 */

object AdType {

    // 【重要】正常后台轮播（信息流场景主要都是这个）
    const val TIMELINE = 0

    // 【废弃】前台轮播（目前没用过）
    const val PLAY_ROUND_SDK = 1

    // 【重要】闪屏预加载
    const val SPLASH_PRELOAD = 2

    // 【重要】闪屏实时选单
    const val SPLASH_REALTIME = 3

    // 【废弃】缓存池请求（目前没用过）
    const val PLAY_ROUND_BACKUP = 4

    // 【一般】预加载，有定向逻辑
    const val TIMELINE_PRELOAD = 6

}