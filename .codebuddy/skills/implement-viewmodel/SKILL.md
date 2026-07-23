---
name: implement-viewmodel
description: Use when 用户已经有需求文档、上报文档、VM 接口和接口层代码，准备按面向 UI 设计的方式完成 ViewModel 实现或迭代修改。
---

# ViewModel 实现编码

## 目标
根据需求文档、上报需求文档、VM 接口定义和接口实现代码，自动生成 ViewModel 完整实现代码。遵循**面向 UI 设计**的 VM 理念，将 90% 的业务逻辑沉淀在纯 KMM 实现层，UI 层仅消费 VM 接口暴露的属性和函数，实现逻辑与 UI 彻底解耦。

---

## 核心设计理念：面向 UI 设计

> 参考规范：`docs/本地知识库/开发规范/如何设计一个优雅的ViewModel.md`

### VM 接口设计原则

一个 VM 接口应该**只包含**以下 4 类成员：
1. **固定属性**：基础数据类型（String、Int、Boolean 等），UI 直接消费
2. **可变属性**：统一用 `StateFlow` 或 `SharedFlow` 包装，表达可变状态
3. **与 UI 层交互的函数**：`onXxxClick()`、`onXxxChange()` 等纯交互回调
4. **子组件的 VM**：组合模式，子 UI 组件持有子 VM 接口

### VM 接口中**不应该出现**：
- ❌ 数据结构 model 类（如 `IUserInfo`、`IAdOrder`）—— 说明 UI 与数据强耦合
- ❌ 业务层的 controller / presenter —— 说明 UI 与逻辑强耦合
- ❌ `var` 可变属性 —— 修改逻辑应在 VM 实现类执行
- ❌ `MutableStateFlow` / `MutableSharedFlow` —— flow 的更新应在 VM 实现类执行

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
- ViewModel 接口定义代码（Step 4 design-viewmodel-interface 的输出）
- 接口实现代码（Step 8 implement-api-layer 的输出）

---

## 输入

| 参数 | 说明 | 是否必须 |
|------|------|----------|
| 需求文档 | 评审通过的需求文档（`docs/component` 下的 `.md`），包含业务逻辑、交互规则、异常场景 | ✅ 必须 |
| 上报需求文档 | 上报事件清单、字段映射、触发条件（`docs/component` 下的 `_report.md`） | ✅ 必须 |
| ViewModel 接口定义代码 | VM 接口（面向 UI 设计的属性 + 函数 + 子 VM） | ✅ 必须 |
| 接口实现代码 | Repository 和数据转换扩展 | ✅ 必须 |
| UI 组件代码 | Step 5 输出的 UI 代码，辅助理解 UI 状态映射关系 | 可选 |
| 已有 ViewModel 实现代码 | 已有页面的完整 ViewModel 实现 | 迭代模式必须 |

---

## 模式判断

```
CHECK: 当前页面目录下是否存在 diff/ 子目录，且其中包含 {页面名下划线}_diff.md？
  ├── YES → ✏️ 迭代模式：读取已有 VM 实现，只修改 diff 涉及的部分
  └── NO  → 🆕 新建模式：全量生成完整 ViewModel 实现
```

---

## 输出

ViewModel 完整实现代码，包含：
- **VM 实现类**：实现 VM 接口，将 model 数据转换为 UI 友好的属性
- **业务逻辑内聚**：路由跳转、上报埋点、日志记录全部封装在实现类内部
- **子组件 VM 实现类**（如有）：独立的子 VM 实现，支持不同业务场景复用同一 UI 组件
- **上报埋点调用**（按上报需求文档中的触发条件插入）

---

## 执行步骤

### Step 0：模式判断

- **新建模式**：页面目录下不存在 `diff/` 子目录或 diff 需求文档，直接执行 Step 1
- **迭代模式**：页面目录下存在 `diff/{页面名下划线}_diff.md`，先读取已有 ViewModel 实现代码，再结合 diff 需求分析变更部分，只修改 diff 涉及的部分

