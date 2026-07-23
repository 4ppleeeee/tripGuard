---
name: restore-ui-design
description: Use when 用户提供 Figma 链接或旧项目 UI 代码，希望通过 Deco 还原 Kuikly Compose UI，进行 MVVM 或 Struct 架构重构，并绑定 ViewModel 接口。
---

# 设计稿还原（基于 Deco D2C）

## 目标
使用 Deco CLI 工具（Figma → Kuikly Compose 自动生成）+ 人工微调，从设计稿高效还原 UI 代码，并与 ViewModel 接口绑定。支持从旧项目迁移已有代码。

## 核心原则（最高优先级）

1. **只要存在可用的 Figma 设计稿链接，就必须先使用 `Deco` 转码**，禁止直接跳过 `Deco` 改为手写 UI。
2. **一个页面可能对应多个 Figma 链接**（默认态、选中态、展开态、空态等），只要这些链接都属于本次还原范围，就必须逐个执行 `Deco`，分别生成并比对对应状态代码。
3. **没有 Figma 设计稿时，先暂停等用户提供设计稿，用户提供后则使用 `Deco` 还原。** 禁止无设计稿直接手写 UI。
4. `Deco` 的职责是还原视觉结构与资源，后续仍需执行 5a/5b 完成项目规范改造、结构化拆分和业务绑定。

---

## 触发条件
用户提供以下输入时触发本 skill：
- ViewModel 接口代码（Step 4 design-viewmodel-interface 的输出）
- 设计稿 Figma 链接（含 `node-id` 参数）
- 或：旧项目中已有的 ViewModel / UI 代码

---

## 输入

| 参数 | 说明 | 是否必须 |
|------|------|----------|
| ViewModel 接口代码 | VM 接口定义，包含 UIState 数据结构和 Action 定义 | ✅ 必须 |
| 设计稿 | Figma 链接（必须包含 `node-id` 参数） | ✅ 必须 |
| 需求文档 | 评审通过的需求文档（`docs/component` 下的 `.md` 文件） | ✅ 必须 |
| 旧项目代码 | 旧项目中已有的 ViewModel / UI 代码路径或文件 | ⚡ 可选（迁移模式） |

---

## 输出

- Compose UI 组件代码（Deco 生成 / 迁移适配 + 手动调整，与 VM 接口绑定）
- 图片资源文件（将 Deco 产出的静态设计资源下载到项目本地 `composeResources/drawable/`，优先通过 `Res.drawable.xxx` / 本地 drawable 方式引用；仅业务动态图片继续保留 URL）

---

## Step -1：前置检查 Figma 链接

在执行任何步骤之前，先检查 Figma 链接是否可用，并**收集本次页面涉及的全部 Figma 链接**：

```
CHECK: 用户消息中是否提供了含 `node-id` 参数的 Figma 链接？
  ├── YES → 提取全部链接（不是只取第一个），继续执行 Step 0
  └── NO  → CHECK: 需求文档（docs/component 下对应 .md 文件）中是否已有 Figma 链接？
              ├── YES（文档中存在设计稿链接字段且含 node-id）→ 提取文档中的全部链接，继续执行 Step 0
              └── NO  → ⛔ 暂停任务，等待用户确认
                        提示用户：
                        "未找到 Figma 设计稿链接（用户未提供，需求文档中也没有）。
                        请提供 Figma 设计稿链接（需包含 node-id 参数），
                        例如：https://www.figma.com/design/ABC123/FileName?node-id=640-4637
                        获取方式：在 Figma 中选中目标 Frame/Component → 右键 → Copy link to selection"
                        ⛔ 必须等待用户提供 Figma 链接后，才能继续执行。
```

**多链接处理规则（MANDATORY）：**

1. 需要同时扫描**用户消息**和**需求文档**中的 Figma 链接，合并去重后形成本次输入链接列表。
2. 若同一页面存在多个 Figma 链接，默认视为**不同状态/场景**（如默认态、选中态、展开态、空态），不得只挑一个链接执行。
3. 若能从文案判断状态含义，应记录为「链接 → 状态」映射；若无法判断，也要保留全部链接并在后续比对时分别处理。
4. 只有用户明确缩小范围（例如"这次只还原默认态"）时，才允许只处理其中一部分链接。

---

## Step 0：选择执行模式

根据用户输入判断走哪条路径：

```
CHECK 0: 需求文档是否为 diff 文档（路径包含 /diff/ 或文件名包含 _diff）？
  ├── YES → ✏️ 迭代模式（Path I）
  │         已有页面迭代修改，只修改 diff 涉及的 UI 组件，不重新实现整个页面
  │
  └── NO  → CHECK: 用户是否提供了旧项目的 ViewModel / UI 代码？
              ├── YES → 🔄 迁移模式（Path M）
              │         旧代码已有基础，搬运 + 适配 Kuikly Compose + Deco 补差异
              │
              └── NO  → 🆕 Deco 新建模式（Path A）
                        Deco CLI 自动生成 + 调整适配
```

**模式选择后的执行路径：**

| 模式 | 执行步骤 |
|------|---------|
| ✏️ 迭代模式（Path I） | Step I1 → I2 → I3 → Step 6 → 7 → 8 |
| 🔄 迁移模式（Path M） | Step M1 → M2 → M3 → M4 → M5 → Step 6 → 7 → 8 |
| 🆕 Deco 新建模式（Path A） | Step 1 → 2 → 3 → 4 → **5a**(规范性改造) → **5ar**(架构重构) → **5b**(业务绑定) → Step 6 → 7 → 8 |

> Step 6（验证）、Step 7（预览）、Step 8（回填设计稿链接 + 更新 component-map）三条路径共享。

---

## ✏️ 迭代模式（Path I）：已有页面迭代修改

### Step I1：读取基线文档和 diff 文档

1. 读取基线需求文档（`{page}.md`）和 diff 需求文档（`diff/{page}_diff.md`），理解：
   - 基线文档中已有的完整组件清单和交互逻辑
   - diff 文档中本次新增/修改的组件和交互
2. 读取已有的 UI 代码文件（`module/{模块名}/{功能名}/ui/` 目录下），理解现有实现结构
3. 读取 diff VM 接口变更（Step 4 design-viewmodel-interface 迭代模式的输出），了解新增/修改的 State/Action

---

### Step I2：定位需要修改的 UI 文件

根据 diff 文档中的变更内容，精确定位需要修改的文件和代码位置：

1. **新增组件**：确定新组件应放在哪个文件的哪个位置（参考基线文档的区域划分）
2. **修改组件**：找到对应组件的 Composable 函数，确认修改范围
3. **新增页面区域**：若 diff 中有全新的 UI 区域，确定是新建文件还是在已有文件中追加

> ⚠️ **严格约束**：只修改 diff 文档中明确列出的变更组件，不触碰其他已有组件的代码。

---

### Step I3：执行 UI 修改

按 diff 文档逐项修改 UI 代码：

1. **新增组件**：
   - 若有 Figma 链接，对新增区域使用 Deco CLI 生成代码（参考 Path A 的 Step 3）
   - 若无 Figma 链接，根据 diff 文档描述手动编写新组件
   - 新增代码加注释 `// [diff] 新增：{TAPD需求标题}`

2. **修改组件**：
   - 在已有 Composable 函数中修改对应逻辑
   - 修改处加注释 `// [diff] 修改：{TAPD需求标题}`

3. **绑定新 State/Action**：
   - 将新增的 UIState 字段绑定到对应 UI 展示
   - 将新增的 Action 绑定到对应交互事件

4. **规范性检查**：确保新增/修改的代码符合 5a 规范（import 路径、组件替换、颜色语义化等）

---



---

## 🔄 迁移模式（Path M）：从旧项目迁移已有代码

### Step M1：定位旧项目代码

1. 根据用户提供的信息，在旧项目中找到相关文件：
   - ViewModel 文件（含 UIState、Action、业务逻辑）
   - UI 组件文件（Compose / XML / 其他 UI 框架）
   - Repository / UseCase 文件（数据层）
   - 资源文件（图片、字符串）

2. 读取所有相关文件，理解：
   - 数据模型结构（字段、类型、嵌套关系）
   - UI 组件层级（页面 → 区域 → 子组件）
   - 业务逻辑（状态管理、事件处理、网络请求）
   - 导航关系（页面跳转、参数传递）

---

### Step M2：搬运到新项目目录

将旧代码复制到项目标准路径：

```
shared/src/commonMain/kotlin/com/tencent/weishi/module/{模块名}/{功能名}/
├── data/          # UIState + 数据模型
├── action/        # Action 定义
├── ui/            # UI 组件代码
├── repository/    # 数据层（如果有）
├── usecase/       # UseCase（如果有）
└── {PageName}ViewModel.kt
```

---

