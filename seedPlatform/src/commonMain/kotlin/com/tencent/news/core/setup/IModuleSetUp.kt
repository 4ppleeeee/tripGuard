package com.tencent.news.core.setup

import com.tencent.news.core.extension.ISetUpDoc


interface IModuleSetUp : ISetUpDoc {

    fun buildModuleDependency()     // 模块依赖注入

    fun initAfterStartUp() {} // 通知kmm app冷启动完毕，一些子业务初始化可以在这做（这个时机不会阻塞app启动速度）

}