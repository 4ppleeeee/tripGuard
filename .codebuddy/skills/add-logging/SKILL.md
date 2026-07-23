---
name: add-logging
description: Use when 需要为业务代码增加日志，要求 debug 包下日志尽可能详细，release 包下精简，遵循项目日志分层体系。
---

# 增加日志 Skill

## 目标

使用 **BaseBizLog** 为业务代码增加合理的日志，遵循以下核心原则：
- **Debug 包**：用 `.debug {}` 打印尽可能详细的日志（入参、出参、中间状态、分支走向、列表快照等），lambda 延迟求值，Release 下零开销
- **Release 包**：用 `.fileLog()` / `.error()` 打印关键业务节点，精简但足够定位问题

---

## 触发条件

- "加日志"
- "增加日志"
- "添加日志"
- "加 log"
- "补充日志"
- "debug 下打印详细日志"

---

## 输入

| 参数 | 说明 | 是否必须 |
|------|------|----------|
| 目标文件/模块 | 需要增加日志的文件或模块路径 | ✅ 必须 |
| 业务场景描述 | 需要追踪的业务流程（如"登录流程"、"弹窗显示"） | 可选 |

---

## BaseBizLog API 速查

**位置**：`qnPlatform/src/commonMain/kotlin/com/tencent/news/core/list/trace/NewsLogHelper.kt`

### 完整 API

```kotlin
open class BaseBizLog(val tag: String, val subTag: String = "") {

    // Debug 专属（仅 debug 包输出到控制台，release 下完全不执行）
    inline fun verbose(subTag: String, msg: () -> String)           // 极详细，一般不用
    inline fun debug(subTag: String = this.subTag, msg: () -> String)  // ⭐ 主力 debug 日志
    inline fun warn(subTag: String = this.subTag, msg: () -> String)   // debug 包的警告级控制台日志

    // Release 也生效（写入文件日志，所有包均写入）
    fun fileLog(msg: String)                                        // ⭐ 关键节点（w 级别）
    fun fileLog(subTag: String = this.subTag, msg: String)          // 带 subTag 的关键节点
    fun error(msg: String, error: Throwable? = null)                // ⭐ 错误（e 级别）
    fun error(subTag: String = this.subTag, msg: String, error: Throwable? = null)
}
```

### 日志输出格式

- `debug("SubTag") { "消息" }` → 控制台输出 `tag/SubTag: 消息`（仅 debug 包）
- `fileLog("SubTag", "消息")` → 文件写入 `tag/SubTag: 消息`（w 级别，所有包）
- `error("SubTag", "消息", throwable)` → 文件写入 `tag/SubTag: 消息`（e 级别，所有包）

### 关键区别

| API | Debug 包 | Release 包 | 字符串求值 | 用途 |
|-----|----------|------------|-----------|------|
| `debug {}` | ✅ 控制台输出 | ❌ 完全跳过 | lambda 延迟 | 详细调试信息 |
| `fileLog()` | ✅ 写入文件 | ✅ 写入文件 | 立即求值 | 关键业务节点 |
| `error()` | ✅ 写入文件 | ✅ 写入文件 | 立即求值 | 异常和错误 |

---

## 已有 Log 对象清单

在 `NewsLogHelper.kt` 中已定义的 Log 对象，**优先复用**：

```
// 信息流
NewsChannelLog("NChl")

// 底层页
NewsDetailLog("NewsDetailLog")、MorningPostLog("MorningPost")

// 媒体
NewsVideoLog("NewsVideo")、LiveLog("NewsLive")、NewsAudioLog("NewsRadio")
AudioPodLog("AudioPod")、TTSLog("TTSLog")、AlphaVideoLog("AlphaVideo")、SportLog("SportLog")

// 用户
NewsFavoriteLog("NewsFavorite")、NewsHistoryLog("NewsHistory")、NewsFollowLog("NewsFollow")
NewsSubLog("NewsSub")、NewsLikeLog("NewsLike")、NewsLoginLog("NewsLogin")
NewsPayLog("NewsPay")、NewsAIGCLog("NewsAigc")、CheckInLog("CheckIn")

// 公共
NewsRouterLog("NewsRouter")、AppStatusLog("AppStatus")、NetworkLog("NetworkRelay")
PopLog("Pop")、ShareLog("Share")、BeaconLog("Beacon")、NotificationLog("Notification")
ComposeViewLog("ComposeView")、DayFreqLog("DayFreq")、NewsTimeLog("NewsTime")
```

