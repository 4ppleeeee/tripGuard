---
type: on-demand
---

# 【开发指南】如何新增一个compose页面

首先介绍一下页面架构的关键概念：
- pageArgs：页面启动参数，封装启动一个页面所需的参数集合；是一个model类，可序列化，实现 IComposePageArgs 接口；
- pageVM：封装页面级别交互逻辑的抽象接口；简写自 pageViewModel，采用 MVVM 设计模式，实现 IStructPageViewModel 接口；
- pageWidget：页面架构自定义的一套树状model结构，用来描述结构化页面（也叫‘品字形’页面）中的UI组件关系，继承自 StructPageWidget2（创建一个 pageVM 时，需要传入一个 pageWidget）
- dataRepo：封装页面网络接口请求及数据解析逻辑，实现 IStructDataRepo 接口（创建一个 pageWidget 时，需要传入一个 dataRepo）

其次介绍一下工程模块化规范：
- 新增代码，应该区分出 对外接口、逻辑实现、UI组件，3个层次；保证代码模块结构合理
- 对外接口层：只有 qnCore 模块一个，内部主要定义各类业务接口（接口普遍命名以 I 开头）、枚举值、简单model类
- 逻辑实现层：qnAd、qnDetail、qnFeeds、qnMedia、qnUser 其中之一，具体根据开发的功能模块，自行选择合适模块
- UI组件层：有 qnCompose 和 qnView 2个模块，qnView 放置全局公共UI组件；qnCompose 为各业务UI组件
- 逻辑服务接口：AdService、DetailService、FeedsService、MediaService、UserService 其中之一，代码同样位于 qnCore 模块中，与`逻辑实现层`的几个模块一一对应；
由于工程结构约束：qnCompose和qnView 模块，只能调用 qnCore 的代码，无法直接访问`逻辑实现层`的几个模块，因此需要通过对应的`逻辑服务接口`，对外提供方法调用，以达到抽象解耦、隐藏实现的目的

新增一个compose页面，分为以下4大步骤：

## 1. 【约定业务命名】
明确新功能的：功能前缀英文名、所在业务模块、业务包名，以 `AI问答` 功能为例：
- 功能前缀英文名：AIQA （后续的类名、常量名，起名时应该统一使用该前缀）
- 所在业务模块：qnUser （用户相关业务，逻辑实现类应该都尽可能放到该模块下）
- 业务包名：com.tencent.news.core.aigc.qa （隶属于 AIGC 大模块下，二级子业务包名：qa）

## 2. 【注册页面路由】
- 在 ComposeViewKey 类中，选择合适的模块，注册新页面的路由 key；例如，`AI问答`页面的key是：ComposeViewKey.Aigc.QA_PAGE
- 继承 `ComposePage` 实现一个新页面，并通过 @Page 注解绑定 ComposeViewKey

代码示例：
```kotlin
// AI问答底层页（从专题头部能跳转过来）
@Page(ComposeViewKey.Aigc.QA_PAGE)
internal class AIQADetailPage : ComposePage() {
    // 暂时省略页面实现
}
```

## 3. 【声明页面启动参数】
1. 每个新页面，都建议新增一个自己的页面启动参数model类（鉴于以往经验，随着业务复杂，各业务总会有自己的特殊参数；因此复用现有的pageArgs类并非最佳选择）
2. 新增的 pageArgs 类，应该遵循以下规范：
- 实现 IComposePageArgs 接口
- 通过 @Serializable 进行注解，代表该类可序列化（尤其对于鸿蒙平台使用时，是必须要序列化传参的）

以`AI问答`页面为例，其 pageArgs 声明如下：
```kotlin
// AI问答页面启动参数
@Serializable
data class AIQAPageArgs(
    val eventId: String = "",            // 事件id
    val index: Int = 0,                  // 锚点
    val from: String = ""                // 来源
) : IComposePageArgs {

    fun isFromScheme(): Boolean = from == "scheme"

}
```

## 4. 【创建结构化页面组件】
1. 结构化页面组件，应该统一声明在 ComposePage 的 OnSetContent 方法中
2. 页面往往会用到协程作用域，统一用：`val pageScope = rememberCoroutineScope()`
3. 页面启动参数的解析，统一使用 rememberedPageArgs 方法，例如：`val pageArgs = rememberedPageArgs<AIQAPageArgs>()`
4. 结构化页面的 Compose组件 有2种，分别是：StructComposePage4VM 和 StructComposePage，二者差异如下：
- 如果一个页面具有较为特殊的新逻辑，需要声明新的 pageVM，则需要使用 StructComposePage4VM
- 如果一个页面使用标准的结构化页面能力就够了（IStructPageViewModel 接口提供的能力），则可以使用 StructComposePage