### Step M3：适配 Kuikly Compose 框架

旧项目代码可能使用标准 Android Compose、XML View、或其他框架，需要逐项适配：

#### M3.1 替换 import 路径

```kotlin
// ❌ 旧项目（标准 Android Compose）
import androidx.compose.foundation.*
import androidx.compose.material3.*
import androidx.compose.ui.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

// ✅ 新项目（Kuikly Compose）
import com.tencent.kuikly.compose.foundation.*
import com.tencent.kuikly.compose.material3.*
import com.tencent.kuikly.compose.ui.*
```

#### M3.2 替换页面容器

```kotlin
// ❌ 旧项目（Activity/Fragment + setContent）
class MyActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        setContent { MyPage() }
    }
}

// ✅ 新项目（ComposePage + 按用户选择接入对应页面架构）
@Page(ComposeViewKey.{Module}.{PAGE_KEY})
internal class {Feature}Page : ComposePage() {

    override fun sceneName() = "{Feature}"

    @Composable
    override fun OnSetContent() {
        super.OnSetContent()
        val pageScope = rememberCoroutineScope()
        val pageArgs = rememberedPageArgs<{Feature}PageArgs>()

        // 如果用户选择 Struct：使用 StructComposePage / StructComposePage4VM
        // 如果用户选择 MVVM：使用自定义 ViewModel + 页面级 Composable
        // 参考 doc/开发指南/如何新增一个compose页面.md
    }
}
```

#### M3.3 替换图片加载

```kotlin
// ❌ 旧项目
AsyncImage(model = url, contentDescription = null)  // Coil
Glide.with(context).load(url)                        // Glide

// ✅ 新项目
Image(
    painter = rememberAsyncImagePainter(url),  // com.tencent.kuikly.compose.coil3
    contentDescription = null
)
```

#### M3.4 替换导航方式

```kotlin
// ❌ 旧项目
navController.navigate("route")       // Navigation Compose
startActivity(Intent(this, XxxActivity::class.java))  // Activity

// ✅ 新项目
PagerManager.getPager()?.let {
    RouterModule.openPage(it, targetPageName, params)
}
```

#### M3.5 适配 ViewModel 基类

如果旧 ViewModel 继承 `androidx.lifecycle.ViewModel`，需要适配为项目的 VM 模式（参考 `doc/开发指南/如何设计一个优雅的ViewModel.md` + Step 5ar 架构重构的输出格式）。

#### M3.6 处理平台特定代码

旧代码中的 Android 特定 API（`Context`、`SharedPreferences`、`Toast` 等）需要：
- 替换为 KMM `expect/actual` 模式
- 或使用项目已有的跨平台封装

---

### Step M4：用 Deco 补充 UI 差异（可选）

如果新版设计稿和旧版有差异：

1. 对比新旧设计稿，识别差异部分（新增组件、布局变化、样式更新）
2. 对差异部分使用 Deco CLI 生成新代码（参考 Path A 的 Step 1~3）
3. 将 Deco 生成的新组件合并到迁移后的代码中

```
旧代码迁移后的 UI ──┐
                     ├──► 合并 → 最终 UI 代码
Deco 生成的差异 UI ──┘
```

---

### Step M5：绑定新 ViewModel 接口

迁移的 UI 代码可能仍然绑定旧 ViewModel 接口，需要与 Step 4 生成的新 VM 接口对齐：

1. 比对旧 UIState vs 新 UIState：
   - 字段名变化 → 批量替换
   - 新增字段 → 添加 UI 展示
   - 删除字段 → 移除 UI 引用
2. 比对旧 Action vs 新 Action：
   - 事件名变化 → 批量替换
   - 新增事件 → 绑定到对应的 UI 交互
   - 删除事件 → 移除 UI 回调
3. 确保 `viewModel.dispatchAction()` 调用全部匹配新 Action 定义

---

## 🆕 Deco 新建模式（Path A）：以下为从零生成的步骤

## 执行步骤

### Step 1：环境检查与 Deco CLI 准备

按以下决策树逐项检查环境（**仅支持 remote 模式**，无需本地 Figma Desktop）：

```
CHECK 1: Node.js 是否已安装？
  执行: node -v
  ❌ 未安装 → 安装 Node.js (>= 18.0.0)：
    curl -o- https://raw.githubusercontent.com/nvm-sh/nvm/v0.40.3/install.sh | bash
    \. "$HOME/.nvm/nvm.sh"
    nvm install 24
  ✅ 已安装 → 继续

CHECK 2: Deco CLI 是否已安装？
  执行: deco --version
  ❌ 未安装 → 安装 Deco：
    npm install -g @tencent/deco --registry=https://mirrors.tencent.com/npm/
  ✅ 已安装 → 检查是否需要更新：
    npm update -g @tencent/deco --registry=https://mirrors.tencent.com/npm/

CHECK 3: Deco 是否已登录认证？（remote 模式必需）
  ❌ 未认证 → 执行 deco → 输入 /login → 完成 IOA 企业登录
  ✅ 已认证（~/.deco/config.json 存在有效 token）→ 继续
```

**注意**：
- Remote 模式通过 Figma API 远程获取设计稿，无需安装 Figma Desktop
- 确保网络可访问 `figma.com`（如需代理，请提前配置）

---

### Step 2：获取 Figma 设计链接

**关键要求：URL 必须包含 `node-id` 参数。**

```yaml
url_format: "https://www.figma.com/design/{fileKey}/{fileName}?node-id={nodeId}"
valid_node_id_formats: ["0-627", "0:627"]  # 两种格式均可
```

**获取方式：**

1. 在 Figma 中选中目标元素（必须是 **Frame 或 Component**，不能是 Group）
2. 右键 → "Copy link to selection"
3. 验证 URL 包含 `?node-id=` 参数

**验证规则：**

| 检查项 | 不通过处理 |
|--------|-----------|
| URL 不含 `node-id` | 提示用户先选中元素再复制链接 |
| URL 从浏览器地址栏复制 | 警告：可能缺少 node-id，请用右键菜单 |
| 选中的是 Group 而非 Frame/Component | 警告：转换可能失败，请选择 Frame 或 Component |

---

### Step 3：使用 Deco CLI 生成 Kuikly Compose 代码

根据环境状况选择合适的转换模式：

```
决策树（仅支持 remote 模式）：
  → 直接使用 --remote 模式（通过 Figma API 远程获取设计稿，无需本地 Figma Desktop）
```

**执行转换命令：**

> ⚠️ Deco CLI `0.6.20+` 支持 `--prompt-dir` 参数指定自定义 Prompt Profile 目录，优先于全局配置。
> 项目自定义规则位于 `qnView/deco-prompt/`（含 `profile.json` + `sections/`）。

> ⚠️ **强制执行规则（MANDATORY）**：在 Path A 模式下，Deco 生成设计稿代码是**必须执行**的步骤，**禁止跳过**。
> 无论遇到何种情况，AI 代理**不得**自行决定跳过 Deco 转码，**必须先尝试 remote 模式**，失败后**立即中断流程并提示用户**。

**执行转换命令（对每个 `figma_url` 使用 remote 模式）：**

```
强制执行规则：
FOR 每一个 figma_url IN figma_url_list
  Step 3.1: 使用 Remote 模式
    执行: deco to-kuikly --prompt-dir ./qnView/deco-prompt --remote --image-scale 3 -o ./qnView/deco-output "{figma_url}"
    ├── ✅ 成功 → 记录该链接成功，处理下一个 figma_url
    └── ❌ 失败 → 记录该链接失败

IF 存在任一 figma_url 失败
  → ⛔ 汇总失败链接并立即中断（见下方阻塞规则）
ELSE
  → 全部链接均成功，继续 Step 4
```

```bash
# Remote 模式（无需本地 Figma Desktop / Dev Mode）
deco to-kuikly --prompt-dir ./qnView/deco-prompt --remote --image-scale 3 -o ./qnView/deco-output "https://www.figma.com/design/ABC123/FileName?node-id=xxxxxx"

# 交互式 REPL 模式
deco
> /to-Kuikly --remote --image-scale 3 https://www.figma.com/design/ABC123/FileName?node-id=xxxxxx
```

**生成产物结构：**

```
{deco_install_dir}/output/
  {TaskName}_{date}_{seq}/
    assets/                   # Deco 导出的原始图片资源
      icon_xxx.png
      bg_xxx.svg
    assets-manifest.json      # 资源清单（含原始 URL / 文件名映射，用于下载并落本地 drawable）
    ComponentName.kt          # 生成的 Kuikly Compose 代码
```

**查看输出路径（macOS）：**

```bash
open /usr/local/lib/node_modules/@tencent/deco/output/
# 注意：实际路径以 CLI 输出为准
```

**常见错误处理：**

