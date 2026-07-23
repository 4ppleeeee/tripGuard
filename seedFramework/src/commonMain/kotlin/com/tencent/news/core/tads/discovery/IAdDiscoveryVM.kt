package com.tencent.news.core.tads.discovery

import com.tencent.news.core.list.vm.IImageVM


/**
 * 发现频道
 */

interface IAdDiscoveryVM {
    /** 封面图*/
    val imgVm: IImageVM?

    /** 比例*/
    val coverRatio: Float

    /** 是否是视频*/
    val isVideoItem: Boolean
}