package com.tencent.news.core.ohos.setup.knoi.consumer

import com.tencent.tmm.knoi.annotation.ServiceConsumer

val ohosCookieService: OhosCookieService = getOhosCookieServiceApi()

/**
 * 鸿蒙端 ArkWeb Cookie 同步服务，由 ArkTS 侧 OhosCookieServiceImpl 提供实现。
 *
 * 业务意图：
 * - KMM common 层负责计算 Cookie 注入策略；
 * - ArkTS 侧负责真正写入 ArkWeb CookieManager，保证 H5 请求能携带登录态。
 */
@ServiceConsumer
interface OhosCookieService {

    /**
     * 读取指定 URL 对应的 ArkWeb Cookie。
     * @param url 页面 URL
     * @return Cookie 字符串，读取失败或为空时返回空字符串
     */
    fun getCookie(url: String): String

    /**
     * 写入指定 URL 对应的 ArkWeb Cookie。
     * @param url 页面 URL
     * @param value Set-Cookie 格式字段
     */
    fun setCookie(url: String, value: String)

    /**
     * 清理 ArkWeb 全部 Cookie。
     */
    fun removeAllCookies()
}
