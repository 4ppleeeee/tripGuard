---
description: 按照 Struct（品字形）架构，驱动 AI Coding 全流程完成页面开发需求（需求分析 → 设计 → 编码 → 测试）
type: on-demand
---

## 用户输入

```text
$ARGUMENTS
```

你 **必须** 在执行前先理解用户输入的内容（如果非空）。

**输入约定**：用户输入通常是一个需求文档的路径或名称，该文档位于 `docs/component/` 目录下。如果用户只提供了文档名（如 `xxx.md`），需要在 `docs/component/` 目录树中查找匹配的文件；必要时结合 `docs/component-map.md` 反查对应代码入口。

## 前置检查

1. **读取工作流定义**：读取 [ai-coding-workflow](../skills/ai-coding-workflow)，理解完整的 13 步流程编排、依赖图、并行组规则、进度表规则。
2. **获取设计稿**：
   - 如果用户输入中包含 Figma 链接，直接使用该链接作为设计稿。
   - 如果用户输入中未提供 Figma 链接，在后续需要设计稿的步骤（如 Step 6 UI 还原）中，主动向用户索要。
3. **确认开发架构**：本 command 强制使用 **Struct（品字形）架构**。在 `QnCore` 中，页面开发的核心要素是：
   - `PageArgs`：页面启动参数，实现 `IComposePageArgs`，标记 `@Serializable`
   - `PageViewModel`：页面级 ViewModel；普通页面可直接使用框架默认 `StructPageViewModel`，复杂页面可自定义并实现 `IStructPageViewModel`
   - `PageWidget`：页面 UI 组件结构，继承 `StructPageWidget2`
   - `DataRepo`：数据请求与页面结构拼装层，实现 `IStructDataRepo`
   - `ComposePage`：`qnCompose` 中的 `@Page` 入口，使用 `StructComposePage` 或 `StructComposePage4VM`
4. **确认目标仓库的真实导航入口**：优先使用以下文档，不要引用 `wesee-core` 中不存在的路径：
   - `docs/component-map.md`
   - `doc/【规范】模块结构及业务包名.md`
   - `doc/开发指南/如何新增一个compose页面.md`
   - `doc/开发指南/如何新增一个composeCell.md`
   - `doc/开发指南/如何新增一个composeWidget组件.md`
   - `doc/开发指南/如何使用qnView组件.md`

---

## QnCore Struct 页面代码范式（基于目标仓库真实实现）

> 编码阶段 **必须** 优先遵循 `QnCore` 里已经存在的实现方式，不要把 `wesee-core` 的 `wsCore / wsCompose / wsDrama / wsUser` 目录范式和注册链原样照搬过来。

### 一、模块与目录范式

Struct 页面在 `QnCore` 中通常分布于以下 4 个层次：

```text
# 1. 契约 / 门面层（qnCore，必要时结合 qnFramework）
qnCore/src/commonMain/kotlin/com/tencent/news/core/{domain}/{feature}/
├── {Feature}PageArgs.kt                  # @Serializable + IComposePageArgs
├── I{Feature}PageViewModel.kt            # 自定义页面 VM 接口（如需要）
├── ...                                   # 业务接口 / VM 接口 / 轻量 model

qnCore/src/commonMain/kotlin/com/tencent/news/core/service/
├── AdService.kt / DetailService.kt / FeedsService.kt / MediaService.kt / UserService.kt

qnCore/src/commonMain/kotlin/com/tencent/news/core/service/api/
├── IAdServiceRegistry.kt / IDetailServiceRegistry.kt / IFeedsServiceRegistry.kt
├── IMediaServiceRegistry.kt / IUserServiceRegistry.kt
├── ICollectionPageService.kt / IUserPageWidgetFactory.kt / ...

qnFramework/src/commonMain/kotlin/com/tencent/news/core/
├── router/contants/ComposeViewKey.kt     # 路由常量
├── compose/scaffold/IStructPageViewModel.kt
├── list/api/IStructDataRepo.kt
├── page/model/StructPageConfig.kt
├── page/model/StructPageWidget2.kt
├── page/model/StructWidgetType.kt

# 2. 逻辑实现层（按业务归属选择一个模块）
qnUser | qnFeeds | qnDetail | qnMedia | qnAd
└── src/commonMain/kotlin/com/tencent/news/core/{path}/{feature}/
    ├── {Feature}PageWidget.kt
    ├── {Feature}DataRepo.kt
    ├── {Feature}PageViewModel.kt         # 仅自定义 VM 页面需要
    ├── model/ vm/ setup/ mock/ ...       # 按需补充

# 3. UI 入口层（qnCompose）
qnCompose/src/commonMain/kotlin/com/tencent/news/core/compose/{domain}/{feature}/
├── {Feature}Page.kt                      # @Page 页面入口
├── {Feature}Cell.kt / {Feature}Card.kt   # 业务 cell / card
└── {Feature}View.kt                      # 业务 widget / view

# 4. 通用 Struct UI 壳（qnView）
qnView/src/commonMain/kotlin/com/tencent/news/core/compose/page/
├── StructComposePage.kt
├── StructComposeView.kt
└── StructPageUICustomize.kt
```

### 二、模块职责映射（从 `wesee-core` 迁移到 `QnCore` 时最容易搞错的点）

- `wsCore` → **不是单独一个等价模块**；在 `QnCore` 中通常拆成：
  - `qnCore`：公共接口、Service 门面、PageArgs、VM 接口、业务抽象
  - `qnFramework`：Struct 基础框架、`IStructDataRepo`、`IStructPageViewModel`、`ComposeViewKey`、`StructPageWidget2`
