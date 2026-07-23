---
name: "dev-viewmodel-design"
description: "KMM/Compose 场景下的 ViewModel 设计与开发 Skill。用于新业务 VM 的接口设计、实现开发，重点确保 VM 面向 UI 设计、分层合理、可复用与可扩展。"
keywords: "ViewModel,VM,KMM,Compose,qnCore,qnCompose,接口设计,面向UI设计,VM开发,VM代码生成,ViewModel代码生成,VM重构"
triggers:
  - "设计新的 ViewModel"
  - "帮我设计 VM 接口"
  - "帮我生成 VM 相关代码"
  - "帮我生成 ViewModel 代码"
  - "KMM Compose VM 规范"
  - "qnCore VM 接口怎么定义"
  - "如何实现一个新的 ViewModel"
  - "重构现有 VM"
  - "ViewModel 开发规范"
---

# KMM/Compose ViewModel 设计与开发 Skill

> 目标：将 VM 设计为"面向 UI 的稳定接口"，将业务细节收敛到实现层，确保新业务 VM 设计合理、可复用、可扩展。

## 1. 适用范围

适用于以下任务：
1. 设计新业务 VM 接口（`qnCore`）
2. 实现新业务 VM（业务模块，如 `qnUser`、`qnAd`）
3. 重构现有 VM 接口
4. 提供 VM 设计的最佳实践指导

---

## 2. 核心设计原则（必须遵守）

### 2.1 一个合理 VM 接口应满足"核心必备 + 按场景可选"

**核心必备（必须具备）：**
1. 固定属性：基础类型（`String`、`Int`、`Boolean`）
2. 可变状态：`StateFlow` / `SharedFlow`（只读）
3. UI 交互方法：`fun`（如 `onClick()`、`refresh()`）

**按场景可选（复杂页面再引入）：**
4. 子组件 VM：用于复杂 UI 拆分与组合，简单场景可不提供

### 2.2 VM 接口禁止项（硬性）

1. 禁止在接口暴露业务数据模型（如 `IUserInfo`、`IAdOrder`）
2. 禁止在接口暴露 controller / presenter / router
3. 禁止在接口使用 `var`
4. 禁止在接口暴露 `MutableStateFlow`、`MutableSharedFlow`

### 2.3 分层边界（硬性）

1. `qnCore`：只定义接口，不放实现
2. `qnCompose`：只消费接口，禁止依赖业务实现细节
3. 业务模块：承载业务判断、路由、埋点、日志、数据转换

### 2.4 面向 UI 设计（硬性）

1. UI 只拿"可直接渲染的数据"（如 `avatarUrl`、`titleText`）
2. UI 只调用"语义动作方法"（如 `onAvatarClick()`），不拼业务分支
3. 当业务规则变化但 UI 形态不变时，优先只改 VM 实现，不改 UI 与接口

### 2.5 可维护性（硬性）

1. 新增公开方法必须有方法注释，描述用途与业务语义
2. 复杂组件优先拆分为子 VM，避免单 VM 过大
3. 一次性事件（跳转、Toast）与持久状态分离

---

## 3. 开发 SOP（新建 VM）

### Phase 1：建模 UI 能力

1. **列出 UI 只需要的字段（渲染字段）**
   - 示例：头像 URL、标题文本、按钮文案
   - 排除：业务对象、复杂结构体

2. **列出 UI 只需要的行为（动作入口）**
   - 示例：点击头像、刷新数据、加载更多
   - 命名规则：`onXxx()` 语义化方法

3. **列出一次性事件（导航、提示）**
   - 示例：页面跳转、Toast 提示、弹窗显示
   - 使用 `SharedFlow` 传递事件

4. **评估是否需要子 VM（复杂组合场景再拆分）**
   - 判断依据：UI 组件是否可独立复用
   - 简单场景：不提供子 VM
   - 复杂场景：提供子 VM 接口

### Phase 2：定义 qnCore 接口

1. **只暴露基础字段 + 只读 Flow + 动作方法**
   ```kotlin
   interface IUserIconVM {
       // 固定属性
       val iconSize: Int
       
       // 可变状态（只读）
       val userIcon: String
       
       // UI 交互方法
       fun onIconClick()
       
       // 子 VM（可选，复杂场景）
       val childVM: IChildVM?
   }
   ```

2. **删除所有业务模型类型暴露**
   - ❌ 错误：`val userInfo: IUserInfo`
   - ✅ 正确：`val userIcon: String`

3. **删除所有可变流暴露**
   - ❌ 错误：`val state: MutableStateFlow<UiState>`
   - ✅ 正确：`val state: StateFlow<UiState>`

4. **为每个公开方法补充注释**
   ```kotlin
   /** 处理用户头像点击事件，跳转到对应页面 */
   fun onIconClick()
   ```

