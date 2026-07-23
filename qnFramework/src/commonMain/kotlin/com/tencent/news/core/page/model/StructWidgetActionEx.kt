@file:Suppress("PropertyName", "VariableNaming")

package com.tencent.news.core.page.model

import com.tencent.news.core.extension.IKmmKeep
import com.tencent.news.core.extension.concatPrefix
import com.tencent.news.core.extension.concatUriPath
import com.tencent.news.core.extension.takeIfNotBlank
import com.tencent.news.core.platform.api.AppHost
import com.tencent.news.qnchannel.api.IChannelInfo
import com.tencent.news.qnchannel.api.getRequestHost
import kotlinx.serialization.Serializable


@Serializable
class DataRequestAction : StructWidgetAction(), IKmmKeep {
    @RequestTrigger
    var trigger_type: String = ""

    var request: MutableList<DataRequest>? = null


    fun pickClickRequest(): DataRequest? = pickAnyRequest(RequestTrigger.CLICK)

    fun pickAutoRequest(): DataRequest? = pickAnyRequest(RequestTrigger.AUTO)

    fun pickAnyRequest(@RequestTrigger triggerType: String = ""): DataRequest? {
        if (triggerType.isBlank() || (trigger_type == triggerType)) {
            return this.request.pickOne()
        }
        return null
    }

    companion object {
        fun create(cgi: String, params: Map<String, String>): DataRequestAction {
            return DataRequestAction().apply {
                trigger_type = RequestTrigger.AUTO

                request = mutableListOf(DataRequest().apply {
                    type = RequestType.REQUEST

                    service = cgi.concatPrefix("/")

                    reqdata = params.toMutableMap()
                })
            }
        }
    }

}

@Serializable
class DataFilterAction : StructWidgetAction(), IKmmKeep {
    var show_type: Int = 0

    /**
     * 用于筛选的tag，目前主要用到2个字段：
     * [TagInfoItem.id]：筛选项的id，会用于回传给接入层
     * [TagInfoItem.name]：筛选项的外显文案
     */
//    var tag_list: List<TagInfoItem>? = null
}

@Serializable
class DataRequest : IKmmKeep {
    @RequestType
    var type: String = RequestType.REQUEST

    // 优先用 dataRepoRequest，其次用 host/service 拼接
    var host: String = ""
    var service: String = ""

    var reqdata: MutableMap<String, String>? = null

    var local_data: LocalData? = null

    // 特殊的网络请求逻辑，需要强行指定dataRequest，忽略dataRepo的默认处理时使用：
    // （例如：‘话题专题’的 最新/最热 排序）
    var forceRequestIgnoreDataRepo: Boolean = false

    fun buildRequestUrl(channelInfo: IChannelInfo?): String {
        if (service.startsWith("http://") || service.startsWith("https://")) {
            return service // 如果service自己已经拼了域名了，不再重复拼接
        }

        val finalHost = host.takeIfNotBlank()
            ?: channelInfo.getRequestHost().takeIfNotBlank() // 大圣下发，替换请求接口
            ?: AppHost.READ_HOST
        return finalHost.concatUriPath(service)
    }

    fun isValid(): Boolean = service.isNotEmpty() || isValidLocalRequest()

    fun isValidLocalRequest(): Boolean = (type == RequestType.LOCAL && local_data.isValid())

}

@Serializable
class LocalData : IKmmKeep {
    var widget_list: List<StructWidget>? = null
}

fun LocalData?.isValid(): Boolean = !this?.widget_list.isNullOrEmpty()

annotation class RequestTrigger {
    companion object {
        const val INIT = "init"   // 首刷
        const val CLICK = "click"   // 用户手动点击触发
        const val AUTO = "auto"     // 列表footer加载自动触发
    }
}

annotation class RequestType {
    companion object {
        const val REQUEST = "request"   // 发起网络请求
        const val LOCAL = "local"       // 读取本地数据
    }
}

fun List<DataRequest>?.pickOne(): DataRequest? = this?.firstOrNull { it.isValid() }