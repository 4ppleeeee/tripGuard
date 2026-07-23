# kmm跨端背景下，客户端如何设计一个优雅的ViewModel

> UI层架构设计的核心宗旨：逻辑与UI解耦

## 先说结论，一个vm接口应该包含以下4个方面
1. 固定的属性：基础数据类型，String、Int、Boolean等
2. 可变的属性：统一用 StateFlow 或 SharedFlow 进行包装
3. 与UI层交互的fun
4. 其他子组件的vm

## 一个设计合理的vm接口内，不应该出现什么？
1. 数据结构model类：出现这个就说明UI与数据强偶合了
2. 业务层的controller、presenter：出现这个说明UI与逻辑强偶合了
3. 不应该出现 var 可变属性：修改逻辑应该在vm实现类执行，不应该暴露在vm接口中
4. 不应该出现 MutableStateFlow、MutableSharedFlow：flow的更新应该在vm实现类执行，不应该暴露在vm接口中

## 面向UI设计vm接口

与`面向UI设计`相对的是`面向业务设计`，我们以一个用户头像的需求为例，看设计差异：

### 以‘用户头像’需求，对比两种设计差异

#### 简单逻辑：面向业务设计
```kotlin
interface IUserIconVM {
    val iconSize: Int 
    val userInfo: IUserInfo // 用户信息
    fun jumpUserPage(userInfo: IUserInfo) // 跳转用户个人页
}

@Composable
fun UserIcon(vm: IUserIconVM) {
    Image(
        url = vm.userInfo.resDto.headUrl,
        modifier = Modifier
            .size(vm.iconSize.dp)
            .clickable {
                vm.jumpUserPage(vm.userInfo)
            }
    )
}
```

#### 简单逻辑：面向UI设计
```kotlin
interface IUserIconVM {
    val iconSize: Int       
    val userIcon: String
    fun onIconClick()
}

@Composable
fun UserIcon(vm: IUserIconVM) {
    Image(
        url = vm.userIcon,
        modifier = Modifier
            .size(vm.iconSize.dp)
            .clickable {
                vm.onIconClick()
            }
    )
}
```

两种设计方式初步对比，似乎收益只是隐藏了一个 IUserInfo model类，进步不大；

这也是大家一个场景误区：潜意识都会认为我们的UI逻辑足够简单（因为从视觉稿上看，他确实简单），只是一个model字段的赋值过程，对vm的设计是否过度要求了？

我们再把业务复杂化，再看一下：
- 跳转逻辑复杂化，针对企鹅号、用户自己、客态底层页，要区分不同页面跳转
- 点击跳转需要做埋点上报、需要留下日志
- 头像尺寸逻辑有差异：cp用户头像要放大到50，其余正常头像尺寸40
- 用户头像逻辑有差异：如果是用户自己，优先展示他自创的AI头像

#### 复杂逻辑：面向业务设计
```kotlin
// qnCore模块：接口层
interface IUserIconVM {
    val iconSize: Int // 宽高一致，单位dp
    val userInfo: IUserInfo
    fun jumpToCpPage(userInfo: IUserInfo)          // 企鹅号主页
    fun jumpToUserHomePage(userInfo: IUserInfo)    // 当前登录用户的个人主页
    fun jumpToGuestPage(userInfo: IUserInfo)       // 游客个人主页
}

// qnCompose模块：UI层
@Composable
fun UserIcon(vm: IUserIconVM) {
    val userInfo = vm.userInfo
    val iconUrl = if (userInfo.isMySelf) {
        appLogin().getMainLoginUserInfo().getAIIconUrl()
    } else {
        userInfo.resDto.headUrl
    }

    Image(
        url = iconUrl,
        modifier = Modifier
            .size(vm.iconSize.dp)
            .clickable {
                if (userInfo.isCpUser) {
                    vm.jumpToCpPage(userInfo)          // 企鹅号主页
                } else if (userInfo.isMySelf) {
                    vm.jumpToUserHomePage(userInfo)    // 当前登录用户的个人主页
                } else {
                    vm.jumpToGuestPage(userInfo)       // 游客个人主页
                }

                appReport().reportBeacon(
                    "user_icon_click",
                    params = mapOf("user_id" to userInfo.baseDto.suid)
                )

                UserLog.file("点击用户头像：${userInfo.baseDto.suid}")
            }
    )
}

// qnUser模块：实现层
class UserIconVM(override val userInfo: IUserInfo) : IUserIconVM {

    override val iconSize = buildIconSize()

    private fun buildIconSize() : Int {
        if (userInfo.isCp) {
            return 50   // 企鹅号头像放大，特意强调
        } else {
            return 40
        }
    }

    override fun jumpToCpPage(userInfo: IUserInfo) {
        appRouter().jumpToCpPage(userInfo)
    }

    override fun jumpToUserHomePage(userInfo: IUserInfo) {
        appRouter().jumpToUserHomePage(userInfo)
    }

    override fun jumpToGuestPage(userInfo: IUserInfo) {
        appRouter().jumpToGuestPage(userInfo)
    }

}
```

