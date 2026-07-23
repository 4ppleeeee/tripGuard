# RCProfiler 日志示例

本文档提供各种场景下的 RCProfiler 日志示例，帮助 AI 理解日志格式。

## 示例 1：正常的低重组日志

```
RCProfiler: === Recomposition Report ===
RCProfiler: Session: normal_usage
RCProfiler: Duration: 60000ms | Frames: 400 | Recompositions: 120
RCProfiler: --- HOTSPOTS ---
RCProfiler:   (无热点组件)
RCProfiler: --- Composables ---
RCProfiler:   UserAvatar @UserProfile.kt:45: 3x (avg=0.5ms, max=1ms)
RCProfiler:     → scopes: {UserAvatar: 3x}, no-scope: 0
RCProfiler:   UserName @UserProfile.kt:50: 2x (avg=0.2ms, max=0.5ms)
RCProfiler:     → scopes: {UserName: 2x}, no-scope: 0
RCProfiler:   PostList @UserProfile.kt:80: 5x (avg=2ms, max=5ms)
RCProfiler:     → scopes: {PostList: 5x}, no-scope: 0
RCProfiler: === End of Recomposition Report ===
```

**分析**：
- 总重组次数 120 次，400 帧中只有 120 次重组，说明重组频率低，性能良好
- 无热点组件（重组次数未超过阈值）
- 各组件平均耗时就低，无性能问题

## 示例 2：高频重组日志（问题场景）

```
RCProfiler: === Recomposition Report ===
RCProfiler: Session: high_freq_recomposition
RCProfiler: Duration: 60000ms | Frames: 400 | Recompositions: 2800
RCProfiler: --- HOTSPOTS ---
RCProfiler:   ProgressTrack @WSVideoSeekProgressBar.kt:198: 400x (avg=1.2ms, max=3ms) [HOTSPOT]
RCProfiler:   CustomSlider @WSVideoSeekProgressBar.kt:140: 400x (avg=2.5ms, max=5ms) [HOTSPOT]
RCProfiler:   BaseProgressSlider @WSVideoSeekProgressBar.kt:103: 400x (avg=1ms, max=2ms) [HOTSPOT]
RCProfiler: --- Composables ---
RCProfiler:   ProgressTrack @WSVideoSeekProgressBar.kt:198: 400x (avg=1.2ms, max=3ms) [HOTSPOT] params changed: [#1:400x] state changes: [WSVideoProgressState]
RCProfiler:     → scopes: {ProgressTrack: 400x}, no-scope: 0
RCProfiler:   CustomSlider @WSVideoSeekProgressBar.kt:140: 400x (avg=2.5ms, max=5ms) [HOTSPOT] params changed: [#0:400x]
RCProfiler:     → scopes: {CustomSlider: 0x}, no-scope: 400
RCProfiler:   BaseProgressSlider @WSVideoSeekProgressBar.kt:103: 400x (avg=1ms, max=2ms) [HOTSPOT] params changed: [#0:400x]
RCProfiler:     → scopes: {BaseProgressSlider: 0x}, no-scope: 400
RCProfiler: === End of Recomposition Report ===
```

**分析**：
- 总重组次数 2800 次，400 帧中有 2800 次重组，平均每帧 7 次重组，频率过高
- 热点组件：ProgressTrack、CustomSlider、BaseProgressSlider 都重组了 400 次（每帧都重组）
- 问题原因：
  - ProgressTrack 的参数 #1 每帧都变化（可能是进度值）
  - CustomSlider 和 BaseProgressSlider 没有独立 Scope（no-scope: 400），父组件重组时它们无条件重组
- 优化建议：
  - 使用 `derivedStateOf` 减少 ProgressTrack 的更新频率
  - 为 CustomSlider 和 BaseProgressSlider 创建独立的重组作用域

## 示例 3：参数未变化但重组的日志

```
RCProfiler: Frame #332 START (ts=53815ms)
RCProfiler:   RECOMPOSED: ChildComponent @ParentComponent.kt:50 (1ms) [scope=none] [parent=ParentComponent] params=[no changes] (0/5)
RCProfiler: Frame #332 END (duration=2ms, recomposed=1)
```

**分析**：
- ChildComponent 重组了，但参数没有变化（`params=[no changes]`）
- 原因：父组件 ParentComponent 重组导致子组件无条件重组
- 优化建议：
  - 确保传递给 ChildComponent 的参数是稳定的（使用 `remember`）
  - 使用 `@Stable` 或 `@Immutable` 注解标记参数类型

## 示例 4：耗时组件的日志

