package com.tencent.kmm.demo.startup.sdk.tasks

/**
 * Bridges host-app account identity into the standard Toggle runtime.
 */
object AndroidToggleAccountBridge {
    private val lock = Any()
    private var userIdProvider: (() -> String)? = null

    fun setUserIdProvider(provider: () -> String) {
        synchronized(lock) {
            userIdProvider = provider
        }
    }

    fun currentUserId(): String {
        return synchronized(lock) { userIdProvider }?.invoke().orEmpty()
    }

    fun notifyUserChanged(userId: String = currentUserId()) {
        AndroidToggleRuntime.refreshUser(userId)
    }
}
