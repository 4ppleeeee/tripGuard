package com.tencent.news.core.service

/**
 * qnFramework 用户能力桥接器。
 *
 * 默认实现不包含业务用户体系；业务 core 需要关注态等能力时再注册真实实现。
 */
object FrameworkUserBridge {

    var impl: IFrameworkUserBridge = EmptyFrameworkUserBridge
        private set

    fun register(bridge: IFrameworkUserBridge) {
        impl = bridge
    }

}

interface IFrameworkUserBridge {

    // 判断用户关注态
    fun isFollowUser(suid: String): Boolean

}

private object EmptyFrameworkUserBridge : IFrameworkUserBridge {
    override fun isFollowUser(suid: String): Boolean = false
}
