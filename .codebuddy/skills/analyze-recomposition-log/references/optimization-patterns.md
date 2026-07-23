# 常见优化模式与代码示例

本文档提供常见的 Recomposition 优化模式与代码示例，帮助 AI 为识别出的问题生成优化建议。

## 模式 1：使用 `remember` 缓存计算结果

### 问题场景

组件内部有复杂计算，每次重组都重新计算，导致耗时过长。

### 示例代码

```kotlin
// ❌ 优化前：每次重组都重新计算
@Composable
fun ComplexCalculation(data: List<Item>) {
    // 每次重组都会遍历列表并计算结果
    val result = data.filter { it.isValid() }.map { it.calculate() }
    
    Text(text = "Result: $result")
}

// ✅ 优化后：使用 remember 缓存计算结果
@Composable
fun ComplexCalculation(data: List<Item>) {
    // 只有 data 变化时才重新计算
    val result = remember(data) {
        data.filter { it.isValid() }.map { it.calculate() }
    }
    
    Text(text = "Result: $result")
}
```

### 适用场景

- 组件内部有复杂计算
- 计算结果依赖稳定参数
- 参数变化频率低于重组频率

## 模式 2：使用 `derivedStateOf` 减少 State 变化传播

### 问题场景

State 变化过于频繁（如进度条每帧都变化），导致依赖它的组件高频重组。

### 示例代码

```kotlin
// ❌ 优化前：progress 每帧都变化，导致 ProgressTrack 每帧都重组
@Composable
fun WSVideoSeekProgressBar(progress: Float) {
    ProgressTrack(progress = progress)
}

// ✅ 优化后：使用 derivedStateOf 降低更新频率
@Composable
fun WSVideoSeekProgressBar(progress: Float) {
    // 只有进度变化超过 1% 时才更新
    val derivedProgress by derivedStateOf {
        (progress * 100).toInt() / 100f
    }
    
    ProgressTrack(progress = derivedProgress)
}

// ✅ 优化后（方案2）：使用 snapshotFlow + debounce 降低更新频率
@Composable
fun WSVideoSeekProgressBar(progress: Float) {
    val derivedProgress by produceState(initialValue = progress) {
        snapshotFlow { progress }
            .debounce(100) // 100ms 内的变化会被合并
            .collect { value = it }
    }
    
    ProgressTrack(progress = derivedProgress)
}
```

### 适用场景

- State 变化过于频繁（如进度条、动画）
- 不需要每帧都更新 UI
- 可以接受一定的延迟（如 100ms）

## 模式 3：使用 `remember` 缓存回调函数

### 问题场景

每次重组都创建新的回调函数实例，导致子组件因为参数变化而重组。

### 示例代码

```kotlin
// ❌ 优化前：每次重组都创建新的回调函数实例
@Composable
fun ParentComponent() {
    val onClick = { /* 处理点击 */ }
    
    ChildComponent(onClick = onClick)
}

// ✅ 优化后：使用 remember 缓存回调函数
@Composable
fun ParentComponent() {
    val onClick = remember { { /* 处理点击 */ } }
    
    ChildComponent(onClick = onClick)
}

// ✅ 优化后（方案2）：如果回调函数依赖参数，使用 remember 包装
@Composable
fun ParentComponent(item: Item) {
    val onClick = remember(item) { { /* 处理点击，使用 item */ } }
    
    ChildComponent(onClick = onClick)
}
```

### 适用场景

- 回调函数作为子组件参数
- 子组件因为回调函数变化而重组
- 回调函数的逻辑不依赖频繁变化的 State

## 模式 4：拆分组件，创建独立的重组作用域

### 问题场景

单个组件过于复杂，每次重组都重新计算所有部分，即使有些部分不需要更新。

### 示例代码

```kotlin
// ❌ 优化前：整个组件在一个重组作用域中
@Composable
fun UserProfile(user: User, isFollowed: Boolean) {
    Column {
        // 用户信息部分（不经常变化）
        Text(text = user.name)
        Text(text = "Followers: ${user.followerCount}")
        
        // 关注按钮部分（经常变化）
        Button(
            onClick = { /* 关注/取关 */ },
            text = if (isFollowed) "Following" else "Follow"
        )
    }
}

// ✅ 优化后：拆分为独立的 Composable，创建独立的重组作用域
@Composable
fun UserProfile(user: User, isFollowed: Boolean) {
    Column {
        // 用户信息部分（有自己的重组作用域，只有 user 变化时才重组）
        UserInfo(user = user)
        
        // 关注按钮部分（有自己的重组作用域，只有 isFollowed 变化时才重组）
        FollowButton(isFollowed = isFollowed)
    }
}

@Composable
fun UserInfo(user: User) {
    Column {
        Text(text = user.name)
        Text(text = "Followers: ${user.followerCount}")
    }
}

@Composable
fun FollowButton(isFollowed: Boolean) {
    Button(
        onClick = { /* 关注/取关 */ },
        text = if (isFollowed) "Following" else "Follow"
    )
}
```

