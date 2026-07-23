package com.tencent.news.core.ip.model

import com.tencent.news.core.extension.IKmmKeep


interface ILatestSceneInfo : IKmmKeep {

    /**
     * 用于订阅
     */
    val cmsId: String?

    /**
     * 对应的id
     */
    val sceneId: String?

    /**
     * 对应的title
     */
    val sceneTitle: String?

    /**
     * 是否预约
     */
    var isOrder: Boolean

    /**
     * 类型，是否是预告片: chopper | trailer | playback
     */
    val sceneType: String?

    /**
     * 是否是预告片
     */
    val isTrailer: Boolean
}