package com.tencent.news.core.tads.pendant.vm

import com.tencent.news.core.extension.IVMDoc
import com.tencent.news.core.tads.detail.vm.IAdIPLongDetailViewModel
import com.tencent.news.core.tads.vm.IVMHolder2


// todo【架构说明】新增vm应遵循：(doc/【规范】模块化架构.md)
interface IAdPendantVMHolder : IVMDoc {

    val adIPDetailPendant: IVMHolder2<IAdIPLongDetailViewModel>           // IP 底层页广告挂件

}