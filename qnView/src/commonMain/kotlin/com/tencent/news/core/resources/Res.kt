package com.tencent.news.core.resources

import androidx.compose.runtime.Composable
import com.tencent.kuikly.compose.coil3.rememberAsyncImagePainter
import com.tencent.kuikly.compose.ui.graphics.painter.Painter
import com.tencent.news.core.compose.scaffold.theme.drawable
import com.tencent.news.core.compose.scaffold.theme.isAppInDarkTheme
import com.tencent.news.core.compose.scaffold.theme.lightDrawable

private const val POINTS_HEADER_BG_DARK_URL = "https://inews.gtimg.com/newsapp_bt/0/070816030874_9946/0"
private const val POINTS_HEADER_BG_URL = "https://inews.gtimg.com/newsapp_bt/0/0708160352932_4374/0"
private const val POINTS_VIDEO_REDPACKET_BG_CLAIMED_URL = "https://inews.gtimg.com/newsapp_bt/0/0708161305859_2571/0"
private const val POINTS_VIDEO_REDPACKET_BG_READY_URL = "https://inews.gtimg.com/newsapp_bt/0/0708161331520_8060/0"


object Res {

    object drawable {
        // todo 日夜间切换时触发重组
        val no_network_placeholder: Painter @Composable get() = drawable("no_network_placeholder.webp").value
        val ic_audio_play: Painter @Composable get() = drawable("ic_audio_play.webp").value
        val up_arrow: Painter @Composable get() = drawable("up_arrow.webp").value
        val down_arrow_tlink: Painter @Composable get() = drawable("down_arrow_tlink.png").value
        val up_arrow_tlink: Painter @Composable get() = drawable("up_arrow_tlink.webp").value
        val timeLine_round: Painter @Composable get() = drawable("timeLine_round.png").value
        val timeLine_ai_round: Painter @Composable get() = drawable("timeLine_ai_round.webp").value
        val ai_timeline_biground: Painter @Composable get() = drawable("ai_timeline_biground.webp").value
        val event_timeline_title_logo: Painter @Composable get() = drawable("event_timeline_title_logo.png").value
        val news_logo: Painter @Composable get() = drawable("news_logo.png").value
        val transparent_news_logo: Painter @Composable get() = drawable("transparent_news_logo.png").value

        // 分享图标
        val share_qq_icon: Painter @Composable get() = drawable("share_qq_icon.png").value
        val share_qzone_icon: Painter @Composable get() = drawable("share_qzone_icon.png").value
        val share_sina_icon: Painter @Composable get() = drawable("share_sina_icon.png").value
        val share_weixin_icon: Painter @Composable get() = drawable("share_weixin_icon.png").value
        val share_wx_moment_icon: Painter @Composable get() = drawable("share_wx_moment_icon.png").value
        val share_work_weixin_icon: Painter @Composable get() = drawable("share_work_weixin_icon.png").value
        val share_poster_icon: Painter @Composable get() = drawable("share_poster_icon.png").value
        val share_pdf_icon: Painter @Composable get() = drawable("share_pdf_icon.png").value

        val channel_share_logo: Painter @Composable get() = drawable("channel_share_logo.webp").value
        val channel_share_ewm_text: Painter @Composable get() = drawable("channel_share_ewm_text.webp").value

        val card_share_logo: Painter @Composable get() = drawable("card_share_logo.webp").value
        val morning_post_logo_blue: Painter @Composable get() = drawable("morning_post_logo_blue.png").value
        val evening_post_logo_blue: Painter @Composable get() = drawable("evening_post_logo_blue.png").value
        val image_qr_desc_default_black: Painter @Composable get() = drawable("image_qr_desc_default_black.png").value
        val qr_code_triangle: Painter @Composable get() = drawable("qr_code_triangle.png").value
        val morning_poster_default: Painter @Composable get() = drawable("morning_poster_default.png").value
        val evening_poster_default: Painter @Composable get() = drawable("evening_poster_default.png").value

        val switch: Painter @Composable get() = drawable("switch.webp").value

        val content_preference_bg: Painter @Composable get() = drawable("content_preference_bg.png").value

