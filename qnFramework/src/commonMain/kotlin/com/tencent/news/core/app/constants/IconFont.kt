package com.tencent.news.core.app.constants


// IconFont在线预览：
// https://remote-build.woa.com/legend/fileupload/iconfont_new/demo.html
enum class IconFont(val code: String) {
    RIGHT("xwright"),           // 右箭头

    MAGNIFIER("xwmagnifier"),   // 放大镜（普遍用于搜索）

    CLOSE("xwclose"),           // 关闭按钮：x
    CIRCLE_CLOSE("xwclosex"),   // 关闭按钮：圆圈x
    FACEICON_CLOSE("faceicon_closed"),  // 关闭按钮：圆圈x
    CLOSE_REGULAR("closed_style1_regular"), // 关闭按钮：x
    CLOSE_STYLE1_BOLD("closed_style1_bold"), // 关闭按钮：x

    BACK("xwback"),
    BACKR_REGULAR("back_regular"),
    UP("xwup"),
    UP_BOLD("xwup_bold"),
    SEARCH("search_regular"),
    MORE("xwmore"),
    SHARE("xwshare"),
    SHARE1S("xw-share1s"),
    HSHARE("xwhshare"),
    SHARE_REGULAR("share_regular"),
    FAVOURITE("xwcollection"),
    FAVOURITED("xwcollectiond"),
    FACE_FAVORITE("faceicon_collect"),
    ADD24("xwadd_24"),
    ADD2("xw_add2_regular"),
    FEEDBACK_NEW("feedback_new"),               // 竖直排列的三个点
    XW_PLAY("xwplay"),
    XWPLAYY("xwplayy"),
    XWSTOPY("xwstopy"),
    XW_TINYPLAY("xw_tinyplay"),                // 播放按钮图标
    FACICON_DOWN("facicon_down"),
    TINY_VIEW("xw_tinyview"),
    VIDEO_LINE("xw_video_line"),
    XW_VIDEO_ALBUM_LINE("xw_videozhuanjixian"),
    COMMENT_COUNT("faceicon_mainbody"),
    COMMENT_REGULAR("comment_regular"),
    CHECKIN("xw_checkin"),                      // 日签
    CUSTOMIZE("xw_dingzhi_new"),                // 定制
    HEADSET("xwhheadset"),                      // 耳机
    LOCATION_REGULAR("location_regular"),
    FORBID_COMMENT("no_comment_regular"),       // 禁评
    COPY_LINK("xwcopylink"),                    // 复制链接
    XW_LINK_REGULAR("xw_link_regular"),         // 链接
    SCREENSHOT("xwcrop"),                       // 截图分享
    DOWN_ARROW("xwdownarrow"),                  // 下箭头
    UP_ARROW("xwuparrow"),                  // 上箭头
    DOWNLOAD("xwdownload"),                     // 下载
    TISHI_REGULAR("tishi_regular"),             // 提示
    EDIT_REGULAR("edit_regular"),               // 编辑笔

    WX_STORE("xw_wx_store"),                    // 微信小店
    XW_TAP("xw_tap"),                           // 长按点击
    COPY_REGULAR("copy_regular"),                // 复制
    COPY_DONE("xw_copy_done"),
    XWREFRESH("xwrefresh"),
    THUMBUP_REGULAR("thumbup_regular"),
    THUMBDOWN_REGULAR("thumbdown_regular"),
    FACEICON_THUMBUP("faceicon_thumbup"),
    FACEICON_THUMBDOWN("faceicon_thumdown"),
    TIMELINE_CIRCLES("xw_timeline1"),
    TIMELINE_CIRCLE("xw_timeline2"),
    DOWN_STYLE2_REGULAR("down_style2_regular"),
    UP_STYLE2_REGULAR("up_style2_regular"),
    XW_HONE("xw_home"),
    XW_GOU_24("xw_gou_24"),
    XW_COMPLAINTS("xwcomplaints"),
    XW_DISCOVER("xw_discover"),
    XW_DUIGOU("xwduigou"),
    XW_SETTING("setting_regular"),
    XW_MESSAGE_REGULAR("message_regular"),   // 消息图标
    XW_SHUPING("xw_shuping"),
    XW_SCANNING("xw_scanning"),                   // 扫码图标
    AI_VOICE("xw_ai_voice"),
    XW_VOICE("xwvoice"),
    XW_KEYBOARD_2("xwkeyboard_2"),
    XW_FOLLOW("xwfollow"),
    RADIO2_REGULAR("radio2_regular"),
    RADIO_ING_REGULAR("radio_ing_regular"),
    XW_MIC_FILL("xw_mic_fill"),
    RIGHT_BOLD("right_bold"),
    XW_DELETE("xwdelete"),
    DELETE_REGULAR("delete_regular"),
    HUIDA_REGULAR("xw_huida_regular"),
    XW_DINGZHI("xw_dingzhi"),
    AUDIO_SOUND("xwsound"),
    AUDIO_NO_SOUND("xwnosound"),
    FACEICON_PAN("faceicon_pen"),
    XW_CLEAN("xw_clean"),
    ADD_REQULAR("add_regular"),
    XW_PLUS("xwplus"),
    XW_PHONE("xwphone"),
    XW_VOICE_2("xwvoice_2"),
    XW_VOICE_2_FILL("xwvoice_2_fill"),
    XW_VOICE_2_SLASH("xwvoice_2_slash"),
    ARROW_UNCLOSED_SQUARE("xw_arrow_uturn_unclosed_square"),
    XW_LIKE("xwlike"),
    MAXIMIZE("xwmaximize"),
    FULLSCREEN_REGULAR("fullscreen_regular"),    // 全屏图标
    XW_BAR_PUBLISH("xwbarpublish"),
    XW_AI_WAVE_RIGHT("xw_ai_wave_right"),
    XW_AI_KEYBOARD("xw_ai_keyborad"),
    XW_AI_KEYBOARD_2("xw_ai_keyboard_2"),
    XW_AI_ARROW_DOWN("xw_ai_arrow_down"),
    XW_AI_PAPERPLANE("xw_ai_paperplane"),
    XW_AI_PHONE("xw_ai_phone"),
    XW_AI_PHONE_FILL("xw_ai_phone_fill"),
    XW_WEN("xw_wen"),
    XW_XIEYI("xw_xieyi"),
    TIMING_REGULAR("timing_regular"),
    XW_LISTEN_LATER("xw_listen_later"),
    XW_AI_VOICE("xw_ai_voice"),
    XW_TEXT("xw_text"),
    XW_07("xw_07"),
    XW_10("xw_10"),
    XW_12("xw_12"),
    XW_15("xw_15"),
    XW_20("xw_20"),
    XW_30("xw_30"),
    XW_PODCAST_PREV("xw_podcast_prev"),
    XW_PODCAST_NEXT("xw_podcast_next"),
    XWHAUDIO("xwhaudio"),
    FACEICON_TISHI("faceicon_tishi"),
    AI_POST_AUDIO_LISTEN("xw_erji_new"),
    XW_REPLAY("xwreplay"),
    XW_FILLWECHAT("xwfillwechat"),
    XW_MOMENTS("xwmoments"),
    LIKE_NO_REGULAR("like_no_regular"),
    XWFILLSHARE("xwfillshare"),
    XW_LIKE_3("xw_like_3"),
    XW_DAN_XUAN("xw_danxuan"),
    XW_QYWX("xw_qywx"),
    GOLDCOIN_REGULAR("goldcoin_regular"),
    GOLDCOIN_BOLD("goldcoin_bold"),
    HISTORY_REGULAR("history_regular"),
    XW_H_SPEED("xwhspeed"),
    XW_07_R("xwhspeed07"),
    XW_10_R("xwhspeed10"),
    XW_12_R("xwhspeed12"),
    XW_15_R("xwhspeed15"),
    XW_20_R("xwhspeed20"),
    XW_30_R("xwhspeed30"),
    XW_NO_SOUND_STYLE2("xwnosound_style2"),
    XW_SOUND_FILL("xwsound_fill"),
    XW_FOLLOWED("xw_followed"),
    FOLLOWING_REGULAR("following_regular"),
    XW_RIGHT("xwright"),                        // 右箭头
    XW_VOLUME("xwvolume"),                      // 静态音量，三条线
    LIKE_REGULAR("like_regular"),               // 中间透明的心形图标

