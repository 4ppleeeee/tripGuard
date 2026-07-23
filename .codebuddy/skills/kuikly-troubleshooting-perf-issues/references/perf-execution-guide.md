# 页面性能问题诊断执行指南

## 工作流程

### Step 1：确认问题场景

- 明确是什么页面启动慢，如果用户没有给出，主动询问
- 明确是否运行JS动态化模式，如果用户没有明确，请主动询问

### Step 2：静态代码分析

通过页面名称找到相关的页面文件。Kuikly页面名称通过Kotlin注解标注：
```kotlin
@Page("ButtonExamplePage")
internal class ButtonExamplePage: BasePager() { }
```

分析页面文件及其关联文件代码，按模式匹配问题：
- **内置模式**：优先检查「通用场景问题」
- **JS动态化模式**：优先检查「JS动态化场景问题」+ 「通用场景问题」

当发现问题时，参考下方解决方案中的代码模版修改代码，修改前系统化输出供用户确认。

### Step 3：运行时日志分析

如果通过代码静态分析无法确定问题，则检查用户是否提供了运行时日志。

**前置准备**：如果用户没有提供日志，告知获取步骤：
1. 在Pager页面启用日志输出
```kotlin
@Pager
internal class ExamplePage : BasePager {
    override fun isDebugLogEnable(): Boolean = true
}
```

2. 复现问题后导出日志
```kotlin
override fun created(){
    setTimeout(2000) {
        println(getPageTrace()?.pageEventTrace?.dump(true))
    }
}
```

3. 将日志拷贝出来提供给AI

**日志分析**：通过 `references/PageCreateTrace.kt` 中 `PageEventKind` 了解事件类型定义。事件成组对应（XXXStart/XXXEnd），ViewWillInit/ViewDidInit 是例外。

分析思路：
> Step 1. 通过timestamp差值分析大区间耗时，找出问题区间
>   - CreateStart-CreateEnd：页面初始化耗时
>   - BuildStart-BuildEnd：body函数执行耗时
>   - LayoutStart-LayoutEnd：布局耗时
>   - CallModuleStart-CallModuleEnd：module方法调用耗时
>   - ModuleCallbackStart-ModuleCallbackEnd：module回调耗时
>   - FireObserverFnStart-FireObserverFnEnd：observer调用耗时
>   - ViewWillInit-ViewDidInit：View初始化耗时
> Step 2. 通过事件次数判断是否高频
>   - LogModule的高频或耗时调用
>   - 其他高频函数频率和耗时是否超出预期
> Step 3. 通过Layout后节点数量判断首页是否过于复杂
>   - 节点数量超过1200，建议重点分析布局合理性
> Step 4. 通过observer数量判断是否存在不合理监听
>   - 大量observer关联一个observable时，考虑observable拆分

定位问题后，在工程中找到对应代码，结合Step 2进行针对性分析。

如果日志和代码都无法分析出问题，参考 `references/generic-perf-troubleshooting.md` 为用户提供定位建议。

---

## 通用场景问题

Kuikly内置模式性能接近原生，使用不当时容易出现性能问题，动态化模式下更为明显。

### 1. 生命周期函数调用被阻塞

Pager生命周期包括 `created`、`pageDidAppear`、`viewWillLoad`、`viewDidLoad` 等（详见 [Pager生命周期](pager-lifecycle.md) 和 [ComposeView生命周期](compose-view-lifecycle.md)），以及 `willInit`、`initModule`、`didInit`、`body` 等。确保override实现轻量快速。

**案例**：`created` 中 `loadInitialData` 使用 `awaitAll` 等待所有请求完成，阻塞Pager创建。

```kotlin
import com.tencent.kuikly.core.coroutines.GlobalScope

override fun created() {
    super.created()
    loadInitialData()
}

private fun loadInitialData() {
    GlobalScope.launch {
        try {
            val adsJob = async { requestADSSwitch() }
            val recentListJob = async { requestRecentList() }
            val activityJob = async { requestActivityList() }
            val results = listOf(adsJob, recentListJob, activityJob).awaitAll()
        }
    }
}
```

**解决方案**：改造为异步回调方式，避免阻塞生命周期。

```kotlin
override fun created() {
    super.created()
    loadInitialData()
}

private fun loadInitialData() {
    requestADSSwitch { /* completion callback */ }
    requestRecentList { /* completion callback */ }
    requestActivityList { /* completion callback */ }
}
```

### 2. 首屏异步拉取过多数据

数据量过大导致下载、传输、解析各环节耗时增加。

**案例**：首屏加载300K左右JSON数据（含2、3屏内容），无本地缓存。

**解决方案**：
1. 首屏仅拉取本屏数据，首屏成功后再预加载后续数据
2. 实现本地缓存，先展示缓存数据，拉到远端数据后更新

### 3. 过度的日志输出

避免在生命周期回调或首屏数据处理中输出过大的日志。

**案例**：对网络返回数据整体打印。

```kotlin
acquireModule<NetworkModule>(NetworkModule.MODULE_NAME).requestPost(
    "https://example.com/example_service",
    JSONObject().apply { put("key", "value") }
) { data, success, errorMsg, response ->
    KLog.i("ExampleTag", data.toString()) // 问题：全量打印
}
```

**解决方案**：限制打印长度，使用 debug log。

```kotlin
) { data, success, errorMsg, response ->
    val str = data.toString()
    KLog.d("ExampleTag", if(str.length > 100) str.substring(0, 100) else str)
}
```

### 4. 同步Module调用过于耗时

同步方法会等待返回，耗时过长导致Kuikly线程卡住。

**案例A**：`created` 中调用耗时Module接口。

