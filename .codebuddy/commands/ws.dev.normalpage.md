---
description: 按照通用 MVVM 架构（禁止 Struct），驱动 AI Coding 全流程完成页面开发需求（需求分析 → 设计 → 编码 → 测试）
---

## 用户输入

```text
$ARGUMENTS
```

你 **必须** 在执行前先理解用户输入的内容（如果非空）。

**输入约定**：用户输入通常是一个需求文档的路径或名称，该文档位于 `docs/component/` 目录下。如果用户只提供了文档名（如 `xxx.md`），需要在 `docs/component/` 目录树中查找匹配的文件。

## 前置检查

1. **读取工作流定义**：读取 [ai-coding-workflow](../skills/ai-coding-workflow)，理解完整的 13 步流程编排、依赖图和并行组规则。

2. **获取设计稿**：
   - 如果用户输入中包含 Figma 链接，直接使用该链接作为设计稿。
   - 如果用户输入中未提供 Figma 链接，在后续需要设计稿的步骤（如 Step 6 UI 还原）中，主动向用户索要。

3. **确认开发架构**：本 command **禁止**使用 Struct（品字形）架构，强制使用 **通用 MVVM 架构**，即页面由以下分层组成：
   - `PageArgs`：页面启动参数，实现 `IComposePageArgs`，标记 `@Serializable`
   - `ViewModel`：页面级 ViewModel，持有 `UiState` 与 `UiEffect`，负责状态管理与业务编排
   - `View`（Composable）：页面 UI 组件，使用 `@Page` + `ComposeContainer` 作为入口
   - `UseCase`（可选）：当业务逻辑复杂或需跨页面复用时，下沉到 UseCase 层
   - `Repository`：数据访问层，统一承接网络、缓存与数据源聚合
   
   > ⚠️ **禁止**使用 `StructPageWidget2`、`IStructPageViewModel`、`IStructDataRepo` 等 Struct 体系类。

---

## 普通页面代码范式（从 wsUser 模块抽象）

> 以下范式是从仓库中已有的普通（非 Struct）页面实现中抽象出来的标准模式。编码阶段 **必须** 严格遵循此范式，不要发明新模式。
> 本项目中存在两种普通页面子类型：**表单/设置类页面**（纯 Compose UI）和**列表类页面**（复用 Struct 列表能力但由自定义 VM 驱动）。

### 一、目录结构范式

普通页面的代码分布在三个模块中，按以下目录结构组织：

#### 子类型 A：表单/设置类页面（纯 Compose UI，无 Struct 列表）

```
# 1. 契约层（wsCore）
wsCore/src/commonMain/kotlin/com/tencent/weishi/core/{domain}/{feature}/
├── api/
│   ├── {Feature}PageAssembly.kt           # Assembly + ViewModelProvider 定义
│   └── {Feature}PageArgs.kt               # @Serializable PageArgs（如需要）
└── vm/
    └── I{Feature}PageViewModel.kt         # ViewModel 接口

# 2. 逻辑实现层（wsUser）
wsUser/src/commonMain/kotlin/com/tencent/weishi/core/{domain}/{feature}/
├── vm/
│   └── {Feature}ViewModel.kt             # ViewModel 实现
├── model/
│   ├── {Feature}UiState.kt               # UiState 定义
│   └── {Feature}Action.kt                # Action 定义（如需要）
├── repository/
│   ├── {Feature}Repository.kt            # Repository 接口
│   └── {Feature}RepositoryImpl.kt        # Repository 实现
├── usecase/                               # （可选）UseCase 层
│   └── {Feature}UseCase.kt
└── mock/
    └── {Feature}MockData.kt              # Mock 数据

# 3. UI 入口层（wsCompose）
wsCompose/src/commonMain/kotlin/com/tencent/weishi/compose/{domain}/{feature}/
├── page/
│   ├── {Feature}Page.kt                   # @Page 入口
│   └── {Feature}PageView.kt              # Composable 视图
└── view/
    └── {Feature}*.kt                      # 子组件
```

#### 子类型 B：列表类页面（复用 Struct 列表能力 + 自定义 VM）