- `wsCompose` → `qnCompose`
- `wsView` → `qnView`
- `wsDrama / wsFeeds / wsUser / wsAd / wsMedia` → 在 `QnCore` 中按业务落到 `qnDetail / qnFeeds / qnUser / qnAd / qnMedia`

### 三、文档与导航入口

当你需要定位页面、需求文档、代码入口、注册链时，优先按以下顺序：

1. `docs/component-map.md`
   - 用于通过需求文档或页面名反查 `@Page` 入口文件
   - 也能快速找到一些已有 Struct 页的真实路径
2. `doc/【规范】模块结构及业务包名.md`
   - 用于判断新代码应该放到哪个模块
3. `doc/开发指南/如何新增一个compose页面.md`
   - 用于确认 `PageArgs / PageWidget / DataRepo / StructComposePage / StructComposePage4VM` 的目标仓库标准范式
4. `doc/开发指南/如何新增一个composeCell.md`
   - 用于确认 cell 注册链和 `GlobalFeedsItemRegistry` 的使用方式
5. `doc/开发指南/如何新增一个composeWidget组件.md`
   - 用于确认 `TitleBar / Header / ChannelBar / Layer / BottomBar / Btn` 的注册方式
6. `doc/开发指南/如何使用qnView组件.md`
   - 用于确认基础 UI 组件使用规范

---

## 核心代码范式

### 1. PageArgs（页面启动参数）

`QnCore` 中的新页面参数，默认使用以下范式：

```kotlin
@Serializable
data class {Feature}PageArgs(
    val from: String = "",
    val sourceId: String = "",
) : IComposePageArgs
```

**关键规则**：
- 必须实现 `IComposePageArgs`
- 必须 `@Serializable`
- 页面特有参数尽量收口在自己的 `PageArgs` 中，不要盲目复用别的页面参数类

### 2. PageWidget（页面骨架）

在 `QnCore` 中，`StructPageWidget2 + StructPageConfig + IStructDataRepo` 是 Struct 页的标准骨架。

**标准单页模式**：

```kotlin
class {Feature}PageWidget(pageArgs: {Feature}PageArgs) : StructPageWidget2(
    StructPageConfig(
        dataRepo = {Feature}DataRepo(pageArgs),
        defaultChannelInfo = KmmChannelInfo.createQnInstance(
            channelKey = "{channel_key}",
            channelName = "{页面名}"
        ).apply {
            env.pageArgs = pageArgs
            env.pageItem = pageArgs.pageItem  // 如有 pageItem 再设置
        },
        fixTitleBarAboveContent = true,
    )
)
```

**本地骨架 / 本地分页根容器模式**：

```kotlin
private class {Feature}LocalDataRepo(
    private val pageArgs: {Feature}PageArgs,
) : IStructDataLocalRepo {
    override fun createLocalResetPageWidget(): StructPageWidget =
        StructPageWidget().buildPageWithManual {
            pager = PagerWidget().apply {
                channels = mutableListOf(
                    {Feature}ChannelWidget1(pageArgs),
                    {Feature}ChannelWidget2(pageArgs),
                )
                action.initIndex = 0
            }
            layers = LayersWidget()
        }
}

class {Feature}PageWidget(pageArgs: {Feature}PageArgs) : StructPageWidget2(
    StructPageConfig(
        dataRepo = {Feature}LocalDataRepo(pageArgs),
        defaultChannelInfo = KmmChannelInfo.createQnInstance(
            channelKey = "{channel_key}",
            channelName = "{页面名}"
        ).apply {
            env.pageArgs = pageArgs
        },
        fixChannelBarBelowTitleBar = true,
    )
)
```

**真实参考实现**：
- `qnCore/.../timeline/page/TimelineDetailPageWidget.kt`
- `qnFeeds/.../list/page/ChannelPageWidgetFactory.kt`
- `qnDetail/.../page/biz/collection/setup/CollectionPageServiceImpl.kt`

### 3. DataRepo（数据源与页面结构拼装）

`QnCore` 的 Struct 数据层 **不是** `quickRequest + Repository` 的固定套路；对 Struct 页而言，最核心的是 `IStructDataRepo`：

```kotlin
interface IStructDataRepo {
    fun createLocalResetPageWidget(): StructPageWidget? = null
    fun createResetRequest(defaultRequest: DataRequest, dataEnv: StructDataEnv): NetworkBuilder<*>
    fun createOtherRequest(defaultRequest: DataRequest, dataEnv: StructDataEnv): NetworkBuilder<*>? = null
    fun createPreloadRequest(defaultRequest: DataRequest, dataEnv: StructDataEnv): NetworkBuilder<*>? = null
    fun buildStructPageWidgetWithJson(dataEnv: StructDataEnv, originJson: String): StructPageWidget? = null
}
```

#### 3.1 最常见模式：请求 + 解析 + 组装 `StructPageWidget`

