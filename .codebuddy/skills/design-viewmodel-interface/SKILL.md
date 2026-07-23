---
name: design-viewmodel-interface
description: Use when 用户需要根据需求文档和设计稿设计页面的 ViewModel 接口骨架（面向 UI 设计），或在迭代场景下补充 VM 接口变更。
---

# ViewModel 接口设计（面向 UI 设计）

## 目标

根据评审通过的需求文档和设计稿，按照**面向 UI 设计**的原则，自动生成 ViewModel 接口骨架代码。

**核心原则**：VM 接口面向 UI 设计，而非面向业务设计。UI 层只消费 VM 接口暴露的属性和方法，不感知任何业务
model、controller 或数据结构。

### 三层架构分离

| 层次 | 模块 | 职责 | 代码比例 |
|------|------|------|----------|
| 接口层 | qnCore / wsCore | 纯 KMM 接口定义，绝对干净，引用不到任何 model / controller | ~5% |
| 实现层 | 业务模块（wsDrama / wsFeeds / wsUser 等） | VM 实现类、业务逻辑、路由、上报、日志、数据转换 | ~90% |
| UI 层 | qnCompose / wsCompose | 仅依赖接口层，消费 VM 接口渲染 UI | ~5% |

**编译期约束**：
- 接口层保持绝对干净，该模块引用不到任何数据结构、controller
- UI 层只能依赖接口层，访问不到实现层
- 所有 model 类、工具方法、controller 逻辑全部在实现层，通过依赖注入注入给接口层

---

## 触发条件

用户提供以下输入时触发本 skill：

- 评审通过的需求文档（`docs/component` 目录下的 `.md` 文件路径）
- 设计稿（可选，提供更准确的 UI 状态定义）

---

## 输入

| 参数       | 说明                         | 是否必须   |
|----------|----------------------------|--------|
| 需求文档     | 评审通过的需求文档，包含页面清单、组件清单、交互逻辑 | ✅ 必须   |
| 设计稿      | UI 设计稿，用于辅助确定 UI 状态和数据结构   | 可选     |
| 已有 VM 代码 | 已有页面的 VM 接口代码              | 迭代模式必须 |

---

## 模式判断

```
CHECK: 当前页面目录下是否存在 diff/ 子目录，且其中包含 {页面名下划线}_diff.md？
  ├── YES → ✏️ 迭代模式：读取已有 VM 接口，只输出变更的属性/方法/子 VM
  └── NO  → 🆕 新建模式：全量生成完整 VM 接口骨架
```

---

## 核心设计规范

### 一个 VM 接口应该包含以下 4 个方面

1. **固定的属性**：基础数据类型（String、Int、Boolean 等），不会变化的数据
2. **可变的属性**：统一用 `StateFlow` 或 `SharedFlow` 进行包装，供 UI 侧做状态监听
3. **与 UI 层交互的方法 fun**：以 `onXxx()` 命名，隐藏所有业务逻辑
4. **其他子组件的 VM**：将页面拆分为多个子 VM，每个子 VM 负责一个独立的 UI 区域

### VM 接口中不应该出现什么

1. **❌ 数据结构 model 类**（如 `IUserInfo`、`IAdOrder`、`EpisodeGridItem`）：出现这个就说明 UI 与数据结构强耦合了。所有业务数据必须在 VM 实现层完成转换后，以基础类型或子 VM 接口暴露给 UI
2. **❌ 业务层的 controller、presenter**：出现这个就说明 UI 与逻辑强耦合了
3. **❌ var 可变属性**：可变属性应该由 StateFlow 提供，供 UI 侧做状态监听
4. **❌ MutableStateFlow、MutableSharedFlow**：flow 的更新应该在 VM 实现类中执行，不应该暴露在 VM 接口中
5. **❌ sealed class UIState（Loading/Error/Success）**：不要额外封装私有的 UIState 结构，应该将状态拆分为独立的属性和子 VM
6. **❌ sealed interface Action**：不要定义 Action 分发模式，交互方法直接以 `onXxx()` 暴露在接口上
7. **❌ data class 定义在接口文件中**：接口文件中不应该定义 data class 作为属性类型，列表项应设计为子 VM 接口
8. **❌ 不必要的父接口继承**：VM 接口不应继承与其职责无关的父接口（如内容 VM 不应继承 `IStructWidgetVM`）

