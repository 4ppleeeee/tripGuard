package com.tencent.news.core.live.vm

import com.tencent.news.core.extension.IVMDoc
import com.tencent.news.core.tads.vm.VMHolder


// todo【架构说明】新增vm应遵循：(doc/【规范】模块化架构.md)
interface ILiveVMHolder : IVMDoc {
    
    /**
     * 直播分享卡片 ViewModel
     * 用于处理分享卡片的生命周期和资源清理
     */
    val shareCardVM: VMHolder<ILiveShareCardVM>
}