```kotlin
class {Feature}DataRepo(
    private val pageArgs: {Feature}PageArgs,
) : IStructDataRepo {

    override fun createResetRequest(
        defaultRequest: DataRequest,
        dataEnv: StructDataEnv,
    ) = NetworkBuilder(
        url = "{请求地址}",
        parser = null,
        params = mapOf(
            "id" to pageArgs.sourceId,
        )
    )

    override fun buildStructPageWidgetWithJson(
        dataEnv: StructDataEnv,
        originJson: String,
    ): StructPageWidget {
        val response = parse{Feature}Response(originJson)
        return StructPageWidget().buildPageWithManual {
            titleBar = buildTitleBar(response)
            header = buildHeader(response)
            buildPageWithItemList(
                channel = dataEnv.channelInfo.createChannelWidget(),
                newsList = response.createListItems(),
            )
            layers = buildLayer(response)
        }
    }
}
```

#### 3.2 本地模式：不发网络，直接构造页面树

- 适用于多 Tab 根容器页、本地拼装的壳页
- 使用 `IStructDataLocalRepo`

```kotlin
private class {Feature}LocalDataRepo : IStructDataLocalRepo {
    override fun createLocalResetPageWidget(): StructPageWidget =
        StructPageWidget().buildPageWithManual {
            pager = PagerWidget().apply {
                channels = mutableListOf(...)
                action.initIndex = 0
            }
        }
}
```

#### 3.3 何时需要单独自定义 PageViewModel

只有当页面需要以下能力时，才优先考虑 `StructComposePage4VM`：

- 页面级生命周期编排（如进入 / 退出 / 轮询 / 心跳）
- 顶部 overlay / 未读气泡 / 锚点联动 / Tab 同步
- 页面级事件流 / 额外交互处理 / 与宿主交互
- 不是简单的“拉数据 → 组装 Widget → 展示列表”

否则，优先使用 `StructComposePage` + 默认 `StructPageViewModel`。

### 4. PageViewModel（仅复杂页面需要）

在 `QnCore` 中，自定义 Struct 页 VM 的目标是：
- 继承 `StructPageViewModel`
- 管理页面级特殊行为
- 不要回退到传统 `UiState / Action / dispatch()` 套路

#### 4.1 VM 接口（qnCore）

```kotlin
interface I{Feature}PageViewModel : IStructPageViewModel
```

如确有页面级状态，再按需增补：

```kotlin
interface I{Feature}PageViewModel : IStructPageViewModel {
    val refreshKey: StateFlow<Int>
}
```

#### 4.2 VM 实现（业务模块）

**目标仓库真实可行的两类实现方式**：

##### 方式 A：基于 `StructPageEnv` 的工厂模式

常见于 `AIQADetailPage`、`AigcDiscoveryPage` 一类页面：

```kotlin
class {Feature}PageViewModel(
    val pageEnv: StructPageEnv<{Feature}PageArgs>
) : StructPageViewModel(
    {Feature}PageWidget(pageEnv.pageArgs).toFlex(),
    pageEnv.pageFlow,
    pageEnv.pageScope,
), I{Feature}PageViewModel
```

配套 Service 暴露：

```kotlin
interface I{Feature}Manager {
    fun create{Feature}PageVM(pageEnv: StructPageEnv<{Feature}PageArgs>): I{Feature}PageViewModel
}

internal object {Feature}Manager : I{Feature}Manager {
    override fun create{Feature}PageVM(pageEnv: StructPageEnv<{Feature}PageArgs>) =
        {Feature}PageViewModel(pageEnv)
}
```

##### 方式 B：显式传入 `rootWidget + pageFlow + pageScope`

常见于 `TimelineDetailStructPage` 一类页面：

```kotlin
fun create{Feature}PageViewModel(
    pageArgs: {Feature}PageArgs,
    rootWidget: StructPageWidget2,
    pageFlow: SharedFlow<PageLifecycleEvent>,
    pageScope: CoroutineScope,
): IStructPageViewModel = {Feature}PageViewModel(
    pageArgs = pageArgs,
    controller = FrameworkService.createFlexFeedsController(
        rootWidget = rootWidget,
        pageItem = { rootWidget.findPageItem() }
    ),
    pageFlow = pageFlow,
    pageScope = pageScope,
)

internal class {Feature}PageViewModel(
    private val pageArgs: {Feature}PageArgs,
    controller: IFlexibleFeedsController,
    pageFlow: SharedFlow<PageLifecycleEvent>,
    pageScope: CoroutineScope,
) : StructPageViewModel(controller, pageFlow, pageScope), IStructPageViewModel {
    // 处理轮询 / overlay / 顶部气泡 / 页面级逻辑
}
```

#### 4.3 行为红线

- 不要给 Struct 页面设计 `sealed class UiState`
- 不要定义 `sealed interface Action` + `dispatch(action)`
- 不要强行引入 `UseCase` 作为固定中间层
- 不要让 `qnCompose` 直接依赖 `qnUser / qnFeeds / qnDetail / qnMedia / qnAd`
- `qnCompose` 只能通过 `AdService / DetailService / FeedsService / MediaService / UserService` 访问逻辑实现

### 5. ComposePage 入口（StructComposePage vs StructComposePage4VM）

#### 5.1 使用 `StructComposePage`（标准页面，优先）

```kotlin
@Page(ComposeViewKey.{Domain}.{FEATURE}_PAGE)
internal class {Feature}Page : ComposePage() {

    @Composable
    override fun OnSetContent() {
        super.OnSetContent()
        val pageArgs = rememberedPageArgs<{Feature}PageArgs>() ?: return
        StructComposePage(
            pageWidget = {
                {Domain}Service.{factory}.create{Feature}PageWidget(pageArgs)
            },
            pageFlow = pageLifecycleFlow.lifecycleFlow,
        )
    }
}
```

