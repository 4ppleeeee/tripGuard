@file:Suppress("UnstableApiUsage")

import java.io.File
import java.util.Properties


val BUILD_TYPE_ANDROID = "android"
val BUILD_TYPE_IOS = "ios"
val BUILD_TYPE_OHOS = "ohos"

val AGP = "agp"
val KOTLIN = "kotlin"
val KSP = "ksp"
val SERIALIZATION = "serialization"
val COMPOSE = "compose-multiplatform"
val COMPOSE_PLUGIN = "compose-plugin"
val KUIKLY = "kuikly"
val KUIKLY_COMPOSE = "kuikly-compose"
val KUIKLY_SHORT = "kuikly-short"
val KOTLINX_COROUTINES = "kotlinx-coroutines"
val MARKDOWN_RENDER = "markdown-render"
val KOTLINX_DATETIME = "kotlinx-datetime"
val KTOR = "ktor"
val KOTLIN_PLUGIN = "kuikly-plugin"

// 【注意】升级kuikly时统一改这个版本，sdk的后缀在各个平台不一样，轻易不会改
val KUIKLY_VERSION = "1.3.185"

// 【注意】一般正式发布版本，要求3端一致；除非临时打个测试版本，否则不要改这几个
val KUIKLY_VERSION_ANDROID = KUIKLY_VERSION
val KUIKLY_VERSION_IOS = KUIKLY_VERSION
val KUIKLY_VERSION_OHOS = KUIKLY_VERSION

val MARKDOWN_VERSION = "1.0.2"

// KGP、AGP、Gradle版本兼容：https://kotlinlang.org/docs/gradle-configure-project.html#apply-the-plugin
val versions = hashMapOf(
    // android 产物编译环境
    BUILD_TYPE_ANDROID to mutableMapOf(
        KOTLINX_COROUTINES to "1.6.0",
        AGP to "8.9.1",
        KOTLIN to "2.1.21",
        KSP to "2.1.21-2.0.1",
        SERIALIZATION to "1.8.1",
        COMPOSE to "1.7.3",
        COMPOSE_PLUGIN to "2.1.21",
        KUIKLY_COMPOSE to "${KUIKLY_VERSION_ANDROID}-compose-beta-2.1.21",
        KUIKLY to "${KUIKLY_VERSION_ANDROID}-compose-beta-2.1.21",
        KUIKLY_SHORT to "${KUIKLY_VERSION_ANDROID}-compose-beta-2.1.21",
        KOTLIN_PLUGIN to "${KUIKLY_VERSION_ANDROID}-compose-beta-2.1.21",
        MARKDOWN_RENDER to MARKDOWN_VERSION,
        KTOR to "3.1.3",
        KOTLINX_DATETIME to "0.6.0-RC.2"
    ),

    // 鸿蒙端编译环境（与 QnCore/version.compat.gradle.kts 保持基线对齐）
    // 【注意】以下各版本中的 `mini-xxx` 后缀来自不同产物线的独立迭代补丁号，并非 Kotlin 语义版本：
    //   - KOTLIN(mini-112)        : Kotlin/Native 编译器分支补丁
    //   - COMPOSE_PLUGIN(mini-040): JetBrains Compose KCP 插件补丁
    //   - KUIKLY*(mini-009)       : Kuikly compose beta 迭代号
    //   - KOTLIN_PLUGIN(mini-040) : Kuikly KCP 编译器插件补丁
    // 三者发布节奏不同，强行对齐版本号会导致内部 Maven 仓库 404（产物根本不存在）。
    // 升级前请先与 Kuikly/KMP 团队确认新的兼容矩阵，不要仅因 mini 号看起来不一致而自行修改。
    BUILD_TYPE_OHOS to mutableMapOf(
        KOTLINX_COROUTINES to "1.8.4-kno",
        AGP to "8.1.0",
        KOTLIN to "2.0.21-mini-112",
        KSP to "2.0.21-1.0.27",
        SERIALIZATION to "1.6.6-dev",
        COMPOSE to "1.7.3",
        COMPOSE_PLUGIN to "2.0.21-mini-040",
        KUIKLY_COMPOSE to "${KUIKLY_VERSION_OHOS}-compose-beta-2.0.21-mini-009",
        KUIKLY to "${KUIKLY_VERSION_OHOS}-compose-beta-2.0.21-mini-009",
        KUIKLY_SHORT to "${KUIKLY_VERSION_OHOS}-compose-beta",
        KOTLIN_PLUGIN to "${KUIKLY_VERSION_OHOS}-compose-beta-2.0.21-mini-040",
        MARKDOWN_RENDER to "${MARKDOWN_VERSION}-ohos",
        KTOR to "3.0.3.8",
        KOTLINX_DATETIME to "0.6.0-RC.6-SNAPSHOT"
    ),

    // iOS 产物编译环境
    BUILD_TYPE_IOS to mutableMapOf(
        KOTLINX_COROUTINES to "1.6.0",
        AGP to "8.1.0",
        KOTLIN to "2.1.21",
        KSP to "2.1.21-2.0.1",
        SERIALIZATION to "1.6.6-dev",
        COMPOSE to "1.7.3",
        COMPOSE_PLUGIN to "2.1.21",
        KUIKLY_COMPOSE to "${KUIKLY_VERSION_IOS}-compose-beta-2.1.21",
        KUIKLY to "${KUIKLY_VERSION_IOS}-compose-beta-2.1.21",
        KUIKLY_SHORT to "${KUIKLY_VERSION_IOS}-compose-beta-2.1.21",
        KOTLIN_PLUGIN to "1.3.22-compose-beta-2.0.21-SNAPSHOT",
        MARKDOWN_RENDER to MARKDOWN_VERSION,
        KTOR to "3.1.3",
        KOTLINX_DATETIME to "0.6.0-RC.2"
    )
)