```
# 1. 契约层（wsCore）
wsCore/src/commonMain/kotlin/com/tencent/weishi/core/{domain}/{feature}/
├── api/
│   ├── {Feature}PageAssembly.kt           # Assembly（含 pageWidget + pageViewModelProvider）
│   └── {Feature}PageArgs.kt               # @Serializable PageArgs
└── vm/
    ├── I{Feature}PageViewModel.kt         # PageViewModel 接口
    └── I{Feature}CellVM.kt               # Cell VM 接口

# 2. 逻辑实现层（wsUser）
wsUser/src/commonMain/kotlin/com/tencent/weishi/core/{domain}/{feature}/
├── page/
│   ├── {Feature}PageWidget.kt             # StructPageWidget2（仅作列表容器）
│   ├── {Feature}DataRepo.kt              # IStructDataSuspendRepo（数据加载）
│   └── {Feature}PageViewModel.kt         # StructPageViewModel 子类
├── widget/
│   ├── {Feature}TitleBarWidget.kt         # TitleBar Widget
│   └── {Feature}EmptyWidget.kt           # 空态 Widget
├── vm/
│   └── {Feature}CellVM.kt               # Cell VM 实现
├── model/
│   ├── {Feature}Action.kt                # Action 定义
│   ├── {Feature}UiState.kt               # UiState 定义
│   ├── {Feature}FeedsVMItem.kt           # FeedsVMItem
│   └── {Feature}MsgItem.kt              # 业务领域模型
├── repository/
│   ├── {Feature}Repository.kt            # Repository 接口
│   └── {Feature}RepositoryImpl.kt        # Repository 实现
├── usecase/
│   ├── Load{Feature}ListUseCase.kt       # 加载列表
│   ├── Delete{Feature}UseCase.kt         # 删除操作
│   └── Toggle{Feature}UseCase.kt         # 切换操作
└── mock/
    └── {Feature}MockData.kt

# 3. UI 入口层（wsCompose）
wsCompose/src/commonMain/kotlin/com/tencent/weishi/compose/{domain}/{feature}/
├── page/
│   └── {Feature}Page.kt                  # @Page 入口（使用 StructComposePage4VM）
├── cell/
│   ├── {Feature}CellRegistry.kt          # Cell 注册器
│   └── {Feature}Cell.kt                  # Cell Composable 视图
└── view/
    └── {Feature}EmptyView.kt             # 空态视图
```

### 二、子类型 A 核心文件范式（表单/设置类页面）

#### 2.1 ViewModel 接口（wsCore）

```kotlin
interface I{Feature}PageViewModel {
    val uiState: StateFlow<{Feature}UiState>

    // 页面动作
    suspend fun onBackClick()
    suspend fun onItemClick(item: {Feature}ItemUi)
    fun onPageResume() {}
}
```

#### 2.2 Assembly 定义（wsCore）

```kotlin
fun interface {Feature}PageViewModelProvider {
    operator fun invoke(): I{Feature}PageViewModel
}

data class {Feature}PageAssembly(
    val pageViewModelProvider: {Feature}PageViewModelProvider,
)
```

#### 2.3 UiState 定义

```kotlin
data class {Feature}UiState(
    val sections: List<{Feature}SectionUi> = emptyList(),
    val isLoading: Boolean = false,
    val showDialog: Boolean = false,
)
```

#### 2.4 ViewModel 实现

```kotlin
internal class {Feature}ViewModel(
    private val repository: {Feature}Repository = {Feature}RepositoryImpl(),
) : I{Feature}PageViewModel {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val _uiState = MutableStateFlow({Feature}UiState())
    override val uiState: StateFlow<{Feature}UiState> = _uiState

    override suspend fun onBackClick() {
        appRouter().back()
    }

    override suspend fun onItemClick(item: {Feature}ItemUi) {
        when (val action = item.action) {
            is {Feature}Action.Navigate -> appRouter().toComposePage(
                pageName = action.pageName,
                pageArgs = action.pageArgs,
            )
            is {Feature}Action.Web -> appRouter().to(scheme = action.url)
        }
    }
}
```

#### 2.5 Page 入口（wsCompose）

```kotlin
@Page(ComposeViewKey.{Domain}.{FEATURE})
class {Feature}Page : ComposePage() {
    @Composable
    override fun OnSetContent() {
        val pageAssembly = remember {
            {Domain}Service.pageFactory.create{Feature}PageAssembly()
        }
        val viewModel = remember(pageAssembly) {
            pageAssembly.pageViewModelProvider()
        }
        {Feature}PageView(viewModel = viewModel)
    }
}
```

#### 2.6 Composable 视图（wsCompose）

