import com.android.build.api.dsl.LibraryExtension
import com.tencent.news.build.logic.LocalModules
import com.tencent.news.build.logic.LocalModules.exportFeatureModule
import com.tencent.news.build.logic.LocalModules.enableFatKmmPublication
import com.tencent.news.build.logic.LocalModules.tryAddFatAarAndroidDependencies
import com.tencent.news.build.logic.LocalModules.tryAddFatAarCommonDependencies
import com.tencent.news.build.logic.LocalModules.tryAddFatKmpIosDependencies
import com.tencent.news.build.logic.LocalModules.tryAddLocalProjectDependency4Main
import com.tencent.news.build.logic.LocalModules.tryMergeLocalAssets
import com.tencent.news.build.logic.LocalModules.tryMergeLocalSourceSets
import com.tencent.news.build.logic.findPropertyAnyWhere
import com.tencent.news.detekt.applyDetekt
import com.tencent.news.extension.addDependencies
import com.tencent.news.extension.cocoapods
import com.tencent.news.extension.libs
import com.tencent.news.extension.qnModuleName
import com.tencent.news.extension.setupAndroid
import com.tencent.news.extension.setupKmmAndroid
import com.tencent.news.extension.setupKmmOhos
import com.tencent.news.utils.isAndroidBuild
import com.tencent.news.utils.isIOSBuild
import com.tencent.news.utils.isOhosBuild
import com.tencent.news.utils.runIfNotOhosBuild
import com.tencent.news.utils.runIfOhosBuild
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.getValue
import org.gradle.kotlin.dsl.getting
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.cocoapods.CocoapodsExtension
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.NativeBuildType

/**
 * Kmm主模块初始化插件
 */
class QNKmmMainPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        with(target) {
            setupProject()
            applyPlugins()
            extensions.configure<LibraryExtension> { setupAndroid(this) }
            extensions.configure<KotlinMultiplatformExtension> { setupKmm(this) }
            applyDetekt()
        }
    }

    private fun Project.setupProject() {
        group = "com.tencent.news.core"
    }

    private fun Project.applyPlugins() {
        with(pluginManager) {
            apply(libs.plugins.androidLibrary.get().pluginId)
            runIfNotOhosBuild {
                apply(libs.plugins.kotlinCocoapods.get().pluginId)
            }
            apply(libs.plugins.kotlinMultiplatform.get().pluginId)
            apply(libs.plugins.kotlinSerialization.get().pluginId)
        }
    }

    private fun Project.setupKmm(extension: KotlinMultiplatformExtension) = extension.apply {
        runIfNotOhosBuild {
            // android
            setupKmmAndroid()
            // iOS
            runIfNotOhosBuild {
                // 读取动态库配置（需要在 setupCocoapods 之前读取）
                val enableDynamicFrameworkLink =
                    project.findPropertyAnyWhere("qn.ios.enableDynamicFrameworkLink")?.toString()
                        ?.toBoolean() ?: false
                setupIosTarget(extension)
                setupCocoapods(cocoapods, enableDynamicFrameworkLink)
            }
        }

        runIfOhosBuild {
            setupKmmOhos(this)
        }

        setupSourceSet4NonLocalBuild(this)
    }

    private fun Project.setupIosTarget(extension: KotlinMultiplatformExtension) = extension.apply {
        // 根据 Xcode/CocoaPods 传入的平台和架构决定编译哪些 iOS target。
        val iosTargets = requestedIosTargets(this@setupIosTarget)
        val iosMain = sourceSets.maybeCreate("iosMain")
        iosMain.dependsOn(sourceSets.getByName("commonMain"))
        iosTargets.forEach { target ->
            sourceSets.getByName("${target.name}Main").dependsOn(iosMain)
        }

        // 配置 iOS 编译器标志
        val disableOptimization = true
        val enableSizeOptimization =
            project.findPropertyAnyWhere("qn.ios.enableSizeOptimization")?.toString()?.toBoolean()
                ?: true  // 默认开启尺寸优化
        val enableDynamicFrameworkLink =
            project.findPropertyAnyWhere("qn.ios.enableDynamicFrameworkLink")?.toString()
                ?.toBoolean() ?: false  // 默认开启动态库强链接

        // 打印优化配置日志
        println("========================================")
        println("iOS Compilation Configuration:")
        println("  - platform: ${project.findPropertyAnyWhere("kotlin.native.cocoapods.platform") ?: "default"}")
        println("  - archs: ${project.findPropertyAnyWhere("kotlin.native.cocoapods.archs") ?: "default"}")
        println("  - disableOptimization: $disableOptimization")
        println("  - enableSizeOptimization: $enableSizeOptimization")
        println("  - enableDynamicFrameworkLink: $enableDynamicFrameworkLink")
        println("  - targets: ${iosTargets.map { it.name }}")
        println("========================================")

        iosTargets.forEach { target ->
            if (disableOptimization) {
                // 完全禁用优化
                target.binaries.all {
                    optimized = false  // 禁用优化，包括 LTO
                }
            }

            target.compilations.all {
                compilerOptions.configure {
                    freeCompilerArgs.addAll(
                        "-Xllvm-module-passes=default<Os>",
                        "-Xllvm-lto-passes=internalize,globaldce,lto<Os>",
                        "-Xoverride-konan-properties=clangOptFlags.ios_arm64=-Os"
                    )
                }
            }

            // 配置 KuiklyIOSRender 强链接（动态库模式）
            // 可通过 qn.ios.enableDynamicFrameworkLink=false 禁用
            if (enableDynamicFrameworkLink) {
                target.binaries.all {
                    // 智能查找 Pods 路径
                    // 1. 优先查找 KMM 项目自己的 Pods: iosApp/Pods
                    // 2. 其次查找宿主项目的 Pods: ../Pods 或 ../../Pods
                    val podsPath = when {
                        // 宿主项目：../Pods（KMM 在宿主的子目录）
                        rootProject.file("../Pods").exists() ->
                            rootProject.file("../Pods").absolutePath

                        // 宿主项目：../../Pods（KMM 在宿主的子子目录）
                        rootProject.file("../../Pods").exists() ->
                            rootProject.file("../../Pods").absolutePath

                        // KMM 项目：iosApp/Pods
                        rootProject.file("iosApp/Pods").exists() ->
                            rootProject.file("iosApp/Pods").absolutePath

                        // 默认：使用 KMM 项目路径
                        else -> rootProject.file("iosApp/Pods").absolutePath
                    }

                    val xcframeworkPath =
                        "$podsPath/KuiklyIOSRender/KuiklyIOSRender/KuiklyIOSRender.xcframework"

                    val frameworkPath = when (target.name) {
                        "iosArm64" -> "$xcframeworkPath/ios-arm64"
                        "iosSimulatorArm64", "iosX64" -> "$xcframeworkPath/ios-arm64_x86_64-simulator"
                        else -> "$xcframeworkPath/ios-arm64_x86_64-simulator"
                    }

                    // 添加搜索路径
                    linkerOpts("-F$frameworkPath")
                    // 使用强链接
                    linkerOpts("-framework", "KuiklyIOSRender")

                    // 强制加载整个 framework，避免链接器优化掉
                    val kuiklyBinary = "$frameworkPath/KuiklyIOSRender.framework/KuiklyIOSRender"
                    linkerOpts("-Wl,-force_load,$kuiklyBinary")

                    // TDFCommon 是源码 Pod，符号由宿主提供
                    linkerOpts("-U", "_OBJC_CLASS_\$_TDFBaseModule")
                    linkerOpts("-U", "_OBJC_METACLASS_\$_TDFBaseModule")
                    linkerOpts("-U", "_TDGGetModuleClass")
                }
            }
        }

        // 或者使用通用的 KotlinNativeTarget 配置 (作为备选方案)
        if (enableSizeOptimization && !disableOptimization) {
            targets.withType<org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget> {
                compilations.all {
                    compilerOptions.configure {
                        freeCompilerArgs.addAll(
                            "-Xllvm-module-passes=default<Os>",
                            "-Xllvm-lto-passes=internalize,globaldce,lto<Os>",
                            "-Xoverride-konan-properties=clangOptFlags.ios_arm64=-Os"
                        )
                    }
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

    private fun Project.setupCocoapods(
        extension: CocoapodsExtension,
        enableDynamicFrameworkLink: Boolean
    ) = extension.apply {

        xcodeConfigurationToNativeBuildType["Distribution"] = NativeBuildType.RELEASE
        xcodeConfigurationToNativeBuildType["RDM"] = NativeBuildType.RELEASE

        summary = "Some description for the Shared Module"
        homepage = "Link to the Shared Module homepage"
        version = "1.0"
        ios.deploymentTarget = "14.0"
        extraSpecAttributes["resources"] =
            "['build/compose/cocoapods/compose-resources/composeResources/weseecore.wscompose.generated.resources/*']"
        podfile = rootProject.file("./iosApp/Podfile")

        // 统一声明子模块所需的 pod 依赖（如 wsPlayer 等）
        addDependencies()

        framework {
            baseName = project.qnModuleName
            // 根据 qn.ios.enableDynamicFrameworkLink 决定是否使用动态库
            // false: 静态库（不需要强链接外部依赖）
            // true: 动态库（需要强链接 KuiklyIOSRender）
            isStatic = !enableDynamicFrameworkLink
            license = "MIT"
            // fat KMP 发布已通过源码合并导出能力，不再 export 本地 project，避免 Maven 发布触发 Pod framework 链接失败。
            if (!enableFatKmmPublication()) {
                exportFeatureModule(this)
            }
        }
    }

    private fun Project.setupKmmOhos(extension: KotlinMultiplatformExtension) = with(extension) {
        setupKmmOhos() {
            if (enableFatKmmPublication()) {
                val main by compilations.getting
                main.cinterops.create("interop") {
                    definitionFile.set(rootProject.file("qnPlatform/src/ohosInterop/cinterop/interop.def"))
                    includeDirs(rootProject.file("qnPlatform/src/ohosInterop/cinterop/cpp/include"))
                }
            }
            // 只对鸿蒙 OHOS target 生效的编译参数
            compilations.all {
                compilerOptions.configure {
                    freeCompilerArgs.addAll(
                        "-Xdisable-phases=RemoveRedundantCallsToStaticInitializersPhase",
                        "-Xdisable-phases=DevirtualizationAnalysis",
                        "-Xdisable-phases=DCEPhase",
                        "-Xadd-light-debug=enable",
                        // 切回标准 CMS GC，避免腾讯视频魔改的 PCMS 在鸿蒙上出现长时停顿
                        "-Xbinary=gc=cms",
                        // 尺寸优化：-Os 替代默认 O3，配合 function/data sections 便于链接器剔除未用符号
                        "-Xoverride-konan-properties=clangOptFlags.ohos_arm64=-Os -ffunction-sections -fdata-sections",
                        // Debug 编译也启用 gc-sections，降低链接耗时
                        "-Xbinary=enableGCSectionsWhenDebug=true",
                        // 输出详细编译日志，便于在流水线和本地对照分析编译耗时瓶颈
                        "-verbose"
                    )
                }
            }
            binaries.sharedLib {
                // 仅导出主模块的符号，其他模块一律忽略
                freeCompilerArgs += "-Xadd-light-debug=enable"
            }
        }
    }

    /**
     * 非本地debug编译模式，合并其他模块的sourceSet到主模块
     * 本地编译模式，添加对其他[Project]的依赖
     */
    private fun Project.setupSourceSet4NonLocalBuild(extension: KotlinMultiplatformExtension) =
        extension.apply {

            sourceSets.apply {
                if (isAndroidBuild()) {
                    listOf(commonMain, commonTest, androidMain).forEach {
                        tryMergeLocalSourceSets(it.name, it.get())
                    }
                    val androidLibraryExtension =
                        project.extensions.getByType(LibraryExtension::class.java)
                    tryMergeLocalAssets(androidLibraryExtension.sourceSets.getByName("main"))
                }

                if (isIOSBuild()) {
                    listOf("commonMain", "commonTest", "iosMain").forEach { sourceSetName ->
                        findByName(sourceSetName)?.let {
                            tryMergeLocalSourceSets(sourceSetName, it)
                        }
                    }
                }

                if (isOhosBuild()) {
                    listOf("commonMain", "commonTest", "ohosArm64Main").forEach { sourceSetName ->
                        findByName(sourceSetName)?.let {
                            tryMergeLocalSourceSets(sourceSetName, it)
                        }
                    }
                }

                commonMain.configure {
                    dependencies {
                        tryAddLocalProjectDependency4Main(this)
                        tryAddFatAarCommonDependencies(this)
                        api(libs.kotlin.serialization)
                        implementation(project.libs.kotlinx.collections.immutable)
                    }
                }

                if (isAndroidBuild()) {
                    androidMain.configure {
                        dependencies {
                            tryAddFatAarAndroidDependencies(this)
                        }
                    }
                }

                if (isIOSBuild()) {
                    findByName("iosMain")?.dependencies {
                        tryAddFatKmpIosDependencies(this)
                    }
                }

                commonTest.configure {
                    dependencies {
                        implementation(project.libs.kotlin.test)
                    }
                }

                all {
                    languageSettings.optIn("org.jetbrains.compose.resources.ExperimentalResourceApi")
                    languageSettings.optIn("kotlin.experimental.ExperimentalNativeApi")
                    languageSettings.optIn("kotlinx.cinterop.ExperimentalForeignApi")
                }
            }
        }
}
