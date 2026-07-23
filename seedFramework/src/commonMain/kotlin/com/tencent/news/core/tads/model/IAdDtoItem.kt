package com.tencent.news.core.tads.model

import com.tencent.news.core.list.model.ITestDto
import com.tencent.news.core.tads.model.interact.IAdInteractDto


interface IAdDtoItem {

    val adIndex: IAdIndexDto                // 广告位信息（index 字段下发的，客户端绑定到 order 上）

    val info: IAdOrderInfo                  // 订单基础信息
    val action: IAdOrderAction              // 功能行为相关（含跳转）
    val report: IAdOrderReport              // 上报相关

    val res: IAdOrderRes                    // 素材资源
    val mdpaDto: IAdMdpaDto                 // 商品相关
    val oneShotDto: IAdOrderOneShotDto      // 闪屏联动OneShot广告
    val adInteractDto: IAdInteractDto       // interact dto (游戏礼包、互动蒙层)

    val downloadDto: IAdOrderDownloadDto    // 下载相关

    val state: AdOrderPermanentState        // 客户端本地绑定的状态，特殊之处在于clone后，这个state也不会变化

}

interface IAdSerialDtoItem {
    val env: KmmAdOrderEnv          // 客户端绑定的一些环境参数，方便逻辑判断；例如广告所在页面的 idStr 等等
    val testDto: ITestDto           // 集成测试配置
}
