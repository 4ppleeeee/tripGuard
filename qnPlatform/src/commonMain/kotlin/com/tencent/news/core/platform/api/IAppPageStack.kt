package com.tencent.news.core.platform.api

import com.tencent.news.core.annotation.RestrictedApi
import com.tencent.news.core.app.IKmmContext
import com.tencent.news.core.platform.QnPlatformLogic


interface IAppPageStack {
    // 获取栈中所有页面
    fun getAllPages(): List<IKmmContext>

    // 获取栈里在前台的页面, 大部分时候返回一个，android在平行视界可能会返回多个
    fun getActivePages(): List<IKmmContext>

    // 获取栈顶活跃的页面
    @RestrictedApi("调用时请说明原因，防止获取的Context不满足预期")
    fun getTopValidPage(): IKmmContext?

    // 某个页面是否在前台
    fun isPageActive(context: IKmmContext): Boolean

    // app是否在前台
    fun applicationStateActive(): Boolean

    // 获取到页面当前的状态
    fun getPageLifecycleState(context: IKmmContext): PageLifecycleState
}

fun appPageStack(): IAppPageStack? = QnPlatformLogic.appPageStack

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