| 错误 | 原因 | 解决方案 |
|------|------|---------|
| `node-id` missing | 复制 URL 时未选中元素 | ⛔ 中断：提示用户先选中 Frame/Component，再右键 Copy link |
| 转换 Group 失败 | Group 不支持作为转换目标 | ⛔ 中断：提示用户选择 Frame 或 Component |
| Remote relay 连接超时 | Relay 服务器不可达 | ⛔ 中断：提示用户检查网络连接 |
| Login timeout / 未登录 | IOA 登录未完成或 token 失效 | ⛔ 中断：提示用户执行 `deco` 后输入 `/login` 完成登录 |
| Figma 权限不足 / 403 | 账号无文件访问权限 | ⛔ 中断：通知用户联系设计稿 Owner 添加访问权限 |
| Remote 模式失败 | 无法从 Figma API 获取设计稿数据 | ⛔ **强制中断**：禁止继续，必须通知用户 |

---

> ⛔ **Deco 生成强制执行 & 失败中断规则（MANDATORY - 最高优先级）**
>
> **核心原则：Path A 模式下，Deco 生成是不可跳过的必要步骤。**
>
> #### 强制执行要求
> 1. AI 代理在 Path A 模式下**必须**执行 Deco CLI 转码命令
> 2. **必须**按照「本地模式 → Remote 模式 → Sloth 模式」的顺序依次尝试
> 3. 每种模式失败后，**必须**记录具体错误信息，再尝试下一种模式
> 4. **禁止**在未尝试所有模式的情况下放弃 Deco 生成
> 5. **禁止**自行跳过 Deco 转码直接手写 UI 代码
>
> #### 失败中断规则
> 当所有模式（本地 + remote + sloth）均失败时，**必须立即中断整个流程**：
> - **不得**继续执行 Step 4 及后续步骤
> - **不得**自行用截图/猜测/AI 生成方式替代 Deco 输出
> - **不得**跳过 Deco 转码直接手写 UI 代码
> - **必须**立即暂停并输出以下提示：
>
> ```
> ⛔ Deco 设计稿生成失败 — 流程已中断
>
> 已尝试的模式及错误：
> 1. Remote 模式：{Remote 模式错误信息，或 "未尝试（原因）"}
>
> 请检查以下事项：
> 1. 确认 Figma 链接正确且包含 node-id 参数
> 2. 确认 Figma 账号对该文件有访问权限
> 3. 检查网络连接是否正常（Remote 模式需要访问 Figma API）
> 4. 执行 `deco` 后输入 `/login` 确认登录状态
>
> 解决后请告诉我，我将从 Deco 生成步骤重新开始。
> ```
>
> **只有在用户明确回复后，才能继续执行。任何自动跳过行为均视为违规。**
>
> #### 权限类错误的特殊处理
> 当错误明确为 Figma 权限问题（403、文件无访问权限等）时，
> 在上述提示基础上额外强调：
> ```
> ⚠️ 检测到 Figma 权限问题，Remote 模式也无法绕过权限限制。
> 请联系设计稿文件的 Owner 为你的 Figma 账号添加访问权限。
> ```

---

### Step 4：读取项目 UI 规范

无论是 Deco 生成的代码还是手动编写，都需要遵守项目规范。读取以下文档：

1. **`doc/开发指南/如何新增一个compose页面.md`** — 页面入口与路由接入参考：
   - `ComposePage` 作为页面基类 + `@Page` 注解注册路由
   - `OnSetContent {}` 中构建 Compose UI
   - **Struct 架构**：使用 `StructComposePage` / `StructComposePage4VM` + PageWidget + DataRepo
   - **MVVM 架构**：使用 `ComposePage` + 自定义 ViewModel + 页面级 Composable
2. **`doc/开发指南/如何设计一个优雅的ViewModel.md`** — ViewModel 接口设计原则（面向 UI 设计）
3. **`doc/开发指南/如何新增一个composeCell.md`** — Cell 组件开发模式
4. **`doc/开发指南/如何新增一个composeWidget组件.md`** — Widget 组件开发模式

**项目 Kuikly Compose 规范要点（用于校验 Deco 生成代码）：**

| 标准 Compose | 项目 Kuikly Compose |
|-------------|-------------------|
| `androidx.compose.foundation.*` | `com.tencent.kuikly.compose.foundation.*` |
| `androidx.compose.material3.*` | `com.tencent.kuikly.compose.material3.*` |
| `androidx.compose.ui.*` | `com.tencent.kuikly.compose.ui.*` |
| `coil.compose` | `com.tencent.kuikly.compose.coil3.rememberAsyncImagePainter(url)` |
| `NavHost` 路由 | `@Page(ComposeViewKey.xxx)` + `ComposePage` + 按用户选择接入 Struct 页面容器（StructComposePage）或 MVVM 页面（自定义 ViewModel + Screen Composable） |

---

### Step 5a：规范性改造（机械性替换，可批量执行）

> Deco 生成的代码是「纯 UI 还原」，使用原生 `Text`/`Image` 和硬编码颜色。本步骤将其改造为符合 qnView 组件规范的代码。
> **完整改造规则详见 Constitution §五·5a**，以下为执行要点。

#### 5a.1 搬运 & 改包名

将 Deco 输出的 `.kt` 文件复制到项目标准路径并修正 package：

```
package com.kuikly.generated          →  删除
package com.tencent.weishi.module.{模块名}.{功能名}.ui  →  添加
```

路径：`shared/src/commonMain/kotlin/com/tencent/weishi/module/{模块名}/{功能名}/ui/`

#### 5a.2 组件替换（逐一全局替换）

按 Constitution §五·5a.2 执行：

```kotlin
// ❌ Deco 原始
Text(text = "标题", color = Color.White, fontSize = 17.sp, ...)
Image(painter = rememberAsyncImagePainter(url), contentDescription = "图片", ...)
Spacer(modifier = Modifier.height(12.dp))

// ✅ 改造后
QnText(text = "标题", color = QNTheme.colorScheme.t4, fontSize = 17.sp, ...)
QnImage(painter = Res.drawable.img_play_icon, contentDescription = "图片", ...)
SpacerHeight(12.dp)
```

> 说明：
> - 上述 `Res.drawable.xxx` 仅针对**设计稿静态图片资源**。
> - 如果图片是业务动态数据（如封面、头像、标签图 URL），仍然使用 `rememberAsyncImagePainter(url)` / `QnImage(url = xxx)`。

#### 5a.3 import 清洗

```kotlin
// ❌ 删除这些 import
import com.tencent.kuikly.compose.material3.Text
import com.tencent.kuikly.compose.foundation.Image
import com.tencent.kuikly.compose.foundation.layout.Spacer

// ✅ 添加这些 import
import com.tencent.news.core.compose.view.QnText
import com.tencent.news.core.compose.view.QnImage
import com.tencent.news.core.compose.view.SpacerHeight
import com.tencent.news.core.compose.view.SpacerWidth
import com.tencent.news.core.compose.scaffold.theme.QNTheme
import com.tencent.news.core.resources.Res
```

#### 5a.4 颜色语义化

Deco 生成的代码使用硬编码 `Color(0xFFxxxxxx)`，需要根据下方**色值反查表**替换为 `QNTheme.colorScheme.xxx`。

**反查方式**：拿到 Deco 生成的色值 → 在下表中查找对应的日间/夜间色值 → 替换为对应的代码属性。

##### 背景色

| 设计 Token | 日间色值 | 夜间色值 | 代码属性 | 说明 |
|-----------|---------|---------|---------|------|
| bg_page | `#FFFFFF` | `#1F1F23` | `QnColor.bgPage` | 页面全局背景 |
| bg_page_grey | `#F5F5F5` | `#121212` | `QnColor.bgPageGrey` | 浅灰页面背景 |
| bg_block | `#F7F7F7` | `#262626` | `QnColor.bgBlock` | 区块背景 |
| bg_card | `#FFFFFF` | `#2A2A2A` | `QnColor.bgCard` | 卡片背景 |
| bg_bar | `#F9F9F9` | `#232327` | `QnColor.bgBar`* | 底 bar 背景 |
| bg_snackbar | `#FFFFFF` | `#2B2B2B` | `QnColor.bgSnackBar` | Snackbar 背景 |
| bg_top_light | `#EFEFEF` | `#303035` | — | 标签/气泡/toast 背景 |
| bg_middle_standard | `#F6F6F6` | `#27272B` | — | 选中类背景 |

##### 文字色

