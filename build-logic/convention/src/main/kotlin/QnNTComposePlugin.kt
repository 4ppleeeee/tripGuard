import com.google.devtools.ksp.gradle.KspExtension
import com.tencent.news.build.logic.LocalModules
import com.tencent.news.build.logic.composeModuleProperties
import com.tencent.news.build.logic.LocalModules.enableFatKmmPublication
import com.tencent.news.build.logic.findPropertyAnyWhere
import com.tencent.news.extension.isMainProject
import com.tencent.news.extension.libs
import com.tencent.news.extension.qnModuleName
import com.tencent.news.utils.runIfNotIosBuild
import com.tencent.news.utils.runIfNotOhosBuild
import com.tencent.news.utils.runIfOhosBuild
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.exclude
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import java.io.File

class QnNTComposePlugin : Plugin<Project> {

    override fun apply(project: Project) {
        with(project) {
            applyPlugins()
            setupKsp()
            setupKnio()
            extensions.configure<KotlinMultiplatformExtension> { setupKmm(this) }
        }

        project.afterEvaluate {
            project.copyIOSResources()
        }
    }

    private fun Project.applyPlugins() {
        with(pluginManager) {
            apply(libs.plugins.kotlinMultiplatform.get().pluginId)
            apply(libs.plugins.composeMultiplatform.get().pluginId)
            // Kotlin 2.0.0-RC2 起，使用 Compose Multiplatform 必须 apply compose compiler 插件
            apply(libs.plugins.composePlugin.get().pluginId)
            apply(libs.plugins.ksp.get().pluginId)

            runIfNotIosBuild {
                apply(libs.plugins.kuikly.get().pluginId)
            }

            runIfOhosBuild {
                apply(libs.plugins.ohos.knoi.get().pluginId)
            }
        }
    }

    private fun Project.setupKmm(extension: KotlinMultiplatformExtension) = extension.apply {
        setupComposeSourceSet(project)
    }

    private fun KotlinMultiplatformExtension.setupComposeSourceSet(project: Project) =
        with(project) {

            sourceSets.apply {

                runIfNotOhosBuild {
                    androidMain.get().dependencies {
                        implementation(libs.kuikly.core.render.android)
                        implementation(libs.androidx.dynamicanimation)
                        implementation(libs.androidx.appcompat)
                    }
                }

                commonMain.get().dependencies {
                    implementation(libs.kuikly.compose)
                    implementation(libs.kuikly.core.annotation)
                    implementation(libs.kotlinx.coroutines.core)
                    // implementation(libs.qqnews.markdown.render)

                    val markdownDep =
                        libs.jetbrains.markdown.get().let { "${it.module}:${it.version}" }
                    implementation(markdownDep) {
                        exclude(group = "org.jetbrains.kotlin", module = "kotlin-stdlib")
                    }
                }
            }
        }

    private fun Project.setupKnio() = runIfOhosBuild {
        val clz = Class.forName("com.tencent.tmm.knoi.KnoiExtension")
        extensions.configure(clz) {
            val tsGenDir = clz.getDeclaredField("tsGenDir").also {
                it.isAccessible = true
            }
            // 每个模块输出到各自的子目录，避免多模块之间覆盖
            tsGenDir.set(this, "${rootProject.rootDir}/ohosApp/shared/src/main/ets/kniogen/${project.name}")
        }

        // 将 knoi 配置文件注册为 KSP task 的 input，确保配置内容变化时 KSP 会重新运行
        // 解决增量编译时 KSP 跳过执行导致 .d.ts/.ets 产物不生成的问题
        afterEvaluate {
            val knoiConfigFile = File(rootProject.rootDir, ".gradle/knoi/${project.name}-config.ini")
            val knoiOutputDir = File(rootProject.rootDir, "ohosApp/shared/src/main/ets/kniogen/${project.name}")
            tasks.findByName("kspKotlinOhosArm64")?.let { kspTask ->
                kspTask.inputs.file(knoiConfigFile).optional()
                // 如果 knoi 产物目录不存在或为空（比如被清理或首次构建），强制 KSP 重新运行
                kspTask.outputs.upToDateWhen {
                    knoiOutputDir.exists() && (knoiOutputDir.listFiles()?.isNotEmpty() == true)
                }
            }
        }
    }