**真实参考实现**：
- `qnCompose/.../pay/present/page/PresentCardListPage.kt`
- `qnCompose/.../channel/marketing/channelpod/page/AudioPodChannelPage.kt`
- `qnCompose/.../page/biz/collection/CollectionPage.kt`
- `qnCompose/.../event/StructEventPage.kt`

#### 5.2 使用 `StructComposePage4VM`（复杂页面）

**模式 A：直接传 `StructPageEnv`**

```kotlin
@Page(ComposeViewKey.Aigc.QA_PAGE)
internal class {Feature}Page : ComposePage() {

    @Composable
    override fun OnSetContent() {
        super.OnSetContent()
        val pageEnv = rememberedPageEnv<{Feature}PageArgs>() ?: return
        StructComposePage4VM(
            pageViewModel = {
                UserService.aigc.create{Feature}PageVM(pageEnv)
            }
        )
    }
}
```

**模式 B：页面自己构造 `rootWidget` 与 VM**

```kotlin
@Page(ComposeViewKey.Event.TIMELINE_PAGE)
internal class {Feature}Page : ComposePage() {

    @Composable
    override fun OnSetContent() {
        super.OnSetContent()
        val pageArgs = rememberedPageArgs<{Feature}PageArgs>() ?: return
        val pageScope = rememberCoroutineScope()
        val rootWidget = remember { {Feature}PageWidget(pageArgs) }

        StructComposePage4VM(
            pageViewModel = {
                create{Feature}PageViewModel(
                    pageArgs = pageArgs,
                    rootWidget = rootWidget,
                    pageFlow = pageLifecycleFlow.lifecycleFlow,
                    pageScope = pageScope,
                )
            }
        )
    }
}
```

**真实参考实现**：
- `qnCompose/.../aigc/qa/page/AIQADetailPage.kt`
- `qnCompose/.../aigc/discovery/page/AigcDiscoveryPage.kt`
- `qnCompose/.../timeline/TimelineDetailStructPage.kt`
- `qnCompose/.../pay/memberarea/view/CPMemberAreaPage.kt`
- `qnCompose/.../morningpost/MorningPostPage.kt`

### 6. Cell 注册范式（QnCore 特有，不能写成 `WsFeedsItemCardService`）

`QnCore` 里新增 Struct 列表卡片时，遵循以下链路：

1. 在 `qnCore` 定义 cell 对应的 VM 接口 / VMHolder（按需要）
2. 在业务模块实现 VM
3. 在 `qnCompose` 新增 `IFeedsItemCard` 实现类
4. 在业务 `*FeedsItemRegistry` 中注册卡片
5. 如需新增新的业务 registry，再把该 registry 挂到 `GlobalFeedsItemRegistry`

关键文件：
- `qnCompose/.../scaffold/GlobalFeedsItemRegistry.kt`
- `qnCompose/.../compose/{biz}/.../*FeedsItemRegistry.kt`
- `qnCompose/.../feeds/ComposeItemCell.kt`
- `doc/开发指南/如何新增一个composeCell.md`

### 7. Widget 注册范式（QnCore 特有，不能写成 `WsStruct*Registry`）

页面级 widget 注册统一走 `ViewServiceRegistryImpl` + `Struct*Registry`：

- `StructTitleBarRegistry`
- `StructHeaderRegistry`
- `StructChannelBarRegistry`
- `StructLayerRegistry`
- `StructBottomBarRegistry`
- `StructBtnRegistry`
- `StructChannelRegistry`

统一入口：
- `qnCompose/src/commonMain/kotlin/com/tencent/news/core/setup/ViewServiceRegistryImpl.kt`
- `qnCompose/src/commonMain/kotlin/com/tencent/news/core/setup/ComposeModuleSetUp.kt`

**规则**：
- 新增 `TitleBar / Header / ChannelBar / Layer / BottomBar / Btn / Channel` 时，优先复用已有 registry
- 新增页面级 widget 时，优先按 `widgetVM` 模式注册
- 只有确实需要按 widget 类型或 `showType` 分发时，才走类型分支

### 8. Service 与模块注册链

在 `QnCore` 中，新页面通常还需要补齐以下注册链：

1. **路由 Key**：`qnFramework/.../router/contants/ComposeViewKey.kt`
2. **Service 接口或 Factory 接口**：`qnCore/.../service/api/...`
3. **Service 门面暴露**：`qnCore/.../service/UserService.kt` / `FeedsService.kt` / ...
4. **业务实现挂载**：`qn{Module}/.../setup/*ServiceRegistryImpl.kt`
5. **模块初始化注册**：`qn{Module}/.../setup/*ModuleSetUp.kt`
6. **全量模块装配**：`qnCommon/.../setup/KmmModulesSetUp.kt`

**常见真实文件**：
- `qnUser/.../setup/UserServiceRegistryImpl.kt`
- `qnFeeds/.../setup/FeedsServiceRegistryImpl.kt`
- `qnDetail/.../setup/DetailServiceRegistryImpl.kt`
- `qnUser/.../setup/UserModuleSetUp.kt`
- `qnFeeds/.../setup/FeedsModuleSetUp.kt`
- `qnDetail/.../setup/DetailModuleSetUp.kt`

### 9. StructPageConfig 常用配置速查

