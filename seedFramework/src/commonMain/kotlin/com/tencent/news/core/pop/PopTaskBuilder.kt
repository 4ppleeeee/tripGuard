package com.tencent.news.core.pop

import com.tencent.news.core.annotation.KmmInternalApi
import com.tencent.news.core.annotation.RestrictedApi
import com.tencent.news.core.app.IKmmContext
import com.tencent.news.core.isAndroidPlatform
import com.tencent.news.core.platform.api.DialogParam
import com.tencent.news.core.platform.api.appPageStack
import com.tencent.news.core.platform.api.appPopBridge
import com.tencent.news.core.platform.api.getShiplySwitch
import kotlin.experimental.ExperimentalObjCName
import kotlin.native.ObjCName

class PopTaskBuilder {
    private var id: String? = null
    private var priority = 0
    private var dialog: IPopUpView? = null

    // 用于增长弹窗/广告弹窗，当出其他高优先级弹窗时，将本弹窗dismiss掉(不允许不同优先级弹窗层叠展示)
    private var dismissSelfByHigherPriority = false

    // 弹窗出现位置，除隐私弹窗之外，其他弹窗应该是分位置管理优先级.默认弹窗展示位置为全屏展示
    private var viewLocation = PopUpViewLocation.FULL

    // 是否忽略位置判断，有些情况下，弹窗特别大，又占用底部又占用中部，为了防止拦截失效，让他们和所有的弹窗都认为是同一个位置
    private var isIgnoreViewLocation: Boolean? = null

    // 相同id的弹窗，是否可重复出现
    private var canSameIdDialogShow: Boolean = false
    private var popType: PopType? = null
    private var compare: ((currentTask: KmmPopTask, showingTask: KmmPopTask) -> Int)? = null
    private var triggerType: TriggerType = TriggerType.DEFAULT
    private var vm: IPopVM? = null
    private var isSetTriggerType = false
    private var disableReCreate = false // 安卓端弹窗是否禁用重新创建，比如页面重建时，fragment重建
    private var lifecycleObserver: PopLifecycleObserver? = null

    private var context: IKmmContext? = null

    /**
     * 无参构造函数，用于兼容历史调用
     */
    @Deprecated(
        "建议使用 PopTaskBuilder(PopType, IPopVM) 构造函数",
        ReplaceWith("PopTaskBuilder(params)")
    )
    constructor()

    constructor(params: PopTaskNecessaryParams) {
        this.context = params.context
        setType(params.popType)
        this.vm = params.vm
        // 不确定VM的实现，可能业务侧在实现时有某些字段比如View，context不能序列化，导致crash（仅安卓）
        if (isAndroidPlatform()) {
            disableReCreate = true
        }
    }

    @OptIn(ExperimentalObjCName::class)
    @ObjCName("set", swiftName = "setIgnoreViewLocation")
    fun setIgnoreViewLocation(ignoreViewLocation: Boolean): PopTaskBuilder {
        isIgnoreViewLocation = ignoreViewLocation
        return this
    }

    @OptIn(ExperimentalObjCName::class)
    @ObjCName("set", swiftName = "setType")
    @Deprecated("后续新增弹窗，建议使用PopTaskBuilder双参构造，传入PopType和VM，会由宿主的弹窗工厂创建IPopUpView")
    fun setType(type: PopType): PopTaskBuilder {
        this.popType = type
        this.priority = type.getPriority(false)
        return this
    }

    @OptIn(ExperimentalObjCName::class)
    @ObjCName("set", swiftName = "setDialog")
    @Deprecated("后续新增弹窗，建议使用PopTaskBuilder双参构造，传入PopType和VM，会由宿主的弹窗工厂创建IPopUpView")
    fun setDialog(dialog: IPopUpView?): PopTaskBuilder {
        dialog ?: return this
        this.dialog = dialog
        return this
    }

    @OptIn(ExperimentalObjCName::class)
    @ObjCName("set", swiftName = "setDismissSelfByHigherPriority")
    fun setDismissSelfByHigherPriority(dismissSelfByHigherPriority: Boolean): PopTaskBuilder {
        this.dismissSelfByHigherPriority = dismissSelfByHigherPriority
        return this
    }

