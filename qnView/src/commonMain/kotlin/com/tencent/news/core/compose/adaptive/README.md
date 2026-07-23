# AdaptivePage - Compose 侧页面适配框架

> 模块路径: `qnView/src/commonMain/kotlin/com/tencent/news/core/compose/adaptive/`

## 概述

Compose/Kuikly 侧的页面宽度自适应方案，通过 `CompositionLocal` + `AdaptiveContent` / `AdaptiveBox` / `AdaptiveBox4Cell` 实现。

在 `EXPANDED` 断点下，需要限宽的内容区自动增加左右 padding 并居中；在 `COMPACT` 下保持铺满。卡片类组件可通过 `AdaptiveBox4Cell` 使用独立的 cell 限宽策略。

## 核心 API

### AdaptivePage.Style

定义页面级限宽策略：

```kotlin
// 预置样式
AdaptivePage.Style.Normal       // 所有断点不限宽
AdaptivePage.Style.SingleColumn // EXPANDED 下页面限宽 + MEDIUM/EXPANDED 下卡片限宽
AdaptivePage.Style.LimitDetail  // EXPANDED 下页面限宽，卡片不单独限宽

// 自定义样式
AdaptivePage.Style.Custom(
    padding = adaptiveSize {
        mediumSize = 40.fixed()
        expandedSize = AdaptiveUiConfig.expandedPaddingSize
    },
    cellLimit = adaptiveSize {
        expandedSize = 520.fixed()
    }
)
```

### LocalAdaptivePageStyle

`CompositionLocal`，在页面根部注入 Style：

```kotlin
CompositionLocalProvider(
    LocalAdaptivePageStyle provides AdaptivePage.Style.SingleColumn
) {
    // 子树中的 AdaptiveContent / AdaptiveBox / AdaptiveBox4Cell 自动读取此 Style
}
```

### AdaptiveContent / AdaptiveBox / AdaptiveBox4Cell

- `AdaptiveContent`：轻量内容容器；未注入 Style 时不额外包裹容器。
- `AdaptiveBox`：Box 容器替代品；需要 `BoxScope` 能力时使用。
- `AdaptiveBox4Cell`：Cell 级固定宽度限制，避免卡片在大屏被拉伸。

```kotlin
@Composable
fun MyHeader() {
    AdaptiveContent {
        // 此区域在 EXPANDED 下自动跟随页面 padding 限宽
        Text("标题内容")
    }
}

@Composable
fun MyCard() {
    AdaptiveBox4Cell {
        // 此区域在 MEDIUM/EXPANDED 下自动按 cellLimit 限宽
        NewsCard()
    }
}
```

## 使用方法

### 1. 页面级声明

在 `StructComposePage4VM` 中传入 `style` 参数：

```kotlin
class MyComposePage : ComposePage() {
    @Composable
    override fun Content() {
        StructComposePage4VM(
            pageViewModel = { viewModel },
            style = AdaptivePage.Style.SingleColumn
        )
    }
}
```

常用选择：

| 场景 | 推荐 Style |
|------|------------|
| 单列列表、频道页 | `AdaptivePage.Style.SingleColumn` |
| 图文/问答详情页 | `AdaptivePage.Style.LimitDetail` |
| 不需要限宽的沉浸式页面 | `AdaptivePage.Style.Normal` |
| 特殊页面 | `AdaptivePage.Style.Custom(...)` |

### 2. 组件级限宽

将需要限宽的 UI 区域用 `AdaptiveContent` 或 `AdaptiveBox` 包裹：

```kotlin
@Composable
fun MyChannelBar() {
    AdaptiveContent {
        Row(modifier = Modifier.fillMaxWidth()) {
            // 频道栏内容...
        }
    }
}
```

### 3. StructPageScaffold 中的 HoverArea

框架已在 `StructPageScaffold` 中为 `channelBar`、`hangingView` 等组件自动包裹 `AdaptiveContent`，使用 StructPage 体系的页面通常只需要在页面根部传入 `style`。

### 4. 自定义限宽策略

```kotlin
val customStyle = AdaptivePage.Style.Custom(
    padding = adaptiveSize {
        compatSize = NoLimit
        mediumSize = 32.fixed()
        expandedSize = AdaptiveUiConfig.expandedPaddingSize
    },
    cellLimit = adaptiveSize {
        expandedSize = 500.fixed()
    }
)

StructComposePage4VM(
    pageViewModel = { viewModel },
    style = customStyle
)
```

## 扩展指引

### 新增需要限宽的组件

直接用 `AdaptiveContent` 包裹即可：

```kotlin
@Composable
fun NewComponent() {
    AdaptiveContent(modifier = Modifier.padding(16.dp)) {
        // 你的内容
    }
}
```

### 不限宽的内容

不使用 `AdaptiveContent` / `AdaptiveBox` 包裹的内容会按照正常布局规则（通常铺满），适用于背景图、全幅 banner、分割线等。

### 与 View 端的对应关系

| Compose | View |
|---------|------|
| `AdaptiveContent { ... }` / `AdaptiveBox { ... }` | `adaptiveView { pagePadding() }` |
| 不包裹（铺满） | `adaptiveView { expandWidth(view) }` |
| `AdaptiveBox4Cell { ... }` | `adaptiveView { card(view) }` |
| `LocalAdaptivePageStyle` | `AdaptivePageScope.pageStyle` |
| `AdaptivePage.Style` | 共用同一个类 |

## 文件索引

| 文件 | 职责 |
|------|------|
| `AdaptivePage.kt` | Style 定义 + AdaptiveContent / AdaptiveBox / AdaptiveBox4Cell + LocalAdaptivePageStyle |
| `AdaptiveSize.kt` | 断点尺寸策略 DSL |
| `AdaptiveUiConfig.kt` | 页面 padding、cell 限宽等全局配置 |
| `AdaptiveDialog.kt` | 弹窗自适应策略 |