```kotlin
val data = acquireModule<BizDataModule>(BizDataModule.MODULE_NAME).getData()
```

**解决方案**：降低耗时或改为异步回调。

```kotlin
acquireModule<BizDataModule>(BizDataModule.MODULE_NAME).getData {
    processData(it)
}
```

**案例B**：循环中频繁调用同步Native方法（如 `dateFormatter` 通过 `callNativeSync` 调用宿主侧ArkTS）。

```kotlin
internal class MyDateModule : BaseModule() {
    override fun moduleName(): String = MODULE_NAME
    fun dateFormatter(timeStamp: Long, format: String): JSONObject {
        val timeObject = JSONObject()
        timeObject.put("timeStamp", timeStamp)
        timeObject.put("format", format)
        return JSONObject(callNativeSync(DATE_FORMATTER, timeObject))
    }
    fun callNativeSync(methodName: String, data: JSONObject?): String {
        return toNative(false, methodName, data?.toString(), null, true).toString()
    }
}
```

在循环中逐条调用，严重卡顿：

```kotlin
fun parseJsonData(data: JSONObject?) {
    val list = data?.optJSONArray("filterList")
    if (list != null) {
        for (i in 0 until list.length()) {
            val item = list.optJSONObject(i)
            val startTime = item.opt(0)
            val formatStart = dateModule?.dateFormatter(startTime as Long, "yyyy/MM/dd")
        }
    }
}
```

**解决方案**：
1. 端侧以C实现 `MyDateModule`，参考 [registerExampleCModule](https://github.com/Tencent-TDS/KuiklyUI/blob/main/ohosApp/entry/src/main/cpp/napi_init.cpp)
2. 按需调用，仅在显示前执行
3. 使用Kuikly自带 `CalendarModule` 或改为异步调用

### 5. attr block中放置过多业务逻辑

attr block仅用于 observable 及属性更新/绑定，observable更新时会被重复执行，不应包含计算逻辑。

**案例**：attr中包含 `calculateMargin()`、`calulateBorderRadius()`、`calulateHeight()` 等计算。

**解决方案**：将计算逻辑移出attr，不变的用普通变量，会变的用observable。

```kotlin
val myMargin = calculateMargin()
var myBorderRadius by observable(0f)
var myHeight by observable(0f)

fun updateBorderRadius() { myBorderRadius = calulateBorderRadius() }
fun updateHeight() { myHeight = calulateHeight() }

View {
    attr {
        padding(myPadding)
        margin(myMargin)
        borderRadius(myBorderRadius)
        border(Border(lineWidth = 0.5f, lineStyle = BorderStyle.SOLID, color = Color(0xFFFB8C00)))
        allCenter()
        height(myHeight)
    }
}
```

### 6. 数据更新逻辑不合理

**案例**：`mergeItemsOrdered` 复杂度 O(n²)，数据量增长后造成严重卡顿。

```kotlin
private fun refreshViewModel() {
    bindValueChange({ attr.itemsUpdated }) {
        viewModel?.refreshAll(items = attr.items)
    }
}

override fun refreshAll(items: List<ShopItemBean>) {
    val targetDataList = mutableListOf<BaseDataItem<ItemBean>>().also {
        it.addAll(itemProcessor.initDataList(items))
    }
    dataList.mergeItemsOrdered(targetDataList, { item1, item2 -> item1.data == item2.data })
    refreshRenderList()
}
```

**解决方案**：数据不重复且有序时改为append；否则用 [diffUpdate](https://kuikly.tds.qq.com/DevGuide/reactive-update.html#高效更新列表-diffupdate) 差量更新。

### 7. Observable泄漏

**案例**：大量使用 `ReactiveObserver.bindValueChange` 无对应 `unbindValueChange`，observerOwnerMap持续增长。

**解决方案**：
1. 确保 `bindValueChange` 不在attr block等重复执行位置调用
2. 子view移除时执行unbind：

```kotlin
override fun didRemoveFromParentView() {
    super.didRemoveFromParentView()
    ReactiveObserver.unbindValueChange(model.showShowSelector)
}
```

---

## JS动态化场景问题

动态化模式以JS方式运行，在复杂信息流场景中动态化与Native差距约20%。偏差较大时应检查实现细节。

### 1. JSON数据解析慢

内置模式无问题，JS动态化模式易有性能问题。

**案例**：
```kotlin
val jsonObj = JSONObject(jsonStr)
val value = jsonObj.optString("key", "")
```

**解决方案**：使用JS引擎内置的解析能力。**2.7.0+版本默认启用，无需手动设置**。低版本需手动设置：

```kotlin
JSON.useNativeMethod = true
```

### 2. Range比较慢

**案例**：scroll中使用 `IntRange.intersect` 判断重叠，滚动缓慢。

**解决方案**：改为数值比较代替集合重叠判断。

```kotlin
scroll {
    var index = 0
    val offsetX = it.offsetX
    val pageListWidth = it.viewWidth
    ctx.galleryList.forEach { item ->
        val itemLeft = index * pageListWidth
        var lower = max((itemLeft).toInt(), (offsetX).toInt())
        var upper = min((itemLeft + pageListWidth).toInt(), (offsetX + pageListWidth).toInt())
        var count = if (upper - lower >= 0) upper - lower + 1 else 0
        val visiblePercentage = count * 1f / pageListWidth.toInt()
        item.transformScale1 = 0.85f + (1 - 0.85f) * visiblePercentage
        index++
    }
}
```

### 3. 集合操作慢

Kotlin集合类在JS引擎中效率低。Kuikly 1.9.0+ 内置JS集合能力支持，建议升级至2.x或至少1.9.0以上。
