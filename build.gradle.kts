plugins {
    alias(libs.plugins.androidLibrary).apply(false)
    alias(libs.plugins.androidApplication).apply(false)
    alias(libs.plugins.kotlinAndroid).apply(false)
    alias(libs.plugins.kotlinCocoapods).apply(false)
    alias(libs.plugins.kotlinMultiplatform).apply(false)
    alias(libs.plugins.kotlinSerialization).apply(false)
    alias(libs.plugins.composeMultiplatform).apply(false)
    alias(libs.plugins.ksp).apply(false)
    alias(libs.plugins.composePlugin).apply(false)
    alias(libs.plugins.kuikly).apply(false)
    alias(libs.plugins.wire).apply(false)
}

val legacyAndroidMmkvVersion = "1.0.22"

allprojects {
    repositories.addAll(rootProject.buildscript.repositories)
    buildscript.repositories.addAll(repositories)
}

allprojects {
    configurations.configureEach {
        resolutionStrategy.cacheChangingModulesFor(0, "seconds")
        resolutionStrategy.eachDependency {
            if (requested.group == "com.tencent" && requested.name == "mmkv") {
                useVersion(legacyAndroidMmkvVersion)
                because("Align Android MMKV runtime with legacy KMM login storage.")
            }
        }
    }
}

buildscript {
    configurations.all {
        resolutionStrategy.cacheChangingModulesFor(0, "seconds")
    }
}
