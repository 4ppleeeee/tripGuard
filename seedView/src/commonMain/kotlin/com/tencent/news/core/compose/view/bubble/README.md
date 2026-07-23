# 气泡组件

## 用法

1. 继承`IBubbleView`

```kotlin
class DemoBubbleView : IBubbleView {
    override val state: MutableState<BubbleViewState>
        @Composable get() {
            return mutableBubbleViewState(BubbleAnchor(0f, 0f))
        }
    override val content: BubbleViewContent = { scope, controller ->
        Box {
            Text("Hello，I'm Bubble")
        }
    }
}
```

- state: 描述气泡状态，如展示位置、时长、点击是否自动消失等。
- content：描述气泡ui

2. 调用`LocalBubbleViewController`展示或隐藏弹窗

```kotlin
val bubbleViewController = LocalBubbleViewController.current
onClick = {
    // 展示气泡
    bubbleViewController.showBubbleView(DemoBubbleView())
    // 隐藏气泡
    bubbleViewController.dismissBubbleView(this@DemoBubbleView)
}
```