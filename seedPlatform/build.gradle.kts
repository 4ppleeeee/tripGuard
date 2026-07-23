import com.tencent.news.extension.libs
import com.tencent.news.extension.setupKmmOhos
import com.tencent.news.utils.isOhosBuild
import com.tencent.news.utils.runIfOhosBuild
import com.tencent.news.utils.runIfNotOhosBuild

plugins {
    id("qqnews.kmm.library")
    id("qqnews.kmm.harmony")
    // Required by KNOI codegen used in ohosArm64 platform bridges.
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
        val commonMain by getting {
            dependencies {
                implementation(libs.ktor.client.core)
                implementation(libs.wire.runtime)
                implementation(libs.mmkvkotlin)
                runIfOhosBuild {
                    implementation(libs.ktor.client.ohos)
                }
            }
        }
    }

    runIfNotOhosBuild {
        sourceSets {
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
            val cinterop by main.cinterops.creating {
                definitionFile.set(project.file("src/ohosInterop/cinterop/interop.def"))
                includeDirs("$projectDir/src/ohosInterop/cinterop/cpp/include")
            }
        }
    }
}
