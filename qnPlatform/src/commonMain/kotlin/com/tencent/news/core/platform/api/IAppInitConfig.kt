package com.tencent.news.core.platform.api

import com.tencent.news.core.detail.SsmlInfo

interface IAppInitConfig {
    val ssmlInfo: List<SsmlInfo>?
}