#### 复杂逻辑：面向UI设计
```kotlin
// qnCore模块：接口层
interface IUserIconVM {
    val iconSize: Int
    val userIcon: String
    fun onIconClick()
}

// qnCompose模块：UI层
@Composable
fun UserIcon(vm: IUserIconVM) {
    Image(
        url = vm.userIcon,
        modifier = Modifier
            .size(vm.iconSize.dp)
            .clickable {
                vm.onIconClick()
            }
    )
}

// qnUser模块：实现层
class UserIconVM(val userInfo: IUserInfo) : IUserIconVM {
    override val iconSize = buildIconSize()
    override val userIcon = buildIconUrl()
    
    private fun buildIconSize() : Int {
        if (userIcon.isCp) {
            return 50   // 企鹅号头像放大，特意强调
        } else {
            return 40
        }
    }

    private fun buildIconUrl() : String {
        if (userIcon.isMySelf) {
            return appLogin().getMainLoginUserInfo().getAIIconUrl()
        } else {
            return userInfo.resDto.headUrl
        }
    }

    override fun onIconClick() {
        if (userInfo.isCpUser) {
            appRouter().jumpToCpPage(userInfo)          // 企鹅号主页
        } else if (userIcon.isMySelf) {
            appRouter().jumpToUserHomePage(userInfo)    // 当前登录用户的个人主页
        } else {
            appRouter().jumpToGuestPage(userInfo)       // 游客个人主页
        }

        appReport().reportBeacon(
            "user_icon_click",
            params = mapOf("user_id" to userInfo.baseDto.suid)
        )
        
        UserLog.file("点击用户头像：${userInfo.baseDto.suid}")
    }

}
```

两种设计方式初步对比，这次的收益更明显：
- 面向UI设计情况下，vm接口与UI实现，没有任何修改，因为我们所提的几条‘产品逻辑’改动，本质上对UI就是没影响
- 需要向 qnCompose模块 暴露的工具方法，例如：IUserInfo 的几个扩展工具 isCpUser、isMySelf 都隐藏起来了
- 需要向 qnCompose模块 暴露的基础服务，例如：appLogin()、appRouter()、UserLog 都隐藏起来了

我们再把问题复杂化一些：
- 现在除了业务侧用户头像，还要实现一个广告卡片的广告主头像；
- UI与业务完全一致，我们期望能服用 UserIcon 这个UI组件；
- 广告主头像点击后，应该跳转广告底层页，且广告需要上报计费接口

