package com.tencent.news.core.pop

enum class PopResult {
    // 成功展示
    SUCCESS,

    // 非法数据
    ILLEGAL_DATA,

    // 被频次拦截
    FREQUENCY,

    // 当前dialog正在展示
    SHOWING,

    // 有更高优先级的弹窗
    HAS_HIGHER,

    // 被talkback拦截
    TALKBACK,

    // 弹窗自身拦截
    DIALOG_SELF_ERROR
}
