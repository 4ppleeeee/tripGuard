package com.tencent.news.core.tads.model


interface IKmmAdAnyCounselDto {
    fun isDataValid(): Boolean                      // 数据合法性校验
    fun canQuitAdWebPage(): Boolean                 // 能够退出广告Web页面
    fun isWxNativeType(): Boolean                   // 是否是微信客服页
    fun isAnswerViewArea(clickArea: Int): Boolean   // 是否是问答区域

    fun getAnswerQuestionUrl(index: Int): String    // 获取问答的Url
    fun getQuestionIndex(question: String?): Int    // 根据问题文案，查找下标
    fun isAutoSendQuestion(): Boolean               // 是否要携带问题
    fun isEmptyQuestion(): Boolean                  // 问答问题为空

    /**
     * 判断半屏咨询卡点击后是否需要关闭半屏卡。
     */
    fun needCloseHalfCardAfterClick(): Boolean
}