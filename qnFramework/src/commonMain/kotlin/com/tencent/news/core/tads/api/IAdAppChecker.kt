package com.tencent.news.core.tads.api

interface IAdAppChecker {
    // 微信是否安装
    fun isWxAppInstalled(): Boolean

    // 是否是wx支持的api (Android特有)
    fun isWXAppSupportAPI(): Boolean

    // 检查某个scheme对应app是否安装（优先使用pkgName判断，没有的话从openScheme解析包名再判断一次）
    fun isAppInstalled(openScheme: String?, pkgName: String?): Boolean
}