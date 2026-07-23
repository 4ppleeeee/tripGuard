package com.tencent.news.core.view

import com.tencent.news.core.page.model.StructPageWidget2
import com.tencent.news.core.setup.LazyImpl
import com.tencent.news.core.vm.IComposeContext


data class LogicContext(
    var pageWidget: IBreakCircleRef<StructPageWidget2?>? = null,
    var composeContext: LazyImpl<IComposeContext?>? = null
)

interface ILogicContextHolder {
    val logicContext: LogicContext
    fun bindingContext(action: LogicContext.() -> Unit)
}