        // 视频合集
        val icon_tag: Painter @Composable get() = drawable("icon_tag.webp").value
        val default_avatar_round: Painter @Composable get() = drawable("default_avatar_round.webp").value

        val bgBlock: Painter @Composable get() = drawable("bg_block.png").value
        val diamond_small_icon: Painter @Composable get() = drawable("diamond_small_icon.webp").value

        val sponsor_square_pick: Painter @Composable get() = drawable("sponsor_square_pick.webp").value
        val sponsor_square_unpick: Painter @Composable get() = drawable("sponsor_square_unpick.webp").value

        val checked: Painter @Composable get() = drawable("checked.webp").value
        val sponsor_round_pick: Painter @Composable get() = drawable("sponsor_round_pick.webp").value
        val sponsor_round_unpick: Painter @Composable get() = drawable("sponsor_round_unpick.webp").value
        val tad_gameC: Painter @Composable get() = drawable("tad_gameC.webp").value
        val adVideoClose: Painter @Composable get() = drawable("ad_video_close.webp").value
        val adVideoWhiteClose: Painter @Composable get() = drawable("ad_video_white_close.webp").value
        val ad_endCard_next_video: Painter @Composable get() = drawable("ad_endCard_next_video.png").value
        val adCheckInBgStar: Painter @Composable get() = drawable("ad_checkin_bg_star.webp").value
        val adCheckInCoin: Painter @Composable get() = drawable("ad_game_checkin_coin.webp").value


        val xinwenmei: Painter @Composable get() = drawable("xinwenmei.webp").value

        val xwm_yuanbao: Painter @Composable get() = drawable("xwm_yuanbao.webp").value
        val yuanbaoLogo: Painter @Composable get() = drawable("yuanbaoLogo.png").value

        val aiAskEventIc: Painter @Composable get() = drawable("ai_ask_event_ic.webp").value
        val aigcTopBg: Painter @Composable get() = drawable("aigc_top_bg.png").value
        val aigcToBottomIcon: Painter @Composable get() = drawable("dingwei.png").value
        val aigc_share_pick: Painter @Composable get() = drawable("checkbox_default_selected.webp").value
        val aigc_share_unpick: Painter @Composable get() = drawable("checkbox_default.webp").value
        val aigc_ref_arrow: Painter @Composable get() = drawable("aigc_ref_arrow.png").value
        val aigc_discovery_item_icon: Painter @Composable get() = drawable("aigc_discovery_item_icon.webp").value
        val aigc_empty_icon: Painter @Composable get() = drawable("aigc_empty_icon.webp").value
        val aigc_icon: Painter @Composable get() = drawable("aigc_icon.webp").value
        val aigc_timeline_node: Painter @Composable get() = drawable("aigc_timeline_node.png").value
        val ai_stream_lottie_label: Painter @Composable get() = drawable("stream_lottie_label.webp").value
        val ai_stream_lottie_border: Painter @Composable get() = drawable("stream_lottie_border.webp").value
        val aigc_poster_logo: Painter @Composable get() = drawable("aigc_poster_logo.webp").value
        val aigc_poster_logo_white: Painter @Composable get() = drawable("aigc_poster_logo_white.png").value
        val aigc_invalid_icon: Painter @Composable get() = drawable("aigc_invalid_icon.png").value

        // AI 头像
        val rare: Painter @Composable get() = drawable("rare.webp").value
        val limited: Painter @Composable get() = drawable("limited.webp").value
        val aiPlaceholderAvatar: Painter @Composable get() = drawable("ai_placeholder_avatar.png").value
        val lightSweep: Painter @Composable get() = drawable("light_sweep.png").value
        val avatarLightSweep: Painter @Composable get() = drawable("avatar_light_sweep.png").value
        val avatarTitleMark: Painter @Composable get() = drawable(name = "aigc_avatar_title_mark.png").value
        val avatarButtonBlur: Painter @Composable get() = drawable(name = "ai_avatar_button_blur.png").value

        val slotMachineOptionBg: Painter @Composable get() = drawable("slot_machine_option_bg.webp").value