### 适用场景

- 单个组件过于复杂
- 组件的不同部分更新频率不同
- 希望高频更新的部分不影响低频更新的部分

## 模式 5：使用 `key` 参数为列表项创建独立的重组作用域

### 问题场景

LazyColumn 中的列表项因为没有稳定的 key，导致滚动时频繁重组。

### 示例代码

```kotlin
// ❌ 优化前：没有使用 key，或 key 不稳定
@Composable
fun PostList(posts: List<Post>) {
    LazyColumn {
        items(posts) { post ->
            PostItem(post = post)
        }
    }
}

// ✅ 优化后：使用稳定的 key
@Composable
fun PostList(posts: List<Post>) {
    LazyColumn {
        items(
            items = posts,
            key = { post -> post.id } // 使用稳定的唯一 ID 作为 key
        ) { post ->
            PostItem(post = post)
        }
    }
}
```

### 适用场景

- LazyColumn/LazyRow 中的列表项
- 列表项有稳定的唯一 ID
- 列表经常变化（如新增、删除、排序）

## 模式 6：使用 `@Stable` 或 `@Immutable` 注解标记数据类

### 问题场景

数据类没有标记 `@Stable` 或 `@Immutable`，导致 Compose 无法确保其稳定性，从而进行不必要的重组。

### 示例代码

```kotlin
// ❌ 优化前：数据类没有标记 @Stable 或 @Immutable
data class User(
    val name: String,
    val age: Int
)

// ✅ 优化后：使用 @Immutable 标记不可变数据类
@Immutable
data class User(
    val name: String,
    val age: Int
)

// ✅ 优化后（方案2）：使用 @Stable 标记稳定数据类
@Stable
data class User(
    val name: String,
    val age: Int,
    val followerCount: MutableState<Int> = mutableStateOf(0)
)
```

### 适用场景

- 数据类作为 Composable 参数
- 数据类是不可变的（所有属性都是 val）
- 数据类是稳定的（属性变化时会通知 Compose）

## 模式 7：使用 `Modifier.graphicsLayer` 减少绘制区域

### 问题场景

组件频繁重组，但只有部分内容变化，导致整个组件都重新绘制。

### 示例代码

```kotlin
// ❌ 优化前：整个组件都重新绘制
@Composable
fun ProgressBar(progress: Float) {
    Column {
        Text(text = "Progress: ${(progress * 100).toInt()}%")
        LinearProgressIndicator(progress = progress)
    }
}

// ✅ 优化后：使用 graphicsLayer 减少绘制区域
@Composable
fun ProgressBar(progress: Float) {
    Column {
        // Text 部分不参与进度动画的绘制
        Text(text = "Progress: ${(progress * 100).toInt()}%")
        
        // LinearProgressIndicator 使用 graphicsLayer 优化绘制
        LinearProgressIndicator(
            progress = progress,
            modifier = Modifier.graphicsLayer {
                // 只绘制进度条部分
            }
        )
    }
}
```

### 适用场景

- 组件频繁重组，但只有部分内容变化
- 组件包含动画或进度条
- 希望减少绘制区域，提高性能

## 模式 8：使用 `LazyColumn` 替代 `Column` + `verticalScroll`

### 问题场景

使用 `Column` + `verticalScroll` 显示长列表，导致所有列表项都一次性加载，内存占用高，重组性能差。

### 示例代码

```kotlin
// ❌ 优化前：使用 Column + verticalScroll
@Composable
fun PostList(posts: List<Post>) {
    Column(
        modifier = Modifier.verticalScroll(rememberScrollState())
    ) {
        posts.forEach { post ->
            PostItem(post = post)
        }
    }
}

// ✅ 优化后：使用 LazyColumn
@Composable
fun PostList(posts: List<Post>) {
    LazyColumn {
        items(posts, key = { it.id }) { post ->
            PostItem(post = post)
        }
    }
}
```

### 适用场景

- 显示长列表
- 列表项数量不确定
- 希望只加载可见区域的列表项

## 总结

通过以上优化模式，可以有效减少不必要的重组，提高 Compose UI 的性能。在实际优化时，需要根据日志分析结果，选择合适的优化模式。