```
RCProfiler: === Recomposition Report ===
RCProfiler: Session: slow_component
RCProfiler: Duration: 60000ms | Frames: 400 | Recompositions: 800
RCProfiler: --- HOTSPOTS ---
RCProfiler:   ComplexList @FeedPage.kt:120: 100x (avg=15ms, max=30ms) [HOTSPOT]
RCProfiler: --- Composables ---
RCProfiler:   ComplexList @FeedPage.kt:120: 100x (avg=15ms, max=30ms) [HOTSPOT] params changed: [#0:50x, #1:50x]
RCProfiler:     → scopes: {ComplexList: 100x}, no-scope: 0
RCProfiler:   ListItem @FeedPage.kt:150: 500x (avg=1ms, max=3ms)
RCProfiler:     → scopes: {ListItem: 500x}, no-scope: 0
RCProfiler: === End of Recomposition Report ===
```

**分析**：
- ComplexList 平均耗时 15ms，最大耗时 30ms，超过单帧时间预算（16ms），会导致掉帧
- 问题原因：组件内部计算复杂或组件树过深
- 优化建议：
  - 将复杂计算移到 `remember` 或后台线程
  - 拆分组件，减少单个组件的复杂度
  - 使用 `LazyColumn` 替代 `Column` + `verticalScroll`

## 示例 5：逐帧事件日志（实时）

```
RCProfiler: Frame #330 START (ts=53700ms)
RCProfiler: Frame #330 END (duration=2ms, recomposed=0)
RCProfiler: Frame #331 START (ts=53716ms)
RCProfiler:   RECOMPOSED: ProgressTrack @WSVideoSeekProgressBar.kt:198 (1ms) [scope=ProgressTrack] [parent=CustomSlider] params changed: [#1] (1/3) triggers=[WSVideoProgressState(currentPositionMs=8000, durationMs=152416)]
RCProfiler:   RECOMPOSED: CustomSlider @WSVideoSeekProgressBar.kt:140 (2ms) [scope=none] [parent=BaseProgressSlider] params changed: [#0] (1/6)
RCProfiler: Frame #331 END (duration=3ms, recomposed=2)
RCProfiler: Frame #332 START (ts=53733ms)
RCProfiler:   RECOMPOSED: ProgressTrack @WSVideoSeekProgressBar.kt:198 (1ms) [scope=ProgressTrack] [parent=CustomSlider] params changed: [#1] (1/3) triggers=[WSVideoProgressState(currentPositionMs=8166, durationMs=152416)]
RCProfiler:   RECOMPOSED: CustomSlider @WSVideoSeekProgressBar.kt:140 (2ms) [scope=none] [parent=BaseProgressSlider] params changed: [#0] (1/6)
RCProfiler: Frame #332 END (duration=3ms, recomposed=2)
```

**分析**：
- 每帧都有 ProgressTrack 和 CustomSlider 重组
- ProgressTrack 有独立 Scope（`[scope=ProgressTrack]`），只有参数 #1 变化时才重组
- CustomSlider 没有独立 Scope（`[scope=none]`），父组件重组时它会无条件重组
- 优化建议：
  - ProgressTrack 的更新频率已经是最低（每帧一次），如果不需要这么高频率，可以使用 `derivedStateOf` 降低更新频率
  - CustomSlider 需要创建独立的重组作用域，避免父组件重组时无条件重组

## 示例 6：触发源分析的日志

```
RCProfiler: === Recomposition Report ===
RCProfiler: Session: trigger_analysis
RCProfiler: Duration: 60000ms | Frames: 400 | Recompositions: 800
RCProfiler: --- HOTSPOTS ---
RCProfiler:   TimeDisplay @ClockPage.kt:30: 400x (avg=0.5ms, max=1ms) [HOTSPOT]
RCProfiler: --- Composables ---
RCProfiler:   TimeDisplay @ClockPage.kt:30: 400x (avg=0.5ms, max=1ms) [HOTSPOT] params changed: [#0:400x] state changes: [currentTimeMillis]
RCProfiler:     → scopes: {TimeDisplay: 400x}, no-scope: 0
RCProfiler:   DateDisplay @ClockPage.kt:45: 60x (avg=0.3ms, max=0.8ms) params changed: [#0:60x] state changes: [currentTimeMillis]
RCProfiler:     → scopes: {DateDisplay: 60x}, no-scope: 0
RCProfiler: === End of Recomposition Report ===
```

**分析**：
- TimeDisplay 重组了 400 次（每帧都重组），触发源是 `currentTimeMillis` State
- DateDisplay 重组了 60 次（每分钟重组一次），触发源也是 `currentTimeMillis` State
- 问题原因：`currentTimeMillis` State 每帧都变化，导致 TimeDisplay 每帧都重组
- 优化建议：
  - TimeDisplay 可以使用 `derivedStateOf` 或降低更新频率（如每秒更新一次）
  - DateDisplay 的更新频率是合理的（每分钟更新一次）

## 总结

通过以上示例，AI 可以学习如何分析 RCProfiler 日志，识别常见的性能问题模式，并生成相应的优化建议。
