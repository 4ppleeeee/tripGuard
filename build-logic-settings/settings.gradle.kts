@file:Suppress("UnstableApiUsage")

pluginManagement {
    repositories {
        maven("https://mirrors.tencent.com/nexus/repository/gradle-plugins")
        maven("https://mirrors.tencent.com/nexus/repository/maven-public/")

        mavenLocal()
        mavenCentral()
        gradlePluginPortal()
        google()
    }
}


dependencyResolutionManagement {
    repositories.addAll(pluginManagement.repositories)

    versionCatalogs {
        create("libs") {
            from(files("../gradle/libs.versions.toml"))
        }
    }
}