        // 稀有头像占位图
        val emoji1: Painter @Composable get() = drawable("emoji_1.webp").value
        val emoji2: Painter @Composable get() = drawable("emoji_2.webp").value
        val emoji3: Painter @Composable get() = drawable("emoji_3.webp").value

        // 兜底占位图
        val emoji4: Painter @Composable get() = drawable("emoji_4.webp").value
        val emoji5: Painter @Composable get() = drawable("emoji_5.webp").value
        val emoji6: Painter @Composable get() = drawable("emoji_6.webp").value

        // 商业化
        val adMarketingIndicatorNormal: Painter
            @Composable get() = drawable("ad_marketing_indicator_normal.webp").value
        val adMarketingIndicatorSelected: Painter
            @Composable get() = drawable("ad_marketing_indicator_selected.webp").value

        // 空占位
        val empty_common: Painter @Composable get() = drawable("empty_no_data_normal.webp").value
        val ic_history_empty: Painter @Composable get() = drawable("ic_history_empty.webp").value
        val ic_like_empty: Painter @Composable get() = drawable("ic_like_empty.webp").value
        val ic_collection_empty: Painter @Composable get() = drawable("ic_collection_empty.webp").value
        val ic_audio_empty: Painter @Composable get() = drawable("empty_no_data_normal.webp").value

        // 空占位（固定日间版本，不跟随日夜间变化）
        val default_big_logo_icon: Painter @Composable get() = drawable("default_big_logo_icon.png").value
        val tabRadioEmptyData: Painter @Composable get() = lightDrawable("tab3_radio_Empty.webp").value
        val record_img: Painter @Composable get() = drawable("record_img.webp").value

        val ai_qa_header_bg: Painter @Composable get() = drawable("ai_qa_header_bg.webp").value
        val ai_qa_title_logo: Painter @Composable get() = drawable("ai_qa_title_logo.webp").value
        val qa_article_label: Painter @Composable get() = drawable("qa_article_label.webp").value
        val video_article_label: Painter @Composable get() = drawable("video_article_label.webp").value
        val ai_video_article_label: Painter @Composable get() = drawable("ai_video_article_label.png").value
        val top_timeline_label: Painter @Composable get() = drawable("top_timeline_label.png").value
        val ai_top_timeline_label: Painter @Composable get() = drawable("ai_top_timeline_label.png").value
        val ai_gc_icon: Painter @Composable get() = drawable("ai_gc_icon.webp").value
        val ai_qa_post_share_header_logo: Painter @Composable get() = drawable("ai_qa_post_share_header_logo.webp").value
        val ad_nft_close: Painter @Composable get() = drawable("ad_nft_close.webp").value
        val default_list_logo: Painter @Composable get() = drawable("default_list_logo.png").value
        val ad_game_star: Painter @Composable get() = drawable("ad_game_star.png").value
        val ad_mid_article_right_btn: Painter @Composable get() = drawable("ad_mid_article_right_btn.png").value
        val ad_mid_article_game_star: Painter @Composable get() = drawable("ad_mid_article_game_star.png").value

        // 热点聚合
        val more_round_avatar: Painter @Composable get() = drawable("more_avatar_icon.webp").value
        val red_make_top: Painter @Composable get() = drawable("red_make_top.webp").value

        // 事件
        val eventHotQuestion: Painter @Composable get() = drawable("event_hot_question.png").value
        val sponsorFire: Painter @Composable get() = drawable("sponsor_fire.webp").value
        val sponsorFlag: Painter @Composable get() = drawable("sponsor_flag.webp").value
        val sponsorTarget: Painter @Composable get() = drawable("sponsor_target.webp").value

        // 问答专题（@cell618）：互动用户区右侧气泡 icon
        val qa_event_header_core_qipao: Painter
            @Composable get() = drawable("qa_event_header_core_qipao.webp").value

        val presentCardBg: Painter @Composable get() = drawable("present_card_bg.webp").value
        val vipCardBg: Painter @Composable get() = drawable("vip_card_bg.webp").value
        val columnCardBg: Painter @Composable get() = drawable("column_card_bg.webp").value
        val presentMsg1: Painter @Composable get() = drawable("present_msg_1.png").value
        val presentMsg2: Painter @Composable get() = drawable("present_msg_2.png").value
        val presentMsg3: Painter @Composable get() = drawable("present_msg_3.png").value
        val presentMsg4: Painter @Composable get() = drawable("present_msg_4.png").value
        val presentMsg5: Painter @Composable get() = drawable("present_msg_5.png").value

