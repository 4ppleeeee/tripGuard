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
    DIALOG_SELF_ERROR,

    /**
     * 已通过首次展示条件校验，但命中 N/Y 秒规则进入延迟队列。
     * 该结果仅表示任务已被队列接管，不代表弹窗已经真实上屏。
     */
    QUEUED,

    /**
     * 延迟任务因同形态替换、退后台或补弹失败等原因被最终舍弃。
     * 任务不会继续重试，并计入规则舍弃上报。
     */
    RULE_DISCARDED
}
