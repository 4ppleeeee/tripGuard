package com.tencent.news.core.platform

import com.tencent.news.core.annotation.KmmInternalApi
import com.tencent.news.core.platform.api.IAppRegex
import com.tencent.news.core.platform.api.IAppUri


internal expect fun getPlatformUri(): IAppUri

@KmmInternalApi
internal expect fun getPlatformRegex(): IAppRegex