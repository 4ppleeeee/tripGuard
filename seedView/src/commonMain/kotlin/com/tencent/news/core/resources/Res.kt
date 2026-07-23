package com.tencent.news.core.resources

import androidx.compose.runtime.Composable
import com.tencent.kuikly.compose.ui.graphics.painter.Painter
import com.tencent.news.core.compose.scaffold.theme.drawable
import com.tencent.news.core.compose.scaffold.theme.lightDrawable


object Res {

    object drawable {
        val up_arrow: Painter @Composable get() = drawable("up_arrow.webp").value
        val up_arrow_tlink: Painter @Composable get() = drawable("up_arrow_tlink.webp").value
        val wxConsultSmallIcon: Painter @Composable get() = drawable("wx_consult_small_icon.png").value
        val wxConsultGoIcon: Painter @Composable get() = drawable("wx_consult_go_icon.png").value
        val wxConsultLeftDown: Painter @Composable get() = drawable("wx_consult_left_down.png").value
        val wxStoreShopIcon: Painter @Composable get() = lightDrawable("wei_xin_store_default_icon.webp").value
        val wxLiveShopIcon: Painter @Composable get() = lightDrawable("live_shop_default_icon.webp").value

        // 分享图标
        val share_qq_icon: Painter @Composable get() = drawable("share_qq_icon.png").value
        val share_qzone_icon: Painter @Composable get() = drawable("share_qzone_icon.png").value
        val share_sina_icon: Painter @Composable get() = drawable("share_sina_icon.png").value
        val share_weixin_icon: Painter @Composable get() = drawable("share_weixin_icon.png").value
        val share_wx_moment_icon: Painter @Composable get() = drawable("share_wx_moment_icon.png").value
        val share_work_weixin_icon: Painter @Composable get() = drawable("share_work_weixin_icon.png").value
        val share_poster_icon: Painter @Composable get() = drawable("share_poster_icon.png").value
        val share_pdf_icon: Painter @Composable get() = drawable("share_pdf_icon.png").value

        // todo 日夜间切换时触发重组
        val no_network_placeholder: Painter @Composable get() = drawable("no_network_placeholder.webp").value

        val default_list_logo: Painter @Composable get() = drawable("default_list_logo.png").value
        val default_avatar_round: Painter @Composable get() = drawable("default_avatar_round.webp").value
        val default_big_logo_icon: Painter @Composable get() = drawable("default_big_logo_icon.png").value

        val ad_game_star: Painter @Composable get() = drawable("ad_game_star.png").value
        val ad_endCard_next_video: Painter @Composable get() = drawable("ad_endCard_next_video.png").value
        val adCheckInBgStar: Painter @Composable get() = drawable("ad_checkin_bg_star.webp").value
        val adCheckInCoin: Painter @Composable get() = drawable("ad_game_checkin_coin.webp").value
        // 游戏日历卡片背景
        val calendar_item_bg: Painter @Composable get() = drawable("calendar_item_bg.png").value


        val bgBlock: Painter @Composable get() = drawable("bg_block.png").value

        val tad_gameC: Painter @Composable get() = drawable("tad_gameC.webp").value


        val ic_home_search: Painter @Composable get() = drawable("ic_home_search.png").value

        val img_tab_hot_selected: Painter @Composable get() = drawable("img_tab_hot_selected.png").value
        val img_tab_hot_unselected: Painter @Composable get() = drawable("img_tab_hot_unselected.png").value
        val img_tab_new_selected: Painter @Composable get() = drawable("img_tab_new_selected.png").value
        val img_tab_new_unselected: Painter @Composable get() = drawable("img_tab_new_unselected.png").value

        /** 热播榜 Header 背景图 */
        val tab_hot_bg: Painter @Composable get() = drawable("tab_hot_bg.jpg").value

        /** 新剧榜 Header 背景图 */
        val tab_new_bg: Painter @Composable get() = drawable("tab_new_bg.jpg").value

        /** 个人页默认封面背景图（粉色渐变几何图案） */
        val img_mine_profile_default_cover: Painter @Composable get() = drawable("img_mine_profile_default_cover.jpg").value

        /** 个人页作品 Tab 空态插画 */
        val img_mine_profile_empty_works: Painter @Composable get() = drawable("img_mine_profile_empty_works.png").value

        /** 个人页收藏 Tab 空态插画 */
        val img_mine_profile_empty_collect: Painter @Composable get() = drawable("img_mine_profile_empty_collect.png").value

        /** 个人页已赞 Tab 空态插画 */
        val img_mine_profile_empty_liked: Painter @Composable get() = drawable("img_mine_profile_empty_liked.png").value

