package com.tencent.news.utils

import com.tencent.news.build.logic.findPropertyAnyWhere
import org.gradle.api.Project

fun Project.runIfOhosBuild(action: (Project) -> Unit) {
    if (isOhosBuild()) {
        action(this)
    }
}

fun Project.runIfNotOhosBuild(action: (Project) -> Unit) {
    if (!isOhosBuild()) {
        action(this)
    }
}

fun Project.runIfNotIosBuild(action: (Project) -> Unit) {
    println("当前编译环境(非iOS): ${getBuildPlatform()}")
    if (!isIOSBuild()) {
        action(this)
    }
}

fun Project.runIfAndroidBuild(action: (Project) -> Unit) {
    println("当前编译环境(仅Android): ${getBuildPlatform()}")
    if (isAndroidBuild()) {
        action(this)
    }
}

fun Project.isOhosBuild() = "ohos" == getBuildPlatform()

fun Project.isIOSBuild() = "ios" == getBuildPlatform()

fun Project.isAndroidBuild() = "android" == getBuildPlatform()

private fun Project.getBuildPlatform() = findPropertyAnyWhere("build.platform")