package com.tencent.news.core.tads.extension

import com.tencent.news.core.tads.model.IKmmAdOrder

object IAdOrderVMEx {

    fun IKmmAdOrder?.notifyDebugInfoChanged() {
        this?.vm?.debugMsg?.createOrGet()?.notifyDebugInfoChanged()
    }

}