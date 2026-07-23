import com.android.build.api.dsl.LibraryExtension
import com.tencent.news.build.logic.LocalModules.tryAddDependency4Library
import com.tencent.news.build.logic.findPropertyAnyWhere
import com.tencent.news.detekt.applyDetekt
import com.tencent.news.extension.addDependencies
import com.tencent.news.extension.cocoapodsOrNull
import com.tencent.news.extension.libs
import com.tencent.news.extension.qnModuleName
import com.tencent.news.extension.setupAndroid
import com.tencent.news.extension.setupKmmAndroid
import com.tencent.news.extension.setupKmmOhos
import com.tencent.news.utils.runIfNotOhosBuild
import com.tencent.news.utils.runIfOhosBuild
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.getValue
import org.gradle.kotlin.dsl.getting
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

/**
 * 通用KMM模块初始化插件
 */
open class QNKmmLibraryPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        with(target) {
            applyPlugins()
            extensions.configure<LibraryExtension> { setupAndroid(this) }
            extensions.configure<KotlinMultiplatformExtension> { setupKmm(this) }
            applyDetekt()
        }
    }

    open fun Project.applyPlugins() = with(pluginManager) {
        apply(libs.plugins.androidLibrary.get().pluginId)
        apply(libs.plugins.kotlinMultiplatform.get().pluginId)
        apply(libs.plugins.kotlinSerialization.get().pluginId)
        runIfNotOhosBuild {
            if (project.qnModuleName == "wsPlayer") {
                apply(libs.plugins.kotlinCocoapods.get().pluginId)
            }
        }
    }

    private fun Project.setupKmm(extension: KotlinMultiplatformExtension) = with(extension) {
        runIfNotOhosBuild {
            setupKmmAndroid()
            setupKmmIosFramework(this@setupKmm)
        }

        setupKmmSourceSets4LocalBuild()

        runIfOhosBuild {
            setupKmmOhos(this)
        }

        // 监听 cocoapods 插件的应用（可能由子模块 build.gradle.kts 在 plugins 块中声明）
        // 使用 withPlugin 确保无论插件应用顺序如何都能正确配置
        project.pluginManager.withPlugin("org.jetbrains.kotlin.native.cocoapods") {
            setupCocoapodsForLibrary(project, extension)
        }
    }

    /**
     * 当子模块应用了 cocoapods 插件时，配置 cocoapods 并声明 pod 依赖。
     * 通过 [addDependencies] 复用主模块统一声明的 pod 版本，保证版本一致。
     */
    private fun setupCocoapodsForLibrary(
        project: Project,
        extension: KotlinMultiplatformExtension
    ) {
        val enableDynamicFrameworkLink =
            project.findPropertyAnyWhere("qn.ios.enableDynamicFrameworkLink")?.toString()
                ?.toBoolean() ?: false

        extension.cocoapodsOrNull?.apply {
            summary = "${project.qnModuleName} Module"
            homepage = "."
            version = "1.0"
            ios.deploymentTarget = "14.0"

            // 复用主模块统一声明的 pod 依赖，保证版本一致
            addDependencies()

            framework {
                baseName = project.qnModuleName
                isStatic = !enableDynamicFrameworkLink
            }
        }
    }

    private fun KotlinMultiplatformExtension.setupKmmIosFramework(project: Project) {
        // 根据 Xcode/CocoaPods 传入的平台和架构决定编译哪些 iOS target。
        val iosTargets = requestedIosTargets(project)

        // 读取动态库配置
        val enableDynamicFrameworkLink =
            project.findPropertyAnyWhere("qn.ios.enableDynamicFrameworkLink")?.toString()
                ?.toBoolean() ?: false

        // 如果 cocoapods 插件尚未应用，使用常规方式配置 framework
        // （如果 cocoapods 插件已应用或稍后应用，setupCocoapodsForLibrary 会通过 withPlugin 回调配置）
        if (cocoapodsOrNull == null) {
            iosTargets.forEach { target ->
                target.binaries.framework {
                    baseName = project.qnModuleName
                    isStatic = !enableDynamicFrameworkLink
                }
            }
        }

        // 配置 iOS 编译器标志
        iosTargets.forEach { target ->
            val disableOptimization =
                project.findPropertyAnyWhere("qn.ios.disableOptimization")?.toString()?.toBoolean()
                    ?: false

            if (disableOptimization) {
                target.binaries.all {
                    optimized = false
                }
            }
        }
    }

    private fun KotlinMultiplatformExtension.requestedIosTargets(project: Project): List<KotlinNativeTarget> {
        val platform = project.findPropertyAnyWhere("kotlin.native.cocoapods.platform")
        val archs = project.findPropertyAnyWhere("kotlin.native.cocoapods.archs")
            ?.split(' ', ',')
            ?.filter { it.isNotBlank() }
            .orEmpty()
        val arm64Only = project.findPropertyAnyWhere("qn.ios.arm64Only")?.toBoolean() ?: false
        val x86Only = project.findPropertyAnyWhere("qn.ios.x86Only")?.toBoolean() ?: false

        return when {
            platform == "iphonesimulator" && "arm64" in archs && "x86_64" in archs ->
                listOf(iosSimulatorArm64(), iosX64())
            platform == "iphonesimulator" && "arm64" in archs -> listOf(iosSimulatorArm64())
            platform == "iphonesimulator" && "x86_64" in archs -> listOf(iosX64())
            arm64Only -> listOf(iosArm64())
            x86Only -> listOf(iosX64())
            else -> listOf(iosArm64(), iosSimulatorArm64(), iosX64())
        }
    }

    private fun Project.setupKmmOhos(extension: KotlinMultiplatformExtension) = with(extension) {
        val ohosSourceSet = sourceSets
        setupKmmOhos {
        }
    }

    /**
     * 本地debug编译模式，添加对L1层基础模块的依赖（qnPlatform -> qnFramework -> wsCore）
     */
    private fun KotlinMultiplatformExtension.setupKmmSourceSets4LocalBuild() {
        sourceSets.apply {
            val commonMain by getting {
                dependencies {
                    project.tryAddDependency4Library(this)
                    implementation(project.libs.kotlinx.coroutines.core)
                    implementation(project.libs.kotlinx.collections.immutable)
                    // 不依赖 compose 的模块，仍需 androidx.annotation（如 @CallSuper 等注解）
                    // androidx.annotation 是 Android 专属依赖，鸿蒙平台无法解析
                    project.runIfNotOhosBuild {
                        implementation(project.libs.androidx.annotation)
                    }
                }
            }

            val commonTest by getting {
                dependencies {
                    implementation(project.libs.kotlin.test)
                }
            }

            all {
                languageSettings.optIn("kotlin.js.ExperimentalJsExport")
                languageSettings.optIn("kotlin.experimental.ExperimentalNativeApi")
                languageSettings.optIn("kotlinx.cinterop.ExperimentalForeignApi")
            }
        }
    }
}