fun androidCompositeBuild(): Boolean {
    return System.getProperty("qnCommon.composite.build") == "true"
}

fun getLocalPropsFile(): File {
    val localFile = File(rootDir, "local.properties")
    if (localFile.exists()) {
        return localFile
    }
    return File(rootDir.parentFile, "local.properties")
}

fun localProps(key: String): String {
    val localPropFile = getLocalPropsFile()
    if (!localPropFile.exists()) {
        return ""
    }
    val props = Properties().also { it.load(localPropFile.inputStream()) }
    return props.getProperty(key)?.toString() ?: ""
}

fun getQnCompatBuildType(): String {

    // iOS本地XCode编译时的环境变量
    val xCodeFamilyName = System.getenv("PLATFORM_FAMILY_NAME")
    if (xCodeFamilyName == "iOS") {
        println("> QnCompatBuildType1: $BUILD_TYPE_IOS")
        return BUILD_TYPE_IOS
    }

    // 脚本编译
    var result = System.getenv("QN_COMPAT_BUILD_TYPE")?.takeIf { it.isNotBlank() }

    if (!result.isNullOrEmpty()) {
        println("> QnCompatBuildType: $result")
        return result
    }

    // Android和宿主组合编译
    if (result.isNullOrEmpty() && androidCompositeBuild()) {
        result = BUILD_TYPE_ANDROID
    }

    // 用户自定义
    val platform =
        extra.properties.getOrDefault("qqnews.kmm.build.platform", "").toString().ifEmpty {
            localProps("qqnews.kmm.build.platform")
        }
    if (result.isNullOrEmpty() && !platform.isNullOrEmpty()) {
        println("> QnCompatBuildType: $platform")
        return platform
    }


    // 默认安卓
    if (result.isNullOrEmpty()) {
        result = BUILD_TYPE_ANDROID
    }

    println("> QnCompatBuildType: $result，${gradle.gradleVersion}")

    return result!!
}

fun versions(): MutableMap<String, String> {
    val buildType = getQnCompatBuildType()
    return versions[buildType]!!
}

fun modifyHarComposeVersion() {
    val kuiklyShortVersion = versions().get(KUIKLY_SHORT)
    val ohPackageFile = File(rootDir, "ohosApp/shared/oh-package.json5")
    if (ohPackageFile.exists()) {
        val newContent = ohPackageFile.readLines().map {
            if (it.contains("@kuikly/render") && !it.contains(kuiklyShortVersion.toString())) {
                val versionCodeIndex = kuiklyShortVersion?.indexOf("-compose-beta") ?: 0
                val versionCode = kuiklyShortVersion?.subSequence(0, versionCodeIndex)
                """    "@kuikly/render": '${versionCode}-compose-beta',"""
            } else {
                it
            }
        }

        ohPackageFile.writeText(newContent.joinToString("\n"))
    }
}

fun modifyIosPodKuiklyVersion() {
    val kuiklyShortVersion = versions().get(KUIKLY_SHORT)
    val podfile = File(rootDir, "iosApp/Podfile")
    if (podfile.exists()) {
        val newContent = podfile.readLines().map {
            if (it.contains("pod 'KuiklyIOSRender'") && !it.contains(kuiklyShortVersion.toString())) {
                val versionCodeIndex = kuiklyShortVersion?.indexOf("-compose-beta") ?: 0
                val versionCode = kuiklyShortVersion?.subSequence(0, versionCodeIndex)
                "  pod 'KuiklyIOSRender', '${versionCode}-compose-beta'"
            } else {
                it
            }
        }

        podfile.writeText(newContent.joinToString("\n"))
    }
}

extra.let {
    val buildType = getQnCompatBuildType()
//    if (buildType == BUILD_TYPE_OHOS) {
    modifyHarComposeVersion()
    modifyIosPodKuiklyVersion()
//    }

    it.set("versions", versions())
    it.set(
        "qqnews.build.env", mutableMapOf(
            "build.platform" to buildType
        )
    )
}