### Phase 3：实现业务模块 VM

1. **在实现层完成业务分支、路由、埋点、日志**
   ```kotlin
   class UserIconVM(val userInfo: IUserInfo) : IUserIconVM {
       override val iconSize = buildIconSize()
       override val userIcon = buildIconUrl()
       
       private fun buildIconSize(): Int {
           return if (userInfo.isCp) 50 else 40
       }
       
       private fun buildIconUrl(): String {
           return if (userInfo.isMySelf) {
               appLogin().getMainLoginUserInfo().getAIIconUrl()
           } else {
               userInfo.resDto.headUrl
           }
       }
       
       override fun onIconClick() {
           // 业务分支判断
           when {
               userInfo.isCpUser -> appRouter().jumpToCpPage(userInfo)
               userInfo.isMySelf -> appRouter().jumpToUserHomePage(userInfo)
               else -> appRouter().jumpToGuestPage(userInfo)
           }
           
           // 埋点上报
           appReport().reportBeacon("user_icon_click", ...)
           
           // 日志记录
           UserLog.file("点击用户头像: ${userInfo.baseDto.suid}")
       }
   }
   ```

2. **接口字段转换为 UI 直接可用值**
   - 将 `IUserInfo` 转换为 `userIcon: String`
   - 将 `IAdOrder` 转换为 `advertiserName: String`

3. **可变流保留私有（`Mutable*`），对外只暴露只读 Flow**
   ```kotlin
   private val _uiState = MutableStateFlow(UiState())
   override val uiState: StateFlow<UiState> = _uiState
   ```

4. **保持 UI 无业务 if-else**
   - 所有业务分支都在实现层处理
   - UI 层只调用 `vm.onXxx()` 方法

### Phase 4：接入 Compose

1. **Compose 仅渲染 `vm` 暴露内容**
   ```kotlin
   @Composable
   fun UserIcon(vm: IUserIconVM) {
       Image(
           url = vm.userIcon,
           modifier = Modifier.size(vm.iconSize.dp)
       )
   }
   ```

2. **点击事件只调用 `vm.onXxx()`**
   ```kotlin
   .clickable { vm.onIconClick() }
   ```

3. **UI 不直接读取业务对象、不直连业务服务**
   - ❌ 错误：`appLogin().getMainLoginUserInfo()`
   - ❌ 错误：`appRouter().jumpToXxx()`
   - ✅ 正确：通过 VM 接口获取数据

---

## 4. 推荐模板（最小骨架）

### 简单场景

```kotlin
// qnCore: 接口层
interface ISimpleVM {
    // 固定属性
    val title: String
    
    // 可变状态（只读）
    val isLoading: StateFlow<Boolean>
    
    // UI 交互方法
    fun onRefresh()
}
```

### 复杂场景

```kotlin
// qnCore: 接口层
interface IComplexVM {
    // 固定属性
    val title: String
    
    // 可变状态（只读）
    val uiState: StateFlow<UiState>
    val uiEvent: SharedFlow<UiEvent>
    
    // UI 交互方法
    /** 处理主按钮点击 */
    fun onPrimaryClick()
    
    /** 刷新数据 */
    fun refresh()
    
    // 子 VM（复杂组合场景）
    val headerVM: IHeaderVM?
    val footerVM: IFooterVM?
}
```

---

## 5. 高频反模式（发现即标红）

### 反模式 1：接口暴露业务模型

```kotlin
// ❌ 错误
interface IUserIconVM {
    val userInfo: IUserInfo  // 业务模型暴露
}

// ✅ 正确
interface IUserIconVM {
    val userIcon: String  // UI 可直接使用的字段
    val userName: String
}
```

### 反模式 2：接口暴露可变流

```kotlin
// ❌ 错误
interface IMyVM {
    val state: MutableStateFlow<UiState>  // 可变流暴露
}

// ✅ 正确
interface IMyVM {
    val state: StateFlow<UiState>  // 只读流
}
```

### 反模式 3：UI 层包含业务逻辑

```kotlin
// ❌ 错误：UI 层包含业务分支
@Composable
fun UserIcon(vm: IUserIconVM) {
    Image(
        url = vm.userIcon,
        modifier = Modifier.clickable {
            if (vm.userInfo.isCpUser) {  // UI 层业务判断
                appRouter().jumpToCpPage(vm.userInfo)
            } else {
                appRouter().jumpToUserPage(vm.userInfo)
            }
        }
    )
}

// ✅ 正确：业务逻辑收敛到 VM
@Composable
fun UserIcon(vm: IUserIconVM) {
    Image(
        url = vm.userIcon,
        modifier = Modifier.clickable {
            vm.onIconClick()  // 只调用 VM 方法
        }
    )
}
```

### 反模式 4：接口使用 var