---

## 执行步骤

### Step 1：分析目标代码

1. 读取目标文件，理解业务流程和关键路径
2. 识别以下日志插入点：
   - **方法入口**：方法名 + 所有入参
   - **方法出口**：返回值、执行结果
   - **分支决策点**：if/when 的判断条件值和走向
   - **中间变量**：关键计算结果、布尔判断值
   - **异步回调**：网络请求、协程结果
   - **异常处理**：catch 块中的错误信息
   - **状态变更**：StateFlow 更新、列表增删
   - **耗时操作**：网络请求、IO 操作的开始和结束

### Step 2：确定 Log 对象

1. 检查文件中是否已有 `BaseBizLog` 对象引用（如 `private val logger = PopLog`）
2. 检查 `NewsLogHelper.kt` 中是否有对应业务的 Log 对象
3. 如果都没有，在目标文件中创建私有 Log 对象：

```kotlin
// 方式一：在目标文件中创建（推荐，适合独立功能模块）
private object MyFeatureLog : BaseBizLog("MyFeature")

// 方式二：在 NewsLogHelper.kt 中新增（适合需要跨文件共享的业务域）
object MyFeatureLog : BaseBizLog("MyFeature")
```

### Step 3：设计 SubTag 体系

SubTag 用于在同一个 Log 对象下区分不同的功能子模块，便于日志过滤。

**命名原则**：
- 按功能/流程命名，如 `"Show"`, `"Dismiss"`, `"Check"`, `"Load"`, `"Refresh"`
- 保持简短，1-2 个单词
- 同一文件内的 SubTag 应形成有意义的体系

**实际案例**（弹窗管理器的 SubTag 体系）：
```
Show      — 显示入口
ShowFlow  — 显示流程
Dispatch  — 分发通知
Bind      — 绑定 Helper
TryShow   — 尝试显示
DismissLow — 关闭低优弹窗
Check     — 条件检查
Talkback  — 无障碍检查
Dismiss   — 关闭
Remove    — 移除
Clear     — 清空
Find      — 查找
```

### Step 4：按级别插入日志

#### 4.1 debug {} — 详细调试日志（仅 Debug 包）

**原则**：debug 下打印的尽可能详细，让开发者不需要断点就能还原完整执行路径。

```kotlin
// ✅ 方法入口 — 打印方法名 + 所有入参 + 当前状态
fun show(popTask: KmmPopTask): Boolean {
    logger.debug("Show") { "show()调用 ${popTask.briefInfo()}" }
    // ...
}

// ✅ 方法出口 — 打印结果 + 执行后状态
fun show(popTask: KmmPopTask): Boolean {
    // ...
    logger.debug("Show") { "show()完成 结果=${result.desc()} | ${showingListSnapshot()}" }
    return result == PopResult.SUCCESS
}

// ✅ 分支决策 — 打印每个判断条件的实际值和走向
val samePosition = showingTask.isSamePosition(popTask)
val isLower = showingTask < popTask
val needDismiss = showingTask.dismissSelfByHigherPriority && samePosition && isLower
logger.debug("DismissLow") {
    "对比 ${showingTask.id}(优先级${showingTask.priority}) " +
    "samePosition=$samePosition isLower=$isLower " +
    "dismissByHigher=${showingTask.dismissSelfByHigherPriority} needDismiss=$needDismiss"
}

// ✅ 中间变量 — 打印关键计算结果
val freqCheck = popTask.popHelper?.checkBeforeRealShow()
logger.debug("Check") { "${popTask.id} 频次检查结果=$freqCheck" }

// ✅ 集合/列表操作 — 打印 size 和快照，不打印完整内容
logger.debug("Dispatch") { "通知已显示弹窗(${showingDialogList.size}个) 新弹窗=${popTask.id}" }

// ✅ 状态变更前后对比
val snapshot = showingListSnapshot()
showingDialogList.remove(popTask)
logger.debug("Dismiss") { "关闭成功 移除前=$snapshot 移除后=${showingListSnapshot()}" }

// ✅ 跳过/短路路径 — 也要打印，说明为什么跳过
if (findPopTask { it.dialog == popTask.dialog } != null) {
    logger.debug("Bind") { "跳过绑定 ${popTask.id} 已在显示列表中" }
    return
}

// ✅ 空操作路径 — 说明调用了但没有实际执行
if (showingDialogList.isEmpty()) {
    logger.debug("Clear") { "clear()调用但显示列表为空" }
    return@synchronized
}

// ✅ 循环内的逐项对比（注意：仅在列表较小时使用，高频大列表禁止）
showingDialogList.safeForEach {
    logger.debug("Dispatch") { "通知 ${it.id} 有新弹窗 ${popTask.id} 尝试显示" }
    it.popHelper?.onOtherDialogTryShow(popTask)
}

// ✅ 耗时统计
val startTime = System.currentTimeMillis()
// ... 耗时操作
logger.debug("Load") { "网络请求耗时: ${System.currentTimeMillis() - startTime}ms" }
```

