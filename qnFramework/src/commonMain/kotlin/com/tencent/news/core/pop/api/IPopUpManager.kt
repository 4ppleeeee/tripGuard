package com.tencent.news.core.pop.api

import com.tencent.news.core.annotation.RestrictedApi
import com.tencent.news.core.app.CoreDataKey
import com.tencent.news.core.app.IKmmContext
import com.tencent.news.core.app.IPageMap
import com.tencent.news.core.app.LocalKmmContext
import com.tencent.news.core.platform.api.appPageStack
import com.tencent.news.core.pop.KmmPopManager
import com.tencent.news.core.pop.KmmPopTask
import com.tencent.news.core.pop.PopResult
import com.tencent.news.core.pop.PopType

interface IPopUpManager {

    fun show(popTask: KmmPopTask): Boolean

    fun showWithResult(popTask: KmmPopTask): PopResult

    fun checkShowCondition(popTask: KmmPopTask?): Boolean

    fun dismiss(popTask: KmmPopTask?)

    fun removeItem(popTask: KmmPopTask?): Boolean

    fun clear()

    fun findPopTask(condition: (popTask: KmmPopTask) -> Boolean): KmmPopTask?

    fun findPopTaskWithType(type: PopType): KmmPopTask?
}

object PopManagerProvider {
    @OptIn(RestrictedApi::class) // 为了对齐不同宿主Context的颗粒度，这里统一使用top
    fun get(): IPopUpManager? {
        val topPage = appPageStack()?.getTopValidPage() ?: return null
        return get(topPage)
    }

    internal fun get(context: IKmmContext): IPopUpManager {
        val pageMap = context as? IPageMap
        val manager: KmmPopManager? =
            pageMap?.getValue(CoreDataKey.POPUP_MANAGER) as? KmmPopManager
        return manager ?: KmmPopManager(context).apply {
            pageMap?.setValue(CoreDataKey.POPUP_MANAGER, this)
        }
    }

    fun getAll(): List<IPopUpManager> {
        val page = appPageStack()?.getAllPages()
        return page?.map { get(it) } ?: emptyList()
    }
}
