---
name: analyze-recomposition-log
description: 分析 Recomposition Profiler 日志，识别重组热点、耗时组件和参数变化模式，生成优化建议。适用场景：分析 RCProfiler 日志、识别重组性能问题、优化 Compose 重组性能、分析帧耗时、定位不必要的重组。
---

# Recomposition 日志分析

## 核心原则

> **CRITICAL**:
> 1. 实事求是：经过分析仍然不清楚原因的情况下，直接告知用户实情，给出一般性指引
> 2. 避免虚假估算：如果不能确定优化会带来多大提升，避免直接给出估算数值
> 3. 数据驱动：所有优化建议必须基于日志数据，而非主观猜测

## 触发条件

当用户提到以下任一场景时，触发本 skill：

- 分析 RCProfiler 日志
- 分析重组性能问题
- 优化 Compose 重组
- 查看重组热点
- 分析帧耗时
- 定位不必要的重组
- 提供了包含 `RCProfiler` 的日志内容或日志文件路径

## 日志获取方式

### 方式一：从 App 导出日志

1. 通过 App 的 **「分享日志」** 功能导出客户端日志文件
2. 在导出的日志中搜索关键字 **`RCProfiler`** 即可过滤全部重组分析日志

### 方式二：从 Android Studio Logcat 获取

1. 在 Android Studio 的 **Logcat** 中搜索 `RCProfiler`
2. 复制相关日志内容

### 方式三：直接提供日志内容

用户可以直接将日志内容粘贴到对话中

## 日志格式说明

工具会输出两类日志：

### 1. 逐帧事件日志（实时）

每帧产生一组事件，格式如下：

```
RCProfiler: Frame #332 START (ts=53815ms)
RCProfiler:   RECOMPOSED: ProgressTrack @WSVideoSeekProgressBar.kt:198 (1ms) [scope=ProgressTrack] [parent=CustomSlider] params changed: [#1] (1/3) triggers=[WSVideoProgressState(currentPositionMs=8171, durationMs=152416, ...)]
RCProfiler:   RECOMPOSED: CustomSlider @WSVideoSeekProgressBar.kt:140 (3ms) [scope=none] [parent=BaseProgressSlider] params changed: [#0] (1/6)
RCProfiler:   RECOMPOSED: BaseProgressSlider @WSVideoSeekProgressBar.kt:103 (4ms) [scope=none] [parent=WSVideoSeekProgressBar] params changed: [#0] (1/4)
RCProfiler:   RECOMPOSED: WSVideoSeekProgressBar @WSVideoProgressBarContainer.kt:82 (5ms) [scope=none] [parent=WSVideoProgressBar] params changed: [#0, #1] (2/5)
RCProfiler:   RECOMPOSED: WSVideoProgressBar @WSVideoProgressBarContainer.kt:75 (6ms) [scope=none] [parent=WSVideoProgressBarContainer] params changed: [#0, #1] (2/5)
RCProfiler:   RECOMPOSED: WSVideoProgressBarContainer @WSVideoComponent.kt:223 (7ms) [scope=none] [parent=WSVideoComponent] params changed: [#1] (1/5)
RCProfiler:   RECOMPOSED: WSVideoComponent @WSVideoComponent.kt:71 (9ms) [scope=WSVideoComponent] [parent=<unknown>] params=[no changes] (0/12)
RCProfiler: Frame #332 END (duration=10ms, recomposed=7)
```

**各字段含义：**

| 字段 | 含义 | 示例 |
|------|------|------|
| `Frame #N` | 帧编号 | `Frame #332` |
| `START (ts=Nms)` | 帧开始时间戳（毫秒） | `ts=53815ms` |
| `RECOMPOSED: 组件名` | 被重组的 Composable 函数名 | `ProgressTrack` |
| `@文件名:行号` | 源码位置 | `@WSVideoSeekProgressBar.kt:198` |
| `(Nms)` | 该组件本次重组耗时 | `(3ms)` |
| `[scope=xxx]` | 重组 Scope 名称，`none` 表示无独立 Scope | `[scope=ProgressTrack]` |
| `[parent=xxx]` | 父组件名称 | `[parent=CustomSlider]` |
| `params changed: [#N]` | 发生变化的参数索引和数量 | `params changed: [#1] (1/3)` |
| `triggers=[...]` | 触发重组的 State 快照 | `triggers=[WSVideoProgressState(...)]` |
| `END (duration=Nms, recomposed=N)` | 帧总耗时和重组组件数 | `duration=10ms, recomposed=7` |

### 2. 汇总报告日志

通过 Demo 页面点击 **「查看重组分析报告」** 或调用 `RecompositionProfilerManager.getReport()` 生成：