| 配置项 | 默认值 | 说明 |
|--------|--------|------|
| `fixTitleBarAboveContent` | `false` | TitleBar 固定在顶部，不随内容滑动 |
| `fixChannelBarBelowTitleBar` | `false` | ChannelBar 固定在 TitleBar 下方 |
| `contentFullScreen` | `false` | 内容区域全屏 |
| `forceHideTitleBarArea` | `false` | 强制隐藏 TitleBar 区域 |
| `forceHideHeaderArea` | `false` | 强制隐藏 Header 区域 |
| `disableHorizontalPagerGesture` | `false` | 禁用 Pager 用户滑动 |
| `enableFooter` | `true` | 是否开启加载更多 |
| `enableHeader` | `false` | 是否展示列表顶部 header |
| `enablePullRefresh` | `false` | 是否启用下拉刷新 |
| `expandBottomSafeAreaForPage` | `false` | 页面维度底部安全区扩展 |
| `expandBottomSafeAreaForList` | `false` | 列表维度底部安全区扩展 |
| `enableCacheFlexCtrl` | `false` | 是否缓存 flex controller |

---

## 真实参考实现优先级

编码时优先参考以下 `QnCore` 真实实现：

### 标准 `StructComposePage` 页面
- `qnCompose/.../pay/present/page/PresentCardListPage.kt`
- `qnCompose/.../channel/marketing/channelpod/page/AudioPodChannelPage.kt`
- `qnCompose/.../page/biz/collection/CollectionPage.kt`
- `qnCompose/.../event/StructEventPage.kt`

### 自定义 `StructComposePage4VM` 页面
- `qnCompose/.../aigc/qa/page/AIQADetailPage.kt`
- `qnCompose/.../aigc/discovery/page/AigcDiscoveryPage.kt`
- `qnCompose/.../timeline/TimelineDetailStructPage.kt`
- `qnCompose/.../pay/memberarea/view/CPMemberAreaPage.kt`
- `qnCompose/.../morningpost/MorningPostPage.kt`

### 数据源 / 骨架 / 注册链参考
- `qnCore/.../timeline/page/TimelineDetailPageWidget.kt`
- `qnCore/.../timeline/page/TimelineDetailPageViewModel.kt`
- `qnFeeds/.../list/page/ChannelPageWidgetFactory.kt`
- `qnUser/.../setup/AigcManager.kt`
- `qnCompose/.../scaffold/GlobalFeedsItemRegistry.kt`
- `qnCompose/.../scaffold/registry/StructTitleBarRegistry.kt`
- `qnCompose/.../scaffold/registry/StructHeaderRegistry.kt`
- `qnCompose/.../scaffold/registry/StructChannelBarRegistry.kt`
- `qnCompose/.../setup/ViewServiceRegistryImpl.kt`

---

## 执行流程

### 1️⃣ 判断执行模式

**先判断用户意图，再决定执行模式：**
按照 `ai-coding-workflow` 中的进度判断规则，先读取对应需求的进度表，再用真实产物校验，确定当前已完成到哪一步。

- 用户只问“当前进度 / 下一步” → **状态模式**：只检查阶段、阻塞点和下一步，不创建团队。
- 用户要求“继续 / 按流程推进 / 全流程协同开发 / 多 agent 协作” → **团队模式**：创建 team，由协调 agent 派发角色任务。

### 2️⃣ 判断进度

按照 `ai-coding-workflow` skill 中的进度判断规则，**先读取 / 补齐进度表，再用真实产物交叉校验**，确定当前已完成到哪一步。

每一步执行完成后，先更新进度表，再自动判断进度并继续推进，直到遇到以下情况之一时停下：

- **需要用户确认**：Step 5（技术方案设计）完成后，必须停下让用户评审确认。
- **需要用户提供信息**：缺少必要输入（如 TAPD 链接、Figma 设计稿链接）时，停下向用户索要。
- **遇到人工步骤**：Step 2（需求评审）、Step 13（测试执行）为人工步骤，到达时停下提示用户。
- **全部完成**：所有步骤执行完毕。

**同时判断新建 / 迭代模式**：检查对应页面目录下是否存在 `diff/` 子目录且包含 `_diff.md`：
- 无 diff 目录 → 🆕 新建模式
- 有 diff 目录 → ✏️ 迭代模式

### 3️⃣ 状态模式输出

若为状态模式，输出以下格式后停止：

```text
📌 当前模式：{新建模式 / 迭代模式}
📌 当前进度：Step {N} 已完成 / 当前阻塞在 Step {N}
👥 当前编排：状态模式
📋 下一步：Step {N+1} — {名称}
🧩 负责人：{角色 agent 名称}
🛠️ 建议调用：{skill-name 或人工步骤}
💡 推荐 Prompt："{可直接复用的 prompt}"
```

### 4️⃣ 团队模式：创建 Team 并编排任务

#### 4.1 创建 Team

```text
team_create(team_name = "ai-coding-struct-{模块}-{页面}")

成员列表：
workflow-orchestrator → bmad-orchestrator / .codebuddy/agents/bmad-orchestrator.md
requirements-owner    → bmad-po           / .codebuddy/agents/bmad-po.md
protocol-architect    → bmad-architect    / .codebuddy/agents/bmad-architect.md
report-architect      → bmad-architect    / .codebuddy/agents/bmad-architect.md
solution-architect    → bmad-architect    / .codebuddy/agents/bmad-architect.md
# Step 6~9 由主 Agent 直接执行，不创建 sub-agent 成员
quality-engineer-A    → bmad-qa           / .codebuddy/agents/bmad-qa.md
quality-engineer-B    → bmad-qa           / .codebuddy/agents/bmad-qa.md
quality-engineer-C    → bmad-qa           / .codebuddy/agents/bmad-qa.md
quality-engineer-D    → bmad-qa           / .codebuddy/agents/bmad-qa.md
workflow-reviewer     → bmad-review       / .codebuddy/agents/bmad-review.md（按需创建）
```