| 设计 Token | 日间色值 | 夜间色值 | 代码属性 | 说明 |
|-----------|---------|---------|---------|------|
| text_primary | `#1F1F23` | `#FFFFFF` | `QnColor.t1` | 主要文字 |
| text_secondary | `#5C5C5C` | `#A9A9A9` | `QnColor.t2` | 次要文字 |
| text_tertiary | `#999999` | `#696969` | `QnColor.t3` | 辅助文字 |
| （白色文字） | `#FFFFFF` | `#FFFFFF` | `QnColor.t4` | 纯白文字 |
| text_allwhite_secondary | `#80FFFFFF` | `#80FFFFFF` | `QnColor.white80` | 永恒白·半透明 |
| text_alldark_primary | `#1F1F23` | `#1F1F23` | — | 永恒黑·一级 |
| text_link | `#776BFF` | `#776BFF` | `QnColor.tlink`* | 紫色链接色 |
| text_link_blue | `#214CA5` | `#7A95CC` | — | 蓝色链接色 |
| text_btn_primary_disable | `#80FFFFFF` | `#80FFFFFF` | — | 紫色按钮不可点文字 |

##### 图标色

| 设计 Token | 日间色值 | 夜间色值 | 代码属性 | 说明 |
|-----------|---------|---------|---------|------|
| icon_primary | `#1F1F23` | `#FFFFFF` | `QnColor.t1` | 主要图标（同 text_primary） |
| icon_secondary | `#5C5C5C` | `#A9A9A9` | `QnColor.t2` | 次要图标 |
| icon_tertiary | `#999999` | `#696969` | `QnColor.t3` | 辅助图标 |

##### 分割线/描边

| 设计 Token | 日间色值 | 夜间色值 | 代码属性 | 说明 |
|-----------|---------|---------|---------|------|
| line_standard | `#1A000000` | `#1AFFFFFF` | `QnColor.lineFine` | 标准分割线 |
| line_light | `#0D000000` | `#0DFFFFFF` | — | 浅色分割线 |

##### 填充色

| 设计 Token | 日间色值 | 夜间色值 | 代码属性 | 说明 |
|-----------|---------|---------|---------|------|
| fill_primary | `#1A000000` | `#1AFFFFFF` | — | 主要填充 |
| fill_secondary | `#33000000` | `#33FFFFFF` | — | 次要填充 |
| fill_purple | `#33776BFF` | `#33776BFF` | — | 紫色填充（选中标签等） |

##### 品牌色

| 设计 Token | 日间色值 | 夜间色值 | 代码属性 | 说明 |
|-----------|---------|---------|---------|------|
| brand_primary | `#7642F5` | `#7642F5` | `QnColor.purpleNormal`* | 品牌紫 |
| brand_secondary | `#FF4273` | `#F02D65` | `QnColor.rNormal`* | 品牌红 |
| brand_allwhite | `#FFFFFF` | `#FFFFFF` | `QnColor.t4` | 永恒白 |

##### 按钮背景色

| 设计 Token | 日间色值 | 夜间色值 | 代码属性 | 说明 |
|-----------|---------|---------|---------|------|
| btn_primary_default | `#7642F5` | `#7642F5` | `QnColor.purpleNormal`* | 一级按钮 |
| btn_primary_disable | `#807642F5` | `#807642F5` | — | 一级按钮不可点 |
| btn_secondary_default | `#0D000000` | `#0DFFFFFF` | — | 二级按钮 |
| btn_lightbrand_default | `#ECEBFF` | `#ECEBFF` | — | 浅紫品牌色 |
| btn_tertiary_default | `#80FFFFFF` | `#0DFFFFFF` | — | 彩色背景按钮 |

##### 反馈/遮罩

| 设计 Token | 日间色值 | 夜间色值 | 代码属性 | 说明 |
|-----------|---------|---------|---------|------|
| fb_toast | `#80000000` | `#80000000` | `QnColor.shadow50` | Toast 背景 |
| fb_error | `#E6574A` | `#E6574A` | — | 错误色 |
| fb_correct | `#57BE6A` | `#57BE6A` | — | 正确色 |
| mask_20 | `#33000000` | `#33000000` | — | 黑色蒙层 20% |
| mask_50 | `#80000000` | `#80000000` | `QnColor.shadow50` | 黑色蒙层 50% |
| mask_75 | `#BF000000` | `#BF000000` | — | 黑色蒙层 75% |

> **标 `*` 的属性**：代码中的属性名与设计 Token 名不完全一致，使用时以代码属性名为准。
> **无代码属性（`—`）**：ColorScheme 中暂无对应属性，保留硬编码色值并附注释 `// 设计Token: {token_name}`，后续统一补齐。

**反查示例**：

```kotlin
// Deco 生成的硬编码色值 → 查表替换
color = Color(0xFF1F1F23)        // 日间 text_primary → QnColor.t1
color = Color(0xFF5C5C5C)        // 日间 text_secondary → QnColor.t2
color = Color(0xFF999999)        // 日间 text_tertiary → QnColor.t3
color = Color(0xFFFFFFFF)        // 白色文字 → QnColor.t4
color = Color(0xCCFFFFFF)        // 80%白 → QnColor.white80
color = Color(0xFF776BFF)        // text_link → QnColor.tlink（或保留 + 注释）
background(Color(0xFFFFFFFF))    // bg_page → QnColor.bgPage
background(Color(0xFFF7F7F7))    // bg_block → QnColor.bgBlock
background(Color(0xFFF5F5F5))    // bg_page_grey → QnColor.bgPageGrey
background(Color(0xFF7642F5))    // brand_primary → QnColor.purpleNormal
color = Color(0x33776BFF)        // fill_purple → 保留 + 注释 // 设计Token: fill_purple
```

**例外保留**：标签渐变色（`Brush.linearGradient`）和封面底部半透明遮罩渐变附注释保留。

#### 5a.5 文件拆分

按 Constitution §五·5a.5 拆分表拆分，参考已有的 FindDrama 模块拆分方式：

```
{Feature}Page.kt              ← 页面主入口 + 状态分发
{Feature}NavBar.kt            ← 顶部导航栏
{Feature}CategorySection.kt   ← 分类/筛选区
{Feature}CoverCard.kt         ← 卡片组件
{Feature}ResultContent.kt     ← 结果列表
{Feature}TopFloatBar.kt       ← 悬浮条（如有）
{Feature}CommonViews.kt       ← Loading/Error/Empty/Toast
{Feature}Resources.kt         ← 本地图片资源别名 / `Res.drawable` 引用（如有需要）
```

#### 5a.6 静态图片落本地资源

**目标**：Deco 生成后，凡是来自设计稿的**静态图片 URL**，都不直接保留为线上 URL 常量；需要下载到项目本地资源目录，再通过本地 drawable 方式引用。

**执行规则：**

1. 从 `assets-manifest.json` 和 Deco 生成的 `.kt` 文件中提取静态设计图 URL。
2. 将图片下载到当前 UI 模块的 `src/commonMain/composeResources/drawable/`（本仓默认优先放在 `wsCompose/src/commonMain/composeResources/drawable/`）。
3. 文件名统一改成可读的 snake_case，并保留正确后缀（`.png` / `.jpg` / `.webp` / `.svg`）。
4. 在 `qnView/src/commonMain/kotlin/com/tencent/news/core/resources/Res.kt` 中补充对应的 `Res.drawable.xxx` getter；如果页面有较多静态图，可在 `{Feature}Resources.kt` 中再做一层语义化别名。
5. UI 代码里把 `rememberAsyncImagePainter(url)` 这类静态图调用，替换为 `Res.drawable.xxx`（如果任务明确是 Android-only，也可落到 `R.drawable.xxx`，但默认优先跨端写法）。
6. 仅**业务动态图片**保留 URL 形式，例如：封面图、头像、角标图、服务端下发运营图。

```kotlin
// ❌ Deco 原始（静态设计图 URL）
private const val IMG_BANNER = "https://flowly-cdn.gtimg.com/.../banner.png"
private const val IMG_PLAY_ICON = "https://flowly-cdn.gtimg.com/.../play_icon.png"

QnImage(
    painter = rememberAsyncImagePainter(IMG_PLAY_ICON),
    contentDescription = "播放"
)

// ✅ 改造后（下载到本地 drawable）
// wsCompose/src/commonMain/composeResources/drawable/feature_banner.png
// wsCompose/src/commonMain/composeResources/drawable/feature_play_icon.png

// qnView/.../Res.kt
val feature_banner: Painter @Composable get() = drawable("feature_banner.png").value
val feature_play_icon: Painter @Composable get() = drawable("feature_play_icon.png").value

QnImage(
    painter = Res.drawable.feature_play_icon,
    contentDescription = "播放"
)
```

> 删除 Deco 生成的设计稿占位图（如 `IMG_IMAGE_1` ~ `IMG_IMAGE_10`）和系统状态栏图片（如 `IMG_LEVELS`）。
> 如果某张图实际是业务动态位（后续需要由接口返回），则不要下载成固定本地资源，应在 5b 阶段绑定到 `uiState.xxxUrl`。