```
RCProfiler: === Recomposition Report ===
RCProfiler: Session: abc123
RCProfiler: Duration: 60000ms | Frames: 400 | Recompositions: 2800
RCProfiler: --- HOTSPOTS ---
RCProfiler:   ProgressTrack @WSVideoSeekProgressBar.kt:198: 400x (avg=1.2ms, max=3ms)
RCProfiler:   CustomSlider @WSVideoSeekProgressBar.kt:140: 400x (avg=2.5ms, max=5ms)
RCProfiler: --- Composables ---
RCProfiler:   ProgressTrack @...: 400x (avg=1.2ms) [HOTSPOT] params changed: [#1:400x] state changes: [WSVideoProgressState]
RCProfiler:     → scopes: {ProgressTrack: 400x}, no-scope: 0
RCProfiler: === End of Recomposition Report ===
```

**汇总报告字段说明：**

| 字段 | 含义 |
|------|------|
| `Nx` | 该组件被重组了 N 次 |
| `avg=Nms` | 平均每次重组耗时 |
| `max=Nms` | 单次最大重组耗时 |
| `[HOTSPOT]` | 重组次数超过阈值（默认 10 次），被标记为热点 |
| `params changed: [#N:Mx]` | 第 N 个参数变化了 M 次 |
| `state changes: [xxx]` | 触发重组的 State 类型 |
| `scopes: {...}` | Scope 分布（哪些 Scope 触发了多少次重组） |
| `no-scope: N` | 无独立 Scope 的重组次数 |

## 分析流程

### Step 1：获取日志内容

根据用户提供的来源获取日志：

1. **用户提供日志文件路径** → 使用 `read_file` 读取文件内容
2. **用户提供日志内容** → 直接从对话中获取
3. **用户未提供** → 提示用户按照"日志获取方式"获取日志

### Step 2：解析日志

根据日志类型选择解析策略：

#### 策略 A：汇总报告日志（优先）

如果日志中包含 `=== Recomposition Report ===`，说明是汇总报告，直接解析：

```
提取关键信息：
- Session: 会话 ID
- Duration: 监控时长
- Frames: 总帧数
- Recompositions: 总重组次数
- HOTSPOTS: 热点组件列表（重组次数、平均耗时、最大耗时）
- Composables: 所有组件列表（重组次数、参数变化、状态变化、Scope 分布）
```

#### 策略 B：逐帧事件日志

如果日志中是逐帧日志，需要统计：

```
统计关键信息：
1. 组件重组次数：每个组件被重组的次数
2. 组件耗时：每个组件的总耗时、平均耗时、最大耗时
3. 参数变化：每个组件的哪些参数经常变化
4. 触发源：哪些 State 变化触发了重组
5. 父子关系：重组的传递路径
6. 帧耗时分布：哪些帧耗时较长
```

### Step 3：识别问题模式

根据解析结果，识别以下常见问题模式：

#### 问题模式 1：高频重组（Hotspot）

**特征**：
- 组件重组次数接近帧数（如 400 帧重组了 400 次）
- 被标记为 `[HOTSPOT]`

**原因**：
- 父组件每次重组都导致子组件重组
- State 变化过于频繁（如进度条每帧都变化）
- 缺少合理的重组作用域（Scope）

**优化建议**：
- 使用 `remember` 缓存计算结果
- 使用 `derivedStateOf` 减少 State 变化传播
- 使用 `key` 参数帮助 Diff（LazyColumn/items）
- 将高频变化的状态移动到独立 Composable 中，使用独立 Scope

#### 问题模式 2：参数未变化但重组

**特征**：
- 日志显示 `params=[no changes]` 但组件仍然重组
- 或 `params changed: [#0] (0/N)` 表示参数未变化

**原因**：
- 父组件重组导致子组件无条件重组
- 缺少稳定的参数（参数对象每次都是新实例）

**优化建议**：
- 确保传递给子组件的参数都是稳定的（使用 `remember`）
- 使用 `@Stable` 或 `@Immutable` 注解标记数据类
- 使用 `remember` 包装回调函数（如 `onClick`）

#### 问题模式 3：耗时组件

**特征**：
- 单个组件重组耗时较长（如 > 5ms）
- 平均耗时长或最大耗时长

**原因**：
- 组件内部计算复杂
- 组件树过深
- 列表项布局复杂

**优化建议**：
- 将复杂计算移到 `remember` 或后台线程
- 拆分组件，减少单个组件的复杂度
- 使用 `LazyColumn` 替代 `Column` + `verticalScroll`
- 使用 `Modifier.graphicsLayer` 减少绘制区域

#### 问题模式 4：Scope 缺失

**特征**：
- 多个组件都显示为 `[scope=none]`
- 父组件重组时，所有子组件都跟着重组

