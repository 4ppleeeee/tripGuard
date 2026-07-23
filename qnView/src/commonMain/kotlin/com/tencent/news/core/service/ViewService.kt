@file:OptIn(KmmInternalApi::class)

package com.tencent.news.core.service

import com.tencent.news.core.annotation.KmmInternalApi
import com.tencent.news.core.setup.get

// compose UI组件相关服务
object ViewService : IViewServiceRegistry by IViewServiceRegistry.get()