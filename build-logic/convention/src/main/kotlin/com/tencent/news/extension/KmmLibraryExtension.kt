package com.tencent.news.extension

import com.android.build.api.dsl.LibraryExtension
import com.tencent.news.build.logic.findPropertyAnyWhere
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

internal val Project.qnModuleName get() = name

internal fun Project.setupAndroid(extension: LibraryExtension) = extension.apply {
    namespace = "com.tencent.news.core.${qnModuleName}"
    compileSdk = libs.versions.androidCompileSdk.get().toInt()
    defaultConfig {
        minSdk = libs.versions.androidMinSdk.get().toInt()
        targetSdk = libs.versions.androidTargetSdk.get().toInt()
    }
    buildToolsVersion = findPropertyAnyWhere("android.buildToolsVersion")
        ?: libs.versions.androidBuildToolsVersion.get()

    sourceSets {
        named("main") {
            jniLibs.srcDirs("src/androidMain/libs/")
            assets.srcDirs("src/commonMain/composeResources")
        }
    }
}

internal fun KotlinMultiplatformExtension.setupKmmAndroid() {
    androidTarget {
        if (System.getenv("QN_PUBLISH_RELEASE_ONLY") == "true") {
            publishLibraryVariants("release")
        } else {
            publishLibraryVariants("release", "debug")
        }
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_1_8)
        }
    }
}

fun KotlinMultiplatformExtension.setupKmmOhos(config: KotlinNativeTarget.() -> Unit) {
    val clz = KotlinMultiplatformExtension::class.java
    clz.getDeclaredMethod("ohosArm64", String::class.java, kotlin.jvm.functions.Function1::class.java).let {
        it.isAccessible = true
        it.invoke(this, "ohosArm64", config)
    }
}