        /** 个人页网络异常空态插画 */
        val img_mine_profile_empty_network: Painter @Composable get() = drawable("img_mine_profile_empty_network.png").value

        /** 推荐页互动栏 - 点赞图标 */
        val ic_recommend_like: Painter @Composable get() = drawable("ic_recommend_like.png").value

        /** 推荐页互动栏 - 已点赞图标 */
        val ic_recommend_liked: Painter @Composable get() = drawable("ic_recommend_is_liked.png").value

        /** 推荐页互动栏 - 评论图标 */
        val ic_recommend_comment: Painter @Composable get() = drawable("ic_recommend_comment.png").value

        /** 推荐页互动栏 - 收藏图标 */
        val ic_recommend_collect: Painter @Composable get() = drawable("ic_recommend_collect.png").value

        /** 推荐页互动栏 - 收藏图标 已收藏 */
        val ic_recommend_collected: Painter @Composable get() = drawable("ic_recommend_is_collected.png").value

        /** 推荐页互动栏 - 分享图标 */
        val ic_recommend_share: Painter @Composable get() = drawable("ic_recommend_share.png").value

        /** 推荐页互动栏 - 关注图标 */
        val ic_recommend_follow: Painter @Composable get() = drawable("ic_recommend_follow.png").value

        /** 通用返回按钮 */
        val ic_btn_back: Painter @Composable get() = drawable("ic_btn_back.svg").value

        /** 通用右侧箭头 */
        val ic_btn_arrow_more: Painter @Composable get() = drawable("ic_btn_arrow_more.svg").value

        /** 个人页性别标签 - 男性图标 */
        val ic_profile_header_male: Painter @Composable get() = drawable("ic_profile_header_male.png").value

        /** 个人页性别标签 - 女性图标 */
        val ic_profile_header_female: Painter @Composable get() = drawable("ic_profile_header_female.png").value

        /** 个人页 Banner 卡片背景图 */
        val img_mine_profile_banner_bg: Painter @Composable get() = drawable("img_mine_profile_banner_bg.jpg").value

        /** 默认头像占位图 */
        val icon_default_portrait: Painter @Composable get() = drawable("icon_default_portrait.png").value

        val icon_profile_edit: Painter @Composable get() = drawable("icon_profile_edit.png").value

        /** 动作面板 - 不感兴趣图标 */
        val ic_action_dislike: Painter @Composable get() = drawable("ic_action_dislike.png").value

        /** 动作面板 - 举报图标 */
        val ic_action_report: Painter @Composable get() = drawable("ic_action_report.png").value

        /** 动作面板 - 倍速图标 */
        val ic_action_speed: Painter @Composable get() = drawable("ic_action_speed.png").value

        /** 动作面板 - 自动连播图标 */
        val ic_action_autoplay: Painter @Composable get() = drawable("ic_action_autoplay.png").value

        /** 动作面板 - 字体大小图标 */
        val ic_action_fontsize: Painter @Composable get() = drawable("ic_action_fontsize.png").value

        /** 动作面板 - 保存到相册图标 */
        val ic_action_save: Painter @Composable get() = drawable("ic_action_save.png").value

        val snakebar_checkbox: Painter @Composable get() = drawable("snakebar_checkbox.png").value
        val snakebar_right_arrow: Painter @Composable get() = drawable("snakebar_right_arrow.png").value


        /** 关注列表空状态插画 */
        val img_following_empty: Painter @Composable get() = drawable("img_following_empty.png").value

        /** 首页关注频道无关注空态插画：空态强制夜间模式下仍使用设计稿静态资源 */
        val img_home_follow_empty_logo: Painter @Composable get() = lightDrawable("img_home_follow_empty_logo.png").value

        /** 粉丝列表空状态插画 */
        val img_fans_empty: Painter @Composable get() = drawable("img_fans_empty.png").value

        /** 评论面板 - 表情图标 */
        val comment_panel_emoji_icon: Painter @Composable get() = drawable("comment_panel_emoji_icon.png").value

        /** 评论面板 TitleBar - 关闭按钮图标（X） */
        val comment_panel_close_icon: Painter @Composable get() = drawable("comment_panel_close_icon.png").value

        /** 评论面板 TitleBar - 收起按钮图标（全屏态使用） */
        val comment_panel_collapse_icon: Painter @Composable get() = drawable("comment_panel_collapse_icon.png").value

        /** 评论面板 TitleBar - 展开按钮图标（半屏态使用） */
        val comment_panel_expand_icon: Painter @Composable get() = drawable("comment_panel_expand_icon.png").value