```kotlin
@Composable
internal fun {Feature}PageView(viewModel: I{Feature}PageViewModel) {
    val scope = rememberCoroutineScope()
    val uiState by viewModel.uiState.collectAsState()

    CollectPageOnResume(key = viewModel) {
        viewModel.onPageResume()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(QNTheme.colorScheme.bgPage)
    ) {
        // TitleBar
        SettingsStandardTitleBar(
            title = "页面标题",
            onBackClick = { scope.launch { viewModel.onBackClick() } }
        )
        // 内容区域
        LazyColumn(
            modifier = Modifier.fillMaxSize().bouncesEnable(false)
        ) {
            item(key = "content") {
                // 渲染 uiState 中的数据
            }
        }
    }
}
```

#### 2.7 PageFactory 注册

**接口（wsCore IUserPageFactory）**：
```kotlin
fun create{Feature}PageAssembly(): {Feature}PageAssembly
```

**实现（wsUser UserPageFactoryImpl）**：
```kotlin
override fun create{Feature}PageAssembly(): {Feature}PageAssembly {
    return {Feature}PageAssembly(
        pageViewModelProvider = {Feature}PageViewModelProvider {
            {Feature}ViewModel()
        }
    )
}
```

### 三、子类型 B 核心文件范式（列表类页面）

#### 3.1 PageViewModel 接口（wsCore）

```kotlin
interface I{Feature}PageViewModel : IStructPageViewModel
```

#### 3.2 Assembly 定义（wsCore）

```kotlin
fun interface {Feature}PageViewModelProvider {
    operator fun invoke(
        pageFlow: SharedFlow<PageLifecycleEvent>,
        pageScope: CoroutineScope,
    ): I{Feature}PageViewModel
}

data class {Feature}PageAssembly(
    val pageWidget: StructPageWidget2,
    val pageViewModelProvider: {Feature}PageViewModelProvider,
)
```

#### 3.3 Action 定义

```kotlin
internal sealed interface {Feature}Action {
    data object LoadFirstPage : {Feature}Action
    data object LoadMorePage : {Feature}Action
    data class ClickCard(val item: {Item}, val stableId: String) : {Feature}Action
    data class ClickAvatar(val item: {Item}, val stableId: String) : {Feature}Action
    data class SwipeToDelete(val item: {Item}, val stableId: String) : {Feature}Action
    data class ConfirmDelete(val item: {Item}, val stableId: String) : {Feature}Action
}
```

#### 3.4 UiState 定义

```kotlin
internal sealed class {Feature}UiState {
    data object Loading : {Feature}UiState()
    data class Error(val message: String) : {Feature}UiState()
    data class Success(
        val items: List<{Item}> = emptyList(),
        val isFinished: Boolean = false,
    ) : {Feature}UiState()
}
```

#### 3.5 CellVM 接口（wsCore）

```kotlin
interface I{Feature}CellVM {
    // 渲染字段（不可变）
    val id: String
    val title: String
    val avatarUrl: String
    val createTime: Long

    // 状态流（可变）
    val isRead: StateFlow<Boolean>

    // 语义动作
    fun onCardClick()
    fun onAvatarClick()
    fun onSwipeDelete()
    fun onConfirmDelete()
    fun onCardExposure()
}
```

#### 3.6 CellVM 实现

```kotlin
internal class {Feature}CellVM(
    val item: {Item},
    val stableId: String,
    private val isReadFlow: MutableStateFlow<Boolean>,
    private val onCardClickAction: () -> Unit,
    private val onAvatarClickAction: () -> Unit,
    private val onSwipeDeleteAction: () -> Unit,
    private val onConfirmDeleteAction: () -> Unit,
    private val onCardExposureAction: () -> Unit = {},
) : I{Feature}CellVM {

    // 渲染字段
    override val id: String = item.id
    override val title: String = item.title
    override val avatarUrl: String = item.avatarUrl
    override val createTime: Long = item.createTime

    // 状态流
    override val isRead: StateFlow<Boolean> = isReadFlow

    private var hasExposed = false

    fun markAsRead() { isReadFlow.value = true }

    // 语义动作 → 委托给 PageViewModel
    override fun onCardClick() = onCardClickAction()
    override fun onAvatarClick() = onAvatarClickAction()
    override fun onSwipeDelete() = onSwipeDeleteAction()
    override fun onConfirmDelete() = onConfirmDeleteAction()
    override fun onCardExposure() {
        if (!hasExposed) { hasExposed = true; onCardExposureAction() }
    }
}
```

#### 3.7 Repository 接口