**迭代模式修改规则：**
- 对新增的 VM 接口属性：在实现类中新增对应的 `build{PropertyName}()` 方法或直接赋值
- 对新增的交互函数：在实现类中新增 `override fun onXxx()` 并内聚业务逻辑
- 对修改的交互函数：在已有实现方法中调整逻辑
- 对新增的子 VM：新建子 VM 实现类
- 对新增的上报事件：在对应交互函数实现中插入上报调用
- **不修改**已有逻辑的其他部分，不重新输出完整文件
- 修改处加注释 `// [diff] 新增/修改：{TAPD需求标题}`

---

### Step 1：读取所有输入

1. 读取需求文档 `docs/component/{模块名}/{页面名驼峰}/{页面名下划线}.md`
2. 读取上报需求文档 `docs/component/{模块名}/{页面名驼峰}/{页面名下划线}_report.md`
3. 读取已生成的 ViewModel 接口代码
4. 读取已生成的接口层代码（Repository）
5. 阅读设计规范 `docs/本地知识库/开发规范/如何设计一个优雅的ViewModel.md`

---

### Step 2：面向 UI 设计 VM 实现

遵循"面向 UI 设计"理念实现 VM 类。核心规则：

**属性转换——将 model 数据转换为 UI 直接消费的值：**
```kotlin
// VM 接口（接口层）—— 面向 UI，只暴露 UI 需要的值
interface I{PageName}VM {
    val title: String               // 固定属性
    val avatarUrl: String           // 已转换为 UI 可直接使用的值
    val itemsFlow: StateFlow<List<I{ItemName}VM>>  // 可变属性用 Flow 包装
    fun onRefresh()                 // 与 UI 层交互的函数
    fun onItemClick(index: Int)     // 交互回调，参数只传 UI 层已知的值
}

// VM 实现类（实现层）—— 内聚所有业务逻辑
class {PageName}VM(
    private val pageArgs: {PageName}PageArgs = {PageName}PageArgs(),  // 页面参数作为入参
    private val repository: {PageName}Repository,
) : I{PageName}VM {
    
    // 固定属性：从入参转换为 UI 值
    override val title: String = pageArgs.buildTitle()
    
    // 复杂逻辑属性：通过私有方法构建
    override val avatarUrl: String = buildAvatarUrl()
    
    // ── 可变属性简化写法 ──
    // MutableStateFlow 是 StateFlow 的子类型，
    // 接口声明 StateFlow<T>，实现类直接用 override val = MutableStateFlow(...) 即可，
    // 无需拆成 private val _xxx + override val xxx 两行。
    override val itemsFlow = MutableStateFlow<List<I{ItemName}VM>>(emptyList())
    override val isLoading = MutableStateFlow(false)
    override val errorMessage = MutableStateFlow<String?>(null)
    
    // ── 交互函数：业务逻辑内聚 ──
    // 路由跳转、上报、日志等全部在 VM 实现类内部完成，
    // 不通过 callback 参数外泄给 Widget / UI 层。
    override fun onItemClick(index: Int) {
        val item = dataModel.items[index]
        // 路由跳转（VM 内部直接调用 AppRouterEx）
        AppRouterEx.toComposePage(
            pageName = ComposeViewKey.Detail.DETAIL_PAGE,
            pageArgs = DetailPageArgs(itemId = item.id),
        )
        // 上报埋点
        appReport().reportBeacon("item_click", mapOf("id" to item.id))
        // 日志
        qnFileLog()?.logI("TAG", "Item clicked: ${item.id}")
    }
    
    // 交互函数——业务逻辑完全内聚在 VM 内部
    // 即使涉及乐观更新、网络请求、失败回滚等复杂流程，
    // 也应在 VM 内部自行管理协程和状态，不通过回调外泄。
    override fun onFollowClick() {
        // VM 内部自行处理乐观更新 + 网络请求 + 失败回滚
    }
    
    // 私有方法：业务判断逻辑
    private fun buildAvatarUrl(): String {
        return if (dataModel.isMySelf) {
            appLogin().getMainLoginUserInfo().getAIIconUrl()
        } else {
            dataModel.headUrl
        }
    }
}
```

