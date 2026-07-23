package com.tencent.news.core.annotation

@RequiresOptIn("只允许业务侧调用，KMM不允许调用", RequiresOptIn.Level.ERROR)
@Retention(AnnotationRetention.BINARY)
@Target(
    AnnotationTarget.FUNCTION,
    AnnotationTarget.CLASS,
    AnnotationTarget.FIELD,
    AnnotationTarget.PROPERTY
)
annotation class OnlyHostInvokeApi(
    val msg: String = "",
)