```kotlin
interface {Feature}Repository {
    suspend fun fetchList(
        attachInfo: String = "",
        isFirstPage: Boolean = true,
    ): Result<{Feature}ListResult>

    suspend fun deleteItem(itemId: String): Result<Unit>
}

data class {Feature}ListResult(
    val items: List<{Item}>,
    val attachInfo: String,
    val isFinished: Boolean,
)
```

#### 3.8 Repository 实现

```kotlin
internal class {Feature}RepositoryImpl(
    private val useMock: Boolean = false,
) : {Feature}Repository {

    override suspend fun fetchList(
        attachInfo: String,
        isFirstPage: Boolean,
    ): Result<{Feature}ListResult> {
        if (useMock) {
            delay(500L)
            return Result.success({Feature}MockData.listResult())
        }
        return {PbReq}(attach_info = attachInfo)
            .send({PbRsp}.ADAPTER)
            .map { rsp -> rsp.to{Feature}ListResult() }
    }

    override suspend fun deleteItem(itemId: String): Result<Unit> {
        if (useMock) { delay(500L); return Result.success(Unit) }
        return {DeletePbReq}(id = itemId)
            .send({DeletePbRsp}.ADAPTER)
            .map { if (it.ret != 0) throw RuntimeException(it.msg) }
    }

    private fun {PbRsp}.to{Feature}ListResult(): {Feature}ListResult {
        return {Feature}ListResult(
            items = items.map { it.toUiModel() },
            attachInfo = attachInfo,
            isFinished = isFinished,
        )
    }
}
```

#### 3.9 UseCase

```kotlin
class Load{Feature}ListUseCase(private val repository: {Feature}Repository) {
    suspend fun execute(
        attachInfo: String = "",
        isFirstPage: Boolean = true,
    ): Result<{Feature}ListResult> = repository.fetchList(attachInfo, isFirstPage)
}

class Delete{Feature}UseCase(private val repository: {Feature}Repository) {
    suspend fun execute(itemId: String): Result<Unit> = repository.deleteItem(itemId)
}
```

#### 3.10 PageViewModel 实现

```kotlin
class {Feature}PageViewModel(
    private val pageArgs: {Feature}PageArgs,
    private val rootWidget: StructPageWidget2,
    pageFlow: SharedFlow<PageLifecycleEvent>,
    pageScope: CoroutineScope,
    repository: {Feature}Repository = {Feature}RepositoryImpl(),
) : StructPageViewModel(
    controller = FrameworkService.createFlexFeedsController(
        rootWidget = rootWidget,
        pageItem = { rootWidget.findPageItem() },
    ),
    pageFlow = pageFlow,
    pageScope = pageScope,
), I{Feature}PageViewModel {

    private val deleteUseCase = Delete{Feature}UseCase(repository)
    private val _uiState = MutableStateFlow<{Feature}UiState>({Feature}UiState.Loading)
    private val cellVMs = mutableMapOf<String, {Feature}CellVM>()

    init {
        installLocalSkeletonIfNeeded()
        injectCellVMFactory()
        _uiState.value = {Feature}UiState.Success()
        observePageLifecycle()
    }

    internal fun dispatch(action: {Feature}Action) {
        when (action) {
            is {Feature}Action.ClickCard -> handleClickCard(action.item, action.stableId)
            is {Feature}Action.ClickAvatar -> handleClickAvatar(action.item, action.stableId)
            is {Feature}Action.ConfirmDelete -> handleConfirmDelete(action.item, action.stableId)
            else -> { /* Struct controller 处理 */ }
        }
    }

    internal fun createCellVM(item: {Item}, stableId: String): I{Feature}CellVM {
        return {Feature}CellVM(
            item = item,
            stableId = stableId,
            isReadFlow = MutableStateFlow(item.isRead),
            onCardClickAction = { dispatch({Feature}Action.ClickCard(item, stableId)) },
            onAvatarClickAction = { dispatch({Feature}Action.ClickAvatar(item, stableId)) },
            onSwipeDeleteAction = { dispatch({Feature}Action.SwipeToDelete(item, stableId)) },
            onConfirmDeleteAction = { dispatch({Feature}Action.ConfirmDelete(item, stableId)) },
            onCardExposureAction = { reportCardExposure(item) },
        ).also { cellVMs[stableId] = it }
    }

    // ==================== 交互处理 ====================

    private fun handleClickCard(item: {Item}, stableId: String) {
        reportCardClick(item, stableId)
        cellVMs[stableId]?.markAsRead()
        if (item.schema.isNotBlank()) {
            pageScope.launch { appRouter().to(scheme = item.schema) }
        }
    }

    private fun handleConfirmDelete(item: {Item}, stableId: String) {
        pageScope.launch {
            deleteUseCase.execute(item.id)
                .onSuccess {
                    val subCtrl = (rootWidget as? {Feature}PageWidget)
                        ?.pager?.mainChannel?.subTabFeedsCtrl
                    subCtrl?.removeFeedsItem { it.flexDto.idStr == stableId }
                    cellVMs.remove(stableId)
                }
                .onFailure { appAlert().showToast("删除失败，请重试") }
        }
    }

    // ==================== 骨架安装 ====================

    private fun installLocalSkeletonIfNeeded() {
        if (rootWidget.titleBar != null) return
        val localRepo = rootWidget.pageConfig.dataRepo as? IStructDataLocalRepo ?: return
        val skeleton = localRepo.createLocalResetPageWidget() ?: return
        rootWidget.buildPageWithManual2 {
            titleBar = skeleton.titleBar
            pager = skeleton.pager
            layers = skeleton.layers
        }
    }

    private fun injectCellVMFactory() {
        (rootWidget as? {Feature}PageWidget)?.dataRepo?.cellVMFactory = ::createCellVM
    }

    // ==================== 页面生命周期 ====================

    private fun observePageLifecycle() {
        pageScope.launch {
            pageFlow.collect { event ->
                when (event) {
                    PageLifecycleEvent.ON_RESUME -> reportPageEnter()
                    PageLifecycleEvent.ON_PAUSE -> reportPageExit()
                    else -> {}
                }
            }
        }
    }
}
```