**关键要点：**
- VM 接口不暴露任何 model 类、DTO、PB 对象
- 所有业务判断（`isCp`、`isMySelf` 等）在实现类内部完成
- **业务逻辑内聚**：路由跳转、上报、日志等全部在 VM 实现类内部完成，不通过 callback 参数外泄给 Widget / UI 层
- **需要用到的 model 类（如 PageArgs）作为 VM 实现类的构造函数入参**，而非在 Widget 层拆解后传入多个基础类型
- **MutableStateFlow 简化写法**：接口声明 `StateFlow<T>`，实现类直接 `override val xxx = MutableStateFlow(...)` 即可，无需拆成 `private val _xxx` + `override val xxx` 两行
- **业务逻辑完全内聚**：即使涉及乐观更新、网络请求、失败回滚等复杂流程，也应在 VM 内部自行管理协程和状态，不通过回调或注入与外部耦合。VM 可自己持有 `CoroutineScope`、UseCase 和 Job
- **子 VM 交互逻辑内聚**：子 VM 实现类应直接持有业务数据（如 `schema`、`itemId`），在交互方法内部直接处理路由跳转、上报等逻辑，不通过 `onClickAction: () -> Unit` 回调与父 VM 耦合
- 子组件复用：同一个 UI 组件通过不同的 VM 实现类支持不同业务场景

---

### Step 3：子组件 VM 设计（如有）

当 UI 组件需要被多个业务场景复用时，为每个场景创建独立的 VM 实现类：

```kotlin
// 接口层——纯 UI 语义
interface IUserIconVM {
    val iconSize: Int
    val userIcon: String
    fun onIconClick()
}

// 实现层——业务用户头像
class UserIconVM(private val userInfo: IUserInfo) : IUserIconVM {
    override val iconSize = if (userInfo.isCp) 50 else 40
    override val userIcon = buildIconUrl()
    
    override fun onIconClick() {
        // 路由跳转（VM 内部直接调用，不通过 callback 外泄）
        when {
            userInfo.isCpUser -> AppRouterEx.toComposePage(
                pageName = ComposeViewKey.User.CP_PAGE,
                pageArgs = CpPageArgs(userId = userInfo.suid),
            )
            userInfo.isMySelf -> AppRouterEx.toComposePage(
                pageName = ComposeViewKey.User.HOME_PAGE,
                pageArgs = UserHomePageArgs(userId = userInfo.suid),
            )
            else -> AppRouterEx.toComposePage(
                pageName = ComposeViewKey.User.GUEST_PAGE,
                pageArgs = GuestPageArgs(userId = userInfo.suid),
            )
        }
        // 上报
        appReport().reportBeacon("user_icon_click", mapOf("user_id" to userInfo.suid))
    }
    
    private fun buildIconUrl(): String = if (userInfo.isMySelf) {
        appLogin().getMainLoginUserInfo().getAIIconUrl()
    } else {
        userInfo.headUrl
    }
}

// 实现层——广告主头像（复用同一 UI 组件）
class AdvertiserIconVM(private val adOrder: IAdOrder) : IUserIconVM {
    override val iconSize = 40
    override val userIcon = adOrder.info.advertiserIcon
    
    override fun onIconClick() {
        adFeedsManager().jumpToAdDetail(adOrder)
        adReport().reportAdClick(adOrder)
    }
}
```

### 真实项目示例：MineProfileHeaderVM

以下是个人页 Header VM 的真实实现，展示了上述所有规范的综合运用：

