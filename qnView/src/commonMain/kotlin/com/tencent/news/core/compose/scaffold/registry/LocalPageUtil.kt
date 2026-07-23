package com.tencent.news.core.compose.scaffold.registry

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.tencent.news.core.util.lifecycle.PageLifecycleEvent

@Composable
fun CollectPageOnResume(key: Any, action: () -> Unit) {
    val pageLifecycleFlow = LocalComposePageLifecycleFlow.current
    LaunchedEffect(pageLifecycleFlow, key) {
        pageLifecycleFlow?.collect { event ->
            if (event == PageLifecycleEvent.ON_RESUME) {
                action()
            }
        }
    }
}

@Composable
fun CollectPageOnPause(key: Any, action: () -> Unit) {
    val pageLifecycleFlow = LocalComposePageLifecycleFlow.current
    LaunchedEffect(pageLifecycleFlow, key) {
        pageLifecycleFlow?.collect { event ->
            if (event == PageLifecycleEvent.ON_PAUSE) {
                action()
            }
        }
    }
}