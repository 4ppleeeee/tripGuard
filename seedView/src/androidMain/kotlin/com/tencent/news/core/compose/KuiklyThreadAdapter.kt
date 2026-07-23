package com.tencent.news.core.compose

import com.tencent.kuikly.core.render.android.adapter.IKRThreadAdapter
import com.tencent.news.core.platform.api.appTask
import com.tencent.news.core.platform.api.getShiplyLong
import com.tencent.news.core.platform.api.isDebug

class KuiklyThreadAdapter : IKRThreadAdapter {
    override fun executeOnSubThread(task: () -> Unit) {
        appTask().runIOAction(task)
    }

    override fun stackSize(): Long {
        // debug包保持2MB，快速发现不合理的布局
        return if (isDebug()) {
            getShiplyLong("kuikly_stack_size", 1024 * (1024 * 2))
        } else {
            // 对齐主线程8MB栈大小
            getShiplyLong("kuikly_stack_size", 1024 * (1024 * 8 - 1040)) // -1040 是和系统逻辑一致
        }
    }
}