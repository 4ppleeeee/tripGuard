package com.tencent.news.core.dt.constants

enum class DtBizEvent(override val eventId: String) : IDtBizEvent {
    // 关注按钮点击，仅未登录态上报，登录态报dt_clck
    FOLLOW("ev_focus"),
    // 收藏按钮点击，仅未登录态上报，登录态报dt_clck
    FAVORITE("ev_favor"),
    SPEAK("ev_speak"),
    SIGN_FEEDBACK("ev_sign_feedback_imp"),
    AGENT_CALL_HEARTBEAT("ev_agent_call_heartbeat"),
    // AI 播客交互按钮回复音频停止上报
    QA_AUDIO_END("ev_qa_audio_end"),
    // 电台喜欢
    RADIO_LIKE("ev_up"),
    // 发起搜索
    EV_START_SEARCH("ev_start_search"),
    // 划词评论
    EV_WORD_CMT("ev_word_cmt"),

    // 划词
    EV_WORD_UNDERLINE("ev_word_underline"),

    // 划词定位失败
    EM_UNDERLINE_FAIL_TOAST("em_underline_fail_toast"),
}
