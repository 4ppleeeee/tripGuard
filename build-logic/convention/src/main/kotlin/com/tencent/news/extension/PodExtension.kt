package com.tencent.news.extension

import org.jetbrains.kotlin.gradle.plugin.cocoapods.CocoapodsExtension

/**
 * 统一声明子模块所需的 pod 依赖。
 * 注意：如果 pod 来自私有 spec 仓库，需在 [specRepos] 中声明对应仓库地址，
 * 同时需在 iosApp/Podfile 中添加对应的 source 声明。
 */
fun CocoapodsExtension.addDependencies() {

    specRepos {
        url("https://git.woa.com/MicrovisionComponents/Specs.git")
        url("https://git.woa.com/T-CocoaPods/Specs.git")
        url("https://git.woa.com/TencentVideoShared/VideoBase.git")
    }

}