#### 4.2 fileLog() — 关键节点日志（Release 也生效）

**原则**：release 下只打印关键信息，用最少的日志覆盖最重要的业务节点。

```kotlin
// ✅ 操作结果（成功/失败）— 用 ID 标识，不打印完整对象
logger.fileLog("Show", "${popTask.id} 结果=${result.desc()}")

// ✅ 显示失败 — 附带原因
logger.fileLog("ShowFlow", "显示失败 ${popTask.id} 原因=${showResult.desc()}")

// ✅ 被拦截 — 说明拦截原因
logger.fileLog("Frequency", "频次拦截 ${popTask.id}")
logger.fileLog("Talkback", "无障碍拦截 ${popTask.id}")
logger.fileLog("Priority", "优先级拦截 当前=${popTask.id} 高优=${higherTask.id}")

// ✅ 重要状态变更
logger.fileLog("DismissLow", "高优关闭低优 高=${popTask.id} 低=${showingTask.id}")
logger.fileLog("Dismiss", "关闭成功 ${popTask.id}")
logger.fileLog("Clear", "清空弹窗 数量=$size")

// ✅ 异常路径
logger.fileLog("Dismiss", "关闭失败 ${popTask.id} 不在显示列表")
logger.fileLog("Remove", "移除失败 ${popTask.id} 不在显示列表")
```

#### 4.3 error() — 错误日志（Release 也生效）

```kotlin
// ✅ 参数异常
logger.error("Check", "弹窗任务为空")

// ✅ 业务组件自身错误
logger.error("TryShow", "${popTask.id} showPopView返回false，弹窗自身显示失败")

// ✅ 异常捕获 — 必须传入 Throwable
catch (e: Exception) {
    logger.error("Load", "加载数据失败: page=$page", e)
}
```

### Step 5：辅助工具设计（可选）

当对象的日志信息需要在多处复用时，创建辅助工具避免重复拼接：

```kotlin
/**
 * Debug日志辅助工具 — 集中管理日志格式化逻辑
 */
private object MyDebugLog {
    /** 构建对象摘要信息 */
    fun buildItemInfo(item: MyItem?): String {
        return item?.let {
            "项目[${it.id}] 类型=${it.type} 状态=${it.status}"
        } ?: "项目信息为空"
    }

    /** 构建列表快照 */
    fun buildListInfo(list: List<MyItem>): String {
        return if (list.isEmpty()) "列表为空"
        else "列表(${list.size}): ${list.joinToString(", ") { "${it.id}(${it.type})" }}"
    }
}

// 扩展函数简化调用
private fun MyItem.briefInfo(): String = MyDebugLog.buildItemInfo(this)
```

**使用场景**：
- 对象有多个关键字段需要在日志中展示
- 同一个对象的信息在多个方法中都需要打印
- 列表快照需要在多处使用

### Step 6：检查日志质量

| 检查项 | 要求 |
|--------|------|
| **debug 用 lambda** | `debug("Tag") { "msg" }` ✅ — lambda 延迟求值，release 零开销 |
| **fileLog 用直接字符串** | `fileLog("Tag", "msg")` ✅ — 非 lambda，因为 release 下也需要执行 |
| **不泄露敏感信息** | 不打印用户密码、token、完整手机号等 |
| **不打印大对象** | 集合打印 size，对象打印 ID + 关键字段，不打印 toString() |
| **SubTag 有意义** | SubTag 能快速定位到功能子模块 |
| **错误日志带 Throwable** | `error("msg", exception)` 必须传入异常对象 |
| **不重复打印** | 同一信息不在 debug 和 fileLog 中重复（除非 debug 版本更详细） |
| **覆盖所有路径** | 包括正常路径、跳过路径、空操作路径、异常路径 |
| **debug 比 fileLog 更详细** | debug 打印完整对象信息 + 状态快照，fileLog 只打印 ID + 结果 |

