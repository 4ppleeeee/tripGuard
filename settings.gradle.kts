@file:Suppress("UnstableApiUsage")

pluginManagement {
    includeBuild("build-logic")
    includeBuild("build-logic-settings")

    repositories {
        maven("https://mirrors.tencent.com/repository/maven/tencentvideo")
        maven("https://mirrors.tencent.com/repository/maven/tencentvideo-snapshot")
        maven("https://mirrors.tencent.com/repository/maven/sogou_maven_snapshots")
        maven("https://mirrors.tencent.com/repository/maven/tmm-snapshot")
        maven("https://mirrors.tencent.com/nexus/repository/maven-public")
        maven("https://mirrors.tencent.com/repository/maven/thirdparty")
        maven("https://mirrors.tencent.com/nexus/repository/gradle-plugins")
        maven("https://mirrors.tencent.com/repository/maven/tencent_public")
        maven("https://mirrors.tencent.com/repository/maven/tencent_public_snapshots")
        maven("https://mirrors.tencent.com/repository/maven/ABTest-AndroidSDK")
        maven("https://mirrors.tencent.com/repository/maven/tab_sdk")
        maven("https://mirrors.tencent.com/repository/maven/kuikly")
        maven("https://mirrors.tencent.com/repository/maven/kuikly-snapshot")
        maven("https://mirrors.tencent.com/nexus/repository/maven-tencent/")
        maven("https://mirrors.tencent.com/repository/maven/TencentAdSdk") // 腾讯广告SDK
        maven("https://mirrors.tencent.com/repository/maven/playable-ad")
        maven("https://mirrors.tencent.com/repository/maven/tab_sdk")

        gradlePluginPortal()
        mavenCentral()
        mavenLocal()
        google()
    }

    // 鸿蒙端使用定制版 Wire Compiler
    val isOhos = System.getenv("QN_COMPAT_BUILD_TYPE") == "ohos"
            || providers.gradleProperty("qqnews.kmm.build.platform").orNull == "ohos"
    if (isOhos) {
        resolutionStrategy {
            eachPlugin {
                if (requested.id.id == "com.squareup.wire") {
                    useModule("com.squareup.wire.kmm:wire-gradle-plugin:4.6.10.2")
                }
            }
        }
    }
}

plugins {
    id("settings-convention")
    id("tn-enterprise")
}

apply { from("version.compat.gradle.kts") }

dependencyResolutionManagement {
    repositories.addAll(pluginManagement.repositories)

    versionCatalogs {
        create("libs") {
            @Suppress("UNCHECKED_CAST")
            (extra.get("versions") as Map<String, String>).forEach { (key, value) ->
                version(key, value)
            }
        }
    }
}

rootProject.name = "TravelMvpDemo"

include(":androidApp")

val buildEnv = extra.properties["qqnews.build.env"] as MutableMap<String, Any>
gradle.allprojects {
    buildEnv.forEach { (key, value) -> extra.set(key, value) }
}

println("build file ==== ${buildEnv["build.platform"]}, ${rootProject.buildFileName}")
println("编译命令：${gradle.startParameter}")