    // CNY 海报和视频专用图标
    XW_TINYIMG("xw_tinyimg"),                       // 海报图标 (E9bc)
    XW_FACEICON_VERTICALSCREEN("xw_faceicon_verticalscreen"), // 视频图标 (Eab4)
    XW_SPLAY("xwsplay"),                              // 播放图标 (E9bc)
    XW_SUSPED_REGULAR("xw_susped_regular"),           // 暂停图标 (E9bc)
    XW_SUBTITLE_1("xw_subtitle_1"),                   // 字幕模式未选中
    XW_SUBTITLE_2("xw_subtitle_2"),                   // 字幕模式选中
    XW_AD_HAODIAN("xw_ad_haodian"),                   // 微信小店好店标
    XW_AD_R("xw_ad_R"),                               // 微信小店品牌授权R标
    XW_CLEAR_SCREEN("xw_clearscreen"),                        // 清屏
    XW_WING_LEFT("xw_wing_left"),
    XW_WING_RIGHT("xw_wing_right"),

    XW_CAMERA2("xwcamera_2"),

    XW_IMAGE2("xwimage_2"),

    NIGHT_MODE_REGULAR("darkmode_regular"),                   // 夜间模式（月亮），与宿主 iconfont 命名保持一致
    LIGHT_MODE_REGULAR("lightmode_regular"),                  // 日间模式（太阳）
    XW_GIFT("xw_gift"),                                       // 礼物图标（赠送入口）

    FILTER("shaixuan"),                                   // 筛选图标（长视频分类入口）

    // 长视频底层页"更多设置"弹窗用到的图标
    DARKMODE_REGULAR("darkmode_regular"),                 // 夜间模式图标（当前为日间，切到夜间）
    LIGHTMODE_REGULAR("lightmode_regular"),               // 日间模式图标（当前为夜间，切到日间）
    VIDEO_SKIP("xw_video_skip"),                          // 跳过片头尾

    // 视频播控相关图标
    FULL_SCREEN("xwmaximize"),                             // 全屏按钮
    PLAY("xwplay"),                                        // 播放按钮
    PAUSE("xw_susped_regular"),                            // 暂停按钮
    NEXT("xw_podcast_next"),                               // 下一集按钮
    BRIGHTNESS("xw_sun_faceicon"),                          // 亮度调节图标（对齐 Android AdjusterStyle.BRIGHTNESS）
    VOLUME("xw_voice24"),                                  // 音量调节图标（对齐 Android AdjusterStyle.VOLUME）
    MUTE("xw_mute_faceicon"),                              // 静音图标（对齐 Android AdjusterStyle.VOLUME closeIcon）
    LOCK("xwlock"),                                         // 锁屏按钮
    UNLOCK("xwunlock"),                                     // 解锁按钮
    EVEN_MORE_NEW("xwevenmorenew"),                         // 圆圈右箭头

    XW_FONT("xwfont"),                                      // 字体图标

    XW_THING_FILL("xw_thing_fill")                          // 电台合集图标

}
