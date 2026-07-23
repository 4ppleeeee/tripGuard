package com.tencent.news.core.share.model

import com.tencent.news.core.extension.IKmmKeep
import com.tencent.news.core.list.model.ShareDoc


interface IShareDto : IKmmKeep {
    var shareDoc: ShareDoc?
}