> `workflow-orchestrator` 必须最先创建；Step 6~9 由主 Agent 直接执行，禁止为这四个步骤创建或派发 sub-agent。

#### 4.2 Step 1 — 需求分析

```text
task(
  subagent_name = "bmad-po",
  subagent_path = ".codebuddy/agents/bmad-po.md",
  name = "requirements-owner",
  team_name = "ai-coding-struct-{模块}-{页面}",
  description = "Step 1 需求分析",
  prompt = "分析以下需求并产出需求文档。输入：{TAPD链接 或 需求文档路径}。请使用 analyze-tapd-story skill 完成。新建页面输出到 docs/component/{模块}/{页面驼峰}/{页面下划线}.md，已有页面输出到 docs/component/{模块}/{页面驼峰}/diff/{页面下划线}_diff.md。若页面归属不明确，先结合 docs/component-map.md 和 doc/【规范】模块结构及业务包名.md 判断模块与代码入口。",
  mode = "acceptEdits",
  max_turns = 30
)
```

> **Step 2 人工关卡**：Step 1 完成后，停下提示用户进行需求评审，确认后再继续。

#### 4.3 并行组 A — Step 3 + Step 4（需求评审通过后同时派发）

```text
# 同时发出以下两个 task 调用：

task(
  subagent_name = "bmad-architect",
  subagent_path = ".codebuddy/agents/bmad-architect.md",
  name = "protocol-architect",
  team_name = "ai-coding-struct-{模块}-{页面}",
  description = "Step 3 接口协议设计",
  prompt = "根据需求文档设计接口协议。需求文档：docs/component/{模块}/{页面驼峰}/{页面下划线}.md{迭代模式追加：+ diff/{页面下划线}_diff.md}。请使用 design-api-protocol skill 完成。输出：docs/component/{模块}/{页面驼峰}/{页面下划线}_protocol.md（迭代模式：diff/{页面下划线}_protocol_diff.md，无变化则写「保持原样」）。注意：本页面使用 QnCore Struct（品字形）架构，核心数据层是 IStructDataRepo，接口协议设计需要覆盖 pageArgs、请求参数、响应字段、widget 组装所需字段、分页 / channel / showType / widgetType / report 参数等。",
  mode = "plan",
  max_turns = 30
)

task(
  subagent_name = "bmad-architect",
  subagent_path = ".codebuddy/agents/bmad-architect.md",
  name = "report-architect",
  team_name = "ai-coding-struct-{模块}-{页面}",
  description = "Step 4 上报需求分析",
  prompt = "分析上报需求。需求文档：docs/component/{模块}/{页面驼峰}/{页面下划线}.md{迭代模式追加：+ diff/{页面下划线}_diff.md}。请使用 analyze-report-document skill 完成。输出：docs/component/{模块}/{页面驼峰}/{页面下划线}_report.md（迭代模式：diff/{页面下划线}_report_diff.md，无变化则写「保持原样」）。如果页面属于 Struct 复杂页，还需关注页面曝光 / 退出 / 卡片点击 / 浮层 / ChannelBar / 顶部 overlay / 分享等链路。",
  mode = "plan",
  max_turns = 30
)
```

#### 4.4 Step 5 — 技术方案设计（Step 3 + 4 全部完成后）

```text
task(
  subagent_name = "bmad-architect",
  subagent_path = ".codebuddy/agents/bmad-architect.md",
  name = "solution-architect",
  team_name = "ai-coding-struct-{模块}-{页面}",
  description = "Step 5 技术方案设计",
  prompt = "根据需求文档、接口协议和上报文档设计技术方案。需求文档：docs/component/{模块}/{页面驼峰}/{页面下划线}.md。协议文档：docs/component/{模块}/{页面驼峰}/{页面下划线}_protocol.md。上报文档：docs/component/{模块}/{页面驼峰}/{页面下划线}_report.md。{迭代模式追加对应 diff 文档路径}。请使用 design-tech-solution skill 完成。输出：docs/component/{模块}/{页面驼峰}/{页面下划线}_tech_solution.md（迭代模式：diff/{页面下划线}_tech_solution_diff.md）。

⚠️ 架构约束：本页面使用 QnCore Struct（品字形）架构，技术方案必须明确以下内容：
1. `pageArgs / pageWidget / dataRepo / （可选）pageViewModel` 的职责划分
2. `StructComposePage` vs `StructComposePage4VM` 的选型依据
3. 代码落位：qnCore / qnFramework / qnCompose / qnView / 具体业务模块（qnUser / qnFeeds / qnDetail / qnMedia / qnAd）
4. Service 门面选择：AdService / DetailService / FeedsService / MediaService / UserService 之一
5. 注册链路：ComposeViewKey、*ServiceRegistryImpl、*ModuleSetUp、GlobalFeedsItemRegistry、Struct*Registry
6. Cell / Widget / ChannelBar / TitleBar / Header / Layer / BottomBar 是否需要新增与如何注册
7. 日志与可观测性章节（模块日志入口、按层落点、关键节点清单）
8. 如果方案会新建测试用例目录 `docs/testcase/...`，需在文档中明确产物路径约定

产出后停下来等待用户确认是否继续编码。",
  mode = "plan",
  max_turns = 40
)
```

