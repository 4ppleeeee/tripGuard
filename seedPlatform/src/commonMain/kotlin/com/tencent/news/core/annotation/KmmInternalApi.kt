package com.tencent.news.core.annotation

@RequiresOptIn("kmm框架内可调用，业务侧不允许直接调用", RequiresOptIn.Level.ERROR)
@Retention(AnnotationRetention.BINARY)
@Target(
    AnnotationTarget.FUNCTION,
    AnnotationTarget.CLASS,
    AnnotationTarget.FIELD,
    AnnotationTarget.PROPERTY,
    AnnotationTarget.PROPERTY_SETTER,
)
annotation class KmmInternalApi(
    val msg: String = "",
)