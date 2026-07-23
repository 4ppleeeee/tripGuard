package com.tencent.news.core.qncore

import com.tencent.news.core.extension.concatUriPath

object AppHost {
    // 注意时序，这个放最前面；否则鸿蒙里 READ_HOST 引用不到
    const val RELEASE_HOST = "https://r.inews.qq.com/"
    const val DEV_HOST = "https://dev.inews.qq.com/"

    var READ_HOST = RELEASE_HOST
    var WRITE_HOST = "https://w.inews.qq.com/"
    var CDN_HOST = "https://r.inews.qq.com/"
    var UPLOAD_HOST = "https://f.inews.qq.com/"
    var SPORT_MATCH_UPDATE_HOST = "https://news-sports.inews.qq.com/"
    var CREATOR_HOST = "https://api.tnews.qq.com"
    const val AUDIO_STATIC_HOST = "https://audio-static.inews.gtimg.com/"

    fun read(path: String) = READ_HOST.concatUriPath(path)
}