### 4.1 【使用 StructComposePage4VM 创建页面】
1. 使用 StructComposePage4VM 需要新增 pageVM 接口；以`AI问答为例`，在 qnCore 模块中新增 `IAIQAPageViewModel` 接口（继承自 IStructPageViewModel）
2. 在逻辑实现层（qnAd、qnDetail、qnFeeds、qnMedia、qnUser 其中之一），新增 pageVM 接口对应的实现类；以`AI问答为例`，在 qnUser 模块中新增 `AIQAPageViewModel`
3. 创建 pageVM 需要传入一个 pageWidget 参数（继承 StructPageWidget2），用来描述页面UI组件结构；以`AI问答`为例，在 qnUser模块中新增 `AIQAPageWidget`
4. 创建 pageWidget 需要传入一个 dataRepo 参数（实现 IStructDataRepo 接口），用来定义页面的网络请求与数据解析；以`AI问答`为例，在 qnUser模块中新增 `AIQADataRepo`
5. 在逻辑服务接口中（AdService、DetailService、FeedsService、MediaService、UserService 其中之一）提供 pageVM 的创建方法；以`AI问答为例`，新增 `UserService.aigc.createQAPageVM` 方法
（注意，UserService下会按业务模块拆分很多子接口，需要根据当前功能归属，选择合适接口；以`AI问答为例`，就适合放在 aigc 子接口下）
6. 最终，在 ComposePage 的 OnSetContent 方法中，创建 StructComposePage4VM，并传入新创建的 pageVM，即可启动页面进行展示

> 总结来看，新增一个`AI问答`页面，功能前缀英文名：AIQA，采用 pageVM 方式，涉及以下文件改动：
> - qnCore 模块：新增枚举值 ComposeViewKey.Aigc.QA_PAGE
> - qnCore 模块：新增页面启动参数类 AIQAPageArgs
> - qnCore 模块：新增页面pageVM接口 IAIQAPageViewModel
> - qnCore 模块：新增工厂方法 UserService.aigc.createQAPageVM
> - qnUser 模块：新增实现类 AIQAPageViewModel
> - qnUser 模块：新增实现类 AIQAPageWidget
> - qnUser 模块：新增实现类 AIQADataRepo
> - qnCompose 模块：新增UI页面 AIQADetailPage

代码示例如下：
```kotlin
// 该类属于`接口协议层`，位于 qnCore 模块下：
// 路径：qnCore/src/commonMain/kotlin/com/tencent/news/core/aigc/qa/page/IAIQAPageViewModel.kt
interface IAIQAPageViewModel : IStructPageViewModel {
    // AI问答，列表卡片‘引用来源’数据
    fun getRefArticleIndexMap(ref: List<String>): Map<String, IKmmFeedsItem>
}

// 该类数据`逻辑实现层`，且属于用户相关业务，位于 qnUser 模块下：
// 路径：qnUser/src/commonMain/kotlin/com/tencent/news/core/aigc/qa/page/AIQAPageViewModel.kt
class AIQAPageViewModel(
    private val pageArgs: AIQAPageArgs,
    pageLifecycleFlow: SharedFlow<PageLifecycleEvent>,
    pageScope: CoroutineScope,
) : StructPageViewModel(
    FeedsService.listFactory.createFlexFeedsController(AIQAPageWidget(pageArgs)),
    pageLifecycleFlow,
    pageScope
), IAIQAPageViewModel {

    override fun getRefArticleIndexMap(ref: List<String>): Map<String, IKmmFeedsItem> {
        // 省略实现
    }

}

// 该类数据`逻辑实现层`，且属于用户相关业务，位于 qnUser 模块下：
// 路径：qnUser/src/commonMain/kotlin/com/tencent/news/core/aigc/qa/page/AIQAPageWidget.kt
class AIQAPageWidget(val pageArgs: AIQAPageArgs) : StructPageWidget2(
    pageConfig = StructPageConfig(
        dataRepo = AIQADataRepo(pageArgs),
    )
)

// 该类数据`逻辑实现层`，且属于用户相关业务，位于 qnUser 模块下：
// 路径：qnUser/src/commonMain/kotlin/com/tencent/news/core/aigc/qa/page/AIQADataRepo.kt
class AIQADataRepo(val pageArgs: AIQAPageArgs) : IStructDataRepo {
    override fun createResetRequest(
        defaultRequest: DataRequest,
        dataEnv: StructDataEnv
    ): NetworkBuilder<IKmmKeep> {
        // 省略实现
    }
}


// 该类位于 UserService 的逻辑服务接口中，位于 qnUser 模块下：
// 路径：qnUser/src/commonMain/kotlin/com/tencent/news/core/setup/AigcManager.kt
internal object AigcManager : IAigcManager {

    override fun createQAPageVM(
        pageArgs: AIQAPageArgs,
        pageFlow: SharedFlow<PageLifecycleEvent>,
        pageScope: CoroutineScope,
    ) = AIQAPageViewModel(pageArgs, pageFlow, pageScope)

}

// 该类属于`UI实现层`，位于 qnCompose 模块下：
// 路径：qnCompose/src/commonMain/kotlin/com/tencent/news/core/compose/aigc/qa/page/AIQADetailPage.kt
@Page(ComposeViewKey.Aigc.QA_PAGE)
internal class AIQADetailPage : ComposePage() {

    override fun sceneName() = "AIQA"

    @Composable
    override fun OnSetContent() {
        super.OnSetContent()

        val pageScope = rememberCoroutineScope()
        val pageArgs = rememberedPageArgs<AIQAPageArgs>()

        StructComposePage4VM({
            // qnCompose 模块无法直接应用 qnUser 模块中的 AIQAPageViewModel 类；
            // 需要通过 UserService 暴露接口进行调用，createQAPageVM 方法返回值类型为 IAIQAPageViewModel
            UserService.aigc.createQAPageVM(pageArgs, pageLifecycleFlow.lifecycleFlow, pageScope)
        })
    }

}
```

