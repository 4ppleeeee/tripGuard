# 转码产物集成指南

> 💡 **快捷方式**：`bash scripts/deco-integrate.sh` 可自动完成查找产物、复制文件、提示路由注册。
> 先预览：`bash scripts/deco-integrate.sh --dry-run`

## 产物目录结构

转码成功后，产物位于 Deco CLI 输出目录：

```
<deco-output-base>/
  {TaskName}_{date}_{seq}/
    assets/                   # 图片资源
      icon_xxx.png
      bg_xxx.svg
    assets-manifest.json      # 资源清单（含 CDN/COS URL）
    ComponentName.kt          # 生成的 Kotlin 代码
```

> 产物目录位置因 Node.js 管理工具而异（npm / volta / nvm）。`deco-convert.sh` 和 `deco-integrate.sh` 会自动探测。

手动查找最新输出目录：

```bash
# 方式 1：从 deco-convert.sh 的缓存读取
cat /tmp/deco-latest-output

# 方式 2：从转码输出日志中查找路径（看 "Kotlin 代码:" 或 "输出目录:" 行）

# 方式 3：手动查找
ls -td "$(npm root -g)/@tencent/deco/output"/*/ | head -1
```

---

## 集成步骤

### 1. 复制 .kt 文件

将生成的 `.kt` 文件复制到项目中存放页面代码的目录：

```bash
# 使用集成脚本（自动探测目标目录）
bash scripts/deco-integrate.sh

# 或指定目标目录
bash scripts/deco-integrate.sh --pages-dir src/commonMain/kotlin/com/yourcompany/app/pages/
```

脚本自动处理：
- **文件重命名**：去掉日期后缀（`Foo_2026-03-16_12-43.kt` → `Foo.kt`）
- **package 修复**：将 Deco 默认 package 替换为目标目录对应的 package（从 `kotlin/` 或 `java/` 后的路径自动推断）

### 2. 复制图片资源

将 `assets/` 中的图片复制到项目资源目录：

```bash
# 检查是否有 assets 目录
ls "$LATEST_DIR/assets/"
```

> 资源目录路径因项目配置而异，常见位置：
> - `commonMain/resources/`（KMP 通用资源）
> - `androidApp/src/main/res/`（Android）
> - `iosApp/Assets.xcassets/`（iOS）
>
> 如果 `assets-manifest.json` 中的图片已上传到 CDN/COS，且代码引用的是网络 URL，则无需复制本地资源。

### 3. 注册路由

页面路由注册方式取决于项目配置：

- **Compose DSL + `@Page` 注解**：自动注册，无需手动操作
- **传统 DSL / 无 `@Page` 注解**：在项目的路由配置文件中手动添加导航入口

> 集成脚本会自动检测 `@Page` 注解并提示路由信息。
