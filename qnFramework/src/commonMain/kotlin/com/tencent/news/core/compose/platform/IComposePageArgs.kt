package com.tencent.news.core.compose.platform

import com.tencent.news.core.extension.IKmmKeep
import com.tencent.news.core.list.model.IKmmFeedsItem
import com.tencent.news.core.platform.api.isDebug

/**
 * WARNING：内部要新增任何参数和接口。
 *
 * Compose页面（包含弹窗）参数的基类。
 * 因为Compose页面传参不支持复杂model类，所以该类的主要职责是：
 * 1. 将[IComposePageArgs]的子类加入到[NTComposePageArgsPool]，key是[IComposePageArgs.hashCode]；
 * 2. 将[IComposePageArgs.hashCode]加入到Compose页面的[PageData.params];
 * 取参数流程：
 * 1. 从[PageData.params]里取[IComposePageArgs.hashCode]；
 * 2. 从[NTComposePageArgsPool]里取[IComposePageArgs]；
 *
 * 因目前鸿蒙还是跨语言，所以传过来的是json，需要再转车成kotlin对象，所以所有子类尚需要添加[Serializable]
 */
interface IComposePageArgs : IKmmKeep {

    val pushPageArgsToMap: Map<String, Any>
        get() = NTComposePageArgsPool.pushPageArgsToMap(this)

    // 缓存pageArgs用的key（配合 NTComposePageArgsPool 使用）
    val identifier: Int get() = hashCode()

    // 判断数据是否合法
    val isValid: Boolean get() = true

    // fixme: jiaminzhang 这个不应该有pageItem
    val pageItem: IKmmFeedsItem? get() = null
}

object ComposeDemoRuntime {
    // 仅 standalone QnCore Demo 启动时置 true；主端打开 Demo 壳仍保持 false。
    var isStandaloneDemo: Boolean = false

    /**
     * Demo 调试能力的最终运行态。
     *
     * 调用方只消费该结果，避免各处重复组合 debug 包、页面 Demo 标记、独立 Demo 启动态。
     */
    fun isDebugInDemo(runInDemo: Boolean): Boolean =
        isDebug() && runInDemo && isStandaloneDemo
}

// 需要感知在demo里运行，做一些开发工具时，让 IComposePageArgs 实现这个
interface IComposeDemoPage {
    var runInDemo: Boolean
    val debugInDemo: Boolean get() = ComposeDemoRuntime.isDebugInDemo(runInDemo)
}