```kotlin
// ── 子 VM 实现类：快捷操作项 ──
// ✅ 子 VM 内部直接持有业务数据（schema），内聚路由跳转逻辑
// ❌ 不通过 onClickAction: () -> Unit 回调与父 VM 耦合
internal class MineProfileQuickActionVMImpl(
    override val title: String,
    override val iconUrl: String,
    override val badgeText: String,
    private val schema: String,
) : IMineProfileQuickActionVM {
    override fun onClick() {
        navigateBySchema(schema)
    }
}

// ── 子 VM 实现类：Banner 卡片 ──
internal class MineProfileBannerVMImpl(
    override val title: String,
    override val subtitle: String,
    override val iconUrl: String,
    override val ctaText: String,
    private val schema: String,
) : IMineProfileBannerVM {
    override fun onClick() {
        navigateBySchema(schema)
    }
}

// ── Header 主 VM 实现类 ──
class MineProfileHeaderVM(
    // ✅ 需要用到的 model 类作为构造函数入参
    private val pageArgs: MineProfilePageArgs = MineProfilePageArgs(),
) : IMineProfileHeaderVM {

    // ✅ 业务依赖在 VM 内部自行持有，不由外部注入
    private val toggleFollowUseCase = ToggleFollowingUseCase(ProfileFollowingRepositoryImpl())
    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var followToggleJob: Job? = null

    // ✅ 固定属性：从入参直接转换
    override val isHost: Boolean = pageArgs.isHostPage()

    // ✅ 可变属性简化写法：直接 override val = MutableStateFlow(...)
    override val coverUrl = MutableStateFlow("")
    override val avatarUrl = MutableStateFlow("")
    override val nickname = MutableStateFlow("")
    override val weishiId = MutableStateFlow("")
    override val verifiedText = MutableStateFlow("")
    override val intro = MutableStateFlow("")
    override val gender = MutableStateFlow(0)
    override val age = MutableStateFlow("")
    override val region = MutableStateFlow("")
    override val followCountText = MutableStateFlow("0")
    override val fansCountText = MutableStateFlow("0")
    override val likedCountText = MutableStateFlow("0")
    override val isIntroExpanded = MutableStateFlow(false)
    override val followStatus = MutableStateFlow(0)
    override val quickActions = MutableStateFlow<List<IMineProfileQuickActionVM>>(emptyList())
    override val banner = MutableStateFlow<IMineProfileBannerVM?>(null)

    // ✅ 交互方法：路由跳转内聚在 VM 内部，不通过 callback 外泄
    override fun onFollowingClick() {
        AppRouterEx.toComposePage(
            pageName = ComposeViewKey.User.FOLLOWING_PAGE,
            pageArgs = FollowingPageArgs(
                targetUserId = pageArgs.targetUserId,
                source = MINE_PROFILE_FOLLOW_STAT_SOURCE,
            ),
        )
    }

    override fun onFansClick() {
        AppRouterEx.toComposePage(
            pageName = ComposeViewKey.User.FANS_PAGE,
            pageArgs = ProfileFansPageArgs(),
        )
    }

    override fun onEditProfileClick() {
        AppRouterEx.toComposePage(
            pageName = ComposeViewKey.Setting.EDIT_PROFILE,
            pageArgs = emptyPageArgs(),
        )
    }

    // ✅ 关注/取关逻辑完全内聚在 VM 内部，包括乐观更新、网络请求、失败回滚
    override fun onFollowClick() {
        if (followToggleJob?.isActive == true) return
        val currentStatus = followStatus.value
        val targetUserId = pageArgs.resolvedPersonId()
        if (targetUserId.isBlank()) return
        followStatus.value = toggleFollowUseCase.predictNewStatus(currentStatus)
        followToggleJob = coroutineScope.launch {
            toggleFollowUseCase.execute(targetUserId, currentStatus)
                .onSuccess { followStatus.value = it }
                .onFailure { followStatus.value = currentStatus }
        }
    }

    override fun onAvatarClick() {
        if (isHost) onEditProfileClick()
    }
}

// ┅ 文件级私有路由辅助方法，子 VM 和主 VM 共用 ┅
private fun navigateBySchema(schema: String) {
    val trimmed = schema.trim()
    if (trimmed.isBlank()) return
    if (trimmed.isMessageSchema()) {
        AppRouterEx.toComposePage(
            pageName = ComposeViewKey.User.MESSAGE_PAGE,
            pageArgs = MessagePageArgs(
                source = trimmed.extractMessageSource(),
                enterFrom = "mine_profile",
            ),
        )
    } else {
        AppRouterEx.toScheme(trimmed)
    }
}
```