        val payLetterAngleTop: Painter @Composable get() = lightDrawable("pay_letter_angle_top.webp").value
        val radioImageCoverHolder: Painter @Composable get() = lightDrawable("radio_image_place_holder.webp").value
        val radioImageNoNetwork: Painter @Composable get() = lightDrawable("audio_radio_no_net_work.png").value
        val audioSquareBannerHolder: Painter @Composable get() = lightDrawable("audio_square_banner_holder.png").value
        val audioTab3NavBtnHolder: Painter @Composable get() = lightDrawable("audio_tab3_nav_btn_holder.png").value
        val audio_radio_hanyi_font_title: Painter
            @Composable get() = lightDrawable("audio_radio_hanyi_font_title.png").value

        // Member Zone 装饰
        val member_zone_left: Painter @Composable get() = lightDrawable("member_zone_left.png").value
        val member_zone_right: Painter @Composable get() = lightDrawable("member_zone_right.png").value

        // Member Zone 底部栏图标
        val member_zone_fire: Painter @Composable get() = lightDrawable("member_zone_fire.png").value
        val member_zone_heartbroken: Painter @Composable get() = lightDrawable("member_zone_heartbroken.png").value
        val payLetterAngleBottom: Painter @Composable get() = lightDrawable("pay_letter_angle_bottom.webp").value
        val payLetterLight: Painter @Composable get() = lightDrawable("pay_letter_light.webp").value
        val payLetterWordsMember: Painter @Composable get() = lightDrawable("pay_letter_words_member.png").value
        val payLetterWordsColumn: Painter @Composable get() = lightDrawable("pay_letter_words_column.png").value
        val payLetterWordsGiftPack: Painter @Composable get() = lightDrawable("pay_letter_words_gift_pack.png").value
        val payDiamond: Painter @Composable get() = lightDrawable("pay_diamond_big.png").value
        val payGift: Painter @Composable get() = lightDrawable("pay_gift_box.png").value
        val hotEventShadow: Painter @Composable get() = lightDrawable("hot_event_shadow.png").value
        val leftWing: Painter @Composable get() = drawable("left_wing.png").value
        val rightWing: Painter @Composable get() = drawable("right_wing.png").value
        val payCrownSmall: Painter @Composable get() = lightDrawable("pay_crown_small.png").value
        val paySuccessAddChannel: Painter @Composable get() = drawable("pay_success_add_channel.png").value
        val presentListEmpty: Painter @Composable get() = drawable("present_list_empty.webp").value
        val crownGold: Painter @Composable get() = lightDrawable("crown_gold.png").value
        val crownSilver: Painter @Composable get() = lightDrawable("crown_silver.png").value
        val crownBronze: Painter @Composable get() = lightDrawable("crown_bronze.png").value
        val pointsIcon: Painter @Composable get() = lightDrawable("points_icon.png").value

        val audioLoading: Painter @Composable get() = drawable("audio_loading.png").value
        val aiAudioTop: Painter @Composable get() = lightDrawable("ai_audio_top.webp").value
        val miaoDongLogo: Painter @Composable get() = lightDrawable("event_post_miaodong_logo.webp").value
        val miaoDongTitleLogo: Painter @Composable get() = drawable("miaodong_logo.webp").value
        val eventPostShareLogo: Painter @Composable get() = lightDrawable("event_post_share_logo.webp").value

        val wxConsultSmallIcon: Painter @Composable get() = drawable("wx_consult_small_icon.png").value
        val wxConsultGoIcon: Painter @Composable get() = drawable("wx_consult_go_icon.png").value
        val wxConsultLeftDown: Painter @Composable get() = drawable("wx_consult_left_down.png").value
        val adLocationBlue: Painter @Composable get() = drawable("ad_location_blue.png").value