### 交互方法参数约束

交互方法的参数设计必须遵循以下规则：

- **参数只传 UI 层已知的值**：如 `index`、`tag`（文本）、`position` 等 UI 层本身就持有的信息
- **不要把业务参数传给 UI 层再传回来**：如 `fun onEpisodeClick(feedId: String)` 是错误的，`feedId` 是业务数据，UI 层不应该感知
- **正确做法**：将业务数据内聚在子 VM 实现类中，交互方法不需要参数或只需 UI 层已知的参数

```kotlin
// ❌ 错误：UI 层不应该知道 feedId
fun onEpisodeClick(feedId: String, episodeNum: Int)

// ✅ 正确方案 A：子 VM 内聚业务数据，无参数
interface IEpisodeItemVM {
    val episodeNum: Int
    fun onClick()  // 实现类内部持有 feedId，直接处理
}

// ✅ 正确方案 B：只传 UI 层已知的索引
fun onEpisodeClick(index: Int)  // 实现类通过 index 查找对应的业务数据
```

### MutableStateFlow 简化写法约束（面向实现层）

> 此约束在接口设计阶段提前告知，确保实现层遵循：

- 接口声明 `StateFlow<T>`，实现类直接 `override val xxx = MutableStateFlow(...)` 即可
- **无需拆成** `private val _xxx` + `override val xxx` 两行
- 这是因为 `MutableStateFlow` 是 `StateFlow` 的子类型，Kotlin 允许用子类型覆盖父类型声明

```kotlin
// 接口层
interface IMyVM {
    val isLoading: StateFlow<Boolean>  // 声明为只读 StateFlow
}

// ✅ 实现层（简化写法）
class MyVM : IMyVM {
    override val isLoading = MutableStateFlow(false)  // 一行搞定
}

// ❌ 实现层（冗余写法，禁止）
class MyVM : IMyVM {
    private val _isLoading = MutableStateFlow(false)
    override val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
}
```

---

## 输出

以下 Kotlin 代码文件（放在 `wsCore` 接口层）：

- **页面级 VM 接口**：`I{PageName}PageVM`，包含固定属性、可变属性、交互方法、子 VM
- **子组件 VM 接口**：`I{ComponentName}VM`，每个独立 UI 区域一个子 VM 接口

---

## 执行步骤

### Step 0：模式判断

- **新建模式**：页面目录下不存在 `diff/` 子目录或 diff 需求文档，直接执行 Step 1
- **迭代模式**：页面目录下存在 `diff/{页面名下划线}_diff.md`，先读取已有页面的 VM 接口代码，再结合 diff
  需求分析变更部分，只输出需要新增或修改的属性/方法/子 VM

**迭代模式输出规则：**

- 对已有 VM 接口：只说明新增哪些属性、方法、子 VM，修改哪些，不重新输出完整接口
- 以代码差异的形式输出，明确标注「新增」或「修改」
- **不修改**已有的属性和方法，不重新输出完整文件

---

### Step 1：读取需求文档，提取关键信息

1. 读取 `docs/component/{模块名}/{页面名驼峰}/{页面名下划线}.md`
2. 提取以下信息：
    - **组件清单**：所有 UI 组件及其类型
    - **互动组件**：用户可交互的组件 → 对应 `onXxx()` 方法
    - **动态组件**：受数据控制的组件 → 对应 VM 接口中的属性（固定属性或 StateFlow 属性）
    - **独立 UI 区域**：可拆分为子 VM 的区域（如列表项、头部区域、底部操作栏等）
    - **页面生命周期**：进入页面、加载成功/失败 → 对应页面级状态属性

---

### Step 2：参考项目现有 ViewModel 模式

搜索项目中已有的 ViewModel 接口，了解项目规范：

