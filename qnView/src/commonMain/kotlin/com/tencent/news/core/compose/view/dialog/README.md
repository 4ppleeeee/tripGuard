# 弹窗使用指南

## 概述

`DialogController` 是一个用于管理和控制弹窗显示的组件，支持多种弹窗类型（全屏、底部弹窗、居中弹窗）。通过继承
`IDialog` 抽象类，可以自定义弹窗内容，并通过 `DialogController` 控制弹窗的显示和关闭。

## 弹窗类型

`DialogShowType` 定义了以下弹窗类型：

- `FullScreen`：全屏弹窗。
- `BottomSheet`：底部弹窗。
- `Center`：居中弹窗。

## 基本用法

### 1. 定义弹窗

通过继承 `IDialog` 抽象类，定义弹窗的内容和类型：

```kotlin
internal class MyDialog : IDialog() {
    override val showType: DialogShowType = DialogShowType.BottomSheet
    override val content: @Composable (pageScope: CoroutineScope, controller: DialogController) -> Unit =
        { _, _ ->
            // 弹窗内容
        }
}
```

### 2. 显示弹窗

使用 `DialogController` 显示弹窗：

```kotlin
val dialogController = LocalDialogController.current
onClick = {
    dialogController.showDialog(scope = coroutineScope, dialog = MyDialog())
}
```

### 3. 关闭弹窗

关闭当前显示的弹窗：

```kotlin
dialogController.dismissDialog(null)
```