        val wxStoreShopIcon: Painter @Composable get() = lightDrawable("wei_xin_store_default_icon.webp").value
        val adWeixinStore: Painter @Composable get() = lightDrawable("ad_weixin_store.webp").value
        val wxLiveShopIcon: Painter @Composable get() = lightDrawable("live_shop_default_icon.webp").value
        val adStoreCuponIcon: Painter @Composable get() = drawable("ad_store_cupon.png").value
        val adShop618ActivityIcon: Painter @Composable get() = drawable("ad_shop_618_activity_icon.png").value
        val adLiveStoreVoucher: Painter @Composable get() = drawable("ad_liveStore_voucher.png").value
        val we_chat_pay_score_icon: Painter @Composable get() = drawable("we_chat_pay_score_icon.webp").value

        val ecommerceVoucher: Painter @Composable get() = drawable("ecommerce_voucher.png").value
        val couponIcon: Painter @Composable get() = drawable("coupon_icon.png").value

        val liveCardShareEwmText: Painter @Composable get() = drawable("live_card_share_ewm_text.png").value
        val liveCardShareSlice: Painter @Composable get() = drawable("live_card_share_slice.png").value

        // 签到相关
        val check_in_coin_no: Painter @Composable get() = drawable("check_in_coin_no.webp").value
        val check_in_coin_yes: Painter @Composable get() = drawable("check_in_coin_yes.webp").value
        val check_in_coin_today: Painter @Composable get() = drawable("check_in_coin_today.webp").value
        val points_check_in_coin_default: Painter
            @Composable get() = lightDrawable("points_check_in_coin_default.webp").value
        val points_check_in_coin_current: Painter
            @Composable get() = lightDrawable("points_check_in_coin_current.webp").value
        val points_check_in_coin_signed: Painter
            @Composable get() = lightDrawable("points_check_in_coin_signed.webp").value
        val check_in_title_bottom: Painter @Composable get() = drawable("check_in_title_bottom.png").value
        val check_in_coin_icon: Painter @Composable get() = drawable("check_in_coin_icon.webp").value
        val coin_center_checkin_header: Painter @Composable get() = drawable("coin_center_checkin_header.png").value
        val coin_center_checkin_title: Painter @Composable get() = drawable("coin_center_checkin_title.png").value
        val coin_center_video_task_title: Painter
            @Composable get() = drawable("coin_center_video_task_title.png").value
        val coin_center_checkin_chest_closed: Painter
            @Composable get() = drawable("coin_center_checkin_chest_closed.png").value
        val coin_center_checkin_chest_fade: Painter
            @Composable get() = drawable("coin_center_checkin_chest_fade.png").value
        val coin_center_checkin_chest_open: Painter
            @Composable get() = drawable("coin_center_checkin_chest_open.png").value
        val coin_center_checkin_coin: Painter @Composable get() = drawable("coin_center_checkin_coin.png").value
        val coin_center_checkin_coin_closed: Painter
            @Composable get() = drawable("coin_center_checkin_coin_closed.png").value
        val coin_center_checkin_coin_stack: Painter
            @Composable get() = drawable("coin_center_checkin_coin_stack.png").value
        val coin_center_checkin_coin_stack_closed: Painter
            @Composable get() = drawable("coin_center_checkin_coin_stack_closed.png").value
        val coin_center_checkin_success_coins: Painter
            @Composable get() = drawable("coin_center_checkin_success_coins.png").value
        // 兼容旧红包背景命名，实际复用 points_video_redpacket_bg_* 正式资源，避免保留重复图片。
        val points_redpacket_bg_claimed: Painter
            @Composable get() = rememberAsyncImagePainter(POINTS_VIDEO_REDPACKET_BG_CLAIMED_URL)
        val points_redpacket_bg_ready: Painter
            @Composable get() = rememberAsyncImagePainter(POINTS_VIDEO_REDPACKET_BG_READY_URL)
        val points_redpacket_bg_progress: Painter
            @Composable get() = lightDrawable("points_video_redpacket_bg_progress.png").value
        val points_redpacket_bg_normal: Painter
            @Composable get() = lightDrawable("points_video_redpacket_bg_normal.png").value
        val points_header_bg: Painter
            @Composable get() = rememberAsyncImagePainter(
                if (isAppInDarkTheme()) POINTS_HEADER_BG_DARK_URL else POINTS_HEADER_BG_URL
            )
        val points_mini_coin: Painter @Composable get() = lightDrawable("points_mini_coin.png").value
        val points_video_redpacket_bg_claimed: Painter
            @Composable get() = rememberAsyncImagePainter(POINTS_VIDEO_REDPACKET_BG_CLAIMED_URL)
        val points_video_redpacket_bg_ready: Painter
            @Composable get() = rememberAsyncImagePainter(POINTS_VIDEO_REDPACKET_BG_READY_URL)
        val points_video_redpacket_bg_progress: Painter
            @Composable get() = lightDrawable("points_video_redpacket_bg_progress.png").value
        val points_video_redpacket_bg_normal: Painter
            @Composable get() = lightDrawable("points_video_redpacket_bg_normal.png").value
        // 感谢信分享卡片相关
        val daily_sign_logo: Painter @Composable get() = drawable("daily_sign_logo.png").value
        val icon_payment: Painter @Composable get() = drawable("icon_payment.png").value