### Step 7：输出修改摘要

完成日志添加后，输出以下摘要：

```
## 日志添加摘要 - {文件/模块名}

### Log 对象
{使用的 BaseBizLog 对象名称和 TAG}

### SubTag 体系
{列出所有使用的 SubTag 及其含义}

### 新增日志统计
| 级别 | 数量 | 说明 |
|------|------|------|
| debug{}（仅调试包） | N | 入参、出参、中间状态、分支走向等 |
| fileLog()（所有包） | N | 关键业务节点成功/失败 |
| error()（所有包） | N | 异常和错误 |

### Debug 包日志示例输出
{模拟一次正常流程的完整日志输出}

### Release 包日志示例输出
{模拟一次正常流程的日志输出（仅 fileLog/error）}
```

---

## debug 与 fileLog 的选择决策树

```
这条日志在 Release 包下是否有价值？
├── 是 → 这是错误/异常吗？
│   ├── 是 → 用 error()
│   └── 否 → 用 fileLog()
└── 否 → 用 debug {}
```

**具体场景对照**：

| 场景 | 用 debug {} | 用 fileLog() | 用 error() |
|------|:-----------:|:------------:|:----------:|
| 方法入口 + 入参 | ✅ | | |
| 方法出口 + 返回值详情 | ✅ | | |
| 分支判断条件和走向 | ✅ | | |
| 中间变量/计算结果 | ✅ | | |
| 列表快照/状态快照 | ✅ | | |
| 跳过/短路路径 | ✅ | | |
| 空操作路径 | ✅ | | |
| 逐项对比详情 | ✅ | | |
| 操作成功（简要） | | ✅ | |
| 操作失败（简要） | | ✅ | |
| 被拦截/被阻止 | | ✅ | |
| 重要状态变更 | | ✅ | |
| 清空/批量操作 | | ✅ | |
| 参数异常/为空 | | | ✅ |
| 组件自身错误 | | | ✅ |
| catch 异常 | | | ✅ |

---

## debug 与 fileLog 配对模式

对于重要操作，推荐 **debug 打详细 + fileLog 打精简** 的配对模式：

```kotlin
// 模式：debug 打完整信息，fileLog 只打 ID + 结果
logger.fileLog("Dismiss", "关闭成功 ${popTask.id}")
logger.debug("Dismiss") { "关闭成功 ${popTask.briefInfo()} 移除前=$snapshot 移除后=${showingListSnapshot()}" }

// 模式：fileLog 打拦截原因，debug 打判断过程
logger.fileLog("Priority", "优先级拦截 当前=${popTask.id} 高优=${higherTask.id}")
logger.debug("Priority") { "优先级拦截详情 当前=${popTask.briefInfo()} 高优=${higherTask.briefInfo()}" }
```

---

## 反模式清单

- ❌ **不要在 debug lambda 外拼接字符串**：`debug { "size=${list.size}" }` ✅ vs `debug("size=${list.size}")` ❌（后者的字符串参数是 subTag，不是消息）
- ❌ **不要把 fileLog 当 debug 用**：fileLog 在 release 下也会写文件，不要用它打印详细调试信息
- ❌ **不要只打 debug 不打 fileLog**：关键业务节点必须有 fileLog，否则 release 下无法排查问题
- ❌ **不要打印完整集合内容**：用 `list.size` 和 `joinToString` 摘要代替 `list.toString()`
- ❌ **不要打印敏感信息**：密码、token、完整身份证号等
- ❌ **不要在高频循环内打 debug**：大列表遍历中的逐项日志会严重影响 debug 包性能
- ❌ **不要吞掉异常不打日志**：`catch (e: Exception) { }` 必须至少打一条 error
- ❌ **不要混用日志体系**：同一文件内统一使用 BaseBizLog，不要混用 qnLogcat/qnFileLog/WsLogger
- ❌ **不要用 `println()` 打日志**：commonMain 中必须使用 BaseBizLog
- ❌ **不要忽略 debug 的 subTag 参数**：`debug { "msg" }` 会使用默认 subTag（可能为空），建议显式传入 `debug("SubTag") { "msg" }`
- ❌ **不要在 fileLog 中打印 briefInfo() 等详细信息**：fileLog 只打 ID + 关键结果，详细信息留给 debug