### 4.2 【使用 StructComposePage 创建页面】
> 使用 StructComposePage 实现，相对简单，因为不需要新增 pageVM，而是使用通用的 StructPageViewModel
1. 在逻辑实现层（qnAd、qnDetail、qnFeeds、qnMedia、qnUser 其中之一），新增 pageWidget 实现类；以`AI问答为例`，在 qnUser 模块中新增 `AIQAPageWidget`
2. 创建 pageWidget 需要传入一个 dataRepo 参数（实现 IStructDataRepo 接口），用来定义页面的网络请求与数据解析；以`AI问答`为例，在 qnUser模块中新增 `AIQADataRepo`
3. 在逻辑服务接口中（AdService、DetailService、FeedsService、MediaService、UserService 其中之一）提供 pageVM 的创建方法；以`AI问答为例`，新增 `UserService.aigc.createQAPageWidget` 方法
   （注意，UserService下会按业务模块拆分很多子接口，需要根据当前功能归属，选择合适接口；以`AI问答为例`，就适合放在 aigc 子接口下）
4. 最终，在 ComposePage 的 OnSetContent 方法中，创建 StructComposePage，并传入新创建的 pageWidget，即可启动页面进行展示

> 总结来看，新增一个`AI问答`页面，功能前缀英文名：AIQA，采用 pageWidget 方式，涉及以下文件改动：
> - qnCore 模块：新增枚举值 ComposeViewKey.Aigc.QA_PAGE
> - qnCore 模块：新增页面启动参数类 AIQAPageArgs
> - qnCore 模块：新增工厂方法 UserService.aigc.createQAPageWidget
> - qnUser 模块：新增实现类 AIQAPageWidget
> - qnUser 模块：新增实现类 AIQADataRepo
> - qnCompose 模块：新增UI页面 AIQADetailPage

相关代码实现，大体与 pageVM 实现方式类似，区别在于以下2点：
```kotlin

// UserService 提供的工厂方法，创建的是 pageWidget，而不是 pageVM
internal object AigcManager : IAigcManager {

    override fun createQAPageWidget(pageArgs: AIQAPageArgs): StructPageWidget2 = 
        AIQAPageWidget(pageArgs)

}

// 在 ComposePage 的 OnSetContent 方法中，使用 StructComposePage 组件，直接传入 pageWidget 参数
@Page(ComposeViewKey.Aigc.QA_PAGE)
internal class AIQADetailPage : ComposePage() {

    override fun sceneName() = "AIQA"

   @Composable
   override fun OnSetContent() {
      super.OnSetContent()

      val pageArgs = rememberedPageArgs<AIQAPageArgs>()

      StructComposePage(
         pageWidget = { UserService.aigc.createQAPageWidget(pageArgs) },
         pageLifecycleFlow = pageLifecycleFlow.lifecycleFlow
      )
   }

}
```

### 5. 【常用 prompt 案例】

根据页面开发规范，采用 pageVM方式，新增一个页面：
- 功能前缀英文名：AIDemo，
- 所在业务模块：qnUser，
- 业务包名：com.tencent.news.core.aigc.demo

[操作演示](https://news-bz-1258344701.shiply-cdn.qq.com/reshub/qqnews_android/commonfile/formal/20251029191833449/Production/agent_compose_page.mp4)