**该示例体现的规范要点：**
1. **pageArgs 作为入参**：VM 内部持有 `MineProfilePageArgs`，直接从中获取 `targetUserId`、`isHostPage()` 等信息
2. **MutableStateFlow 简化写法**：16 个可变属性全部用 `override val xxx = MutableStateFlow(...)` 一行搞定
3. **路由跳转内聚**：`onFollowingClick()`、`onFansClick()`、`onEditProfileClick()` 等全部在 VM 内部直接调用 `AppRouterEx`
4. **业务逻辑完全内聚**：`onFollowClick()` 内部自行管理乐观更新、网络请求、失败回滚等复杂流程，VM 自己持有 `CoroutineScope`、`ToggleFollowingUseCase` 和 `followToggleJob`，不通过回调或注入与外部耦合
5. **子 VM 交互逻辑内聚**：`MineProfileQuickActionVMImpl` 和 `MineProfileBannerVMImpl` 直接持有 `schema`，在 `onClick()` 内部直接调用 `navigateBySchema()` 处理路由，不通过 `onClickAction: () -> Unit` 回调与父 VM 耦合
6. **共用路由辅助方法**：`navigateBySchema()` 提取为文件级私有函数，子 VM 和主 VM 均可直接调用

---

### Step 3.5：组件间通信——findSingleWidgetVM

在 Struct 品字形架构中，页面由多个 Widget 组成（TitleBar、Header、Pager、Cell 等），每个 Widget 持有自己的 VM。组件之间需要通信时，**通过 `findSingleWidgetVM<T>()` 在整个 StructPageWidget 树范围内查找目标 VM**，而非通过回调、注入或事件总线。

#### 核心 API

```kotlin
import com.tencent.news.core.page.model.StructWidgetEx.findSingleWidgetVM

// 在任意 StructWidget 上调用，查找同一 PageWidget 树内的目标 VM
inline fun <reified T : IStructWidgetVM> StructWidget.findSingleWidgetVM(
    noinline condition: WidgetCondition? = null,
): T?
```

**查找范围**：
- 先在当前 `pageWidget` 树内查找
- 如果当前是多 tab 嵌套的子页面，还会自动向上查找 `parentRootWidget`（外层父页面）

#### 场景 1：PageViewModel 更新子组件 VM

PageViewModel 在数据加载完成后，通过 `findSingleWidgetVM` 找到子组件 VM 并更新状态：

```kotlin
// MineProfilePageViewModel.kt
private fun applyPageState(state: MineProfilePageState) {
    _isHost.value = state.isHost
    // ...

    // ✅ 通过 findSingleWidgetVM 找到 Header VM 并更新数据
    rootWidget.findSingleWidgetVM<MineProfileHeaderVM>()
        ?.updateFromPageState(state)

    // ✅ 通过 findSingleWidgetVM 找到 TitleBar VM 并更新标题
    rootWidget.findSingleWidgetVM<IMineProfileTitleBarVM>()
        ?.updateTitle(state.profile?.nickname.orEmpty())
}
```

#### 场景 2：Cell VM 通知页面级组件

列表中的 Cell VM 在被选中时，通过 `findSingleWidgetVM` 找到 TitleBar VM 并更新选中状态：

```kotlin
// DramaPlayCellVM.kt
class DramaPlayCellVM(
    private val vmItem: IFeedsVMItem,
    private val dramaFeed: stDramaFeed,
    private val drama: stDrama?,
) : IDramaPlayCellVM {

    override fun onSelected() {
        // ✅ Cell 通过 IFeedsVMItem 向上查找 TitleBar VM
        vmItem.findSingleWidgetVM<IDramaPlayTitleBarVM>()
            ?.updateSelection(dramaFeed.num)
    }
}
```