1. 查看 `wsCore` 模块中已有的 VM 接口定义（`I{Xxx}VM` / `I{Xxx}PageVM`）
2. 参考已有 VM 接口的面向 UI 设计模式
3. 确认 VM 接口放置在 `wsCore` 接口层，实现类放在业务模块中

**项目 ViewModel 架构模式总结：**

- VM 接口定义在 `wsCore`（接口层），面向 UI 设计
- VM 实现类在业务模块（如 `wsDrama`、`wsUser`），面向业务实现
- UI 组件在 `wsCompose`（UI 层），只依赖 VM 接口
- 接口层、实现层、UI 层的代码比例期望为 1:8:1

---

### Step 3：拆分子 VM 组件

根据需求文档中的组件清单和 UI 区域，将页面拆分为多个子 VM：

**拆分原则：**

- 每个独立的 UI 区域对应一个子 VM 接口
- 列表中的每个卡片/项对应一个子 VM 接口
- 可复用的 UI 组件（如用户头像、操作栏）对应独立的子 VM 接口
- 子 VM 可以被不同业务场景复用（如用户头像 VM 可被普通用户、广告主等不同实现类复用）

**示例：一个短剧播放页的子 VM 拆分**

```
I{PageName}PageVM（页面级）
├── I{PlayerArea}VM（播放器区域）
├── I{EpisodePanel}VM（选集面板）
│   └── List<I{EpisodeItem}VM>（选集项）
├── I{CommentArea}VM（评论区域）
│   └── List<I{CommentItem}VM>（评论项）
└── I{BottomBar}VM（底部操作栏）
```

---

### Step 4：设计 VM 接口

#### 4.1 页面级 VM 接口

```kotlin
/**
 * {页面名称}页面 ViewModel 接口
 * 定义在 wsCore 接口层
 */
interface I {PageName }PageVM {

    // ── 1. 固定属性 ──
    val pageTitle: String

    // ── 2. 可变属性（StateFlow 包装）──
    val isLoading: StateFlow<Boolean>
    val errorMessage: StateFlow<String?>

    // ── 3. 与 UI 层交互的方法 ──
    fun onPageLoad()
    fun onRefresh()

    // ── 4. 子组件 VM ──
    val { componentA } VM : I { ComponentA } VM
    val { componentB } VM : I { ComponentB } VM
    val { itemList }: StateFlow<List<I{ ItemName } VM > >
}
```

#### 4.2 子组件 VM 接口

```kotlin
/**
 * {组件名称} ViewModel 接口
 * 面向 UI 设计：只暴露 UI 需要的属性和交互方法
 */
interface I {ComponentName }VM {

    // ── 1. 固定属性（UI 直接消费的基础类型）──
    val title: String
    val iconUrl: String
    val isVisible: Boolean

    // ── 2. 可变属性 ──
    val isSelected: StateFlow<Boolean>

    // ── 3. 交互方法（隐藏所有业务逻辑）──
    fun onClick()
}
```

#### 4.3 面向 UI 设计的关键要点

**属性设计：**

- 属性类型必须是 UI 可直接消费的基础类型（String、Int、Boolean、Color 等）
- 不暴露业务 model 类（如 `IUserInfo`、`IAdOrder`）
- 需要从业务 model 中提取的字段，在 VM 实现类中完成转换

**方法设计：**

- 方法命名以 `onXxx()` 开头，表达 UI 事件语义
- 方法内部隐藏所有业务逻辑（跳转、上报、日志等）
- **参数只传 UI 层已知的值**（如 `index`、`tag` 文本），不要把业务参数传给 UI 层再传回来
- 正确做法：`fun onIconClick()`，在实现类中自行持有业务数据
- 当列表项有独立子 VM 时，优先将交互方法放在子 VM 接口上（如 `IEpisodeItemVM.onClick()`），而非父 VM 上传 feedId

**子 VM 设计：**