#### 5a.7 可见性、命名、注释

- 所有 `@Composable` 函数标记 `internal`，工具函数标记 `private`
- 函数名统一 `{Feature}` 前缀（如 `FindDramaNavBar`）
- 每个 Composable 加 KDoc 注释 + `@param` 标签
- 删除 Deco 文件头大段结构注释和系统 UI（状态栏/底部 Tab 栏/Home Indicator）

#### 5a.8 删除非业务代码

删除以下 Deco 自动生成但不属于业务范围的代码：
- `StatusBarSection()` — 系统状态栏由框架处理
- `BottomTabBar()` — 底部 Tab 栏由 App 壳工程处理
- Home Indicator 安全区 — 系统框架处理
- Deco 内联的 `data class`（如 `DramaItem`）— 改用 VM 层 UIState 数据类

#### ✅ 5a 验收（Constitution §五·5a 完成验收清单）

全文搜索确认：
- 无 `Text(` 调用残留 | 无 `Image(` 调用残留 | 无 `Spacer(` 调用残留
- 无 `import ...material3.Text` | 无 `import ...foundation.Image`
- 无主题相关硬编码颜色（视觉资产渐变色除外）
- 设计稿静态图不再直接使用 CDN URL，已落到本地 `composeResources/drawable/`
- 所有 `QnImage` 有 `contentDescription` | 每文件 ≤ 200 行

---

### Step 5ar：架构重构（MVVM 或 Struct）

> Deco 生成 + 5a 规范改造后的代码是"扁平化纯 UI"，不具备分层架构。
> 本步骤将代码重构为项目标准的 **MVVM** 或 **Struct** 架构，确保逻辑与 UI 解耦。
> **核心参考文档**：`doc/开发指南/如何新增一个compose页面.md`、`doc/开发指南/如何设计一个优雅的ViewModel.md`。

#### 5ar.0 架构选型确认（MANDATORY）

在开始架构重构前，**必须先确认用户选择的架构模式**：

```
CHECK: 用户是否明确选择了架构模式？
  ├── 用户明确选择 Struct → 走 Struct 架构分支
  ├── 用户明确选择 MVVM  → 走通用 MVVM 架构分支
  └── 用户未指定 → ⛔ 必须询问用户

提示模板：
"当前页面需要进行架构重构，请选择架构模式：
1. **Struct 架构**：适合品字形结构化页面（含 TitleBar / Header / ChannelBar / 列表内容等区域），
   使用 StructPageWidget2 + DataRepo + StructComposePage 方式组织页面
2. **MVVM 架构**：适合自定义布局页面（非标准品字形），
   使用 ComposePage + 自定义 ViewModel + Composable 方式组织页面

请回复「Struct」或「MVVM」。"
```

> ⚠️ **禁止默认强制走 Struct**。未经用户确认不得自动选择任一架构模式。

---

#### 5ar.1 确定业务归属（两种架构共用）

根据功能所属领域，确定代码应放置在哪个业务模块：

| 步骤 | 说明 |
|------|------|
| **功能前缀英文名** | 确定本功能的英文前缀（后续所有类名、常量名统一使用该前缀），如 `FindDrama`、`AIQA`、`GaokaoSearch` |
| **所在业务模块** | 从 `qnAd / qnDetail / qnFeeds / qnMedia / qnUser` 中选择合适的逻辑实现模块 |
| **业务包名** | 确定包路径，如 `com.tencent.news.core.aigc.qa`、`com.tencent.news.core.search.gaokao` |

**三层模块化结构**（所有新增代码必须遵循）：

```
┌──────────────────────────────────────┐
│ 对外接口层 (qnCore)                    │  ← 纯接口、枚举值、简单 model
│  ComposeViewKey、PageArgs、IPageVM     │
├──────────────────────────────────────┤
│ 逻辑实现层 (qnUser/qnFeeds/...)       │  ← VM 实现、DataRepo、PageWidget、
│  PageWidget、DataRepo、ViewModel 实现  │     UseCase、Repository
├──────────────────────────────────────┤
│ UI 组件层 (qnCompose)                  │  ← ComposePage 入口 + Composable 组件
│  Page 入口、各 Composable UI 组件      │     仅依赖 qnCore，不直接引用逻辑实现层
└──────────────────────────────────────┘
```

> **关键约束**：qnCompose/qnView 模块**只能调用 qnCore** 的代码，**不能直接**访问逻辑实现层。
> 需要通过对应的 **Service 工厂方法**（如 `UserService.xxx.createPageVM()`）获取实例。

---

#### 5ar.2 Struct 架构重构（用户选择 Struct 时执行）

> Struct 架构适合品字形结构化页面，参考 `doc/开发指南/如何新增一个compose页面.md`。

##### 5ar.2.1 创建文件清单

| 文件 | 所在模块 | 说明 |
|------|----------|------|
| `ComposeViewKey.{Module}.{PAGE_KEY}` | qnCore | 注册页面路由 key |
| `{Feature}PageArgs.kt` | qnCore | 页面启动参数，实现 `IComposePageArgs` + `@Serializable` |
| `I{Feature}PageViewModel.kt`（如需自定义 pageVM） | qnCore | pageVM 接口，继承 `IStructPageViewModel` |
| `{Service}.xxx.create{Feature}PageVM()` 或 `create{Feature}PageWidget()` | qnCore | Service 工厂方法 |
| `{Feature}PageWidget.kt` | 逻辑实现层 | 继承 `StructPageWidget2`，传入 `StructPageConfig(dataRepo = ...)` |
| `{Feature}DataRepo.kt` | 逻辑实现层 | 实现 `IStructDataRepo` 或 `IStructDataSuspendRepo`，定义网络请求与数据解析 |
| `{Feature}PageViewModel.kt`（如需自定义 pageVM） | 逻辑实现层 | pageVM 实现类 |
| `{Feature}Page.kt` | qnCompose | 继承 `ComposePage`，`@Page` 注解，`OnSetContent` 中使用 `StructComposePage` 或 `StructComposePage4VM` |

##### 5ar.2.2 判断是否需要自定义 pageVM

```
CHECK: 页面是否有超出标准 StructPageViewModel 的特殊逻辑？
  ├── YES → 使用 StructComposePage4VM + 自定义 I{Feature}PageViewModel
  │         （参考文档 §4.1）
  └── NO  → 使用 StructComposePage + 直接传入 PageWidget
            （参考文档 §4.2，更简单）
```

##### 5ar.2.3 代码模板

**无自定义 pageVM（简单页面）：**

```kotlin
// qnCore: 路由 key
object ComposeViewKey {
    object {Module} {
        const val {PAGE_KEY} = "{module}_{page_key}"
    }
}

// qnCore: 页面参数
@Serializable
data class {Feature}PageArgs(
    val param1: String = "",
) : IComposePageArgs

// qnCore: Service 工厂方法
interface I{Module}Manager {
    fun create{Feature}PageWidget(pageArgs: {Feature}PageArgs): StructPageWidget2
}

// 逻辑实现层: PageWidget
class {Feature}PageWidget(val pageArgs: {Feature}PageArgs) : StructPageWidget2(
    pageConfig = StructPageConfig(
        dataRepo = {Feature}DataRepo(pageArgs),
    )
)

// 逻辑实现层: DataRepo
class {Feature}DataRepo(val pageArgs: {Feature}PageArgs) : IStructDataRepo {
    override fun createResetRequest(
        defaultRequest: DataRequest,
        dataEnv: StructDataEnv
    ): NetworkBuilder<IKmmKeep> {
        // 网络请求逻辑
    }
}

// qnCompose: 页面入口
@Page(ComposeViewKey.{Module}.{PAGE_KEY})
internal class {Feature}Page : ComposePage() {
    override fun sceneName() = "{Feature}"

    @Composable
    override fun OnSetContent() {
        super.OnSetContent()
        val pageArgs = rememberedPageArgs<{Feature}PageArgs>()
        StructComposePage(
            pageWidget = { {Module}Service.xxx.create{Feature}PageWidget(pageArgs) },
            pageLifecycleFlow = pageLifecycleFlow.lifecycleFlow
        )
    }
}
```

**有自定义 pageVM（复杂页面）：**

