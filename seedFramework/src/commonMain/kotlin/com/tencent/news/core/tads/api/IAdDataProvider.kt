package com.tencent.news.core.tads.api

interface IAdDataProvider {

    // ssp下发的广告json数据
    fun getAdListJson(): String?
    fun setAdListJson(newAdList: String?) {}

    // 迁移了kmm逻辑才有这个
    fun getAdHolder(): IAdHolder? = null
    fun setAdHolder(adHolder: IAdHolder?) {}

    // 广告后台cep服务给的中插等其他广告数据
    fun getValueAddedContent(): String? = null

}