- 子 VM 接口同样遵循面向 UI 设计原则
- 子 VM 可以被不同业务场景的实现类复用
- 列表项的子 VM 通过 `StateFlow<List<I{Item}VM>>` 暴露
- **子 VM 交互逻辑内聚**：子 VM 实现类应直接持有业务数据（如 `feedId`、`schema`），在交互方法内部直接处理路由跳转、上报等逻辑，不通过 `onClickAction: () -> Unit` 回调与父 VM 耦合
- **空白占位项**：列表中如需空白占位格，应设计为子 VM 接口的特殊实现（如 `object EmptyItemVM : IItemVM`），而非在 data class 中加 `isEmpty` 字段

**model 类作为 VM 实现类构造函数入参（面向实现层）：**

> 此约束在接口设计阶段提前告知，确保实现层遵循：

- 需要用到的 model 类（如 `PageArgs`、`DramaInfo`）作为 VM 实现类的构造函数入参
- 不要在 Widget 层拆解 model 后传入多个基础类型参数
- VM 实现类内部自行从 model 中提取并转换为 UI 友好的属性

---

### Step 5：生成代码文件

**🆕 新建模式**：在 `wsCore/src/commonMain/kotlin/com/tencent/weishi/core/{模块名}/{功能名}/vm/` 下创建
VM 接口文件：

```
wsCore/.../core/{模块名}/{功能名}/vm/
├── I{PageName}PageVM.kt          # 页面级 VM 接口
├── I{ComponentA}VM.kt            # 子组件 A 的 VM 接口
├── I{ComponentB}VM.kt            # 子组件 B 的 VM 接口
└── I{ItemName}VM.kt              # 列表项的 VM 接口
```

**✏️ 迭代模式**：不创建新文件，直接在已有文件中追加变更内容：

- 在已有 VM 接口中新增属性/方法/子 VM，注释标注 `// [diff] 新增：{需求标题}`
- **不修改**已有的属性和方法，不重新输出完整文件

**VM 接口代码模板：**

```kotlin
package com.tencent.weishi.core.{ 模块名 }.{ 功能名 }.vm

import kotlinx . coroutines . flow . StateFlow

/**
 * {页面名称}页面 ViewModel 接口
 *
 * 面向 UI 设计：
 * - 属性为 UI 可直接消费的基础类型
 * - 可变状态通过 StateFlow 暴露
 * - 交互方法以 onXxx() 命名，隐藏业务逻辑
 * - 通过子 VM 拆分独立 UI 区域
 */
interface I {PageName }PageVM {

    // ── 固定属性 ──

    // ── 可变属性 ──

    // ── 交互方法 ──

    // ── 子组件 VM ──
}
```

---

### Step 6：更新 component-map

**新建模式**：在 `docs/component-map.md` 文件中追加新页面的映射关系：

```
| 页面 | {pageId} | component/{模块名}/{页面名} | I{PageName}PageVM |
```

**迭代模式**：不修改 `component-map`，已有页面的条目保持不变。

---

## 接口设计验证清单

在输出 VM 接口代码前，必须逐项检查以下规则：

| # | 检查项 | 说明 |
|---|--------|------|
| 1 | VM 接口中不出现任何 model 类、data class、DTO、PB 对象 | 所有业务数据必须转换为基础类型或子 VM 接口 |
| 2 | VM 接口中不出现 `var`、`MutableStateFlow`、`MutableSharedFlow` | 可变状态通过 `StateFlow` 只读暴露 |
| 3 | 交互方法参数只传 UI 层已知的值（index、tag 文本等） | 不传 feedId、userId 等业务数据 |
| 4 | 列表项设计为子 VM 接口，而非 data class | 如 `List<IEpisodeItemVM>` 而非 `List<EpisodeGridItem>` |
| 5 | 子 VM 接口包含 `onClick()` 等交互方法 | 交互逻辑内聚在子 VM 实现类中 |
| 6 | 不继承与职责无关的父接口 | 如内容 VM 不应继承 `IStructWidgetVM` |
| 7 | 不定义 `sealed class UIState` 或 `sealed interface Action` | 状态拆分为独立属性 |
| 8 | 接口文件中不定义 data class | data class 属于实现层或 model 层 |
| 9 | 每个独立 UI 区域对应一个子 VM 接口 | 遵循子 VM 拆分原则 |
| 10 | 可复用组件有独立子 VM 接口 | 支持不同业务场景复用 |

