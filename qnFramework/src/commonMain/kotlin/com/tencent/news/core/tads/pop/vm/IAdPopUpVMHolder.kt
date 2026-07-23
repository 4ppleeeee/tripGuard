package com.tencent.news.core.tads.pop.vm

import com.tencent.news.core.extension.IVMDoc
import com.tencent.news.core.tads.vm.VMHolder


// todo【架构说明】新增vm应遵循：(doc/【规范】模块化架构.md)
interface IAdPopUpVMHolder : IVMDoc {
    val superMaskVM: VMHolder<IAdFillScreenDialogVM>
}