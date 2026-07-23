package com.tencent.news.core.tads.model

interface IAdQuickJumpInfo {

    val hapName: String               // 快应用名称，弹窗时展示
    val hapPackageName: String        // 快应用包名，免弹窗白名单配置使用
    var hapScheme: String             // 快应用跳转scheme,前缀hap
    val isInWhiteList: Boolean        // 在白名单
    val isInBlackList: Boolean        // 在黑名单

}