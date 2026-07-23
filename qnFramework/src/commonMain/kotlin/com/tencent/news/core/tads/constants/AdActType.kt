package com.tencent.news.core.tads.constants

import com.tencent.news.core.extension.IAdEnumDoc
import com.tencent.news.core.tads.model.IKmmAdOrder

/**
 * 广告跳转类型（order下发的 act_type 字段）
 */
object AdActType : IAdEnumDoc {
    const val NONE = -1

    const val DOWNLOAD = 1          // 应用下载
    const val LANDING = 2           // 普通落地页（默认值）
    const val OPEN_APP = 3          // 应用直达广告

    @Deprecated("已废弃")
    const val CANVAS = 4            // 广平canvas

    @Deprecated("已废弃")
    const val WLD = 5               // 微粒贷

    const val OPEN_APP_BY_H5 = 6    // 落地页应用直达

    @Deprecated("不投放了，都可以用8代替")
    const val OPEN_NEWS_DETAIL = 7  // 新闻端内底层页打开

    const val NAV_NATIVE = 8        // 新闻Native页通过Scheme打开
    const val MINI_GAME = 9         // 微信小游戏
    const val ACT_DOUBLE_LINK = 10  // 双链直达（已安装app就直达跳转，没安装就打开外链h5）
    const val IOS_TRIPLE_LINK = 11  // iOS专用：ulink > openScheme > wxMini > h5
    const val WX_NATIVE_PAGE = 12   // 跳转微信原生页
    const val HAP = 13              // 快应用

    @Deprecated("这个含义不对")
    const val LEADS = 14            // 拉起手机号收集弹窗

    const val WX_MINI_APP = 14      // 微信小程序
    const val WX_STORE = 16         // 微信小店

}

object AdDestType {
    const val IOS_ULINK = 1     // iOS专用
    const val WX_CONSUL = 23   // 微信客服页

    fun isWxConsul(destType: Int): Boolean {
        return destType == WX_CONSUL
    }
}

fun IKmmAdOrder?.isWxConsulDt(): Boolean {
    if (this == null) return false
    return info.destType == AdDestType.WX_CONSUL
}