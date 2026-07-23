package com.tencent.news.qnchannel.api

import com.tencent.news.core.extension.IKmmKeep


/**
 * 频道信息由 大圣配的静态数据 + 算法/指令操作的动态数据 拼接而成；IUserChannelData 里装用户的动态数据
 */

interface IUserChannelData : IKmmKeep {
    @get:ModifyFrom
    val modifyFrom: String?

    /**
     * 频道修改的时间
     */
    val modifyTime: Long
}