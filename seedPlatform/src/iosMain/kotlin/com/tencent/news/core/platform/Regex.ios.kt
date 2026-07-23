package com.tencent.news.core.platform

import com.tencent.news.core.platform.api.IAppRegex

actual fun getPlatformRegex(): IAppRegex = AppPlatformRegex()