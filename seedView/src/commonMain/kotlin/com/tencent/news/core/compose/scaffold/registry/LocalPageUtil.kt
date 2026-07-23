package com.tencent.news.core.compose.scaffold.registry

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberUpdatedState
import com.tencent.news.core.platform.qnFileLog
import com.tencent.news.core.util.lifecycle.PageLifecycleEvent

private const val DEBUG_TAG = "PageVisitDebug"

@Composable
fun CollectPageOnResume(key: Any, action: () -> Unit) {
    val pageLifecycleFlow = LocalComposePageLifecycleFlow.current
    LaunchedEffect(pageLifecycleFlow, key) {
        qnFileLog()?.logI(
            DEBUG_TAG,
            "[CollectOnResume] start key=${key::class.simpleName} flow=${pageLifecycleFlow != null}"
        )
        pageLifecycleFlow?.collect { event ->
            qnFileLog()?.logI(
                DEBUG_TAG,
                "[CollectOnResume] receive key=${key::class.simpleName} event=$event"
            )
            if (event == PageLifecycleEvent.ON_RESUME) {
                qnFileLog()?.logI(
                    DEBUG_TAG,
                    "[CollectOnResume] >>> invoke onPageResume key=${key::class.simpleName}"
                )
                action()
            }
        }
    }
}

@Composable
fun CollectPageOnPause(key: Any, action: () -> Unit) {
    val pageLifecycleFlow = LocalComposePageLifecycleFlow.current
    LaunchedEffect(pageLifecycleFlow, key) {
        qnFileLog()?.logI(
            DEBUG_TAG,
            "[CollectOnPause] start key=${key::class.simpleName} flow=${pageLifecycleFlow != null}"
        )
        pageLifecycleFlow?.collect { event ->
            qnFileLog()?.logI(
                DEBUG_TAG,
                "[CollectOnPause] receive key=${key::class.simpleName} event=$event"
            )
            if (event == PageLifecycleEvent.ON_PAUSE) {
                qnFileLog()?.logI(
                    DEBUG_TAG,
                    "[CollectOnPause] >>> invoke onPagePause key=${key::class.simpleName}"
                )
                action()
            }
        }
    }
}

@Composable
fun rememberPageResumedState(): State<Boolean> {
    val pageLifecycleFlow = LocalComposePageLifecycleFlow.current
        ?: return rememberUpdatedState(true)
    val initialLifecycleEvent =
        pageLifecycleFlow.replayCache.lastOrNull() ?: PageLifecycleEvent.ON_RESUME
    val lifecycleEventState = pageLifecycleFlow.collectAsState(initialLifecycleEvent)
    return rememberUpdatedState(lifecycleEventState.value == PageLifecycleEvent.ON_RESUME)
}
