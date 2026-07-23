package com.tencent.news.core.platform.api

import com.tencent.news.core.app.IKmmContext
import com.tencent.news.core.platform.QnPlatformLogic


interface IAppPageStack {

    fun onPageCreated(stack: Any?, context: IKmmContext)

    fun onPageDestroyed(stack: Any?, context: IKmmContext)

    // 获取栈中所有页面
    fun getAllPages(): List<IKmmContext>

    // 获取栈里在前台的页面, 大部分时候返回一个，android在平行视界可能会返回多个
    fun getActivePages(): List<IKmmContext>

    // 获取栈顶活跃的页面，栈为空时返回 null（如 Application onCreate 阶段尚未有 Activity 入栈）
    fun getTopValidPage(): IKmmContext?

    // 某个页面是否在前台
    fun isPageActive(context: IKmmContext): Boolean

    // app是否在前台
    fun applicationStateActive(): Boolean

    // 获取到页面当前的状态
    fun getPageLifecycleState(context: IKmmContext): PageLifecycleState
}

fun appPageStack(): IAppPageStack = QnPlatformLogic.appPageStack ?: emptyPageStackImpl

private val emptyPageStackImpl by lazy { DefaultAppPageStack() }

/**
 * IAppPageStack 的默认空实现，各方法返回安全的默认值
 */
open class DefaultAppPageStack : IAppPageStack {

    private val stackPages = mutableMapOf<Any?, MutableList<IKmmContext>>()

    override fun onPageCreated(stack: Any?, context: IKmmContext) {
        val pages = stackPages.getOrPut(stack, { mutableListOf() })
        pages.add(context)
    }

    override fun onPageDestroyed(stack: Any?, context: IKmmContext) {
        stackPages[stack]?.let { pages ->
            pages.remove(context)
            if (pages.isEmpty()) {
                stackPages.remove(stack)
            }
        }
    }

    override fun getAllPages(): List<IKmmContext> = stackPages.values.flatten()
    override fun getActivePages(): List<IKmmContext> = stackPages.values.mapNotNull { it.lastOrNull() }
    // todo fitzwu 适配平行视界
    override fun getTopValidPage(): IKmmContext? = getActivePages().firstOrNull()
    override fun isPageActive(context: IKmmContext): Boolean = getActivePages().contains(context)
    override fun applicationStateActive(): Boolean = false
    override fun getPageLifecycleState(context: IKmmContext): PageLifecycleState =
        PageLifecycleState.UNKNOWN
}

/**
 * 首页，目前只做标记使用，后续kmm使用可以拓展此方法
 */
interface IKmmHomePage : IKmmContext

/**
 * h5页面，用作标记
 */
interface IWebPage : IKmmContext

/**
 * 登录相关页面，用作标记
 */
interface ILoginPage : IKmmContext

/**
 * 首页四大tab
 */
interface IKmmHomeSubPage {
    // todo 这里叫getId，getPageId更好，没办法好名字都被其他接口占用了，先叫这个凑活用着
    fun getHomeSubPageId(): String
}

/**
 *  列表页卡
 */
interface IKmmListFragment


enum class PageLifecycleState {
    UNKNOWN,
    CREATE,
    START,
    RESUME,
    PAUSE,
    STOP,
    DESTROY
}