package com.tencent.news.core.compose.platform

import kotlinx.serialization.Serializable

@Serializable
class EmptyPageArgs : IComposePageArgs

/**
 * 快速创建一个[IComposePageArgs]的空实现，可用于无参页面
 */
fun emptyPageArgs(): IComposePageArgs = EmptyPageArgs()