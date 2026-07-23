package com.tencent.news.core.tads.model.interact

/**
 * 广告互动效果配置参数接口
 * 来源: AdParser#toAdOrder 的 cross_interactive 字段
 */
interface IAdCrossInteractive {

    val backgroundColor: String       // 卡片背景色值
    val nightBgColor: String          // 卡片夜间颜色
    val imageUrl: String              // 破框图片url
    val imageHeight: Int              // 破框图片高度
    val interactDirection: Int        // 破框动效方向
    val maskShowDuration: Int         // 【必须配置】互动引导蒙层持续时长（<=0的话不展示互动）
    val maskAppearTime: Int           // 互动引导蒙层弹出时机（视频卡片用）（没配默认值 3000）
    val scrollTime: Int               // 【暂未启用】扭动时长
    val maskAppearViewedPercent: Int  // 决定广告框在屏幕中曝光N%时，可以扭动触发（没配默认50）
    val twistForwardAngle: Int        // 正向扭动(向右扭动角度)的最小角度
    val twistBackAngle: Int           // 【暂未启用】反向扭动(向左扭动角度)的最小角度
    val interactTitle: String         // 轻互动引导文案
    val flipDirection: Int            // 翻转方向

}

/**
 * 广告互动效果配置参数的扩展工具方法
 * 原 AdInteractData 类已废弃，数据已下沉到 KMM 层 IAdCrossInteractive
 */

fun IAdCrossInteractive?.positiveNum(action: IAdCrossInteractive.() -> Int, default: Int = 0): Int {
    this ?: return default

    val target = action(this)
    return if (target > 0) target else default
}
