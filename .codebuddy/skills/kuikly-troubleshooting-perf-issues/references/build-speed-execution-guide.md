# 编译效率优化执行指南

Kotlin Native编译耗时主要分布在：**下载依赖项、编译klib、内联和Link阶段**，最主要是Link阶段。

## 从使用角度避免构建耗时

### 避免构建前下载依赖项
流水线构建场景下，如发现下载依赖项耗时较大，可将依赖预置到构建机镜像中，或使用专用构建机。

### 避免过多导出
export的类/方法过多影响编译时间和产物大小。参考「安装包优化」中的符号内部化部分。

对OC平台可使用标志位控制不导出符号，通过显式 `@ObjCName` 注解导出：

```kotlin
// 推荐升级到2.0.21-mini-060或更新版本启用
freeCompilerArgs.add("-Xenable-default-objc-export=false")
```

---

## Debug Build编译优化

### 全量构建场景

**1. 禁用LTO**

链接阶段耗时占大头，Debug及不需要关注性能的Release构建可禁用LTO。示例：视频鸿蒙工程通过关闭LTO编译耗时由2.5h降至0.5h（下降80%）。

```kotlin
val isReleaseBuild = project.findProperty("isReleaseBuild")?.toString()?.toBoolean() ?: false

kotlin {
    ohosArm64 {
        compilations.all {
            compilerOptions.options.apply {
                if (!isReleaseBuild) {
                    freeCompilerArgs.add("-opt=false}")
                    freeCompilerArgs.add("-Xadd-light-debug=enable")
                    if (enableLinkOpt().not()) {
                        val CLANG_FLAGS = "clangFlags.ohos_arm64=-cc1 -emit-obj -disable-llvm-optzns -disable-llvm-passes -x ir -fno-emulated-tls;" + 
                                "clangOptFlags.ohos_arm64=-O0 -ffunction-sections;" + 
                                "clangNooptFlags.ohos_arm64=-O0;" +
                                "clangReleaseFlags.ohos_arm64=-O0"
                        freeCompilerArgs.add("-Xoverride-konan-properties=$CLANG_FLAGS")
                    }
                }
            }
        }
    }
}
```

通过 `-PisReleaseBuild=true` 在命令行控制。

**2. 减少内联**

视频主端工程从17min优化至3.5min（M3设备），下降79%。内网Kotlin 2.0.21版本Debug Build默认减少内联。使用1.9.x的建议升级至2.0.21。

**3. 启用llvmOptLevel和llvmSizeLevel**

解决Debug包体过大导致的lld耗时激增。腾讯视频Debug全量编译从18min优化至11min。**2.0.21-mini-062版本起支持**，低版本建议升级。

```kotlin
freeCompilerArgs.add("-Xbinary=llvmOptLevel=1")
freeCompilerArgs.add("-Xbinary=llvmSizeLevel=2")
```

### 增量编译优化

**1. 启用增量编译**

Kotlin 2.0.21-mini-031支持鸿蒙增量编译。腾讯视频全量编译从4.5min优化至1.2min。

**2. 文件级别增量编译优化（2.0.21-mini-070起支持）**

- `Xenable-cache-transitive-opt-for-cache`：仅重编对外部依赖有影响的修改。广告业务鸿蒙增量从12min降至3min
- `Xenable-retry-single-thread-for-cache`：缓存并发编译崩溃时单线程重试，降低构建失败率

```kotlin
freeCompilerArgs.add("-Xenable-retry-single-thread-for-cache=true")
freeCompilerArgs.add("-Xenable-cache-transitive-opt-for-cache=true")
```

**3. 替换链接器**

替换为mold链接器：

```kotlin
kotlin {
    ohosArm64 {
        compilations.all {
            compilerOptions.options.apply {
                val first = gradle.startParameter.taskNames.firstOrNull()
                first?.let {
                    if (it.contains("Debug")) {
                        if (execCommandForStdout(project, projectDir, "which", "mold") == 0) {
                            val linker = "linker.macos_arm64-ohos_arm64=/opt/homebrew/bin/mold"
                            freeCompilerArgs.add("-Xoverride-konan-properties=$linker")
                        } else {
                            execCommandForStdout(project, projectDir, "brew", "install", "mold")
                        }
                    }
                }
            }
        }
    }
}
```

---

## Release Build编译优化

Release Build耗时大的主要原因是LTO全局优化。以下3个Phase对性能影响较小，但对编译耗时有明显降低（下降30%～50%，具体因项目而异，使用时请验证）：

```kotlin
kotlin {
    ohosArm64 {
        compilations.all {
            compilerOptions.options.apply {
                // 禁用虚调用消除
                freeCompilerArgs.add("-Xdisable-phases=DevirtualizationAnalysis")
                // 禁用冗余静态初始化调用移除
                freeCompilerArgs.add("-Xdisable-phases=RemoveRedundantCallsToStaticInitializersPhase")
                // 禁用死代码移除（对产物大小有影响，死代码较多时切勿禁用）
                freeCompilerArgs.add("-Xdisable-phases=DCEPhase")
            }
        }
    }
}
```

**4. LLVM GlobalOpt优化（2.0.21-mini-112起支持）**

减少链接阶段对高复杂度函数的调用，编译耗时下降70%。低版本建议升级。
