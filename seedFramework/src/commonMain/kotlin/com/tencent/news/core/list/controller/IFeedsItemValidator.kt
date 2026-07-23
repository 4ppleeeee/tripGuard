package com.tencent.news.core.list.controller

import com.tencent.news.core.extension.ResultEx

interface IFeedsItemValidator {

    // 是否禁止插入到列表（例如 负反馈、非法数据 等策略）
    fun isForbidInsert(): ResultEx

}