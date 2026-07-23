package com.tencent.news.core.compose.scaffold

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.tencent.news.core.compose.platform.EmptyPageArgs
import com.tencent.news.core.compose.platform.IComposePageArgs


@Composable
inline fun <reified T : IComposePageArgs> rememberPageEnv(
    pageArgs: T,
    pageFlow: PageFlow
): StructPageEnv<T> {
    val pageScope = rememberCoroutineScope()
    return remember(pageArgs, pageFlow) {
        StructPageEnv(pageArgs, pageFlow, pageScope)
    }
}

@Composable
fun rememberEmptyPageEnv(pageFlow: PageFlow): StructPageEnv<EmptyPageArgs> {
    val pageScope = rememberCoroutineScope()
    return remember(pageFlow) {
        StructPageEnv(EmptyPageArgs(), pageFlow, pageScope)
    }
}