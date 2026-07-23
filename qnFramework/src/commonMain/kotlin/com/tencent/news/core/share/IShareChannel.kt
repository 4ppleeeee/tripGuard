package com.tencent.news.core.share

import com.tencent.news.core.app.IKmmContext
import com.tencent.news.core.share.api.IKmmShareData
import com.tencent.news.core.share.api.ShareChannel
import com.tencent.news.core.share.model.IShareContent

interface IShareChannel {

    val channel: ShareChannel

    fun needTwiceShare(): Boolean = false // 是否需要二次分享

    fun isSupported(): Boolean

    fun share(context: IKmmContext?, shareContent: IShareContent, shareData: IKmmShareData)

}