        val comment_input_delete_icon: Painter @Composable get() = drawable("comment_input_delete_icon1.png").value
        val comment_input_delete_disable_icon: Painter @Composable get() = drawable("comment_input_delete_disable_icon1.png").value

        /** 评论面板 - 0评论空态插图 */
        val comment_panel_empty_icon: Painter @Composable get() = drawable("comment_panel_empty_icon.png").value



        /** 消息页，评论和@的头像 */
        val icon_message_comment_at: Painter @Composable get() = drawable("icon_message_comment_at.png").value

        /** 消息页，点赞和收藏的头像 */
        val icon_message_like_collect: Painter @Composable get() = drawable("icon_message_like_collect.png").value

        /** 消息页，新增粉丝的头像 */
        val icon_message_new_fans: Painter @Composable get() = drawable("icon_message_new_fans.png").value

        /** 仅浏览弹窗，播放装饰图 */
        val icon_readonly_play: Painter @Composable get() = drawable("icon_readonly_play.png").value

        /** 仅浏览弹窗，福利装饰图 */
        val icon_readonly_welfare: Painter @Composable get() = drawable("icon_readonly_welfare.png").value
        val readonly_mark: Painter @Composable get() = drawable("readonly_mark.png").value

        /** 底部挂卡 - 合集图标 */
        val ic_bottombar_collection: Painter @Composable get() = drawable("ic_bottombar_collection.png").value

        /** 底部挂卡 - 短剧图标 */
        val ic_bottombar_drama: Painter @Composable get() = drawable("ic_bottombar_drama.png").value
        /** 底部挂卡 - 放映厅 */
        val ic_bottombar_hall: Painter @Composable get() = drawable("ic_bottombar_hall.png").value

        /** 底部挂卡 - 右箭头图标 */
        val ic_bottombar_arrow_right: Painter @Composable get() = drawable("ic_bottombar_arrow_right.png").value

        /** 底部挂卡 - 榜单时钟图标 */
        val ic_bottombar_rank_clock: Painter @Composable get() = drawable("ic_bottombar_rank_clock.png").value

        /** 福利挂件 - 红包图标 */
        val welfare_pendant_icon_red: Painter @Composable get() = drawable("welfare_pendant_icon_red.png").value
        val welfare_pendant_icon_drama: Painter @Composable get() = drawable("welfare_pendant_icon_drama.png").value

        /** 热榜底部导航 - 播放图标 */
        val ic_bottombar_play_icon: Painter @Composable get() = drawable("ic_bottombar_play_icon.png").value

        /** 热榜底部导航 - 下个热点双箭头 */
        val ic_bottombar_next_arrow: Painter @Composable get() = drawable("ic_bottombar_next_arrow.png").value

        val ic_tab_entry_publish: Painter @Composable get() = drawable("ic_tab_entry_publish.png").value

        val ic_collection_detail_entry : Painter @Composable get() = drawable("ic_collection_detail_entry.png").value
        val dark_ic_collection_detail_entry : Painter @Composable get() = drawable("dark_ic_collection_detail_entry.png").value
        val ic_up_arrow : Painter @Composable get() = drawable("ic_up_arrow.png").value
        val ic_follow_add : Painter @Composable get() = drawable("ic_follow_add.png").value
        val ws_add_regular : Painter @Composable get() = drawable("ws_add_regular.png").value

        val comment_liked_icon : Painter @Composable get() = drawable("comment_liked_icon.png").value

        val comment_unliked_icon : Painter @Composable get() = drawable("comment_unliked_icon.png").value

        // ==================== 登录页资源（Figma 1957:30790）====================

        /** 微信登录主按钮背景（带绿色 + 阴影，157.5×23 @3x = 473×69） */
        val login_wechat_button_bg: Painter @Composable get() = drawable("login_wechat_button_bg.png").value

        /** "其它方式登录" 左右细分隔线（9×0.25 @3x = 27×1） */
        val login_divider_line: Painter @Composable get() = drawable("login_divider_line.png").value

        /** 扫码方式选择气泡背景（72×42.66 @3x = 216×128） */
        val login_scan_popup_bg: Painter @Composable get() = drawable("login_scan_popup_bg.png").value

        val comment_reply_icon : Painter @Composable get() = drawable("comment_reply_icon.png").value

        val ic_private_chat : Painter @Composable get() = drawable("ic_private_chat.png").value

        /** 个人页 - 「刚刚看过」目标 Cell 蒙层上的播放三角 icon（24×24dp，纯白） */
        val icon_profile_play: Painter
            @Composable get() = lightDrawable("icon_profile_play.png").value

    }

}
