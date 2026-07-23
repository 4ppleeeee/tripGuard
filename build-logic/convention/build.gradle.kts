import java.util.Properties

plugins {
    `kotlin-dsl`
}

apply(from = "../../version.compat.gradle.kts")

dependencies {
    api(gradleKotlinDsl())
    api(libs.kotlin.gradle.plugin)
    api(libs.kotlin.serialization.plugin)
//    api(libs.compose.gradle.plugin)
//    api(libs.compose.compiler.gradle.plugin)
    api(libs.android.gradlePluginApi)
    api(libs.android.gradlePlugin)
    api(libs.detekt.gradlePluginApi)
    api(libs.ksp.plugin)
    if (isOhosBuild()) {
        // 升级kotlin2.0
        api(libs.ohos.knoi.plugin) {
            exclude(group = "org.jetbrains.kotlin")
        }
    }
    // LibrariesForLibs
    api(files(libs.javaClass.superclass.protectionDomain.codeSource.location))
}


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

gradlePlugin {
    plugins {
        register("qnKmmPublish") {
            id = "qqnews.kmm.publish"
            implementationClass = "QNKmmPublishPlugin"
        }

        register("qnKmmLibrary") {
            id = "qqnews.kmm.library"
            implementationClass = "QNKmmLibraryPlugin"
        }

        register("qnKmmCoreLibrary") {
            id = "qqnews.kmm.main"
            implementationClass = "QNKmmMainPlugin"
        }

        register("qnKmmHarmonyLibrary") {
            id = "qqnews.kmm.harmony"
            implementationClass = "QNKmmHarmonyPlugin"
        }

        register("qnKmmCompose") {
            id = "qqnews.kmm.compose"
            implementationClass = "QnNTComposePlugin"
        }
    }
}

fun Project.isOhosBuild() = "ohos" == getBuildPlatform()

fun Project.getBuildPlatform(): String {
    return System.getenv("QN_COMPAT_BUILD_TYPE")?.takeIf { it.isNotBlank() }
        ?: globalProps("build.platform").ifBlank { globalProps("qqnews.kmm.build.platform") }
}

fun globalProps(key: String): String {

    if (project.properties.containsKey(key)) {
        return project.properties[key].toString()
    }

    val localPropFile = File(rootDir.parentFile, "local.properties")
    if (!localPropFile.exists()) {
        return ""
    }
    val props = Properties().also { it.load(localPropFile.inputStream()) }
    return props.getProperty(key)?.toString() ?: ""
}