> **注意**：`IFeedsVMItem` 实现了 `ILogicContextHolder`，可通过 `StructPageWidgetEx.findSingleWidgetVM` 扩展方法查找。

#### 场景 3：跨父子页面通信

当组件挂载在父页面（如首页），而触发点在子页面（如推荐 Tab）时，需要通过 `parentRootWidget` 向上查找：

```kotlin
// 评论面板 Layer 挂载在首页顶层（HomePageWidget），
// 评论按钮在推荐 Tab 的子页面中，需通过 parentRootWidget 找到父页面再查找 VM
val rootWidget = pageVM?.pageRootWidget
val targetWidget = rootWidget?.parentRootWidget ?: rootWidget
targetWidget?.findSingleWidgetVM<ICommentPanelLayerVM>()?.show(...)
```

#### 使用规范

| 规则 | 说明 |
|------|------|
| **优先使用 VM 接口类型** | 泛型参数优先传 VM 接口（如 `IMineProfileTitleBarVM`），而非实现类，保持解耦 |
| **允许使用实现类类型** | 当需要调用实现类独有方法（如 `updateFromPageState`）时，可传实现类类型（如 `MineProfileHeaderVM`） |
| **空安全处理** | 返回值可能为 null（组件未安装或类型不匹配），必须用 `?.` 安全调用 |
| **不要在构造函数中调用** | VM 构造时 Widget 树可能尚未完全安装，应在业务方法中按需查找 |
| **不要缓存查找结果** | Widget 树可能因刷新而重建，每次需要时重新查找 |

---

### Step 4：页面级 ViewModel 实现

对于需要页面级状态管理的场景（如加载态、分页、刷新），使用 ViewModel + StateFlow：

```kotlin
class {PageName}ViewModel(
    private val pageArgs: {PageName}PageArgs = {PageName}PageArgs(),
    private val repository: {PageName}Repository,
) : ViewModel() {
    // ── 可变属性简化写法 ──
    // MutableStateFlow 是 StateFlow 的子类型，
    // 接口声明 StateFlow<T>，实现类直接 override val = MutableStateFlow(...) 即可
    override val isLoading = MutableStateFlow(false)
    override val errorMessage = MutableStateFlow<String?>(null)
    override val items = MutableStateFlow<List<I{ItemName}VM>>(emptyList())
    
    init {
        loadData()
    }
    
    // 交互函数——面向 UI 的简单回调
    fun onRefresh() {
        loadData(isRefresh = true)
    }
    
    fun onLoadMore() {
        loadMore()
    }
    
    fun onItemClick(index: Int) {
        val currentItems = items.value
        if (index in currentItems.indices) {
            val item = currentItems[index]
            // 业务逻辑内聚：路由跳转在 VM 内部直接完成
            AppRouterEx.toComposePage(
                pageName = ComposeViewKey.Detail.DETAIL_PAGE,
                pageArgs = DetailPageArgs(itemId = item.id),
            )
            appReport().reportBeacon("item_click", mapOf("id" to item.id))
        }
    }
    
    private fun loadData(isRefresh: Boolean = false) {
        viewModelScope.launch {
            if (!isRefresh) isLoading.value = true
            repository.fetchData().fold(
                onSuccess = { data ->
                    items.value = data.map { buildItemVM(it) }
                    isLoading.value = false
                    errorMessage.value = null
                },
                onFailure = { error ->
                    isLoading.value = false
                    errorMessage.value = error.message ?: "加载失败"
                }
            )
        }
    }
    
    // 将 model 转换为子 VM
    private fun buildItemVM(model: {ItemModel}): I{ItemName}VM {
        return {ItemName}VM(model, repository)
    }
}
```

> **注意**：不要使用 `sealed class UIState(Loading/Error/Success)` 封装状态，应将状态拆分为独立的 StateFlow 属性（如 `isLoading`、`errorMessage`、`items`）。

