package com.tencent.news.core.tads.model

import com.tencent.news.core.extension.IAdOrderDtoDoc


interface IAdIndexDto : IAdOrderDtoDoc {

    // 【广告三元组之一】广告位类型【与客户端请求的广告位匹配，客户端绑定到order里】
    var loid: Int

    // 【广告三元组之一】广告频道（和新闻的二级频道id一致）【与adReqData里的频道匹配，客户端绑定到order里】
    var adChannel: String

    // 广告频道id，数字版本的【与adReqData里的频道匹配，客户端绑定到order里】
    var adChannelId: Int

    // 订单绝对位置【由index下发，客户端绑定到order里】
    var seq: Int

    // 广告位名称（字符串版本的，与loid对应，主要用于上报）【order和index都有下发，谁不空先用谁】
    var loc: String

    // 订单来源【由index下发，客户端绑定到order里】
    // @AdOrderSource
    var orderSource: Int

    // 订单的后台透传数据，加密格式【由index下发，客户端绑定到order里】
    var serverData: String

    // 空单时前端替换方式（可能已废弃）【由index下发，客户端绑定到order里】
    var replaceType: Int

    // oneshot广告可能存在规避策略，记录原始seq
    var originalSeq: Int?

}