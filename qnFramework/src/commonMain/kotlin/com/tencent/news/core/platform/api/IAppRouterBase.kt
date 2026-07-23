package com.tencent.news.core.platform.api

import com.tencent.news.core.annotation.KmmInternalApi
import com.tencent.news.core.app.IKmmContext
import com.tencent.news.core.app.LocalKmmContext
import com.tencent.news.core.compose.platform.IComposePageArgs
import com.tencent.news.core.list.model.IKmmFeedsItem
import com.tencent.news.core.platform.QnFrameworkLogic
import com.tencent.news.core.pop.PopType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// 业务侧以前抽取的 IAppRouter 与业务偶合过重，抽取一个干净的基类放在框架层
// 同样可以通过 ModelToBiz 里的工具方法做转换
interface IAppRouterBase {

    /**
     * 通过[IKmmFeedsItem]跳转到底层页
     * @param context 当前上下文，对应Android的Activity/Application或iOS的UiController，依赖于[IAppPageStack]
     * @param request 路由参数
     */
    suspend fun to(context: IKmmContext? = LocalKmmContext, request: ComponentRequest)

    /**
     * 通过[scheme]跳转到目的页
     */
    suspend fun to(context: IKmmContext? = LocalKmmContext, scheme: String)

    /**
     * 跳转到compose页面
     */
    suspend fun toComposePage(
        context: IKmmContext? = LocalKmmContext,
        pageName: String,
        pageArgs: IComposePageArgs
    )

    /**
     * 跳转到Compose Dialog，壳native实现，内容compose实现的。
     */
    suspend fun toComposeDialog(
        context: IKmmContext?,
        popType: PopType,
        pageName: String,
        pageArgs: IComposePageArgs
    )

    /**
     * 退出当前页面
     * @param context 当前上下文，对应Android的Activity/Application或iOS的UiController，依赖于[IAppPageStack]
     */
    suspend fun goBack(context: IKmmContext?)

    /**
     * 替换当前页面
     * 注意：该方法双端实现不一致
     */
    suspend fun replace(context: IKmmContext?, pushAnimation: Boolean = true, scheme: String)

    /**
     * 退出指定 context 对应的页面，context 为空时由平台实现兜底退出当前页面
     * 鸿蒙端退出 kmm 的页面可能需要提供页面名称
     */
    suspend fun quit(context: IKmmContext?, pageName: String? = null)

    /**
     * Pop 掉指定 context 上面的所有页面，让 context 自身回到导航栈顶
     * 是 [goBack] 的增强版：goBack 只退一层，popToSelf 退到指定层
     *
     * iOS: 等效 UINavigationController.popToViewController(_:animated:)
     * Android: finish 掉 context 上方的所有 Activity
     *
     * @param context 目标页面上下文，pop 完成后该页面将位于栈顶
     */
    suspend fun popToSelf(context: IKmmContext?)

}

data class ComponentRequest(
    val item: IKmmFeedsItem? = null,
    val channelId: String = "news_news_top",
    val extras: Map<String, String>? = null,
)


@OptIn(KmmInternalApi::class)
fun appRouter(): IAppRouterBase = SafeAppRouter(QnFrameworkLogic.appRouter)

/**
 * 打开新页面后退出打开前的页面。
 * @param context 打开新页面前的页面 context
 */
suspend fun IAppRouterBase.replace(context: IKmmContext?, scheme: String) {
    to(context = context, scheme = scheme)
    quit(context)
}

open class SafeAppRouter<T : IAppRouterBase>(protected val target: T?) : IAppRouterBase {

    /** 将所有调用统一切到主线程执行，同时处理 platformRouter 的空安全 */
    protected suspend inline fun onMain(crossinline block: suspend T.() -> Unit) {
        withContext(Dispatchers.Main) { target?.block() }
    }

    override suspend fun to(context: IKmmContext?, request: ComponentRequest) =
        onMain { to(context, request) }

    override suspend fun to(context: IKmmContext?, scheme: String) =
        onMain { to(context, scheme) }

    override suspend fun toComposePage(
        context: IKmmContext?,
        pageName: String,
        pageArgs: IComposePageArgs
    ) = onMain { toComposePage(context, pageName, pageArgs) }

    override suspend fun toComposeDialog(
        context: IKmmContext?,
        popType: PopType,
        pageName: String,
        pageArgs: IComposePageArgs
    ) = onMain { toComposeDialog(context, popType, pageName, pageArgs) }

    override suspend fun goBack(context: IKmmContext?) =
        onMain { goBack(context) }

    override suspend fun replace(context: IKmmContext?, pushAnimation: Boolean, scheme: String) =
        onMain { replace(context, pushAnimation, scheme) }

    override suspend fun quit(context: IKmmContext?, pageName: String?) =
        onMain { quit(context, pageName) }

    override suspend fun popToSelf(context: IKmmContext?) =
        onMain { popToSelf(context) }

}