#### 扩展与复用：面向业务设计
```kotlin
// qnCore模块：接口层
interface IUserIconVM {
    val iconSize: Int // 宽高一致，单位dp
    val userInfo: IUserInfo
    val adOrder: IAdOrder?
    fun jumpToCpPage(userInfo: IUserInfo)          // 企鹅号主页
    fun jumpToUserHomePage(userInfo: IUserInfo)    // 当前登录用户的个人主页
    fun jumpToGuestPage(userInfo: IUserInfo)       // 游客个人主页
    fun jumpToAdDetail(adOrder: IAdOrder)          // 跳转广告落地页
}

// qnCompose模块：UI层
@Composable
fun UserIcon(vm: IUserIconVM) {
    val adOrder = vm.adOrder
    val userInfo = vm.userInfo

    val iconUrl = if (adOrder != null) {
        adOrder.info.advertiserIcon
    } else if (userInfo.isMySelf) {
        appLogin().getMainLoginUserInfo().getAIIconUrl()
    } else {
        userInfo.resDto.headUrl
    }

    Image(
        url = iconUrl,
        modifier = Modifier
            .size(vm.iconSize.dp)
            .clickable {
                if (adOrder != null) {
                    vm.jumpToAdDetail(adOrder)

                    adReport().reportAdClick(adOrder)

                    AdFeedsLog.file("点击广告主头像：${adOrder.info.oid}")
                } else {
                    if (userInfo.isCpUser) {
                        vm.jumpToCpPage(userInfo)          // 企鹅号主页
                    } else if (userInfo.isMySelf) {
                        vm.jumpToUserHomePage(userInfo)    // 当前登录用户的个人主页
                    } else {
                        vm.jumpToGuestPage(userInfo)       // 游客个人主页
                    }

                    appReport().reportBeacon(
                        "user_icon_click",
                        params = mapOf("user_id" to userInfo.baseDto.suid)
                    )

                    UserLog.file("点击用户头像：${userInfo.baseDto.suid}")
                }
            }
    )
}

// qnUser模块：实现层
class UserIconVM(
    override val userInfo: IUserInfo,
    override val adOrder: IAdOrder
) : IUserIconVM {

    override val iconSize = buildIconSize()

    private fun buildIconSize(): Int {
        if (userInfo.isCp) {
            return 50   // 企鹅号头像放大，特意强调
        } else {
            return 40
        }
    }

    override fun jumpToCpPage(userInfo: IUserInfo) {
        appRouter().jumpToCpPage(userInfo)
    }

    override fun jumpToUserHomePage(userInfo: IUserInfo) {
        appRouter().jumpToUserHomePage(userInfo)
    }

    override fun jumpToGuestPage(userInfo: IUserInfo) {
        appRouter().jumpToGuestPage(userInfo)
    }

    override fun jumpToAdDetail(adOrder: IAdOrder) {
        adFeedsManager().jumpToAdDetail(adOrder)
    }

}
```


#### 扩展与复用：面向UI设计
```kotlin
// qnCore模块：接口层
interface IUserIconVM {
    val iconSize: Int
    val userIcon: String
    fun onIconClick()
}

// qnCompose模块：UI层
@Composable
fun UserIcon(vm: IUserIconVM) {
    Image(
        url = vm.userIcon,
        modifier = Modifier
            .size(vm.iconSize.dp)
            .clickable {
                vm.onIconClick()
            }
    )
}

// qnUser模块：实现层
class UserIconVM(val userInfo: IUserInfo) : IUserIconVM {
    override val iconSize = buildIconSize()
    override val userIcon = buildIconUrl()

    private fun buildIconSize() : Int {
        if (userInfo.isCp) {
            return 50   // 企鹅号头像放大，特意强调
        } else {
            return 40
        }
    }

    private fun buildIconUrl() : String {
        if (userInfo.isMySelf) {
            return appLogin().getMainLoginUserInfo().getAIIconUrl()
        } else {
            return userInfo.resDto.headUrl
        }
    }

    override fun onIconClick() {
        if (userInfo.isCpUser) {
            appRouter().jumpToCpPage(userInfo)          // 企鹅号主页
        } else if (userInfo.isMySelf) {
            appRouter().jumpToUserHomePage(userInfo)    // 当前登录用户的个人主页
        } else {
            appRouter().jumpToGuestPage(userInfo)       // 游客个人主页
        }

        appReport().reportBeacon(
            "user_icon_click",
            params = mapOf("user_id" to userInfo.baseDto.suid)
        )

        UserLog.file("点击用户头像：${userInfo.baseDto.suid}")
    }

}

// qnUser模块：实现层
class AdvertiserIconVM(val adOrder: IAdOrder): IUserIconVM {
    override val iconSize = 40
    override val userIcon = adOrder.info.advertiserIcon

    override fun onIconClick() {
        adFeedsManager().jumpToAdDetail(adOrder)

        adReport().reportAdClick(adOrder)

        AdFeedsLog.file("点击广告主头像：${adOrder.info.oid}")
    }

}
```

