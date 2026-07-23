package com.tencent.news.core.platform.api

var iosComposeBridge: IIOSComposeBridge? = null

interface IIOSComposeBridge {
    /**
     * 获取iconFont字体名字和字的映射关系
     */
    fun getIconFontMapping(): Map<String, String>
}