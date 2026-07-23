package com.tencent.news.qncore.platform.api

import com.tencent.news.core.platform.api.PermissionScene
import com.tencent.news.core.platform.api.PermissionScenes

object QnCoreLocationScenes {
    val Recommend = PermissionScenes.DefaultLocation
    val PublishComment = PermissionScene("qncore.location.publish_comment", legacyCode = 2)
    val PublishWeibo = PermissionScene("qncore.location.publish_weibo", legacyCode = 3)
    val ChangeProfileLocal = PermissionScene("qncore.location.change_profile_local", legacyCode = 4)
    val PostWeather = PermissionScene("qncore.location.post_weather", legacyCode = 5)
    val AigcPostDetail = PermissionScene("qncore.location.aigc_post_detail", legacyCode = 6)
    val AigcAgentDetail = PermissionScene("qncore.location.aigc_agent_detail", legacyCode = 7)
}