    @OptIn(ExperimentalObjCName::class)
    @ObjCName("set", swiftName = "setViewLocation")
    fun setViewLocation(viewLocation: Int): PopTaskBuilder {
        this.viewLocation = viewLocation
        return this
    }

    @OptIn(ExperimentalObjCName::class)
    @ObjCName("set", swiftName = "setTriggerType")
    fun setTriggerType(triggerType: TriggerType): PopTaskBuilder {
        this.isSetTriggerType = true
        this.triggerType = triggerType
        return this
    }

    @OptIn(ExperimentalObjCName::class)
    @ObjCName("set", swiftName = "setCompare")
    fun setCompare(compare: (currentTask: KmmPopTask, showingTask: KmmPopTask) -> Int): PopTaskBuilder {
        this.compare = compare
        return this
    }

    @OptIn(ExperimentalObjCName::class)
    @ObjCName("set", swiftName = "disableReCreate")
    fun disableReCreate(disableReCreate: Boolean): PopTaskBuilder {
        this.disableReCreate = disableReCreate
        return this
    }

    @OptIn(ExperimentalObjCName::class)
    @ObjCName("set", swiftName = "setPopVm")
    fun setPopVM(vm: IPopVM): PopTaskBuilder {
        this.vm = vm
        return this
    }

    @OptIn(ExperimentalObjCName::class)
    @ObjCName("set", swiftName = "setLifecycleObeserver")
    fun setLifecycleObserver(lifecycleObserver: PopLifecycleObserver): PopTaskBuilder {
        this.lifecycleObserver = lifecycleObserver
        return this
    }

    @OptIn(ExperimentalObjCName::class)
    @ObjCName("set", swiftName = "setCanSameDialogShow")
    fun setCanSameIdDialogShow(canSameDialogShow: Boolean): PopTaskBuilder {
        this.canSameIdDialogShow = canSameDialogShow
        return this
    }

    fun build(): KmmPopTask {
        if (dialog == null) {
            setDialog(tryCreateDialogFromType())
        }
        if (!isSetTriggerType) {
            setTriggerType(popType?.triggerType ?: TriggerType.DEFAULT)
        }
        return KmmPopTask(
            id = popType?.name,
            priority = this.priority,
            type = this.popType,
            dialog = this.dialog,
            dismissSelfByHigherPriority = this.dismissSelfByHigherPriority,
            viewLocation = this.viewLocation,
            isIgnoreViewLocation = this.isIgnoreViewLocation ?: getDefaultIgnoreViewLocation(),
            compare = this.compare,
            triggerType = this.triggerType,
            canSameIdDialogShow = canSameIdDialogShow,
            disableReCreate = this.disableReCreate,
            lifecycleObserver = this.lifecycleObserver
        )
    }

    @OptIn(KmmInternalApi::class, RestrictedApi::class)
    private fun tryCreateDialogFromType(): IPopUpView? {
        val context = context ?: appPageStack()?.getTopValidPage() ?: return null
        val popType = popType ?: return null
        val vm = vm ?: return null
        return appPopBridge()?.createDialog(
            DialogParam(
                context = context,
                type = popType,
                vm = vm
            )
        )
    }

    /**
     * [viewLocation]默认取值[PopUpViewLocation.FULL]，全屏弹窗应当可与任何弹窗比较优先级（无论处于任何位置）
     * 历史逻辑上[isIgnoreViewLocation]默认取值是false
     * 这里加一个开关，开关关闭时，仍然默认false；开关开启时，全屏弹窗默认忽略位置
     * 注意：此方法返回的是业务未通过[setIgnoreViewLocation]指定值时，在[build]时取的默认值，若业务有指定，仍然以业务指定值为准
     */
    private fun getDefaultIgnoreViewLocation(): Boolean {
        return if (getShiplySwitch("pop_compat_ignore_view_location")) {
            // 全屏弹窗默认忽略位置，即全屏弹窗可与任何弹窗比较
            viewLocation == PopUpViewLocation.FULL
        } else {
            // 开关关闭时，还保持旧逻辑，默认取值false
            false
        }
    }
}


data class PopTaskNecessaryParams(
    val context: IKmmContext,   // 弹窗上下文
    val popType: PopType,       // 弹窗类型
    val vm: IPopVM              // 弹窗VM
)
