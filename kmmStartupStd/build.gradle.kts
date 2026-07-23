import com.tencent.news.utils.runIfOhosBuild
import java.io.File

plugins {
    id("qqnews.kmm.library")
    id("qqnews.kmm.harmony")
}

runIfOhosBuild {
    pluginManager.apply("com.google.devtools.ksp")
    pluginManager.apply("com.tencent.tmm.knoi.plugin")

    val knoiExtensionClass = Class.forName("com.tencent.tmm.knoi.KnoiExtension")
    extensions.configure(knoiExtensionClass) {
        val tsGenDir = knoiExtensionClass.getDeclaredField("tsGenDir").also {
            it.isAccessible = true
        }
        tsGenDir.set(this, "${rootProject.rootDir}/ohosApp/shared/src/main/ets/kniogen/${project.name}")
    }

    afterEvaluate {
        val knoiConfigFile = File(rootProject.rootDir, ".gradle/knoi/${project.name}-config.ini")
        val knoiOutputDir = File(rootProject.rootDir, "ohosApp/shared/src/main/ets/kniogen/${project.name}")
        tasks.findByName("kspKotlinOhosArm64")?.let { kspTask ->
            kspTask.inputs.file(knoiConfigFile).optional()
            kspTask.outputs.upToDateWhen {
                knoiOutputDir.exists() && (knoiOutputDir.listFiles()?.isNotEmpty() == true)
            }
        }
    }
}

android {
    dependencies {
        implementation(project(":qnPlatform"))
        implementation(libs.ktor.client.okhttp)
        implementation(libs.tencent.mmkv)
        implementation("com.tencent.news:interesting_lottie:0.0.51")
    }
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(project(":kmmStartupCore"))
            api(project(":qnPlatform"))
            implementation(libs.ktor.client.core)
        }
    }
}