        // 勾选框图标
        val check_false_v2: Painter @Composable get() = drawable("check_false_v2.png").value
        val check_true_v2: Painter @Composable get() = drawable("check_true_v2.png").value
        val tl_ic_favor_cb: Painter @Composable get() = drawable("tl_ic_favor_cb.png").value
        val tl_ic_favor_cb_checked: Painter @Composable get() = drawable("tl_ic_favor_cb_checked.png").value
        val tl_ic_favor_cb_real_checked: Painter @Composable get() = drawable("tl_ic_favor_cb_real_checked.png").value

        // 登录相关图标
        val login_wechat: Painter @Composable get() = drawable("login_wechat.png").value
        val login_qq: Painter @Composable get() = drawable("login_qq.png").value
        val login_phone: Painter @Composable get() = drawable("login_phone.png").value
        val login_huawei: Painter @Composable get() = drawable("login_huawei.png").value
        val login_phone_alt: Painter @Composable get() = drawable("login_phone_alt.png").value

        // 快捷入口图标
        val quick_entry_dress: Painter @Composable get() = drawable("quick_entry_dress.png").value
        val quick_entry_points: Painter @Composable get() = drawable("quick_entry_points.png").value
        val quick_entry_purchased: Painter @Composable get() = drawable("quick_entry_purchased.png").value
        val quick_entry_daily_sign: Painter @Composable get() = drawable("quick_entry_daily_sign.png").value
        val quick_entry_coin: Painter @Composable get() = drawable("quick_entry_coin.png").value

        // 快速登录相关图标
        val ic_last_login_wechat: Painter @Composable get() = drawable("ic_last_login_wechat.png").value
        val ic_last_login_qq: Painter @Composable get() = drawable("ic_last_login_qq.png").value
        val ic_last_login_phone: Painter @Composable get() = drawable("ic_last_login_phone.png").value
        val ic_switch_account: Painter @Composable get() = drawable("ic_switch_account.png").value
        val ic_user_center_app_update: Painter @Composable get() = drawable("ic_user_center_app_update.png").value
        val ic_user_center_app_update_has_skin: Painter @Composable get() = drawable("ic_user_center_app_update_has_skin.png").value
        val common_fun_placeholder: Painter @Composable get() = drawable("common_fun_placeholder.png").value
        val more_fun_placeholder: Painter @Composable get() = drawable("more_fun_placeholder.png").value

        // 常用功能图标
        val common_func_focus: Painter @Composable get() = drawable("common_func_focus.png").value
        val common_func_collect: Painter @Composable get() = drawable("common_func_collect.png").value
        val common_func_history: Painter @Composable get() = drawable("common_func_history.png").value
        val common_func_thumb_up: Painter @Composable get() = drawable("common_func_thumb_up.png").value
        val common_func_listen_later: Painter @Composable get() = drawable("common_func_listen_later.png").value
        val common_func_yaowen: Painter @Composable get() = drawable("common_func_yaowen.png").value

