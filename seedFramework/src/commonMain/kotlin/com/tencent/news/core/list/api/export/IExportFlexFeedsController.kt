package com.tencent.news.core.list.api.export

import com.tencent.news.core.extension.IOhosExportDoc

typealias FeedsCallback = (success: Boolean, json: String) -> Unit

typealias FeedsDataListChangedCallback = (refreshForward: Int, listJson: String, hasMore: Boolean) -> Unit

// todo 【架构说明】：对鸿蒙暴露的接口，支能导出基础数据结构
//  封装的 IFlexibleFeedsController 相关逻辑
interface IExportFlexFeedsController : IOhosExportDoc {

    // 枚举取值见：NewsChannel
    fun initController(argsJson: String, extraArgs: String? = null): Boolean

    // 清理 controller 持有的资源（如回调、flexCtrl 引用等）。
    // 默认返回 false，表示当前实现不支持清理，由子类按需覆写。
    fun cleanController(argsJson: String, extraArgs: String? = null): Boolean = false

    // 枚举取值见：ListRefreshForward
    // callback 中的json格式是与鸿蒙约定好的，见：ExportFeedsListBuilder
    fun doListRefresh(refreshForward: Int, paramsJson: String, callback: FeedsCallback): Boolean

    // 路由跳转：uniqueKey 会从 ExportFeedsListBuilder 返回结果里获取
    fun doClick(uniqueKey: String)

    // 曝光上报：uniqueKey 会从 ExportFeedsListBuilder 返回结果里获取
    fun doOriginExpose(uniqueKey: String)   // 原始曝光
    fun doRealExpose(uniqueKey: String)     // 真实曝光（广告特有口径：露出50%+停留1s）

    // 负反馈，删除adFeedsController持有的广告并上报
    fun doDislike(uniqueKey: String)

    // 设置列表的布局模式 0单列、1双列
    fun setListLayoutType(layoutType: Int) {}
    
    // 监听列表数据变化，callback
    fun onDataListChanged(key: String, callback: FeedsDataListChangedCallback) {}

    // 当 flexCtrl 实例就绪或被替换后调用（如 initController 完成后），补注册已缓存的 processor
    fun onFlexCtrlReady() {}

}