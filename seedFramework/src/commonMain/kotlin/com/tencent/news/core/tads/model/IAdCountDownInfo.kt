package com.tencent.news.core.tads.model

// 广告倒计时信息接口，继承 IAdCountable 提供倒计时能力
interface IAdCountDownInfo : IAdCountable {
    val countDownTime: Long // 倒计时时间（毫秒）
    val countDownType: Int // 倒计时类型：1-开始倒计时，2-结束倒计时
    var orderFetchedTimestamp: Long // 订单获取时间戳（客户端本地赋值）

    override fun countdownTime(): Long = countDownTime
    override fun fetchedTimestamp(): Long = orderFetchedTimestamp

    fun isTimeUp(): Boolean { // 是否已到时
        return countDownTime > 0 && getCurrentRestTime() <= 0
    }

    companion object {
        const val COUNTDOWN_TYPE_START = 1
        const val COUNTDOWN_TYPE_FINISH = 2
    }
}
