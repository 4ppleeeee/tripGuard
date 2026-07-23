package com.tencent.news.core.compose.scaffold

import com.tencent.news.core.compose.platform.IComposePageArgs
import com.tencent.news.core.util.lifecycle.PageLifecycleEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharedFlow

data class StructPageEnv<T : IComposePageArgs>(
    val pageArgs: T,
    val pageFlow: SharedFlow<PageLifecycleEvent>,
    val pageScope: CoroutineScope,
)