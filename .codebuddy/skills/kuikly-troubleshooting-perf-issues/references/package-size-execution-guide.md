# 安装包优化（包体积优化）执行指南

> **注意**：多数业务在鸿蒙上将Kotlin Native产物编译为动态库，iOS上编译为静态framework，因此iOS上宿主编译选项也影响最终链接产物大小。可结合iOS苹果官方及业界优化措施进行整体优化，本指引重点关注Kotlin Native产物大小。

## 1. Kotlin Native符号内部化

使用 `kuikly-internalizing-kotlin-native-symbols` skill 进行符号内部化。

关于 Shrinker 插件的详细配置和使用，参见 [Shrinker.md](Shrinker.md)。

## 2. 编译选项优化

**具体优化项**：

| 优化项 | 说明 | 注意事项 |
|-------|------|---------|
| `--pack-dyn-relocs=relr` | 动态重定位打包 | - |
| `--gc-sections` + `-ffunction-sections` + `-fdata-sections` | 垃圾代码清除与函数/数据级别分段 | - |
| `-Os` | 优化代码大小 | `-Oz` 效果更佳但对性能影响偏大 |
| `-mllvm -enable-machine-outliner=always` | 提取重复指令 | 对性能影响偏大，使用时多加关注 |

**配置示例**（build.gradle.kts）：

```kotlin
val isReleaseBuild = project.findProperty("isReleaseBuild")?.toString()?.toBoolean() ?: false

kotlin {
    targets.all {
        compilations.all {
            kotlinOptions {
                if (isReleaseBuild) {
                    // 注意: -enable-machine-outliner=always 对性能影响偏大，后续将提供和PGO结合的优化
                    val CLANG_OPT_FLAGS = "-Os -mllvm -enable-machine-outliner=always -ffunction-sections"
                    val CLANG_FLAGS = "clangOptFlags.ios_arm64=$CLANG_OPT_FLAGS;clangDebugFlags.ios_arm64=$CLANG_OPT_FLAGS;clangOptFlags.ohos_arm64=$CLANG_OPT_FLAGS;clangDebugFlags.ohos_arm64=$CLANG_OPT_FLAGS"
                    freeCompilerArgs += "-Xoverride-konan-properties=$CLANG_FLAGS"
                }
            }
        }
    }
    ohosArm64 {
        binaries.sharedLib("shared") {
            // ... 省略其他选项 ...
            freeCompilerArgs += "-Xadd-light-debug=enable"
            linkerOpts += "--pack-dyn-relocs=relr"
            linkerOpts += "--gc-sections"
        }
    }
}
```
