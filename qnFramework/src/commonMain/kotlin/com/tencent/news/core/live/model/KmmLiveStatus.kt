package com.tencent.news.core.live.model

import com.tencent.news.core.extension.IEnumDoc

enum class KmmLiveStatus(val value: Int) : IEnumDoc {
    COMING(1),
    LIVING(2),
    END(3),
    REPLAY(4),
}