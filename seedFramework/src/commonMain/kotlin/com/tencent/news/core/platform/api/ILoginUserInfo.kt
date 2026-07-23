package com.tencent.news.core.platform.api


/**
 * 登录信息协议
 */

interface ILoginUserInfo {
    // 新闻登录态是否有效（平时大多数用这个，理论上永不过期）
    // todo genesisli opt: 这个名改一下，不适合叫 strict了
    fun isStrictLogin(): Boolean

    // 特定渠道强登录态：普遍用于视频会员等特殊场景
    fun isQQStrictLogin(): Boolean  // （不论主副账号）
    fun isWxStrictLogin(): Boolean  // （不论主副账号）
}