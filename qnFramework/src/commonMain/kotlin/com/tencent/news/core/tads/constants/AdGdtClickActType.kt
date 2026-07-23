package com.tencent.news.core.tads.constants


// todo【重要】广点通点击上报，acttype 参数
enum class AdGdtClickActType(val value: String) {

    // 各种基础组件区域：
    USER_ICON("1003"),              // 头像区域
    TITLE("1011"),                  // 卡片标题区域
    VIDEO_PLAY_MANUAL("1013"),      // 信息流视频手动播放
    LARGE_PIC("1014"),              // 大图区域点击
    ACTION_BTN("1021"),             // 按钮点击
    DEFAULT_CLICK("1024"),          // 默认点击
    LABEL("1040"),                  // 标签组件点击

    // 特殊交互：
    GALLERY_SLIDE("1041"),          // 画廊左滑进落地页
    FLIP_CARD_BACK("1042"),         // 反转大卡背部
    SLIDE("1048"),                  // 滑动手势出发

    // 竖版三段卡：
    TRINITY_STAGE2_BTN("1051"),     // 高亮行动按钮（例如：三段卡二阶段的）
    TRINITY_STAGE3_BTN("1053"),     // 大卡行动按钮（例如：三段卡三阶段的）
    TRINITY_DEFAULT_CLICK("1054"),  // 三段卡-默认区域
    TRINITY_STAGE3_IMAGE("1101"),   // 三段卡宣传大图

    // 【废弃】下展组件都废弃了：
    EXPAND_FLOAT_ICON("1057"),      // 下展组件模板一自定义头像
    EXPAND_USER_ICON("1002"),       // 下展组件广告主头像
    EXPAND_APP_ICON("1065"),        // 下展组件应用头像
    EXPAND_GOODS_ICON("1059"),
    EXPAND_TEXT_TITLE("1055"),      // 下展组件模板一标题
    EXPAND_APP_TITLE("1062"),       // 下展组件app标题
    EXPAND_SHOP_TITLE("1066"),      // 下展组件门店标题
    EXPAND_GOODS_TITLE("1067"),     // 下展组件商品标题
    EXPAND_COMMON_TITLE("1045"),    // 下展组件转化信息标题
    EXPAND_TEXT_DESC("1056"),       // 下展组件模板一文案
    EXPAND_COMMON_DESC("1069"),     // 下展组件兜底文案
    EXPAND_LABEL("1058"),           // 下展组件标签
    EXPAND_COUNTDOWN("1061"),       // 下展组件倒计时
    EXPAND_APP_STAR("1063"),        // 下展组件评分星级
    EXPAND_APP_DOWNLOAD_NUM("1064"),    // 下展组件app下载人数
    EXPAND_GOODS_LAYOUT("1068"),        // 下展组件商品价格
    EXPAND_AREA("1070"),            // 下展组件区域

    RESERVE("9000"),                // 表单组件点击
    TEL("9001"),                    // 电话组件点击
    CONSULT("9002"),                // 咨询组件点击
    CARD_AREA("9004"),              // 合约组图

    // 试玩小游戏
    PLAYABLE_GAME_ACTION_BTN("10183"),              // 试玩页行动按钮
    PLAYABLE_GAME_FINISH_CARD("10184"),             // 试玩结束页卡片
    PLAYABLE_GAME_FINISH_PAGE_ACTION_BTN("10185"),  // 试玩结束页行动按钮

    ;

    fun isClickActionBtn(): Boolean {
        return this in setOf(
            ACTION_BTN,
            TRINITY_STAGE2_BTN,
            TRINITY_STAGE3_BTN
        )
    }
}

fun AdGdtClickActType?.isCardArea(): Boolean {
    this ?: return false
    return this == AdGdtClickActType.CARD_AREA
}
