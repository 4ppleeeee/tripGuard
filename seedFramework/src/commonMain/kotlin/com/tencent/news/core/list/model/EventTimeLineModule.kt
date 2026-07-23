package com.tencent.news.core.list.model

import com.tencent.news.core.extension.IKmmKeep
import com.tencent.news.core.extension.isNotNullOrEmpty
import com.tencent.news.core.model.pojo.QnImage
import kotlinx.serialization.Serializable


/**
 * 事件脉络完整模块
 * Created by hengyangji on 2017/8/7.
 */


interface IKmmTimeLineModule : IKmmKeep {
    var title: String?
    var data: List<EventTimeLine>?
    var mid: String?
    var style: String?

    /** 摘要开关状态（替代旧字段 hasAbstract）：1 展示摘要开关 / 0 不展示 */
    var digestStatus: Int?

    val isAi: Boolean
        get() = "ai_thing_trace" == style
}

@Serializable
class EventTimeLineModule : IKmmTimeLineModule, IKmmKeep {
    var type: String? = null
    override var style: String? = null
    override var mid: String? = null
    override var title: String? = null

    /** 摘要开关状态原始字段，保持与 JSON key 一致，兼容 Gson 路径 */
    var digest_status: Int? = null

    /** 对外保持 camelCase 访问入口 */
    override var digestStatus: Int?
        get() = digest_status
        set(value) {
            digest_status = value
        }

    var desc: String? = null
    var count: String? = null
    var image: QnImage? = null // 时间线图片
    var lastData: EventTimeLine? = null // 事件脉络最新的一条数据
    override var data: List<EventTimeLine>? = null // 全部的事件脉络数据
    var order_show: String? = ""
    var curShowCount: Int = 0 // 正在展示的条数，处理展开逻辑，目前只在图文底层页使用
        private set
    var clientNeedHideShare: Boolean = false
    val lastTime = lastData?.time ?: ""
    val dataCount = data?.size?.toLong() ?: 0L

    fun hasTimeLineData() = data.isNotNullOrEmpty()

    fun increaseCurShowCount() {
        curShowCount += 1
    }

    fun resetCurShowCount() {
        curShowCount = 0
    }
}
