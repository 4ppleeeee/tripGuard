package com.tencent.news.core.annotation

@RequiresOptIn("受限API，使用前请确认权限和使用场景", RequiresOptIn.Level.ERROR)
@Retention(AnnotationRetention.BINARY)
@Target(
    AnnotationTarget.FUNCTION,
    AnnotationTarget.CLASS,
    AnnotationTarget.FIELD,
    AnnotationTarget.PROPERTY
)
annotation class RestrictedApi(
    val msg: String = "",
)