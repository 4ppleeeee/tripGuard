import com.tencent.news.extension.libs
import com.tencent.news.extension.setupKmmOhos
import com.tencent.news.utils.isOhosBuild
import com.tencent.news.utils.runIfOhosBuild
import com.tencent.news.utils.runIfNotOhosBuild

plugins {
    id("qqnews.kmm.library")
    id("qqnews.kmm.harmony")
    id("qqnews.kmm.compose")
}

kotlin {
    targets.configureEach {
        compilations.configureEach {
            compileTaskProvider.configure {
                compilerOptions.freeCompilerArgs.add("-Xallow-unstable-dependencies")
            }
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.ktor.client.core)
            implementation(libs.wire.runtime)
            implementation(libs.mmkvkotlin)
            runIfOhosBuild {
                implementation(libs.ktor.client.ohos)
            }
        }
        runIfNotOhosBuild {
            androidMain.dependencies {
                implementation(libs.ktor.client.okhttp)
                implementation(libs.tencent.mmkv)
            }
            iosMain.dependencies {
                implementation(libs.ktor.client.darwin)
            }
        }
    }

    if (isOhosBuild()) {
        setupKmmOhos {
            val main by compilations.getting
            val interop by main.cinterops.creating {
                defFileProperty.set(project.file("src/ohosInterop/cinterop/interop.def"))
                includeDirs("$projectDir/src/ohosInterop/cinterop/cpp/include")
            }
        }
    }
}
