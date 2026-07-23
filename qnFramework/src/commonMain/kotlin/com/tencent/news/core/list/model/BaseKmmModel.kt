package com.tencent.news.core.list.model

import com.tencent.news.core.extension.IKmmKeep
import com.tencent.news.core.list.api.IExposure
import com.tencent.news.core.platform.BasePlatformModel


open class BaseKmmModel : BasePlatformModel(), IKmmKeep


// 可曝光的数据结构：Item、GuestInfo、TagInfo等等
open class BaseExposureKmmModel : BaseKmmModel(), IExposure {

    override val baseReportData: MutableMap<String?, String?>?
        get() = null

    override val autoReportData: MutableMap<String, Any>?
        get() = null

    private var _exposureSet: MutableSet<String>? = null

    private fun getExposureSet(): MutableSet<String> {
        val result = _exposureSet ?: HashSet()
        _exposureSet = result
        return result
    }

    override fun hasExposed(key: String?): Boolean {
        key ?: return false
        return getExposureSet().contains(key)
    }

    override fun setHasExposed(key: String?) {
        key ?: return
        getExposureSet().add(key)
    }

    fun clearExposed(key: String?): Boolean {
        key ?: return false
        return getExposureSet().remove(key)
    }

}

// 可关注的数据结构：Item、GuestInfo、TagInfo等等
open class BaseFocusKmmModel : BaseExposureKmmModel() {

    /**
     * hippy透传参数：后台下发，客户端透传给hippy，防止频繁新增客户端不用的字段
     */
    var hippyTransMap: MutableMap<String?, String?>? = null

}