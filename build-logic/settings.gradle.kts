@file:Suppress("UnstableApiUsage")

pluginManagement {
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
        maven("https://mirrors.tencent.com/repository/maven/kuikly")
        maven("https://mirrors.tencent.com/repository/maven/kuikly-snapshot")
        maven("https://mirrors.tencent.com/repository/maven/playable-ad")
        maven("https://mirrors.tencent.com/repository/maven/TencentAdSdk/")
        maven("https://mirrors.tencent.com/repository/maven/tab_sdk")

        mavenLocal()
        mavenCentral()
        gradlePluginPortal()
        google()
    }
}


apply(from = "../version.compat.gradle.kts")

dependencyResolutionManagement {
    versionCatalogs {
        val libs = create("libs") {
            from(files("../gradle/libs.versions.toml"))
        }

        (extra.get("versions") as Map<String, String>).forEach {
            libs.version(it.key, it.value)
        }
    }
    repositories.addAll(pluginManagement.repositories)
}

include("convention")