**原因**：
- 未使用独立的作用域
- 组件未正确分组

**优化建议**：
- 为独立更新的区域创建独立的 Composable 函数
- 使用 `key` 参数为列表项创建独立的重组作用域
- 将高频更新的组件拆分为独立的 Composable

### Step 4：生成分析报告

输出格式如下：

```markdown
## Recomposition 分析报告

### 基本信息
- 会话 ID: abc123
- 监控时长: 60000ms
- 总帧数: 400
- 总重组次数: 2800
- 平均帧重组次数: 7

### 🔥 热点组件（高频重组）

| 组件名 | 位置 | 重组次数 | 平均耗时 | 最大耗时 | 问题模式 |
|--------|------|----------|----------|----------|----------|
| ProgressTrack | WSVideoSeekProgressBar.kt:198 | 400x | 1.2ms | 3ms | 参数 #1 每帧变化 |
| CustomSlider | WSVideoSeekProgressBar.kt:140 | 400x | 2.5ms | 5ms | 父组件传递导致 |

### ⏱️ 耗时组件

| 组件名 | 位置 | 平均耗时 | 最大耗时 | 建议 |
|--------|------|----------|----------|------|
| WSVideoComponent | WSVideoComponent.kt:71 | 9ms | 15ms | 拆分组件，减少复杂度 |

### 📊 参数变化分析

| 组件名 | 常变参数 | 触发源 | 优化建议 |
|--------|----------|--------|----------|
| ProgressTrack | #1 (400x) | WSVideoProgressState | 使用 derivedStateOf 减少更新频率 |

### 🔧 优化建议（按优先级排序）

#### 高优先级（立即修复）

1. **ProgressTrack 高频重组**
   - 问题：每帧都重组，参数 #1（可能是进度值）每帧变化
   - 建议：使用 `derivedStateOf` 或降低更新频率（如每 100ms 更新一次）
   - 代码示例：
     ```kotlin
     val derivedProgress by derivedStateOf {
         // 计算逻辑
     }
     ```

2. **CustomSlider 跟随父组件重组**
   - 问题：父组件重组时，CustomSlider 无条件重组
   - 建议：确保传递给 CustomSlider 的参数是稳定的（使用 remember）
   - 代码示例：
     ```kotlin
     val stableOnClick = remember { { /* onClick */ } }
     CustomSlider(onClick = stableOnClick)
     ```

#### 中优先级（近期优化）

3. **WSVideoComponent 耗时较长**
   - 问题：单次重组耗时 9ms，影响帧率
   - 建议：拆分组件，将独立部分提取为独立 Composable

#### 低优先级（长期优化）

4. **增加重组作用域**
   - 为高频更新区域创建独立的 Composable
   - 使用 key 参数为列表项创建独立 Scope
```

### Step 5：生成优化代码示例

根据用户需要，为识别出的问题生成优化后的代码示例：

```kotlin
// 优化前
@Composable
fun WSVideoSeekProgressBar(progress: Float) {
    // 每次 progress 变化，整个组件都会重组
    CustomSlider(progress = progress, onClick = { /* ... */ })
}

// 优化后
@Composable
fun WSVideoSeekProgressBar(progress: Float) {
    // 使用 derivedStateOf 减少更新频率
    val derivedProgress by derivedStateOf {
        // 可以降低更新频率
        progress
    }
    
    // 使用 remember 缓存回调函数
    val stableOnClick = remember { { /* ... */ } }
    
    CustomSlider(
        progress = derivedProgress,
        onClick = stableOnClick
    )
}

// 进一步优化：拆分为独立 Composable
@Composable
fun WSVideoSeekProgressBar(progress: Float) {
    // 高频变化的部分独立出来
    ProgressTrack(progress = progress)
    
    // 低频变化的部分
    OtherControls()
}

@Composable
fun ProgressTrack(progress: Float) {
    // 这个组件有自己的重组作用域
    // 只有 progress 变化时才重组
}
```

## 参考文档

| 文件 | 说明 |
|------|------|
| [references/rcprofiler-log-examples.md](references/rcprofiler-log-examples.md) | RCProfiler 日志示例 |
| [references/optimization-patterns.md](references/optimization-patterns.md) | 常见优化模式与代码示例 |

## 注意事项

1. **汇总报告优先**：如果用户提供了汇总报告日志，优先解析汇总报告，因为它已经包含了统计信息
2. **逐帧日志量大**：如果逐帧日志非常大（> 1000 帧），建议用户生成汇总报告后再分析
3. **结合实际代码**：分析结果需要结合项目实际代码才能给出准确的优化建议
4. **避免过早优化**：不是所有重组都需要优化，只有影响帧率（> 16ms）的重组才需要优先处理