    private fun Project.setupKsp() {
        extensions.configure<KspExtension> {
            // Seed fat KLIB is consumed by another app's final framework. It must not
            // generate Kuikly's process-wide entry symbols, which belong to that app.
            val generateApplicationEntry = isMainProject && !enableFatKmmPublication()
            arg("moduleId", project.qnModuleName)          // 标识模块Id
            arg("isMainModule", "$generateApplicationEntry") // 是否生成应用主模块入口
            arg("enableMultiModule", "true")            // 启用多模块
            arg("caughtException", "false")             // 关闭kuikly异常捕获
            if (generateApplicationEntry) {
                // 只包含有 @Page 注解的模块
                val subModules = composeModuleProperties().keys.joinToString("&")
                arg("subModules", subModules)
            }
        }

        dependencies {
            if (enableFatKmmPublication()) {
                // The consuming app owns KuiklyCoreEntry. Publishing it from Seed would
                // duplicate process-wide native callback symbols during final linking.
                return@dependencies
            }

            val kuiklyVersion = libs.versions.kuikly.asProvider().get()
            add("compileOnly", "com.tencent.kuikly:core-ksp:${kuiklyVersion}")?.apply {
                runIfNotOhosBuild {
                    // 根据参数决定配置哪些iOS架构的KSP
                    val platform = project.findPropertyAnyWhere("kotlin.native.cocoapods.platform")
                    val archs = project.findPropertyAnyWhere("kotlin.native.cocoapods.archs")
                        ?.split(' ', ',')
                        ?.filter { it.isNotBlank() }
                        .orEmpty()
                    val x86Only = project.findPropertyAnyWhere("qn.ios.x86Only")?.toBoolean() ?: false

                    when {
                        platform == "iphonesimulator" && "x86_64" in archs -> add("kspIosX64", this)
                        platform == "iphonesimulator" && "arm64" in archs -> add("kspIosSimulatorArm64", this)
                        x86Only -> add("kspIosX64", this)
                        else -> add("kspIosArm64", this)
                    }

                    add("kspAndroid", this)
                }
                runIfOhosBuild {
                    add("kspOhosArm64", this)
                }
            }
        }
    }

    private fun Project.copyIOSResources() {
        if (!isMainProject) return

        val src = File(rootProject.rootDir, "wsCompose/src/commonMain/composeResources/")
        if (!src.exists()) return

        val moduleName = qnModuleName

        // 1. 拷贝到 podPublish 路径（用于远程发布 XCFramework）
        val publishDest = File(
            rootProject.rootDir,
            "${moduleName}/build/cocoapods/publish/release/build/compose/ios/${moduleName}/compose-resources/"
        )
        src.copyRecursively(publishDest, overwrite = true)

        // 2. 拷贝到 cocoapods compose-resources 路径（用于本地 xcodebuild + syncFramework 构建）
        // podspec 中 spec.resources 声明的路径为：
        //   build/compose/cocoapods/compose-resources/composeResources/weseecore.wscompose.generated.resources/*
        // syncFramework 不会自动触发 Compose 资源同步，需要手动保证该目录有内容
        val cocoapodsDest = File(
            rootProject.rootDir,
            "${moduleName}/build/compose/cocoapods/compose-resources/composeResources/weseecore.wscompose.generated.resources/"
        )
        if (!cocoapodsDest.exists()) {
            cocoapodsDest.mkdirs()
        }
        src.listFiles()?.forEach { file ->
            val target = File(cocoapodsDest, file.name)
            if (file.isDirectory) {
                file.copyRecursively(target, overwrite = true)
            } else {
                file.copyTo(target, overwrite = true)
            }
        }
    }
}
