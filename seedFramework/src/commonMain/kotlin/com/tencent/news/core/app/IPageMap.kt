package com.tencent.news.core.app

/**
 * 页面通用参数Key的通用接口
 */
interface IDataKey

interface IPageMap {
    fun setValue(key: IDataKey?, value: Any?)
    fun getValue(key: IDataKey?): Any?
}

enum class CoreDataKey : IDataKey {
    POPUP_MANAGER, // PopManager 弹窗管理器
}