---

### Step 5：集成上报埋点

上报框架、参数填写、落点约定和模块复用规则，统一参考：

- `docs/本地知识库/开发规范/如何接入上报埋点.md`

**面向 UI 设计下的上报规则：**
- 上报调用**只出现在 VM 实现类**的交互函数中，不在 UI 层调用
- UI 层只触发 `vm.onXxxClick()` 等回调，不感知上报逻辑
- 上报参数从 VM 实现类内部持有的 model 中获取，不需要 UI 层传入

如果实现过程中遇到**通用能力不知道怎么写**（如登录、路由、分享、弹窗、DT、上报、系统能力、Bridge 等），在写 `TODO` 之前必须先查询：

- `qnPlatform/src/commonMain/kotlin/com/tencent/news/core/platform/QnPlatformLogic.kt` 中的 `IPlatformLogic`
- `qnFramework/src/commonMain/kotlin/com/tencent/news/core/platform/QnFrameworkLogic.kt`

确认仓库里确实没有可复用能力后，才允许写 `TODO`。

如果上报需求文档不存在或为空，则跳过上报埋点集成，并在代码中标注 `// TODO: 接入上报埋点`。

---

### Step 6：生成代码文件

在 `shared/src/commonMain/kotlin/com/tencent/weishi/module/{模块名}/{功能名}/` 下生成：

```
module/{模块名}/{功能名}/
├── vm/
│   ├── {PageName}VM.kt              # 页面 VM 实现类（实现 VM 接口）
│   ├── {SubComponent1}VM.kt         # 子组件 VM 实现类
│   └── ...
└── {PageName}ViewModel.kt           # 页面级 ViewModel（如需状态管理）
```

---

### Step 7：验证实现完整性

1. VM 接口中不出现任何 model 类、controller、`var`、`MutableStateFlow`
2. VM 实现类内聚了所有业务逻辑（路由、上报、日志、数据转换），不通过 callback 外泄
3. UI 层只消费 VM 接口暴露的属性和函数，不直接访问 model
4. 同一 UI 组件被不同业务场景复用时，通过不同 VM 实现类区分
5. 成功和失败路径都有对应的状态更新
6. 分页逻辑正确（isRefresh 和追加加载的区分）
7. 上报埋点位置与上报需求文档中的触发条件一致
8. **MutableStateFlow 简化写法**：所有可变属性使用 `override val xxx = MutableStateFlow(...)` 一行声明，不拆成 `private val _xxx` + `override val xxx` 两行
9. **model 类作为入参**：需要用到的 model 类（如 PageArgs）作为 VM 实现类的构造函数入参，而非在 Widget 层拆解后传入多个基础类型
10. **业务逻辑完全内聚**：所有交互逻辑（包括涉及乐观更新、网络请求、失败回滚的复杂流程）均在 VM 内部自行管理，不通过回调或注入与外部耦合
11. **子 VM 交互逻辑内聚**：子 VM 实现类应直接持有业务数据（如 `schema`、`itemId`），在交互方法内部直接处理路由跳转、上报等逻辑，不通过 `onClickAction: () -> Unit` 回调与父 VM 耦合
12. **组件间通信使用 findSingleWidgetVM**：跨组件通信（如 PageViewModel 更新 Header VM、Cell 通知 TitleBar）统一通过 `findSingleWidgetVM<T>()` 在 Widget 树中查找目标 VM，不通过回调注入、事件总线或全局状态

---

## 示例调用

**用户输入：**
> 根据找剧页需求实现 ViewModel

**执行流程：**
1. 读取需求文档、上报需求文档、VM 接口和接口层代码
2. 按照"面向 UI 设计"理念，将 model 转换为 VM 接口暴露的属性
3. 实现 VM 类：业务判断、路由跳转、上报埋点全部内聚在实现层
4. 对子组件（如剧集卡片）创建独立 VM 实现类，支持复用
5. 生成代码到 `vm/` 目录和 `{PageName}ViewModel.kt`