#### 3.11 PageWidget（列表容器）

```kotlin
class {Feature}PageWidget internal constructor(
    pageArgs: {Feature}PageArgs,
    internal val dataRepo: {Feature}DataRepo = {Feature}DataRepo(pageArgs),
) : StructPageWidget2(
    StructPageConfig(
        dataRepo = {Feature}SkeletonRepo(pageArgs, dataRepo),
        defaultChannelInfo = IChannelInfo.new(),
        fixTitleBarAboveContent = true,
        enableHeader = false,
        enableFooter = true,
        expandBottomSafeAreaForPage = true,
        expandBottomSafeAreaForList = true,
        defaultStatusBarColorMode = StatusBarColorMode.ALWAYS_DARK_ICON,
    )
)
```

#### 3.12 Page 入口（wsCompose）

```kotlin
@Page(ComposeViewKey.{Domain}.{FEATURE}_PAGE)
internal class {Feature}Page : ComposePage() {
    override fun sceneName() = "{Feature}"

    @Composable
    override fun OnSetContent() {
        val pageScope = rememberCoroutineScope()
        val pageArgs = rememberedPageArgs<{Feature}PageArgs>()
            ?: {Feature}PageArgs()
        val pageAssembly = remember(pageArgs) {
            {Domain}Service.pageFactory.create{Feature}PageAssembly(pageArgs)
        }
        StructComposePage4VM(
            pageViewModel = {
                pageAssembly.pageViewModelProvider(
                    pageLifecycleFlow.lifecycleFlow,
                    pageScope,
                )
            },
        )
    }
}
```

#### 3.13 PageFactory 注册

**接口（wsCore IUserPageFactory）**：
```kotlin
fun create{Feature}PageAssembly(pageArgs: {Feature}PageArgs): {Feature}PageAssembly
```

**实现（wsUser UserPageFactoryImpl）**：
```kotlin
override fun create{Feature}PageAssembly(pageArgs: {Feature}PageArgs): {Feature}PageAssembly {
    val pageWidget = {Feature}PageWidget(pageArgs)
    return {Feature}PageAssembly(
        pageWidget = pageWidget,
        pageViewModelProvider = {Feature}PageViewModelProvider { pageFlow, pageScope ->
            {Feature}PageViewModel(
                pageArgs = pageArgs,
                rootWidget = pageWidget,
                pageFlow = pageFlow,
                pageScope = pageScope,
            )
        },
    )
}
```

### 四、如何选择子类型

