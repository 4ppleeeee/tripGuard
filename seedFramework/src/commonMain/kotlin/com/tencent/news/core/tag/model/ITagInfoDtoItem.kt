package com.tencent.news.core.tag.model

import com.tencent.news.core.extension.ICmsModelDtoItemDoc


interface ITagInfoDtoItem : ICmsModelDtoItemDoc {
    val baseDto: ITagBaseDto            // tag基础信息
    val resDto: ITagResDto              // 素材相关（图标、背景 等）
    val intensifyDto: ITagIntensifyDto  // tag样式增强展示相关配置
    val columnDto: ITagInfoColumnDto    // 专栏付费信息
    var homePageInfo: ITagHomePageInfo? // tag页面额外配置信息，比如分享信息
    val extraDto: ITagExtraInfoDto      // tag透传信息：后台透传 extra_property_json，从里面解析出来的
    val tagCtxDto: ITagContextDto       // 客户端本地绑定字段
}