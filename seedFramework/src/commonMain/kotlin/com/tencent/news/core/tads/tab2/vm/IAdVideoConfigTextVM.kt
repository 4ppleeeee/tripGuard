package com.tencent.news.core.tads.tab2.vm

interface IAdVideoConfigTextVM {

    // lottie动画文字
    val taskLottieText: String

    // 任务中气泡文字：观看 XX 秒，领取 XX 积分
    val taskBubbleText: String

    // 任务完成气泡文字：点击领取 XX 积分
    val rewardBubbleText: String

    // 成功提示文案：领取成功
    val toastSuccessText: String

    // 失败提示文案：抱歉，来晚啦，领积分任务已经结束了
    val toastFailText: String

    // 完成任务后提示的气泡文案：上滑浏览视频领更多奖励
    val swipeBubbleText: String

    // 达到领取积分的频控次数
    val toastAchieveTimesText: String

}