| 场景 | 选择 | 参考实现 |
|------|------|----------|
| 设置页、表单页、关于页等静态/少量数据页面 | **子类型 A**（纯 Compose UI） | `SettingsPage` / `AboutPage` / `EditProfilePage` |
| 消息列表、粉丝列表、评论列表等需要分页加载的列表页 | **子类型 B**（Struct 列表 + 自定义 VM） | `MsgNewFansPage` / `MsgCommentAtPage` / `MsgLikeCollectPage` |

**判断标准**：
- 如果页面核心是一个需要分页加载、下拉刷新、上拉加载更多的列表 → 子类型 B
- 如果页面核心是表单、设置项、静态内容展示 → 子类型 A
- 如果不确定，默认选择子类型 A，后续需要列表能力时再迁移

### 五、注册链范式

新增普通页面需要完成以下注册：

1. **路由 Key**：在 `qnFramework/.../ComposeViewKey.kt` 中新增常量
2. **PageFactory 接口**：在 `wsCore` 对应 Service 接口中暴露 `create{Feature}PageAssembly()`
3. **PageFactory 实现**：在 `wsUser/setup/UserPageFactoryImpl.kt` 中实现
4. **Cell 注册**（仅子类型 B）：在 `wsCompose/.../setup/WsFeedsItemCardService.kt` 中注册 CellRegistry

---

## 执行流程

### 1️⃣ 判断进度

按照 `ai-coding-workflow` 中的进度判断规则，先读取对应需求的进度表，再用真实产物校验，确定当前已完成到哪一步。

### 2️⃣ 执行下一步

根据进度判断结果，调用对应的 Skill 执行下一步：

1. **优先调用 Skill**：尝试通过 `use_skill` 调用 workflow 路由表中指定的 Skill 名称。
2. **Skill 不可用时回退到文档**：如果对应的 Skill 未注册或不可用，则在 `skills/` 目录下查找同名的 `.md` 文档（如 `skills/design-api-protocol.md`），读取该文档内容并按照文档中的指令要求执行。

每一步执行完成后，先更新进度表，再自动判断进度并继续推进，直到遇到以下情况之一时停下：

- **⚠️ 技术方案确认（强制中断）**：Step 5（技术方案设计）完成并输出技术方案文档后，**必须立即停下**，将技术方案内容展示给用户评审确认。**严禁**在用户确认之前继续执行后续步骤（Step 6/7/8 等编码步骤）。只有用户明确确认（如回复"确认"、"OK"、"继续"等）后，才可继续推进。
- **需要用户提供信息**：缺少必要输入（如 TAPD 链接、Figma 设计稿链接）时，停下向用户索要。
- **遇到人工步骤**：Step 2（需求评审）、Step 13（测试执行）为人工步骤，到达时停下提示用户。
- **全部完成**：所有步骤执行完毕。

### 3️⃣ 输出格式

每完成一个步骤后，输出进度摘要：

```
✅ 已完成：Step {N} — {名称}
📌 当前进度：{已完成步骤概览}
📋 下一步：Step {N+1} — {名称}
📥 需要你提供：{该步骤所需的输入，如果有的话}
```

## 关键规则

1. **架构约束**：所有代码产物**禁止**使用纯 Struct（品字形）结构（子类型 B 中复用 Struct 列表能力是允许的，但页面逻辑必须由自定义 ViewModel 驱动），必须遵循通用 MVVM 分层。技术方案设计阶段需明确 `PageArgs` / `ViewModel` / `View（Composable）` / `Repository` 的职责划分，遵循 `通用架构规范/RULE.mdc` 中的分层要求。
2. **范式优先**：编码阶段必须严格参照上述代码范式中的模板，先判断子类型（A 或 B），再选择对应模板，不要发明新模式。
3. **并行执行**：遵循 workflow 中定义的并行组规则（组 A：Step 3 + 4 可并行；组 B：Step 6 + 7 + 8 可并行）。
4. **信息不足时停下**：如果因信息不完整无法做出确定的决策，必须停下来向用户确认，确认完成后再继续执行。
5. **不要跳步**：严格按照依赖图顺序执行，不要跳过任何步骤。
6. **迭代模式自动识别**：workflow 会自动判断新建模式还是迭代模式，无需用户手动指定。
7. **参考实现**：编码时优先参考以下已有实现：
   - 子类型 A（表单/设置类）：`wsUser/.../settings/main/vm/SettingsPageViewModel.kt` + `wsCompose/.../settings/main/page/SettingsPage.kt`
   - 子类型 B（列表类）：`wsUser/.../message/newFans/` 全套文件（PageWidget / DataRepo / PageViewModel / CellVM / Repository / UseCase）
