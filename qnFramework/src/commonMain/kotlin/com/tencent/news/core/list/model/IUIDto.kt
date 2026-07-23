package com.tencent.news.core.list.model

import com.tencent.news.core.extension.IItemDtoDoc
import com.tencent.news.core.extension.IKmmKeep
import com.tencent.news.core.parcel.IKmmParcelable


interface IUIDto : IItemDtoDoc, IKmmKeep, IKmmParcelable {

    // 打开大图
    var openBigImage: String
    // 显示类型
    var showType: String
    // 大图模式下gif播放封面
    var newsPicGifBig: String
    // 小图模式下gif播放封面
    var newsPicGifSmall: String
    // 控制在TL上是否显示时间戳，1不显示，else显示
    var forbidTimestamp: String
    // 是否显示发布者信息栏
    var enablePublisherBar: Int
    // 列表是否应该显示热榜
    var enableRankingInfo: String
    // 小视频图片
    var miniVideoPic: String
    // 视频直播cell下发1：1图片，字段不为空时认为需要展示1:1样式
    var fimgurl1: String
    // 在列表上外显的图片，只在列表cell上用
    var tlImage: String
    // H5页面URL
    var htmlUrl: String
    // 高度
    var height: String
    // 强制不缓存
    var forceNotCached: String
    // webcell展示形式
    var h5CellShowType: Int
    // H5 cell宽高比
    var h5CellAspectRatio: Double
    // 1 隐藏底部分割线
    var hideBottomDivider: Int
    // 等于1，则列表cell隐藏标题
    var tlForbidTitle: String
    // 用于特殊情况下的外显标题
    var customTitle: String
    // 是否可滚动
    var scrollable: Boolean
    // 热点精选模块标题
    var hotSpotModuleTitle: String
    // 下发的上分割线样式
    var topSepLineType: Int
    // 下发的下分割线样式
    var bottomSepLineType: Int
    // 渲染类型
    var renderType: Int
    // cell类型，native 或 hippy
    var cellRenderType: Int
    // 完整版：playback 预告片：trailer 碎剪：chopper
    var sceneType: String
}