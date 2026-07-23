package com.tencent.news.build.logic

import com.android.build.api.dsl.AndroidSourceSet
import com.tencent.news.build.logic.LocalModules.enableFatAar
import com.tencent.news.extension.libs
import com.tencent.news.extension.qnModuleName
import org.gradle.api.Project
import org.gradle.kotlin.dsl.extra
import org.jetbrains.kotlin.gradle.plugin.KotlinDependencyHandler
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.jetbrains.kotlin.gradle.plugin.mpp.AbstractNativeLibrary
import org.jetbrains.kotlin.konan.properties.hasProperty
import java.io.File
import java.io.FileReader
import java.util.Properties

/**
 * 本地模块结构依赖关系处理脚本
 */
object LocalModules {

    fun Project.exportFeatureModule(framework: AbstractNativeLibrary) = with(framework) {
        val exportProp = moduleExportProperties()
        forEachLocalModule { module ->
            val exportValue = exportProp.getProperty(module, "")
            if (exportValue == "false" || exportValue == "false-ios") {
                println("$module is not exported")
                return@forEachLocalModule
            }
            export(project(":${module}"))
        }
    }

    /**
     * 非本地编译时，将所有模块的src合并到**qnCommon**模块
     */
    fun Project.tryMergeLocalSourceSets(sourceSet: String, set: KotlinSourceSet) = with(set) {
        if (enableFatKmmPublication()) {
            getAllFeatureProject().map { it.qnModuleName }.forEach { module ->
                if (module == "qnDebug" || module == "wsDebug") {
                    return@forEach
                }
                kotlin.srcDir("../${module}/src/${sourceSet}/kotlin")
            }
        }
    }

    /**
     * 非本地编译时，将所有模块的assets合并到**qnCommon**模块
     */
    fun Project.tryMergeLocalAssets(set: AndroidSourceSet) = with(set) {
        if (enableFatAar()) {
            forEachLocalModule { module ->
                if (module == "qnDebug" || module == "wsDebug") {
                    return@forEachLocalModule
                }
                jniLibs.srcDirs("../${module}/src/androidMain/libs/")
                assets.srcDirs("../${module}/src/androidMain/res/")
                assets.srcDirs("../${module}/src/commonMain/composeResources")
            }
        }
    }

    fun Project.tryAddDependency4Library(handler: KotlinDependencyHandler) = with(handler) {
        val projectName = project.name
        if (!enableFatAar()) {
            api(project.libs.kotlin.serialization)
            if (project.useExternalKmmCore()) {
                return@with
            }
            when (projectName) {
                "kmmStartupCore" -> { /* 启动核心模块，无模块依赖 */ }
                "qnPlatform" -> { /* 最底层模块，无模块依赖 */ }
                "kmmStartupStd" -> { /* 底座模块保持独立，不自动依赖 qnFramework */ }
                "qnFramework" -> api(project(":qnPlatform"))
                "qnCore" -> api(project(":qnFramework"))
                "qnView" -> api(project(":qnFramework"))
                else -> api(project(":qnFramework"))
            }
        }
    }

    /**
     * 本地编译时，让所有业务模块依赖**qnCommon**模块
     */
    fun Project.tryAddLocalProjectDependency4Main(handler: KotlinDependencyHandler) =
        with(handler) {
            val exportProp = moduleExportProperties()
            if (!enableFatKmmPublication()) {
                forEachLocalModule { module ->
                    val exportValue = exportProp.getProperty(module, "")

                    val forbidExport = exportValue == "false"
                    val forbid4Android = androidCompositeBuild() && exportValue == "false-android"

                    if (forbidExport || forbid4Android) {
                        implementation(project(":${module}"))
                    } else {
                        api(project(":${module}"))
                    }
                }
            }
        }

    fun Project.tryAddFatAarCommonDependencies(handler: KotlinDependencyHandler) =
        with(handler) {
            if (enableFatKmmPublication()) {
                implementation(project.libs.ktor.client.core)
                implementation(project.libs.wire.runtime)
                implementation(project.libs.mmkvkotlin)
            }
        }

    fun Project.tryAddFatKmpIosDependencies(handler: KotlinDependencyHandler) = with(handler) {
        if (enableFatKmmPublication()) {
            implementation(project.libs.ktor.client.darwin)
        }
    }

    fun Project.tryAddFatAarAndroidDependencies(handler: KotlinDependencyHandler) =
        with(handler) {
            if (enableFatAar()) {
                implementation(project.libs.ktor.client.okhttp)
                implementation(project.libs.tencent.mmkv)
                implementation("com.tencent.news:interesting_lottie:0.0.51")
            }
        }

    private fun Project.forEachLocalModule(action: (String) -> Unit) {
        getAllFeatureProject().forEach { action(it.qnModuleName) }
    }

    internal fun enableFatAar(): Boolean {
        return System.getenv("QN_COMPAT_BUILD_TYPE") == "android"
    }

    internal fun Project.enableFatKmmPublication(): Boolean {
        return enableFatAar() ||
            findPropertyAnyWhere("kmm.seed.fat")?.toString()?.toBoolean() == true
    }

    private fun Project.useExternalKmmCore(): Boolean {
        return findPropertyAnyWhere("kmmCore.external")?.toBoolean() == true ||
            System.getenv("KMM_CORE_EXTERNAL") == "true"
    }

    fun Project.getAllFeatureProject(): List<Project> {
        val projects = mutableListOf<Project>()
        moduleProperties().forEach { name, path ->
            val projectPath = path.toString()
            val module = projectPath.ifEmpty { "$name" }
            projects.add(project(":$module"))
        }
        return projects
    }

}

fun Project.loadProperties(fileObj: Any): Properties {
    val props = Properties()
    val file: File = when (fileObj) {
        is File -> fileObj
        else -> File(rootDir, fileObj.toString())
    }
    if (file.exists()) {
        props.load(FileReader(file))
    }
    return props
}

fun enableLocalModel(): Boolean {
    return !enableFatAar()
}

fun Project.moduleProperties(): Properties {
    val projectModulesFile = "modules-${project.name}.properties"
    return if (File(rootDir, projectModulesFile).exists()) {
        loadProperties(projectModulesFile)
    } else {
        loadProperties("modules.properties")
    }
}

fun Project.composeModuleProperties(): Properties {
    return loadProperties("modules.compose.properties")
}

fun Project.moduleExportProperties(): Properties {
    return loadProperties("modules-export.properties")
}

fun Project.localProps(): Properties {
    return loadProperties("local.properties")
}

fun Project.findPropertyAnyWhere(key: String): String? {
    return when {
        extra.has(key) -> extra.get(key).toString()
        localProps().hasProperty(key) -> localProps().getProperty(key)
        project.hasProperty(key) -> project.properties[key].toString()
        else -> null
    }
}

fun androidCompositeBuild(): Boolean {
    return System.getProperty("qnCommon.composite.build") == "true"
}