> **Step 5 人工关卡**：技术方案产出后，`workflow-orchestrator` 必须向用户汇总技术方案关键结论并停下等待确认，**不得自动进入编码阶段**。

#### 4.5 并行组 B — Step 6 + 7 + 8（用户确认继续编码后，主 Agent 直接并行执行）

> ⚠️ **Step 6~9 由主 Agent 直接执行，禁止派发 sub-agent。**

```text
# 主 Agent 同时调用以下三个 skill（不通过 task 派发）：

[主 Agent] use_skill("restore-ui-design")
  输入：
    - 技术方案：docs/component/{模块}/{页面驼峰}/{页面下划线}_tech_solution.md
    - 需求文档：docs/component/{模块}/{页面驼峰}/{页面下划线}.md
    - Figma 设计稿：{url}（如未提供，向用户索要）
    - 架构约束：
      * 页面 @Page 入口放 qnCompose
      * 通用基础组件优先放 qnView
      * qnCompose 不能直接依赖 qnUser / qnFeeds / qnDetail / qnMedia / qnAd
      * Struct 页面使用 StructComposePage 或 StructComposePage4VM

[主 Agent] use_skill("generate-mock-data")
  输入：
    - 技术方案：docs/component/{模块}/{页面驼峰}/{页面下划线}_tech_solution.md
    - 协议文档：docs/component/{模块}/{页面驼峰}/{页面下划线}_protocol.md
    - 架构约束：Mock 数据放对应业务模块（qnUser / qnFeeds / qnDetail / qnMedia / qnAd）的 `mock/` 或相邻业务包下，服务于 DataRepo / PageViewModel / CellVM 的开发与测试

[主 Agent] use_skill("implement-api-layer")
  输入：
    - 技术方案：docs/component/{模块}/{页面驼峰}/{页面下划线}_tech_solution.md
    - 协议文档：docs/component/{模块}/{页面驼峰}/{页面下划线}_protocol.md
    - 架构约束：
      * Struct 页面核心数据层是 `IStructDataRepo`
      * 首刷请求通过 `createResetRequest()` 定义
      * 页面结构拼装通过 `buildStructPageWidgetWithJson()` 或 `createLocalResetPageWidget()` 定义
      * 只有当技术方案明确需要时，才在业务模块新增额外 Repository/Manager/Facade 封装
```

#### 4.6 Step 9 — VM 实现编码（Step 6 + 8 完成后，主 Agent 直接执行）

```text
[主 Agent] use_skill("implement-viewmodel")
  输入：
    - 技术方案：docs/component/{模块}/{页面驼峰}/{页面下划线}_tech_solution.md
    - 需求文档：docs/component/{模块}/{页面驼峰}/{页面下划线}.md
    - 已有 UI 代码（Step 6 产物）
    - 已有 DataRepo 代码（Step 8 产物）
    - 架构约束：
      * 若选型 StructComposePage：优先不新增自定义页面 VM，直接使用框架默认 StructPageViewModel
      * 若选型 StructComposePage4VM：
        - 自定义页面 VM 实现 `IStructPageViewModel`
        - 继承 `StructPageViewModel`
        - 通过 `AdService / DetailService / FeedsService / MediaService / UserService` 暴露创建方法
        - 禁止定义 `UiState / Action / dispatch()` 套路
      * qnCompose 层必须只依赖 qnCore / qnFramework / qnView 暴露出来的能力
```

> **自动续跑**：Step 9 完成后，主 Agent 自动推进 Step 10 → Step 10.5 → Step 11 & 12（并行），直到 Step 13 人工关卡再停下。

#### 4.7 Step 10 — 需求检查

```text
task(
  subagent_name = "bmad-qa",
  subagent_path = ".codebuddy/agents/bmad-qa.md",
  name = "quality-engineer-A",
  team_name = "ai-coding-struct-{模块}-{页面}",
  description = "Step 10 需求检查",
  prompt = "对照需求文档和技术方案检查代码实现，输出结构化问题清单 JSON（含 severity / file / fix_suggestion）。需求文档：docs/component/{模块}/{页面驼峰}/{页面下划线}.md。技术方案：docs/component/{模块}/{页面驼峰}/{页面下划线}_tech_solution.md。{迭代模式追加对应 diff 文档路径}。请使用 check-requirements skill 完成。

⚠️ QnCore Struct 架构专项检查：
1. `pageArgs / pageWidget / dataRepo / （可选）pageViewModel` 是否按技术方案落位
2. 是否正确选用了 `StructComposePage` 或 `StructComposePage4VM`
3. qnCompose 是否通过 `AdService / DetailService / FeedsService / MediaService / UserService` 访问逻辑实现
4. 若新增 cell：是否注册到业务 `*FeedsItemRegistry`，并接入 `GlobalFeedsItemRegistry`
5. 若新增 widget：是否注册到对应 `Struct*Registry`，并由 `ViewServiceRegistryImpl` 暴露
6. 是否补齐 `ComposeViewKey`、`*ServiceRegistryImpl`、`*ModuleSetUp` 注册链
7. 日志落地验收（对照技术方案「日志与可观测性」章节逐条检查）",
  mode = "acceptEdits",
  max_turns = 30
)
```

> Step 10 完成后，主 Agent 解析问题清单，提取 P0/P1 issues，传给 Step 10.5。

#### 4.8 Step 10.5 — 编译验证与修复

