package com.tencent.news.core.extension


fun MutableSet<String>.triggerOnce(key: String, action: () -> Unit) {
    if (!contains(key)) {
        add(key)
        action()
    }
}