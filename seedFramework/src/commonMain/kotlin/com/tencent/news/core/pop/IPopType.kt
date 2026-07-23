package com.tencent.news.core.pop

interface IPopType {
    val implType: PopImplType
    val typeId: String // 对应枚举值的name
    val triggerType: TriggerType get() = TriggerType.DEFAULT
    val talkbackIntercept: Boolean get() = false // 无障碍模式下，要拦截展示

    fun getPriority(isLocal: Boolean = true): Int
}