```kotlin
// ❌ 错误
interface IMyVM {
    var title: String  // 可变属性
}

// ✅ 正确
interface IMyVM {
    val title: String  // 只读属性
}
```

### 反模式 5：接口不断膨胀

```kotlin
// ❌ 错误：为适配多业务不断叠加字段
interface IUserIconVM {
    val userInfo: IUserInfo?
    val adOrder: IAdOrder?
    val questionInfo: IQuestionInfo?
    // ... 越来越多业务字段
}

// ✅ 正确：保持接口简洁，通过不同实现类扩展
interface IUserIconVM {
    val iconSize: Int
    val userIcon: String
    fun onIconClick()
}

// 业务 1：用户头像
class UserIconVM(userInfo: IUserInfo) : IUserIconVM

// 业务 2：广告主头像
class AdvertiserIconVM(adOrder: IAdOrder) : IUserIconVM

// 业务 3：问答头像
class QuestionIconVM(questionInfo: IQuestionInfo) : IUserIconVM
```

---

## 6. 设计规范参考

本 Skill 的设计原则和示例基于以下规范文档：

**参考文档**：[面向UI设计与面向业务设计的ViewModel接口规范](../../shared/viewmodel-design-spec.md)

该文档详细介绍了：
- VM 接口应该包含的 4 个方面
- VM 接口不应该出现的内容
- 面向 UI 设计 vs 面向业务设计的对比
- 复杂场景下的扩展与复用策略
- 代码质量与架构约束
- 渐进式重构、跨端复用与混合开发

---

## 7. 执行要求

1. **设计前先建模 UI 能力**：列出渲染字段、动作入口、一次性事件
2. **接口定义后先自查**：检查是否违反硬性规则
3. **实现时保持 UI 无业务逻辑**：所有业务分支、路由、埋点都在实现层
4. **为每个公开方法补充注释**：描述用途与业务语义
5. **复杂场景优先拆分子 VM**：避免单 VM 过大

---

## 8. 常见问题

### Q1: 什么时候需要子 VM？

**判断依据**：
- UI 组件是否可独立复用
- VM 是否过于复杂（超过 200 行）
- 是否存在独立的业务子域

**示例**：
```kotlin
// 复杂页面：需要子 VM
interface INewsDetailVM {
    val headerVM: INewsHeaderVM?      // 头部 VM
    val contentVM: INewsContentVM?    // 内容 VM
    val commentVM: ICommentVM?        // 评论 VM
}

// 简单页面：不需要子 VM
interface ISimplePageVM {
    val title: String
    val content: String
    fun onRefresh()
}
```

### Q2: 如何处理一次性事件？

**推荐方案**：使用 `SharedFlow` 传递事件

```kotlin
sealed class UiEvent {
    data class Navigate(val route: String) : UiEvent()
    data class ShowToast(val message: String) : UiEvent()
}

interface IMyVM {
    val uiEvent: SharedFlow<UiEvent>
}

class MyVM : IMyVM {
    private val _uiEvent = MutableSharedFlow<UiEvent>()
    override val uiEvent = _uiEvent.asSharedFlow()
    
    fun onButtonClick() {
        // 发送事件
        _uiEvent.tryEmit(UiEvent.ShowToast("操作成功"))
    }
}
```

### Q3: 如何保证 VM 接口不变的情况下支持多业务？

**方案**：通过不同的实现类扩展

```kotlin
// 接口保持简洁
interface IUserIconVM {
    val iconSize: Int
    val userIcon: String
    fun onIconClick()
}

// 业务 1：用户头像
class UserIconVM(userInfo: IUserInfo) : IUserIconVM {
    override val iconSize = if (userInfo.isCp) 50 else 40
    override val userIcon = userInfo.resDto.headUrl
    override fun onIconClick() { /* 用户头像点击逻辑 */ }
}

// 业务 2：广告主头像
class AdvertiserIconVM(adOrder: IAdOrder) : IUserIconVM {
    override val iconSize = 40
    override val userIcon = adOrder.info.advertiserIcon
    override fun onIconClick() { /* 广告主头像点击逻辑 */ }
}
```

---

## 9. 检查清单

开发完成后，使用以下清单自查：

- [ ] 接口是否只暴露基础字段和只读 Flow？
- [ ] 接口是否没有暴露业务模型（`IUserInfo`、`IAdOrder`）？
- [ ] 接口是否没有使用 `var`？
- [ ] 接口是否没有暴露 `MutableStateFlow`、`MutableSharedFlow`？
- [ ] UI 层是否只调用 `vm.onXxx()` 方法，没有业务分支？
- [ ] 所有业务逻辑是否都在实现层？
- [ ] 所有公开方法是否都有注释？
- [ ] 复杂场景是否拆分了子 VM？
- [ ] 是否遵循分层边界（`qnCore`、`qnCompose`、业务模块）？
