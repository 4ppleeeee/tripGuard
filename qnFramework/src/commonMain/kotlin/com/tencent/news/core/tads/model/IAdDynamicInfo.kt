package com.tencent.news.core.tads.model

import com.tencent.news.core.extension.IKmmKeep
import kotlinx.serialization.Transient


interface IAdDynamicInfo : IKmmKeep {
    var templateId: String      // 模板id（新增模板时，客户端需配置白名单）

    var styleId: String         // 样式id

    var moduleId: String        // 模块id

    var moduleVersion: String   // 对应sdk版本

    @Transient
    var errorInfo: String       // 本地绑定的错误信息

    @Transient
    var rollbackForError: Boolean   // 本地加载发生错误，需要降级回滚的标识

    val upTemplateLevel: Boolean  // 提升模版的优先级（动态化优先级 < 外显下载）
}