```kotlin
// qnCore: pageVM 接口
interface I{Feature}PageViewModel : IStructPageViewModel {
    // 页面特有的业务方法
    fun doSpecialAction()
}

// 逻辑实现层: pageVM 实现
class {Feature}PageViewModel(
    private val pageArgs: {Feature}PageArgs,
    pageLifecycleFlow: SharedFlow<PageLifecycleEvent>,
    pageScope: CoroutineScope,
) : StructPageViewModel(
    FeedsService.listFactory.createFlexFeedsController({Feature}PageWidget(pageArgs)),
    pageLifecycleFlow,
    pageScope
), I{Feature}PageViewModel {
    override fun doSpecialAction() { /* ... */ }
}

// qnCompose: 页面入口
@Page(ComposeViewKey.{Module}.{PAGE_KEY})
internal class {Feature}Page : ComposePage() {
    override fun sceneName() = "{Feature}"

    @Composable
    override fun OnSetContent() {
        super.OnSetContent()
        val pageScope = rememberCoroutineScope()
        val pageArgs = rememberedPageArgs<{Feature}PageArgs>()
        StructComposePage4VM({
            {Module}Service.xxx.create{Feature}PageVM(pageArgs, pageLifecycleFlow.lifecycleFlow, pageScope)
        })
    }
}
```

---

#### 5ar.3 MVVM 架构重构（用户选择 MVVM 时执行）

> MVVM 架构适合非品字形页面或自定义布局较多的场景。

##### 5ar.3.1 创建文件清单

| 文件 | 所在模块 | 说明 |
|------|----------|------|
| `ComposeViewKey.{Module}.{PAGE_KEY}` | qnCore | 注册页面路由 key |
| `{Feature}PageArgs.kt` | qnCore | 页面启动参数，实现 `IComposePageArgs` + `@Serializable` |
| `I{Feature}ViewModel.kt` | qnCore | ViewModel 接口（面向 UI 设计，不暴露数据模型和业务实现） |
| `{Feature}UiState.kt` | qnCore | UI 状态数据类，包含页面展示所需的所有字段 |
| `{Feature}Action.kt` | qnCore | 用户交互事件定义（sealed class 或 sealed interface） |
| `{Service}.xxx.create{Feature}VM()` | qnCore | Service 工厂方法 |
| `{Feature}ViewModel.kt` | 逻辑实现层 | ViewModel 实现类，处理业务逻辑 |
| `{Feature}Repository.kt`（可选） | 逻辑实现层 | 数据仓库，封装网络请求 |
| `{Feature}Page.kt` | qnCompose | 继承 `ComposePage`，`@Page` 注解 |
| `{Feature}Screen.kt` | qnCompose | 页面级 Composable，消费 UiState、分发 Action |
| `{Feature}xxx.kt` (各子组件) | qnCompose | 从 5a 拆分的 UI 子组件 |

##### 5ar.3.2 ViewModel 接口设计原则（面向 UI）

> **核心原则**：ViewModel 接口应面向 UI 设计，不面向业务设计。
> 详见 `doc/开发指南/如何设计一个优雅的ViewModel.md`。

**ViewModel 接口中应该包含的：**
1. 固定的属性：基础数据类型（`String`、`Int`、`Boolean`）
2. 可变的属性：统一用 `StateFlow` 或 `SharedFlow` 包装
3. 与 UI 层交互的 `fun`（如 `onItemClick()`、`onRefresh()`）
4. 其他子组件的 VM

**ViewModel 接口中不应该出现的：**
1. ❌ 数据结构 model 类（如 `IUserInfo`、`IFeedsItem`）→ 应转化为 UI 需要的基础类型
2. ❌ 业务层的 controller、presenter
3. ❌ `var` 可变属性（修改逻辑在实现类中执行）
4. ❌ `MutableStateFlow`、`MutableSharedFlow`（更新在实现类中执行）

```kotlin
// ✅ 面向 UI 设计的 ViewModel 接口
interface I{Feature}ViewModel {
    val uiState: StateFlow<{Feature}UiState>  // UI 状态
    fun onAction(action: {Feature}Action)      // 统一 Action 分发
}

// ✅ UI 状态（只包含 UI 需要的数据，不暴露业务模型）
data class {Feature}UiState(
    val isLoading: Boolean = true,
    val title: String = "",
    val items: List<{Feature}ItemUiState> = emptyList(),
    val errorMessage: String? = null,
)

// ✅ 用户交互事件
sealed interface {Feature}Action {
    data object Refresh : {Feature}Action
    data class OnItemClick(val itemId: String) : {Feature}Action
    data object LoadMore : {Feature}Action
}
```

##### 5ar.3.3 代码模板

```kotlin
// qnCompose: 页面入口
@Page(ComposeViewKey.{Module}.{PAGE_KEY})
internal class {Feature}Page : ComposePage() {
    override fun sceneName() = "{Feature}"

    @Composable
    override fun OnSetContent() {
        super.OnSetContent()
        val pageScope = rememberCoroutineScope()
        val pageArgs = rememberedPageArgs<{Feature}PageArgs>()
        val viewModel = remember {
            {Module}Service.xxx.create{Feature}VM(pageArgs, pageScope)
        }
        {Feature}Screen(viewModel = viewModel)
    }
}

// qnCompose: 页面级 Composable
@Composable
internal fun {Feature}Screen(viewModel: I{Feature}ViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    when {
        uiState.isLoading -> {Feature}LoadingView()
        uiState.errorMessage != null -> {Feature}ErrorView(
            warnText = uiState.errorMessage!!,
            onRetryClick = { viewModel.onAction({Feature}Action.Refresh) }
        )
        else -> {Feature}Content(
            uiState = uiState,
            onAction = viewModel::onAction
        )
    }
}

// qnCompose: 内容区 Composable
@Composable
internal fun {Feature}Content(
    uiState: {Feature}UiState,
    onAction: ({Feature}Action) -> Unit
) {
    // 使用 5a 拆分的子组件组装页面
    // 所有交互通过 onAction 回调，不直接调用业务逻辑
}
```

---

#### 5ar.4 将 5a 产出物搬入架构目录

5a 步骤产出的 UI 组件文件（`{Feature}NavBar.kt`、`{Feature}CoverCard.kt` 等）需要搬运到最终的项目目录结构中：

**Struct 架构目录结构：**
```
qnCore/src/commonMain/kotlin/com/tencent/news/core/{业务包路径}/
├── ComposeViewKey 中注册路由 key
├── {Feature}PageArgs.kt
├── I{Feature}PageViewModel.kt（如需自定义 pageVM）
└── {Service} 中添加工厂方法

{逻辑实现模块}/src/commonMain/kotlin/com/tencent/news/core/{业务包路径}/
├── {Feature}PageWidget.kt
├── {Feature}DataRepo.kt
└── {Feature}PageViewModel.kt（如需自定义 pageVM）

qnCompose/src/commonMain/kotlin/com/tencent/news/core/compose/{功能目录}/
├── {Feature}Page.kt            ← ComposePage 入口
├── {Feature}NavBar.kt          ← 5a 产出的子组件
├── {Feature}CoverCard.kt       ← 5a 产出的子组件
├── {Feature}CommonViews.kt     ← Loading/Error/Empty
└── ...
```

**MVVM 架构目录结构：**
```
qnCore/src/commonMain/kotlin/com/tencent/news/core/{业务包路径}/
├── ComposeViewKey 中注册路由 key
├── {Feature}PageArgs.kt
├── I{Feature}ViewModel.kt
├── {Feature}UiState.kt
├── {Feature}Action.kt
└── {Service} 中添加工厂方法

{逻辑实现模块}/src/commonMain/kotlin/com/tencent/news/core/{业务包路径}/
├── {Feature}ViewModel.kt
└── {Feature}Repository.kt（可选）

qnCompose/src/commonMain/kotlin/com/tencent/news/core/compose/{功能目录}/
├── {Feature}Page.kt            ← ComposePage 入口
├── {Feature}Screen.kt          ← 页面级 Composable
├── {Feature}NavBar.kt          ← 5a 产出的子组件
├── {Feature}CoverCard.kt       ← 5a 产出的子组件
├── {Feature}CommonViews.kt     ← Loading/Error/Empty
└── ...
```

---

#### ✅ 5ar 验收清单

| 检查项 | Struct | MVVM | 说明 |
|--------|:------:|:----:|------|
| 页面路由 `ComposeViewKey.{Module}.{PAGE_KEY}` 已注册 | ✅ | ✅ | |
| `PageArgs` 实现 `IComposePageArgs` + `@Serializable` | ✅ | ✅ | |
| Service 工厂方法已定义 | ✅ | ✅ | qnCompose 通过 Service 获取实例 |
| qnCompose 不直接 import 逻辑实现层的类 | ✅ | ✅ | **编译期拦截** |
| `ComposePage` 入口类 + `@Page` 注解 | ✅ | ✅ | |
| PageWidget + DataRepo 已创建 | ✅ | — | |
| StructComposePage / StructComposePage4VM 正确使用 | ✅ | — | |
| ViewModel 接口面向 UI 设计（无业务模型暴露） | — | ✅ | |
| UiState + Action 定义完整 | — | ✅ | |
| ViewModel 实现类中处理所有业务逻辑 | — | ✅ | |
| Screen 级 Composable 消费 UiState、分发 Action | — | ✅ | |
| Loading / Error / Success 三态框架就绪 | ✅ | ✅ | |
| 所有文件 package 路径正确 | ✅ | ✅ | |

