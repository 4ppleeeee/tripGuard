# BowenCore 独立工程迁移计划

> 给后续接手的 Codex：按任务清单继续推进即可，已完成项不要重复做。

**目标：** 将 `bowenCore` 从 `kmm-core` 仓内 module 拆出去，变成同级独立业务 KMM 工程，通过 Maven 消费 `kmm-core` 发布出的底座产物。

**架构：** `kmm-core` 只保留公共底座并发布 `qnCommon`；`bowenCore` 作为独立业务工程存在，不再依赖 `project(":qnView")`。

**当前策略：** Android-first。先用 `mavenLocal()` 验证独立业务工程消费 `qnCommon` Android fat AAR；iOS / OHOS / KMP metadata 后续再补。

---

### 任务 1：从 kmm-core 中解绑 BowenCore

**涉及文件：**
- 修改：`/Users/aatroxli/coding/tencent/kmm-core/modules.properties`
- 修改：`/Users/aatroxli/coding/tencent/kmm-core/modules.compose.properties`
- 修改：`/Users/aatroxli/coding/tencent/kmm-core/androidApp/build.gradle.kts`
- 修改：`/Users/aatroxli/coding/tencent/kmm-core/androidApp/src/main/java/com/tencent/kuikly/core/android/KuiklyCoreEntry.kt`

- [x] 从 module 列表中移除 `bowenCore`。
- [x] 移除 `project(":bowenCore")` 直接依赖和生成入口引用。
- [x] 保证 `kmm-core` 仍可作为底座工程正常构建。

### 任务 2：创建独立 BowenCore 工程

**涉及文件：**
- 新建：`/Users/aatroxli/coding/tencent/bowenCore/settings.gradle.kts`
- 新建：`/Users/aatroxli/coding/tencent/bowenCore/build.gradle.kts`
- 新建：`/Users/aatroxli/coding/tencent/bowenCore/bowenCore/build.gradle.kts`
- 新建：`/Users/aatroxli/coding/tencent/bowenCore/bowenCore/src/androidMain/kotlin/com/tencent/kmm/demo/bowen/BowenDemoPages.kt`

- [x] 只拷贝 demo 页面源码到独立工程。
- [x] 配置 `mavenLocal()` 和腾讯 Maven 镜像。
- [x] Android 先依赖 `com.tencent.news.core.android:qnCommon`。
- [x] 拷贝 `.claude` / `.codebuddy` / `.codex` 到独立工程。

说明：`commonMain` 暂时后置。当前 `qnCommon` 的 KMP metadata 发布会卡在 `:umbrella:compileCommonMainKotlinMetadata`，原因是合并后的 `expect` 源码，例如 `ConcurrentMap`，需要平台 actual 配对。当前 Android-first 先使用 Android fat AAR 验证链路。

### 任务 3：发布并验证 Android 消费链路

**命令：**
- 发布底座：`./gradlew :umbrella:publishQnkmmAndroidReleasePublicationToMavenLocal -DqnCommon.fat.aar=true --no-daemon --console=plain`
- 构建业务工程：`./gradlew :bowenCore:assembleDebug --no-daemon --console=plain`

- [x] 将 `kmm-core` 发布为本地 Maven Android fat AAR。
- [x] 独立 `bowenCore` 通过 Maven 产物构建通过。
- [x] 记录 KMP metadata 当前阻塞点，保持改动最小。
