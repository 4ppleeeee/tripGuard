import java.util.Properties

plugins {
    id("com.android.application")
    kotlin("android")
}

val resolvedAndroidBuildToolsVersion =
    (findProperty("android.buildToolsVersion") as String?)
        ?: libs.versions.androidBuildToolsVersion.get()

// 读取 local.properties 中的流水线信息
val localProps = Properties().also { props ->
    val f = rootProject.file("local.properties")
    if (f.exists()) props.load(f.inputStream())
}
fun ciProp(key: String) = "\"${localProps.getProperty(key, "local")}\""

val hwAppIdRelease = "10097595"
val hwAppIdAlpha = "102064321"
val vivoAppIdRelease = "10125"
val vivoAppIdAlpha = "104946365"
val vivoAppKeyRelease = "9a8851ad-9773-4de9-95b1-c67181d3eca3"
val vivoAppKeyAlpha = "9f0e48ba4ae5d765eb2cdd15f7b80ef4"
val honorAppId = "104175589"

android {
    namespace = "com.tencent.kmm.demo"
    compileSdk = 34
    buildToolsVersion = resolvedAndroidBuildToolsVersion

    lint {
        // 当前依赖链上的多个 KMM/AAR 模块会产出不兼容 AGP 8.9 lint client 的 `lint.jar`，
        // 本地 `assembleRelease` 时会在 `lintVitalAnalyzeRelease` 阶段触发大量告警并最终 OOM。
        // 先关闭 release assemble 路径上的 lint vital，保证脚本可稳定出包；
        // 需要代码规范检查时，单独执行 lint 任务即可。
        checkReleaseBuilds = false
        abortOnError = false
    }

    defaultConfig {
        applicationId = "com.tencent.travel.mvpdemo"
        minSdk = 23
        targetSdk = 30
        // versionCode规则：灰度不做修改，应用市场+1
        // versionName规则：major.minor.patch.build
        // major.minor: 每个版本+1
        // patch: 应用商店小版本+1，灰度一直是0
        // build: 灰度从108开始，应用商店固定588
        versionCode = 1084
        versionName = "8.170.0.109"

        buildConfigField("String", "QQ_APP_ID", "\"1101083114\"")
        buildConfigField("String", "WX_APP_ID", "\"wx5dfbe0a95623607b\"")
        manifestPlaceholders["QQ_AUTH_SCHEME"] = "tencent1101083114"
        // 流水线构建信息（由构建脚本写入 local.properties）
        buildConfigField("String", "CI_BUILD_ID", ciProp("ci.build.id"))
        buildConfigField("String", "CI_BUILD_NUM", ciProp("ci.build.num"))
        buildConfigField("String", "CI_PIPELINE_NAME", ciProp("ci.pipeline.name"))
        buildConfigField("String", "CI_BRANCH", ciProp("ci.branch"))
        buildConfigField("String", "CI_COMMIT", ciProp("ci.commit"))
        buildConfigField("String", "CI_BUILD_TIME", ciProp("ci.build.time"))

        // 分64/32包
        splits {
            abi {
                isEnable = true
                reset()
                include("armeabi-v7a", "arm64-v8a")
                isUniversalApk = false
            }
        }
    }

    signingConfigs {
        create("demo") {
            storeFile = file("key_3")
            storePassword = "sailfish"
            keyAlias = "sailfish"
            keyPassword = "sailfish"
        }
        create("alpha") {
            storeFile = file("key_alpha.keystore")
            storePassword = "sailfish"
            keyAlias = "sailfish"
            keyPassword = "sailfish"
        }
        create("beta") {
            storeFile = file("key_beta.keystore")
            storePassword = "sailfish"
            keyAlias = "sailfish"
            keyPassword = "sailfish"
        }
    }

    buildFeatures {
        buildConfig = true
    }

    packaging {
        jniLibs {
            // 压缩 .so 文件（Stored → Deflate），显著减小 APK 体积
            useLegacyPackaging = true
        }
    }

    buildTypes {
        getByName("debug") {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("demo")
            buildConfigField("String", "QQ_APP_ID", "\"1101083114\"")
            buildConfigField("String", "WX_APP_ID", "\"wx5dfbe0a95623607b\"")
            manifestPlaceholders["QQ_AUTH_SCHEME"] = "tencent1101083114"
        }

        getByName("release") {
            isMinifyEnabled = true // app能启动了，细节功能还有问题；先关掉
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("demo")
            buildConfigField("String", "QQ_APP_ID", "\"1101083114\"")
            buildConfigField("String", "WX_APP_ID", "\"wx5dfbe0a95623607b\"")
            manifestPlaceholders["QQ_AUTH_SCHEME"] = "tencent1101083114"
        }

        create("alpha") {
            initWith(getByName("debug"))
            versionNameSuffix = "-alpha"
            matchingFallbacks += listOf("debug", "release")
            signingConfig = signingConfigs.getByName("alpha")

            buildConfigField("String", "QQ_APP_ID", "\"101868496\"")
            buildConfigField("String", "WX_APP_ID", "\"wx8fcf169ee9630741\"")
            manifestPlaceholders["QQ_AUTH_SCHEME"] = "tencent101868496"
            manifestPlaceholders["HW_APPID"] = hwAppIdAlpha
            manifestPlaceholders["VIVO_APPID"] = vivoAppIdAlpha
            manifestPlaceholders["VIVO_APPKEY"] = vivoAppKeyAlpha
        }

        create("beta") {
            initWith(getByName("debug"))
            versionNameSuffix = "-beta"
            matchingFallbacks += listOf("debug", "release")
            signingConfig = signingConfigs.getByName("beta")

            buildConfigField("String", "QQ_APP_ID", "\"101870470\"")
            buildConfigField("String", "WX_APP_ID", "\"wx0c4c32515565fc30\"")
            manifestPlaceholders["QQ_AUTH_SCHEME"] = "tencent101870470"
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    sourceSets {
        getByName("main") {
            java.setSrcDirs(listOf("src/main/minimal"))
        }
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    exclude(
        "**/com/tencent/kmm/demo/im/**",
        "**/com/tencent/kmm/demo/module/KRGameModule.kt",
        "**/com/tencent/kmm/demo/module/KRWelfareModule.kt",
        "**/com/tencent/kmm/demo/module/player/**",
        "**/com/tencent/kmm/demo/setup/AndroidBeaconReporter.kt",
        "**/com/tencent/kmm/demo/setup/AndroidPublisherLocationBridge.kt",
        "**/com/tencent/kmm/demo/setup/AndroidUpdateRuntimeContext.kt",
        "**/com/tencent/kmm/demo/setup/SetupAndroidAddFriendAuthBridge.kt",
        "**/com/tencent/kmm/demo/view/webview/AndroidTencentNewsJsBridge.kt",
        "**/com/tencent/kmm/demo/view/webview/AndroidWebViewJSBridge.kt",
    )
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:${libs.versions.kotlinx.coroutines.get()}")
    testImplementation("org.json:json:20240303")
    testImplementation(kotlin("test"))
}