两种设计方式初步对比，这次体现出的是解耦与复用：
- 解耦：解开并隔离的是业务实现
- 复用：干净的UI层、UI组件能被服用

上面的例子我们只涉及了：用户头像、广告主头像；而一个多年沉淀的产品，涉及各种业务：问答的头像、AI助手的头像。。。是可以无穷扩展的；

而这也解释了认知与实践上的一个矛盾：
- 在一个非IT从业人员来看，我们的app界面极其简单，就一个头像图片，有什么可开发的？
- 在一个开发人员的角度来看，我们十几个业务的头像，每个结构和字段都不一样，我都得挨个适配，一个头像组件写了几百行还没完

## 代码质量与架构约束

我们肯定是期望，完成需求的同时，能保持一份高质量的代码、一个设计合理的架构；
而`面向UI设计`的vm接口，是可以配合架构手段，让团队整体自发正向循环的

上面例子中，我们架构上分为3层：
- 接口层（qnCore）：纯kmm，都是接口
- 实现层（qnUser）：纯kmm，提供接口实现类
- UI层（qnCompose）：依赖kuikly等compose框架，实现UI组件

那么，我们想约束大家，定义好接口、隔离好实现、再写UI，一般会怎么做？
- 下策：每次例会定期总结，给大家宣导怎么做是好的
- 中策：在CodeReview期间设置拦截——写一些脚本或添加AI skills来检查不良写法，做红线拦截
- 上策：编译期拦截，让大家写出来的就是对的

那么`面向UI设计`的vm接口，如何实现`编译期拦截`：
- 首先：接口层（qnCore）保持绝对干净，该模块引用不到任何数据结构、controller
- 其次：UI层（qnCompose）只能依赖 接口层（qnCore），同样访问不到 实现层（qnUser）
- 最后：所有的model类、工具方法、controller逻辑，全部都在 实现层（qnUser）里，通过依赖注入的方式将实现类注入给 qnCore

这套体系下：接口层（qnCore）、实现层（qnUser）、UI层（qnCompose）的代码比例，期望是 1：8：1

——即：我们的`逻辑`有90%是与平台无关的纯kmm代码，只有10%的UI代码
这也符合我们上面提到的认知矛盾：为什么app界面看着简单，开发量却很大

而这90%的`逻辑`，也正是我们一个产品的`固定资产`，是不会随着UI框架的更替有所变化的

（做客户端的同学都知道，从native开发、RN、flutter、weex、hippy、lynx、谷歌的compose、kuikly、ovCompose，前端框架永远都在变）


## 渐进式重构、跨端复用与混合开发

- 【渐进式重构】：
根据上文所述，`面向UI设计`的vm，最大的好处是将90%的逻辑沉淀在纯kmm层，与平台无关；
那么这就给了我们渐进式改造老代码的机会： 我们可以先不切换任何新UI框架，依旧保持nativeUI开发；只是沉淀新的vm

- 【跨端复用】：
以vm为粒度，将90%的逻辑做到3端公用（安卓、iOS、鸿蒙）
至于UI跨端，在vm极致简洁的情况下，UI层开发不是效率瓶颈，有跨端框架支持固然好，没有的话native实现一下也简单

- 【混合开发】：
对于老项目，跨端方案与native实现会长期处于一个共存状态，大家肯定都遇到一个组件在native有一份、compose有一份，要维护2份的尴尬； 
而一个设计良好的vm，能解决90%的逻辑复用