package com.tencent.news.core.dt.constants

object DtParamKey {

    // ======================== 文章基础信息 ========================
    const val ARTICLE_ID = "article_id"
    const val ARTICLE_TYPE = "article_type"
    const val ENTITY_TYPE = "entity_type"
    const val IS_AUDIO = "is_audio"
    const val ARTICLE_SUB_ID = "article_sub_id"
    const val ARTICLE_SUB_TYPE_RADIO = "article_sub_type"
    const val ARTICLE_PIC_SHOW_TYPE = "article_ptype"
    const val ARTICLE_UUID = "article_uuid"
    const val ARTICLE_TITLE = "article_title"
    const val ARTICLE_BOOL_PARAMS = "article_bool_params"
    const val ALG_INFO = "alg_info"
    const val SEQ_NO = "seq_no"
    const val REASON_INFO = "reasonInfo"
    const val ARTICLE_MOUNT_TYPE = "article_mount_type"
    const val ARTICLE_REVIEW_STATUS = "article_review_status"
    const val ARTICLE_SUB_TYPE = "sub_atype"
    const val FOCUS_NUM = "focus_num"
    const val P1_ARTICLE_ID = "p1_article_id"
    const val P1_ARTICLE_TYPE = "p1_article_type"

    // ======================== 频道 ========================
    const val CHANNEL_ID = "chl_id" // 二级频道ID
    const val CHL_ID = "chl_id"

    // ======================== 页面启动来源 ========================
    const val PAGE_START_FROM = "page_start_from"
    const val PG_PAGE_START_FROM = "pg_page_start_from"

    // ======================== 设备 ========================
    const val SCREEN_TYPE = "screen_type"

    // ======================== 文章位置 ========================
    const val ARTICLE_POS = "article_pos"
    const val ARTICLE_LIST_POS = "article_list_pos"
    const val ARTICLE_REAL_POS = "article_real_pos"
    const val ARTICLE_MODULE_POS = "article_module_pos"
    const val ARTICLE_PAGE = "article_page"

    // ======================== 模块 ========================
    const val MOD_ARTICLE_TITLE = "mod_article_title"
    const val MOD_ARTICLE_ID = "mod_article_id"
    const val MOD_ARTICLE_PAGE = "mod_article_page"
    const val MOD_ARTICLE_TYPE = "mod_article_type"
    const val MOD_ARTICLE_PIC_SHOW_TYPE = "mod_article_ptype"

    // ======================== 付费 ========================
    const val ARTICLE_PAY_STATUS = "article_pay_status"
    const val IS_CP_MEMBER = "is_cp_member"
    const val IS_COLUMN_PURCHASED = "is_column_purchased"
    const val PRODUCT_ID = "product_id"

    // ======================== 用户/发布者 ========================
    const val USER_ID = "user_id"
    const val USR_UID = "user_id"
    const val USR_VIP_TYPE = "user_viptype"
    const val USR_TYPE = "user_type"
    const val USR_SUID = "user_suid"

    // ======================== 评论 ========================
    const val ARTICLE_SHOWCMT = "article_show_cmt"
    const val ARTICLE_CMT_ID = "article_cmt_id"
    const val ARTICLE_CMT_FROM = "article_cmt_from"

    // ======================== 视频 ========================
    const val VIDEO_VID = "video_vid"
    const val VIDEO_PID = "video_pid"
    const val VID_PRICE_TYPE = "vid_price_type"
    const val VIDEO_CID = "video_cid"
    const val VIDEO_LID = "video_lid"
    const val AUDIO_CATE = "audio_cate"

    // ======================== 标签 ========================
    const val LABELLIST_TYPENAME = "labellist_typename"

    // ======================== 问答 ========================
    const val QUESTION_ID = "question_id"

    // ======================== 热点模板 ========================
    const val ARTICLE_TEMPLATE_ID = "article_template_id"

