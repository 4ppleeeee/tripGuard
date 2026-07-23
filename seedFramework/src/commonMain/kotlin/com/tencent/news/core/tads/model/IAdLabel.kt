package com.tencent.news.core.tads.model

import com.tencent.news.core.extension.IKmmKeep


interface IAdLabel : IKmmKeep {

    /** 类型*/
    val type: Int

    /** 内容*/
    val content: String

    /** 游戏挂件-小游戏-二级类型*/
    val wechatGameType: Int

    /** 原生小游戏外显标签类型 */
    val nativeWGameType: Int

    /** 是否是活动标签 */
    fun isActivityLabel(): Boolean
}
