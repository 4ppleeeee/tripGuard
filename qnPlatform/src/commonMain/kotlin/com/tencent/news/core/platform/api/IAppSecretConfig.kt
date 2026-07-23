package com.tencent.news.core.platform.api

import com.tencent.news.core.platform.QnPlatformLogic

/**
 * 与IAppConfig的区别：
 * 1.IAppConfig是配置一些公共的参数以及shiply参数等
 * 2.IAppSecretConfig是配置一些私密的参数，比如一些secret key/app id等，由于QnCore是public工程，一些参数必须隐藏起来，不能暴露给外部
 */
interface IAppSecretConfig {

    // 广告使用私有参数
    fun getPrivacyDESKey(): String
    fun getPrivacyDESIv(): String

    // V2版本(qn-newsig)网络请求签名密钥
    fun getHttpSignSecretKey(): String
    // V1版本(qn-sig)网络请求签名密钥
    fun getUrlSignSecretKey(): String

    // qq app id
    fun getQQAppId(): String

    // 微信 app id
    fun getWxAppId(): String
}

fun appSecretConfig(): IAppSecretConfig? = QnPlatformLogic.appSecretConfig