        // 更多功能图标
        val more_func_setting: Painter @Composable get() = drawable("more_func_setting.png").value
        val more_func_feedback: Painter @Composable get() = drawable("more_func_feedback.png").value
        val more_func_download: Painter @Composable get() = drawable("more_func_download.png").value
        val more_func_clear_cache: Painter @Composable get() = drawable("more_func_clear_cache.png").value
        val more_func_text_size: Painter @Composable get() = drawable("more_func_text_size.png").value
        val more_func_security: Painter @Composable get() = drawable("more_func_security.png").value
        val more_func_night_mode: Painter @Composable get() = drawable("more_func_night_mode.png").value
        val more_func_reading_report: Painter @Composable get() = drawable("more_func_reading_report.png").value

        // 更多入口功能图标
        val more_entry_video: Painter @Composable get() = drawable("more_entry_video.png").value
        val more_entry_sports: Painter @Composable get() = drawable("more_entry_sports.png").value
        val more_entry_quwan: Painter @Composable get() = drawable("more_entry_quwan.png").value
        val more_entry_bonbon_game: Painter @Composable get() = drawable("more_entry_bonbon_game.png").value
        val more_entry_bonbon_read: Painter @Composable get() = drawable("more_entry_bonbon_read.png").value
        val more_entry_xiaoehuaqian: Painter @Composable get() = drawable("more_entry_xiaoehuaqian.png").value
        val more_entry_caifu: Painter @Composable get() = drawable("more_entry_caifu.png").value
        val more_entry_xinwenmei: Painter @Composable get() = drawable("more_entry_xinwenmei.png").value
        val more_entry_task_center: Painter @Composable get() = drawable("more_entry_task_center.png").value
        val more_entry_ximalaya: Painter @Composable get() = drawable("more_entry_ximalaya.png").value
        val more_entry_jiaozheng: Painter @Composable get() = drawable("more_entry_jiaozheng.png").value
        val more_entry_create_center: Painter @Composable get() = drawable("more_entry_create_center.png").value
        val more_entry_publish: Painter @Composable get() = drawable("more_entry_publish.png").value
        val more_entry_ai_avatar: Painter @Composable get() = drawable("more_entry_ai_avatar.png").value
        val more_entry_ima: Painter @Composable get() = drawable("more_entry_ima.png").value
        val more_entry_assets: Painter @Composable get() = drawable("more_entry_assets.png").value

        // 游戏日历卡片背景
        val calendar_item_bg: Painter @Composable get() = drawable("calendar_item_bg.png").value

        // 游戏日历标签背景
        val calendar_tag_bg: Painter @Composable get() = drawable("calendar_tag_bg.png").value

        // 原生卡片资源
        val native_rank_left: Painter @Composable get() = drawable("ad_native_rank_left.png").value
        val native_rank_right: Painter @Composable get() = drawable("ad_native_rank_right.png").value

        val ad_full_screen_up_icon: Painter @Composable get() = drawable("ad_full_screen_up_icon.png").value

        // 广告时间线行动按钮图标
        val ad_countdown_right_arrow: Painter @Composable get() = drawable("ad_countdown_right_arrow.webp").value
        val ad_countdown_check: Painter @Composable get() = drawable("ad_countdown_check.webp").value

        // 播客频道收听按钮
        val audio_channel_board_cast_play: Painter @Composable get() = lightDrawable("audio_channel_board_cast_play.png").value
        val audio_channel_board_cast_play_bg: Painter @Composable get() = lightDrawable("audio_channel_board_cast_play_bg.png").value

        // 放映厅/长视频列表 cell 播放按钮（对齐 Android @drawable/btn_video_play）
        val btn_video_play: Painter @Composable get() = lightDrawable("btn_video_play.png").value

        // 长视频拖动进度指示器箭头（对齐 Android @drawable/video_timer_toast_*）
        val video_timer_toast_left: Painter @Composable get() = drawable("video_timer_toast_left.webp").value
        val video_timer_toast_right: Painter @Composable get() = drawable("video_timer_toast_right.webp").value

        // AI 专属形象气泡相关图标
        val ai_avatar_toast_icon: Painter @Composable get() = drawable("ai_avatar_toast_icon.png").value
        val ai_avatar_toast_arrow: Painter @Composable get() = drawable("ai_avatar_toast_arrow.png").value

    }

}
