package com.tencent.news.core.list.api

import com.tencent.news.core.page.model.StructPageWidget

/**
 * 后台下发数据合法性校验
 */
interface IStructDataValidator {
    fun isDataValid(newPageWidget: StructPageWidget?): Boolean
}