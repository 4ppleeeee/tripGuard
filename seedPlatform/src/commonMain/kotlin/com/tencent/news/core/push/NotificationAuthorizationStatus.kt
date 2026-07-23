package com.tencent.news.core.push

enum class NotificationAuthorizationStatus(val value: Int) {
    NotDetermined(0),   // 未授权
    Denied(1),          // 拒绝
    Authorized(2),      // 允许
    Other(3)            // 其它
}