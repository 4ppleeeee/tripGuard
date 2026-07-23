package com.tencent.news.core.ohos.framework

import com.tencent.news.core.annotation.KmmInternalApi
import com.tencent.news.core.app.IKmmContext
import com.tencent.news.core.compose.platform.IComposePageArgs
import com.tencent.news.core.platform.QnFrameworkLogic
import com.tencent.news.core.platform.api.ComponentRequest
import com.tencent.news.core.platform.api.IAppRouterBase
import com.tencent.news.core.pop.IPopType
import com.tencent.tmm.knoi.annotation.KNCallback
import com.tencent.tmm.knoi.type.JSValue

typealias IOhosAppRouter = JSValue

/**
 * 鸿蒙端路由注入。
 * 通过 knoi @KNCallback 机制，将 ArkTS 侧的路由实现桥接到 KMP 层的 IAppRouterBase。
 */
@OptIn(KmmInternalApi::class)
fun setupOhosAppRouter(router: IOhosAppRouter) {
    val ohosRouter = router.asOhosAppRouter()
    QnFrameworkLogic.appRouter = object : IAppRouterBase {

        override suspend fun to(context: IKmmContext?, request: ComponentRequest) {
            val scheme = request.item?.flexDto?.url.orEmpty()
            if (scheme.isNotEmpty()) {
                openSchemeOrUrl(ohosRouter, scheme)
            }
        }

        override suspend fun to(context: IKmmContext?, scheme: String) {
            openSchemeOrUrl(ohosRouter, scheme)
        }

        override suspend fun toComposePage(
            context: IKmmContext?,
            pageName: String,
            pageArgs: IComposePageArgs
        ) {
            ohosRouter.toComposePage(pageName, pageArgs.pushPageArgsToMap, pageArgs.launchType.name)
        }

        override suspend fun toComposeDialog(
            context: IKmmContext?,
            popType: IPopType,
            pageName: String,
            pageArgs: IComposePageArgs
        ) {
            // 鸿蒙端暂不区分 Dialog 和 Page，统一走 toComposePage
            ohosRouter.toComposePage(pageName, pageArgs.pushPageArgsToMap, pageArgs.launchType.name)
        }

        override suspend fun goBack(context: IKmmContext?) {
            ohosRouter.goBack()
        }

        override suspend fun moveTaskToBack(context: IKmmContext?) {
            ohosRouter.moveTaskToBack()
        }

        override suspend fun replace(
            context: IKmmContext?,
            pushAnimation: Boolean,
            scheme: String
        ) {
            ohosRouter.goBack()
            ohosRouter.toScheme(scheme)
        }

        override suspend fun quit(context: IKmmContext?) {
            ohosRouter.goBack()
        }
    }
}

private fun openSchemeOrUrl(router: OhosAppRouter, value: String) {
    router.toScheme(value)
}

/**
 * ArkTS 侧路由回调接口。
 * knoi 会自动生成 ArkTS 侧的 TypeScript 接口定义和桥接代码。
 */
@KNCallback
interface OhosAppRouter {
    /** 通过 scheme 跳转 */
    fun toScheme(scheme: String)

    /** 跳转到 Compose 页面 */
    fun toComposePage(pageName: String, pageData: Map<String, Any>, launchType: String)

    /** 返回上一页 */
    fun goBack()

    /** 将当前任务退到后台 */
    fun moveTaskToBack()
}
