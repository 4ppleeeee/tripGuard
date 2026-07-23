package com.tencent.news.core.page.model

open class StructVMHeaderWidget<T : IStructWidgetVM>(
    vm: (StructVMHeaderWidget<T>) -> T
) : HeaderWidget() {
    override fun getWidgetType() = StructWidgetType.VM_WRAPPER
    override val asWidgetVM: T by lazy { vm(this) }
}