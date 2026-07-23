package com.tencent.news.core.platform

import com.tencent.news.core.annotation.KmmInternalApi
import com.tencent.news.core.platform.api.IAppDtReport
import com.tencent.news.core.platform.api.IAppLogin
import com.tencent.news.core.platform.api.IAppPopBridge
import com.tencent.news.core.platform.api.IAppRouterBase
import com.tencent.news.core.platform.api.IAppShare
import com.tencent.news.core.platform.api.IAppViewBridge
import com.tencent.news.core.platform.api.IPushSwitch

// 业务框架层，依赖宿主注入的逻辑：
@KmmInternalApi
object QnFrameworkLogic : IPlatformInject {
    var appRouter: IAppRouterBase? = null   // 路由
    var popBridge: IAppPopBridge? = null    // 全局业务弹窗
    var login: IAppLogin? = null            // 通用登录态查询
    var pushSwitch: IPushSwitch? = null     // 推送相关开关
    var appShare: IAppShare? = null         // 分享弹窗相关
    var dtReport: IAppDtReport? = null      // 大同上报相关业务工具
    var viewBridge: IAppViewBridge? = null  // 视频播放器注入
}