package com.tencent.news.extension

import QNKmmMainPlugin
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.publish.PublishingExtension
import org.gradle.kotlin.dsl.findByType
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.the
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.cocoapods.CocoapodsExtension
import org.jetbrains.kotlin.gradle.plugin.mpp.Framework
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.konan.target.Family.IOS
import java.io.File

// 自定义Task所属分组
internal const val QNKMM_TASK_GROUP = "qnkmm"

// kmm产物发布配置
internal val Project.qnPublishingExtension get() = extensions.getByType(QNKmmPublishingExtension::class.java)

internal fun Project.getMavenArtifactVersion(buildType: String): String {
    if (qnPublishingExtension.variants?.size == 1) {
        return qnPublishingExtension.version
    }
    return "${qnPublishingExtension.version}@${buildType.toLowerCase()}"
}

// Maven发布配置
internal val Project.publishingExtension get() = extensions.getByType<PublishingExtension>()

// KMM扩展
val Project.kotlinExtension get() = extensions.getByType<KotlinMultiplatformExtension>()

// libs配置
val Project.libs get() = the<org.gradle.accessors.dm.LibrariesForLibs>()

val Project.isMainProject get() = qnModuleName == "umbrella"

// Cocoapods配置，可能为空
internal val KotlinMultiplatformExtension.cocoapodsOrNull
    get() = (this as ExtensionAware).extensions.findByType<CocoapodsExtension>()

// Cocoapods配置，为空则编译时报错
internal val KotlinMultiplatformExtension.cocoapods
    get() = cocoapodsOrNull
        ?: error("You must apply the org.jetbrains.kotlin.native.cocoapods plugin to use cocoapods() configuration")

// 所有ios相关的target
internal val KotlinMultiplatformExtension.iOSTargets
    get() = targets.withType(KotlinNativeTarget::class.java)
        .filter { it.konanTarget.family == IOS }

// 是否有cocoapods插件
internal val Project.isCocoaPodsApplied get() = plugins.hasPlugin("org.jetbrains.kotlin.native.cocoapods")

// 通过iOS targets获取项目名称
internal val Project.baseName
    get() = kotlin.runCatching {
        kotlinExtension.iOSTargets.flatMap { it.binaries }.filterIsInstance<Framework>().first().baseName
    }.getOrElse { project.name }

// 产物输出目录
internal val Project.artifactOutputDir get() = "$buildDir/qnkmm"

// Android产物输出目录
internal val Project.androidArtifactOutputDir get() = "$artifactOutputDir/android"

// iOS产物输出目录
internal val Project.iOSArtifactOutputDir get() = "$artifactOutputDir/ios"

// iOS XCFramework打包zip的文件路径
internal fun Project.getXCFrameworkZipFile(buildType: String): File {
    val name = qnPublishingExtension.iosArtifactName
    return File("$iOSArtifactOutputDir/${buildType.toLowerCase()}/$name.zip")
}

// 编译生成的XCFramework产物路径
private val Project.xcFrameworkPath get() = "$buildDir/cocoapods/publish"

/**
 * 获取XCFramework产物的路径
 */
internal fun Project.getXCFrameworkPath(buildType: String): String {
    return "$xcFrameworkPath/${buildType.toLowerCase()}/"
}

/**
 * 首字母大写
 */
internal fun String.capitalizeFirstLetter() = this.replaceFirst(this.first(), this.first().toUpperCase())