---

## 示例

### 面向业务设计 ❌（反例）

```kotlin
// 错误 1：暴露了业务 model 类，UI 层需要感知业务结构
interface IDramaPlayPageVM {
    val dramaInfo: IDramaInfo          // ❌ 暴露业务 model
    val episodeList: List<IEpisodeInfo> // ❌ 暴露业务 model
    fun jumpToEpisode(episode: IEpisodeInfo) // ❌ 把业务参数传给 UI 再传回来
}

// 错误 2：接口中定义 data class，列表项不是子 VM
interface IEpisodePanelVM {
    val episodes: StateFlow<List<EpisodeGridItem>>  // ❌ 暴露 data class
    val currentPlayingFeedId: StateFlow<String>     // ❌ 业务数据不应暴露给 UI
    fun onEpisodeClick(feedId: String, episodeNum: Int) // ❌ feedId 是业务数据
}

data class EpisodeGridItem(  // ❌ data class 不应定义在接口文件中
    val feedId: String,
    val episodeNum: Int,
    val isPlaying: Boolean,
)

// 错误 3：MutableStateFlow 冗余双行写法
class MyVM : IMyVM {
    private val _isLoading = MutableStateFlow(false)           // ❌ 冗余
    override val isLoading: StateFlow<Boolean> = _isLoading    // ❌ 冗余
}
```

### 面向 UI 设计 ✅（正例）

```kotlin
// 页面级 VM 接口
interface IDramaPlayPageVM {
    // 固定属性
    val dramaTitle: String
    val dramaCoverUrl: String

    // 可变属性
    val isLoading: StateFlow<Boolean>
    val currentEpisodeIndex: StateFlow<Int>

    // 交互方法
    fun onPageLoad()

    // 子组件 VM
    val playerVM: IDramaPlayerVM
    val episodePanelVM: IDramaEpisodePanelVM
    val commentAreaVM: IDramaCommentAreaVM
}

// 选集面板子 VM
interface IDramaEpisodePanelVM {
    val panelTitle: String
    val episodes: StateFlow<List<IDramaEpisodeItemVM>>
    val isExpanded: StateFlow<Boolean>

    fun onToggleExpand()
}

// 选集项子 VM（列表项必须设计为子 VM 接口，而非 data class）
interface IDramaEpisodeItemVM {
    val episodeNumber: Int
    val episodeTitle: String
    val isLocked: Boolean
    val isPlaying: Boolean
    val isEmpty: Boolean       // 空白占位格标识

    fun onClick()  // 内部处理：切换播放、上报、日志等（业务数据由实现类内部持有）
}
```

**关键区别：**

- UI 层代码完全不感知 `IDramaInfo`、`IEpisodeInfo` 等业务 model
- 所有业务逻辑（跳转、上报、权限判断）都在 VM 实现类中完成
- 子 VM 可以被不同场景复用（如选集项 VM 可以在播放页和详情页复用）
- 新增业务逻辑时，VM 接口和 UI 层无需修改
- 列表项是子 VM 接口（`IDramaEpisodeItemVM`），不是 data class
- 交互方法 `onClick()` 无参数，业务数据（feedId）由子 VM 实现类内部持有
- 空白占位格通过 `isEmpty` 属性区分，由 `object EmptyEpisodeItemVM` 实现

---

## 示例调用

**用户输入：**
> 根据短剧播放页需求文档生成 ViewModel 接口设计

**执行流程：**

1. 读取 `docs/component/drama/dramaPlayPage/drama_play_page.md`
2. 分析组件清单，识别独立 UI 区域，规划子 VM 拆分
3. 参考已有 VM 接口的面向 UI 设计模式
4. 生成页面级 `IDramaPlayPageVM` + 子组件 `IDramaPlayerVM`、`IDramaEpisodePanelVM`、
   `IDramaEpisodeItemVM` 等
5. 代码输出到 `wsCore/src/commonMain/.../core/drama/play/vm/`