---

### Step 5b：业务绑定（需理解业务逻辑）

> 将 5a 改造 + 5ar 架构重构后的代码与实际业务逻辑绑定。
> 本步骤承接 5ar 已确定的架构模式（Struct 或 MVVM），将静态 UI 中的硬编码数据替换为动态数据，
> 将空的交互回调绑定为实际的 Action/事件处理。
>
> **核心参考文档**：
> - `doc/开发指南/如何新增一个compose页面.md`（页面入口与路由接入）
> - `doc/开发指南/如何设计一个优雅的ViewModel.md`（VM 接口设计原则）
> - `doc/开发指南/如何新增一个composeCell.md`（Cell 组件开发模式）
> - `doc/开发指南/如何新增一个composeWidget组件.md`（Widget 组件开发模式）

#### 5b.0 架构模式确认

> 架构选型已在 **Step 5ar** 中完成。此处直接沿用 5ar 的选择结果：
> - **Struct 架构** → 执行下方 Struct 分支（5b.1 ~ 5b.2 Struct 部分）
> - **MVVM 架构** → 执行下方 MVVM 分支（5b.2 MVVM 部分 ~ 5b.4）

#### 5b.1 UI 区域分析（仅当用户选择 Struct 时执行）

只有在用户明确选择 Struct 时，才需要分析设计稿中各 UI 区域应该对应品字形的哪个 Widget 槽位。参考 `FindDramaPageDataRepo.kt` 中 `buildPageWithManual {}` 的写法：

| 设计稿中的 UI 区域 | 对应 Widget 槽位 | 说明 | FindDrama 示例 |
|------------------|----------------|------|---------------|
| 顶部导航栏（返回按钮 + 标题 + 操作按钮） | `titleBar` | 使用 `CommonTitleBarWidget`，可配置固定/联动模式 | `CommonTitleBarWidget.createFixTopStyle(title = "找剧")` |
| 列表上方的固定区域（Banner、分类筛选、搜索栏等） | `header` | 随列表滚动，滚出屏幕后折叠；继承 `StructVMHeaderWidget` 或用 `VMWrapperHeaderWidget` 包装 VM | `FindDramaCategoryHeaderWidget`（分类筛选区） |
| 折叠后出现的悬浮条（吸顶筛选、精简导航等） | `titleHanging` / `hanging` | Header 折叠后悬停在 TitleBar 下方；`titleHanging` 浮层形式，`hanging` 跟随列表 | `FindDramaCategoryHangingWidget`（悬浮筛选条） |
| Tab 切换栏（多频道/多分类切换） | `pager.channelBar` + `pager.channels` | 使用 `ChannelBarWidget` + 多个 `ChannelWidget`，框架自动处理 HorizontalPager 切换 | — |
| 主体列表/网格内容 | `pager.mainChannel.content` | 通过 `buildPageWithItemList()` 或手动构建 `NewsListWidget` | 三列网格短剧列表 |
| 底部操作栏（评论、分享、收藏等按钮） | `bottomBar` | 使用 `BottomBarWidget`，内部放各种 `StructBtnWidget` | — |
| 悬浮按钮/入口（右下角浮层、游戏挂件等） | `layers` | 使用 `LayersWidget.buildBtnList()` | — |
| 页面背景图/动效 | `bg` | 全屏背景，位于所有内容之下 | — |

**分析步骤**：
1. 对照设计稿，逐个识别页面中的 UI 区域
2. 按上表将每个区域归类到对应的 Widget 槽位
3. 对于不确定的区域，优先放到 `header`（可滚动折叠）或 `content`（主体列表）中
4. 在 DataRepo 的 `buildPageWithManual {}` 中按映射结果赋值各槽位

**输出格式**：完成分析后，以表格形式输出映射结果，格式如下：

```
| 设计稿 UI 区域 | 对应 Widget 槽位 | 对应 5a 文件 / Composable | 说明 |
|--------------|-----------------|-------------------------|------|
| ...          | ...             | ...                     | ...  |
```

> ⛔ **用户确认检查点（MANDATORY，仅 Struct 分支）**
>
> 完成 5b.1 页面区域分析后，**必须暂停流程**，将分析结果展示给用户并等待确认。
>
> **输出模板**：
> ```
> 📋 页面区域分析结果（Step 5b.1）
>
> {上述映射表格}
>
> 请 review 以上区域映射是否正确：
> - 各 UI 区域是否识别完整？是否有遗漏或多余的区域？
> - Widget 槽位归类是否合理？
> - 对应的文件 / Composable 拆分是否合适？
>
> ✅ 确认无误请回复「继续」或「确认」，我将继续执行后续 Struct 绑定步骤
> ✏️ 如需调整请说明具体修改意见，我将更新后重新确认
> ```
>
> **必须等待用户明确回复后，才能继续执行后续 Struct 步骤。禁止自动跳过此检查点。**

#### 5b.2 页面入口与数据绑定

> 5ar 已创建好架构骨架（PageWidget/DataRepo/ViewModel 等），本步骤将 5a 的静态 UI 组件与架构层连接。

根据 5ar 已确定的架构模式执行对应分支：

- **Struct 分支**：
  1. 在 DataRepo 的 `createResetRequest` / `buildPageWithManual {}` 中实现实际的网络请求逻辑
  2. 配置 `StructPageConfig` 中的选项（`fixTitleBarAboveContent`、`forceHideHeaderArea` 等）
  3. 如有自定义 pageVM，在实现类中补充业务方法逻辑
  4. 确保 Service 工厂方法已在对应的 Manager 实现类中注册

- **MVVM 分支**：
  1. 在 ViewModel 实现类中实现各 Action 的处理逻辑（网络请求、状态更新等）
  2. 在 Repository 中封装网络请求（如使用 `quickRequest`）
  3. 在 Screen 级 Composable 中使用 `collectAsState()` 消费 UiState
  4. 确保 Service 工厂方法已在对应的 Manager 实现类中注册

#### 5b.3 数据动态化

- Deco 硬编码文本 → `uiState.xxx` 字段（固定文案如"找剧"可保留）
- Deco 硬编码列表 `listOf(DramaItem(...))` → `uiState.dramaList`
- Deco 硬编码图片 URL → `dramaData.coverUrl`
- 空的 `clickable { }` → `clickable { onAction(XxxAction.OnItemClick(item)) }`

#### 5b.4 交互事件绑定

为每个交互组件绑定对应的 Action：

| UI 交互 | 绑定方式 |
|---------|---------|
| 返回按钮 | `onBackClick: () -> Unit` → 路由返回 |
| 列表项点击 | `onClick: () -> Unit` → `dispatchAction(OnItemClickAction(item))` |
| 分类切换 | `onCategorySelect: (String) -> Unit` → `dispatchAction(OnCategorySelectAction(...))` |
| 下拉刷新 | `onRefresh` → `dispatchAction(RefreshAction)` |
| 上拉加载 | `onLoadMore` → `dispatchAction(LoadMoreAction)` |

#### 5b.5 补充通用视图

在 `{Feature}CommonViews.kt` 中添加：

```kotlin
@Composable internal fun {Feature}LoadingView() { ... }
@Composable internal fun {Feature}ErrorView(warnText: String, onRetryClick: () -> Unit) { ... }
@Composable internal fun {Feature}EmptyView() { ... }
@Composable internal fun {Feature}WarnToast(message: String, onDismiss: () -> Unit) { ... }
@Composable internal fun {Feature}LoadMoreIndicator() { ... }
```

#### ✅ 5b 验收

- **Struct 分支**：
  - DataRepo 网络请求逻辑已实现
  - StructPageConfig 配置项已正确设置
  - Service 工厂方法已在 Manager 实现类中注册
  - 如有自定义 pageVM，业务方法逻辑已实现
- **MVVM 分支**：
  - ViewModel 实现类中所有 Action 处理逻辑已实现
  - Repository 网络请求已封装
  - Screen Composable 正确消费 UiState、分发 Action
  - Service 工厂方法已在 Manager 实现类中注册
- **两条分支共同要求**：
  - 所有 UIState 字段都有对应 UI 展示
  - 所有可交互组件都触发了对应 Action
  - Loading / Error / Success 三态切换完整
  - 无 Deco 内联 `data class`（改用 VM 层数据类）
  - 无空的 `clickable { }` 回调
  - qnCompose 中无直接 import 逻辑实现层的类

---

### Step 6：验证 UI 与 VM 接口绑定

检查生成的 UI 代码是否正确：

