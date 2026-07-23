package com.tencent.news.core.list.model

import com.tencent.news.core.extension.IKmmKeep
import kotlinx.serialization.Serializable

/**
 * todo 后续分享逻辑完全下沉后可以改成 internal
 * Author: joejhzhou
 * Date: 2024/10/17
 **/
@Serializable
// 后台针对不同的分享渠道会下发不同的分享标题，副标题，图片。类似和渠道相关需求在这里扩充。
class ShareDoc : IKmmKeep {

    @Serializable
    class Info : IKmmKeep {
        var shareTitle: String = ""
        var shareSubTitle: String = ""
        var shareImg: String = ""
        var shareURL: String = ""
        var shareContent: String = ""
    }

    var shareDataToFriend: Info? = null
    var shareDataToCircle: Info? = null
    var shareDataToQQFriend: Info? = null
    var shareDataToQZone: Info? = null
    var shareDataToWeibo: Info? = null
}