```text
task(
  subagent_name = "bmad-qa",
  subagent_path = ".codebuddy/agents/bmad-qa.md",
  name = "quality-engineer-B",
  team_name = "ai-coding-struct-{模块}-{页面}",
  description = "Step 10.5 编译验证与修复",
  prompt = "修复 P0/P1 问题并验证 androidApp 编译通过。输入：{主 Agent 汇总的 P0/P1 问题清单 JSON}。请使用 verify-build-integrity skill 完成。编译失败时停止并向主 Agent 汇报错误详情。",
  mode = "acceptEdits",
  max_turns = 40
)
```

> Step 10.5 完成后，主 Agent 汇总修复项与 VM 代码摘要，**并行**派发 Step 11 / 12。编译失败时主 Agent 停止自动推进，向用户汇报并等待指示。

#### 4.9 并行组 C — Step 11 + 12（Step 10.5 编译通过后同时派发）

```text
# 同时发出以下两个 task 调用：

task(
  subagent_name = "bmad-qa",
  subagent_path = ".codebuddy/agents/bmad-qa.md",
  name = "quality-engineer-C",
  team_name = "ai-coding-struct-{模块}-{页面}",
  description = "Step 11 单元测试",
  prompt = "为 ViewModel 生成单元测试。输入：{主 Agent 汇总的 VM 代码摘要（已修复的接口签名、状态机变更、关键文件路径）}。请使用 generate-unit-tests skill 完成。输出：commonTest/.../{Feature}ViewModelTest.kt。

注意：若选型 StructComposePage（无自定义页面 VM），则重点测试 DataRepo 的数据转换、Widget 拼装、CellVM / 局部 ViewModel 的关键逻辑。",
  mode = "acceptEdits",
  max_turns = 30
)

task(
  subagent_name = "bmad-qa",
  subagent_path = ".codebuddy/agents/bmad-qa.md",
  name = "quality-engineer-D",
  team_name = "ai-coding-struct-{模块}-{页面}",
  description = "Step 12 测试用例",
  prompt = "生成测试用例。输入：需求文档 docs/component/{模块}/{页面驼峰}/{页面下划线}.md + {主 Agent 汇总的问题清单摘要}。{迭代模式追加：diff 文档路径}。请使用 generate-test-cases skill 完成。输出：docs/testcase/{模块}/{页面下划线}_testcase.md（如目录不存在则创建；迭代模式：docs/component/{模块}/{页面驼峰}/diff/{页面下划线}_testcase_diff.md）。",
  mode = "acceptEdits",
  max_turns = 30
)
```

#### 4.10 Step 13 — 测试执行（人工关卡）

Step 12 完成后，停下向用户交接：

```text
📋 Step 13 — 测试执行（人工）
📥 测试同学需要：
  - 测试用例：docs/testcase/{模块}/{页面下划线}_testcase.md
  - 单元测试：commonTest/.../{Feature}ViewModelTest.kt
  - 编译验证报告（Step 10.5 产物）
🔔 请测试同学执行测试，完成后提交测试报告与缺陷单。
```

### 5️⃣ 输出格式

每完成一个步骤后，输出进度摘要：

```text
✅ 已完成：Step {N} — {名称}
📌 当前模式：{新建模式 / 迭代模式}
📌 当前进度：{已完成步骤概览}
👥 当前编排：{状态模式 / 团队模式}（协调者：workflow-orchestrator）
📋 下一步：Step {N+1} — {名称}
🧩 负责人：{角色 agent 名称 或 主 Agent}
🛠️ 建议调用：{skill-name 或人工步骤}
```

若存在并行项，追加：

```text
⚡ 可并行执行：
- Step {X} — {名称} → {角色 agent} / {skill-name}
- Step {Y} — {名称} → {角色 agent} / {skill-name}
```

若阻塞在 Step 5 后人工关卡：

```text
⏸️ 等待用户确认：技术方案已产出，请确认是否继续进入编码阶段（Step 6/7/8）。
```

## 关键规则

1. **架构约束**：所有代码产物必须遵循 `QnCore` 的真实 Struct 范式，优先使用 `qnCore + qnFramework + qnCompose + qnView + 对应业务模块` 的分层，而不是沿用 `wesee-core` 的模块名与注册链。
2. **范式优先**：编码阶段必须优先参考目标仓库现有成功模式，例如 `AIQADetailPage`、`AigcDiscoveryPage`、`TimelineDetailStructPage`、`PresentCardListPage`、`AudioPodChannelPage`。
3. **注册链完整**：新增页面时，必须同时检查 `ComposeViewKey`、Service 门面、`*ServiceRegistryImpl`、`*ModuleSetUp`、`GlobalFeedsItemRegistry`、`Struct*Registry`。
4. **并行执行**：遵循 workflow 中定义的并行组规则（组 A：Step 3 + 4 可并行；组 B：Step 6 + 7 + 8 可并行）。
5. **信息不足时停下**：如果因信息不完整无法做出确定的决策，必须停下来向用户确认，确认完成后再继续执行。
6. **不要跳步**：严格按照依赖图顺序执行，不要跳过任何步骤。
7. **迭代模式自动识别**：workflow 会自动判断新建模式还是迭代模式，无需用户手动指定。
8. **页面入口限制**：`qnCompose` 层不能直接依赖业务实现模块，必须经由 `AdService / DetailService / FeedsService / MediaService / UserService` 间接访问。
9. **UI 组件落位**：业务页面 UI 放 `qnCompose`；可复用基础组件优先放 `qnView`；不要把通用基础组件直接写死在业务页面目录中。
