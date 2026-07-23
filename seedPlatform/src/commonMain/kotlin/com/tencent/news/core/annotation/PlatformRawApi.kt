package com.tencent.news.core.annotation

@RequiresOptIn("优先考虑封装成通用方法，屏蔽平台差异。不要直接写分平台逻辑，如需使用必须显式 @OptIn，并通过 @PlatformRawApiReason 写明原因", RequiresOptIn.Level.WARNING)
@Retention(AnnotationRetention.BINARY)
@Target(
    AnnotationTarget.FUNCTION,
    AnnotationTarget.CLASS,
    AnnotationTarget.FIELD,
    AnnotationTarget.PROPERTY,
    AnnotationTarget.PROPERTY_SETTER,
)
annotation class PlatformRawApi(
    val msg: String = "",
)

@Retention(AnnotationRetention.SOURCE)
@Target(
    AnnotationTarget.FILE,
    AnnotationTarget.CLASS,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY,
)
annotation class PlatformRawApiReason(
    val value: String,
)