    // ======================== 焦点文章 ========================
    const val ARTICLE_FOCUS_ID = "focus_article_id"
    const val ARTICLE_MARK_FOCUS_ID = "markfocus_article_id"

    // ======================== Tag ========================
    const val TAG_ID = "tag_id"             // tag 唯一标识
    const val TAG_SCENE = "tag_scene"       // tag 类型（对应接入层下发的tag_scene字段）
    const val IS_COLUMN_ARTICLE = "is_column_article"   // 是否是专栏类型（包括图文、视频）
    const val COLUMN_TYPE = "column_type"   // 专栏类型。图文还是视频

    // ======================== Web ========================
    const val WEB_URL = "web_url"

    // ======================== 事件 ========================
    const val EVENT_ID = "event_id"
    const val EVENT_ENTRANCE_TYPE = "article_relate_event_type"

    // ======================== 直播 ========================
    const val ARTICLE_LIVE_STATUS = "article_live_status"
    const val LIVE_TYPE = "live_type"
    const val SOURCE2 = "source2"
    const val VUID = "vuid"

    // ======================== 页面/导航 ========================
    @Deprecated("与刘悦确认这个基本不用了，：pg_page_start_from")
    const val IS_LANDING_PAGE = "is_landing_page"   // 落地页标识，1：落地页，0:普通底层页

    const val AIGC_MSG_ID = "msg_id"

    const val PG_TYPE = "type"
    const val PG_FROM = "from"
    const val PG_CHL_ID = "pg_chl_id"
    const val PG_TAB_ID = "pg_tab_id"
    const val PG_TAB_SETID = "pg_tab_setid"
    const val DT_PGID = "dt_pgid"
    const val DT_PG_PATH = "dt_pg_path"

    // ======================== 元素 ========================
    const val E_POS = "e_pos"       // 元素位置：公益条、图文中插模块、文末模块 等
    const val E_TYPE = "e_type"
    const val E_STATE = "e_state"    // 元素状态
    const val E_STATUS = "e_status"
    const val E_TITLE = "e_title"    // 元素标题
    const val FOCUS_TYPE = "focus_type"
    const val IS_FOCUS = "is_focus"
    const val AI_STATION_CHL = "ai_station_chl"
    const val BANNER_MODULE_ID = "banner_module_id"
    const val BANNER_SCHEME_URL = "banner_scheme_url"
    const val SECTION_ID = "section_id"
    const val TASK_ID = "task_id"
    const val SUB_TASK_ID = "sub_task_id"
    const val GAME_ID = "game_id"
    const val QUERY_ID = "query_id"
    const val EXTRA_PARAMS = "extra_params"
    const val TASK_TYPE = "task_type"
    const val WINDOW_BTN_TYPE = "window_btn_type"
    const val IS_OPTION_CHECK = "is_option_check"

    // ======================== 海报 ========================
    const val CARDPANEL_PTYPE = "cardpanel_ptype"   // 海报样式：immersion、vague、normal_blue 等
    const val CARDPANEL_TYPE = "cardpanel_type"     // 海报类型：poster、miaodong 等

    // ======================== Scheme ========================
    const val SCHEME_URL = "scheme_url"
    const val SCHEME_TYPE = "scheme_type"

    // ======================== 广告 ========================
    const val ARTICLE_AD_TYPE = "article_ad_type"

    // ======================== 文章布尔信息 ========================
    const val ARTICLE_BOOL_INFO = "article_bool_info"

    // ======================== 导航 ========================
    const val NAV_ITEM_ID = "nav_item_id"   // 一级频道导航条对应频道id
    const val NAV_ID = "nav_id"             // 页面导航条对应tabId

    // ======================== 表单 ========================
    const val FORM_ID = "form_id"         // 表单 id

    @Deprecated("老协议，其实取值是tag_scene，但是起的名字不好；废弃掉，后续用正规的tag_scene")
    const val TAG_TYPE = "tag_type"
}
