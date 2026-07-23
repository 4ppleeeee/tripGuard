package com.tencent.kmm.demo.module

import com.tencent.kuikly.core.render.android.export.KuiklyRenderBaseModule
import com.tencent.kuikly.core.render.android.export.KuiklyRenderCallback

/**
 * WSKuiklyPlatformModule 在 Kuikly Render 模式下的 Native 端空壳。
 *
 * WSKuiklyPlatformModule 的所有逻辑已在 KMP 层（commonMain）实现，不依赖 Native 桥接。
 * 但 Kuikly Render 框架（KuiklyRenderViewDelegator）在查找 Module 时会走 Native 端的
 * moduleExport 注册表，若找不到会抛 KuiklyRenderModuleExportException。
 *
 * 本类仅作为占位注册，保证框架不报错；实际业务逻辑由 DSL 侧的 WSKuiklyPlatformModule 处理。
 */
class KRPlatformModule : KuiklyRenderBaseModule() {

    companion object {
        const val MODULE_NAME = "WSKuiklyPlatformModule"
    }

    override fun call(method: String, params: String?, callback: KuiklyRenderCallback?): Any? {
        // WSKuiklyPlatformModule 的逻辑在 KMP 层实现，Native 端不需要处理任何方法调用。
        // 如果框架意外路由到这里，返回 null 即可。
        return null
    }
}
