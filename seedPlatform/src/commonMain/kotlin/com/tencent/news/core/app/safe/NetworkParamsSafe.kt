package com.tencent.news.core.app.safe

import com.tencent.news.core.extension.IKmmKeep
import com.tencent.news.core.platform.api.isDebug


internal object NetworkParamsSafe {

    fun checkParamsValid(params: Map<*, *>?) {
        if (!isDebug()) {
            return
        }

        params?.forEach {
            when (val value = it.value) {
                is Collection<*> -> {
                    checkParamsValid(value)
                }

                is Map<*, *> -> {
                    checkParamsValid(value)
                }

                is Array<*> -> {
                    checkParamsValid(value)
                }

                else -> {
                    checkPrimaryParamValid(value)
                }
            }

        }
    }

    private fun checkParamsValid(list: Collection<*>) {
        list.forEach {
            checkPrimaryParamValid(it)
        }
    }

    private fun checkParamsValid(list: Array<*>) {
        list.forEach {
            checkPrimaryParamValid(it)
        }
    }

    private fun checkPrimaryParamValid(value: Any?) {
        value ?: return

        if (value is IKmmKeep) { // 先按白名单做检测，发现有风险的结构扔crash
            throw NetworkParamException(value::class)
        }
    }

}

class NetworkParamException(clazz: Any) : RuntimeException(
    """
        【警告】网络请求参数直接传递了model类：${clazz::class.simpleName}，客户端宿主序列化行为可能不一致：
         - 建议 params 里的参数都是基础数据类型，可以使用 List 或 Map 嵌套，但集合里面不要直接装 model 类；
         - 如果要传递复杂结构体，建议用 paramObj 参数；
         - 或主动使用 obj.toPrimitiveJsonMap 方法将请求参数转为基础数据类型。
    """
)