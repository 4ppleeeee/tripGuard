package com.tencent.news.core.list.api

import com.tencent.news.core.page.model.StructPageWidget

class SimpleLocalDataRepo(
    val builder: StructPageWidget.() -> Unit
) : IStructDataLocalRepo {
    override fun createLocalResetPageWidget() = StructPageWidget().buildPageWithManual(builder)
}
