package com.tencent.news.core.tads.model

// 时间轴广告组件接口
interface IAdTimelineWidget {
    val componentList: List<IAdTimelineComponent>? // 组件列表
    val pushInfo: IAdTimelinePushInfo? // 推送信息
    var addToRecordTime: Long // 添加到记录的时间（客户端本地赋值）
}

// 时间轴组件接口，继承 IAdCountable 提供倒计时能力
interface IAdTimelineComponent : IAdCountable {
    val textContent: String // 文字内容
    val imageUrl: String // 中间挂件图片
    val brandImageUrl: String // 左上品牌图片
    val imageNightUrl: String // 左上挂件图片(夜间模式)
    val buttonTxt: String // 按钮文字
    val countdownTime: Long // 倒计时时间（毫秒）
    val componentType: Int // 组件类型：1-普通，2-预约
    val themeTemplate: Int // 主题模板
    var orderFetchedTimestamp: Long // 订单获取时间戳（客户端本地赋值）

    override fun countdownTime(): Long = countdownTime
    override fun fetchedTimestamp(): Long = orderFetchedTimestamp

    fun isTimeUp(): Boolean // 是否已到时

    companion object {
        const val COMPONENT_TYPE_NORMAL = 1
        const val COMPONENT_TYPE_APPOINTMENT = 2

        const val THEME_BLUE = 1
        const val THEME_GOLDEN = 2
        const val THEME_GREEN = 3
        const val THEME_ORANGE = 4
        const val THEME_GREY = 5
    }
}

// 时间轴推送信息接口
interface IAdTimelinePushInfo {
    val imageUrl: String
    val buttonTxt: String
    val titleContent: String
    val descContent: String
    val clickUrl: String
    val durationInfo: IAdTimelinePushDuration?
}

// 时间轴推送时间区间接口
interface IAdTimelinePushDuration {
    val begin: Long
    val end: Long
}
