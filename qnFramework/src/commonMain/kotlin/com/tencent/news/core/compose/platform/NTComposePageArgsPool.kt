package com.tencent.news.core.compose.platform

import com.tencent.news.core.list.trace.ComposeViewLog
import com.tencent.news.core.platform.Lock
import com.tencent.news.core.platform.api.getShiplySwitch
import com.tencent.news.core.platform.synchronized
import kotlin.math.max

/**
 * SDK因为要支持动态化能力，暂时不支持直接传递model类，所以暂时先搞个池子，将宿主传过来的model根据其hashcode存起来,用的时候再取出来
 */
object NTComposePageArgsPool {

    const val PAGE_DATA_KEY = "page_data_identifier"

    private val pageDataPool = RefCountPool()

    private val lock = Lock()

    // todo 【架构说明】对于安卓和iOS，由于没有跨引擎通信，是可以直接传递model类对象的
    //  这里通过一个 identifier 来传递model
    //  （鸿蒙无法这么干，js和c之间有跨栈通信，需要用json传递）
    internal fun pushPageArgsToMap(pageArgs: IComposePageArgs): Map<String, Any> =
        synchronized(lock) {
            pageDataPool.push(pageArgs)
            return mutableMapOf(PAGE_DATA_KEY to pageArgs.identifier)
        }

    // 一次性消费pageArgs，取出来就移除cache
    fun popPageArgs(pageArgsIdentifier: Int): IComposePageArgs? =
        synchronized(lock) {
            pageDataPool.pop(pageArgsIdentifier)
        }


    // 支持引用计数，解决这个crash：
// https://bugly.woa.com/v2/exception/crash/issues/detail?cId=13f46011-ab3f-4715-9d54-da1c769b13c5&clusterStackType=&feature=65A43804539E537E930F82B86319A977&messageTab=%E8%81%94%E5%8A%A8%E7%9B%91%E6%8E%A7&pid=1&productId=0d8bed2efe&tab=case&token=9076995933ec8943e356b0703b6b6f03
    private class RefCountPool {

        private val enableRefCount by lazy {
            getShiplySwitch("enable_page_args_ref_count", true)
        }

        private val pageDataPool = mutableMapOf<Int, IComposePageArgs>()
        private val pageArgsRefCount = mutableMapOf<Int, Int>()

        fun push(pageArgs: IComposePageArgs) {
            val identifier = pageArgs.identifier
            pageDataPool[identifier] = pageArgs

            val newRefCount = pageArgsRefCount.getOrPut(identifier) { 0 } + 1
            pageArgsRefCount[identifier] = newRefCount

            debugLog { "pushPageArgs[${identifier}_ref${newRefCount}]: $pageArgs" }
        }

        fun pop(identifier: Int): IComposePageArgs? {
            val newRefCount = max(0, pageArgsRefCount.getOrPut(identifier) { 0 } - 1)
            pageArgsRefCount[identifier] = newRefCount

            val result = if (enableRefCount && newRefCount > 0) {
                pageDataPool[identifier] // 有不只1个人在引用，先不能remove
            } else {
                pageDataPool.remove(identifier)
            }

            debugLog { "popPageArgs[${identifier}_ref${newRefCount}]: $result" }
            return result
        }

        private inline fun debugLog(msg: () -> String) {
            ComposeViewLog.debug(subTag = "Args", msg = msg)
        }
    }

}