1. ✅ 所有 UIState 中的字段都有对应的 UI 展示
2. ✅ 所有互动组件都触发了对应的 Action
3. ✅ 所有动态组件都根据 State 字段正确控制显示/隐藏
4. ✅ Loading/Error/Success 三态切换完整
5. ✅ import 路径全部为 `com.tencent.kuikly.compose.*`
6. ✅ 设计稿静态图片已下载到本地 drawable，并通过 `Res.drawable.xxx` / 本地 drawable 方式引用
7. ✅ 仅业务动态图片继续保留 URL 形式

---

### Step 7：预览验证（可选）

使用 Deco 的预览功能对比设计稿与代码渲染效果：

```bash
# 在 deco 交互模式中
> /preview path/to/ComponentName.kt

# 对比设计稿与代码渲染
> /compare <figma-url> --code path/to/ComponentName.kt
```

---

### Step 8：更新 component-map 并回填设计稿链接

#### 8.1 回填 Figma 链接到需求文档

若本次执行提供了 Figma 链接（含 `node-id` 参数），将其回填到对应页面的需求文档中：

1. 打开 `docs/component` 下当前页面的需求文档（`.md` 文件）
2. 在**页面简介**（`**页面简介：...`）后面追加设计稿链接
3. 若只有一个链接，使用单链接格式；若有多个链接，按列表逐条回填并标注对应状态

   **单链接格式：**
   ```
   **设计稿：** [Figma 链接]({figma_url})
   ```

   **多链接格式：**
   ```
   **设计稿：**
   - 默认态：[Figma 链接 A]({figma_url_a})
   - 选中态：[Figma 链接 B]({figma_url_b})
   - 展开态：[Figma 链接 C]({figma_url_c})
   ```

   例如：
   ```
   **页面简介：用户通过该页面可以按多维分类筛选短剧，并在结果网格中浏览和进入短剧详情播放。**

   **设计稿：**
   - 默认态：[Figma 链接](https://www.figma.com/design/ABC123/WeSeeDrama?node-id=640-4637)
   - 空态：[Figma 链接](https://www.figma.com/design/ABC123/WeSeeDrama?node-id=640-5000)
   ```

#### 8.2 更新 component-map

在 `docs/component-map.md` 中更新当前页面对应的代码类名：

1. 读取 `docs/component-map.md`，找到当前页面对应的行（按 `组件id` 匹配）
2. **若已有记录**：将「组件代码」列更新为本次实现的页面入口类名（如 `EditPage`、`FindDramaPage`）
3. **若无记录**：追加一行，格式为：
   ```
   | 页面 | {页面名驼峰} | component/{模块名}/{页面名驼峰}/{页面名下划线}.md | {页面入口类名} |
   ```
   例如：`| 页面 | findDramaPage | component/drama/findDramaPage/find_drama_page.md | FindDramaPage |`

> 页面入口类名即继承 `ComposePage` 的类，如 `FindDramaPage`、`EditPage`。

---

## 流程总览

```
                    ┌───────────────────────┐
                    │  Step 0: 选择执行模式   │
                    └───────────┬───────────┘
           ┌────────────────────┼────────────────────┐
           ▼                    ▼                    ▼
  ✏️ Path I: 迭代      🔄 Path M: 迁移       🆕 Path A: Deco
  ┌─────────────┐     ┌─────────────┐       ┌─────────────┐
  │I1: 读基线+diff│     │M1: 定位旧代码│       │1: 环境检查   │
  │I2: 定位UI文件│     │M2: 搬运到新  │       │2: 获取Figma  │
  │I3: 执行修改  │     │M3: 适配Kuikly│       │3: Deco生成   │
  └──────┬──────┘     │M4: Deco补差异│       │4: 读取UI规范 │
         │            │M5: 绑定新VM  │       │5a: 规范性改造│
         │            └──────┬──────┘       │5ar: 架构重构 │
         │                   │              │5b: 业务绑定  │
         │                   │              └──────┬──────┘
         │                   │                     │
         └───────────────────┼─────────────────────┘
                             ▼
              ┌────────────────────────────────┐
              │  Step 6: 验证 UI ↔ VM 绑定     │
              └───────────────┬────────────────┘
                              ▼
              ┌────────────────────────────────┐
              │  Step 7: 预览验证（可选）        │
              └───────────────┬────────────────┘
                              ▼
              ┌────────────────────────────────┐
              │  Step 8: 回填设计稿链接          │
              │         + 更新 component-map    │
              └────────────────────────────────┘
```

---

## 示例调用

### 示例 1：有 Figma 链接（推荐流程）

**用户输入：**
> 根据找剧页设计稿还原 UI
> Figma: https://www.figma.com/design/ABC123/WeSeeDrama?node-id=640-4637
> ViewModel 接口在 FindDramaViewModel.kt 中

**执行流程：**
1. 检查环境：`node -v` ✅ / `deco --version` ✅ / Deco 登录状态 ✅
2. 验证 Figma URL 包含 node-id ✅
3. 执行 `deco to-kuikly --remote "https://www.figma.com/design/ABC123/WeSeeDrama?node-id=640-4637" --image-scale 3 -o ./qnView/deco-output`
4. 读取项目 UI 规范
5. 将 Deco 输出的 `.kt` 搬运到 `shared/.../find_drama/ui/`
6. 拆分为 8 个组件文件（Page、NavBar、CategorySection、CoverCard 等）
7. 修复 import 路径为 `com.tencent.kuikly.compose.*`
8. 绑定 FindDramaViewModel 接口
9. 从 `assets-manifest.json` 提取静态资源并下载到 `composeResources/drawable/`，补充 `Res.drawable.xxx`
10. 验证所有 UIState 字段和 Action 已绑定

### 示例 2：Remote 模式（无本地 Figma）

**用户输入：**
> 还原设计稿 https://www.figma.com/design/ABC123/WeSeeDrama?node-id=640-4637
> 我没有安装 Figma Desktop

**执行流程：**
1. 使用 remote 模式（无需本地 Figma Desktop）
2. 执行 `deco to-kuikly --remote "https://www.figma.com/design/ABC123/WeSeeDrama?node-id=640-4637" --image-scale 3 -o ./qnView/deco-output`
3. 后续流程与示例 1 的 Step 4~10 相同

### 示例 3：迁移模式（从旧项目迁移已有代码）

**用户输入：**
> 找剧页旧项目已经有 ViewModel 和 UI 代码了，在 /path/to/old-project/feature/find_drama/ 下
> 新版设计稿 Figma: https://www.figma.com/design/ABC123/WeSeeDrama?node-id=640-4637
> 需求文档在 docs/component/short_drama/find_drama.md

**执行流程（Path M）：**
1. **M1 定位旧代码**：读取 `/path/to/old-project/feature/find_drama/` 下所有文件，理解数据模型、UI 层级、业务逻辑
2. **M2 搬运**：将旧 ViewModel / UI / Repository 复制到 `shared/.../module/short_drama/find_drama/`
3. **M3 适配 Kuikly Compose**：
   - `androidx.compose.*` → `com.tencent.kuikly.compose.*`
   - `Activity` + `setContent` → `ComposePage` + 按用户选择接入 Struct 页面容器（StructComposePage）或 MVVM 页面
   - `AsyncImage` → `rememberAsyncImagePainter`
   - `navController.navigate()` → `RouterModule.openPage()`
   - 处理 Android 特定 API → KMM `expect/actual`
4. **M4 Deco 补差异**：对比新旧设计稿，新增的 UI 部分用 Deco 生成并合并
5. **M5 绑定新 VM**：比对旧 UIState/Action 与新定义，批量替换字段名，添加新字段展示
6. **Step 6 验证**：检查所有 UIState 字段和 Action 绑定完整
7. **Step 7 预览**：`/compare` 对比新设计稿与代码渲染
8. **Step 8 更新**：回填 Figma 链接到需求文档 + 更新 component-map

### 示例 4：迁移模式（旧代码是 XML View，非 Compose）

**用户输入：**
> 旧项目播放页用的 XML + Fragment，代码在 /path/to/old-project/player/
> 新版要迁移到 Kuikly Compose

**执行流程（Path M）：**
1. **M1 定位旧代码**：读取 XML 布局文件 + Fragment/Activity + ViewModel，理解 UI 结构和业务逻辑
2. **M2 搬运**：只搬运 ViewModel / 数据模型 / Repository（UI 层需重写，XML 无法直接迁移）
3. **M3 适配**：
   - ViewModel 基类从 `AndroidViewModel` → 项目 VM 模式
   - LiveData → StateFlow
   - XML 布局 → 参考设计稿用 Compose 重写（配合 Deco）
4. **M4 Deco 生成 UI**：用 Deco 从设计稿全量生成 Compose UI（因为 XML 无法复用）
5. **M5 绑定**：将 Deco 生成的 UI 与迁移后的 ViewModel 绑定
6. 后续 Step 6~8 同上
