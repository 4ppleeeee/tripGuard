package com.tencent.news.core.tads.tab2.constants

// 广告竖版视频模板卡组件id
object AdVideoCardId {
    // TODO:mountainsli 补一个长期优化：广告的枚举应该移到一个 qnAdCore 模块里，从框架层移出来；可以参考一下微视仓库的做法，新闻还没来得及弄
    object Consult {
        const val BTN_CARD = "consult_btn_card"             // 客服-行动按钮
        const val MARQUEE_CARD = "consult_marquee_card"     // 客服-轮播小卡
        const val BIG_CARD = "consult_big_card"             // 客服-大卡
        const val LOCATION_CARD = "consult_location_card"   // 客服-定位卡
        const val NEW_BIG_CARD = "consult_new_big_card"     // 客服-大卡(新)
    }

    object MiniGame {
        const val SMALL_CARD = "mini_game_type_card"        // 小游戏-小卡
        const val MIDDLE_CARD = "mini_game_simple_card"     // 小游戏-中卡
        const val BIG_CARD = "mini_game_white_card"         // 小游戏-大卡

        // 新三阶段原生卡片
        const val NORMAL_CARD = "mini_game_normal_card"     // 小游戏-第一阶段：icon + 游戏名称 + 引导描述
        const val ACTION_CARD = "mini_game_action_card"     // 小游戏-第二三阶段：icon + 游戏名称 + 轮播 + 行动按钮
        const val INTRO_BIG_CARD = "mini_game_intro_big_card" // 小游戏-三元组信息大卡
    }

    object LiveShop {
        const val SMALL_CARD = "live_shop_small_card"               // 直播小店-小卡（有商品数据）
        const val SMALL_CARD_EMPTY = "live_shop_small_card_empty"   // 直播小店-小卡（无商品数据）-上报用
        const val BIG_CARD = "live_shop_big_card"                   // 直播小店-大卡（有商品数据）
        const val BIG_CARD_EMPTY = "live_shop_big_card_empty"       // 直播小店-大卡（无商品数据）-上报用
        const val SMALL_CARD_NEW = "live_shop_small_card_new"       // 直播小店-新小卡（有商品数据）
        const val BIG_CARD_NEW = "live_shop_big_card_new"           // 直播小店-新大卡（有商品数据）

        // 直播-三阶段原生卡片
        const val NORMAL_CARD = "live_stream_normal_card"               // 直播-第一阶段：固定icon + 直播中
        const val ACTION_CARD = "live_stream_action_card"               // 直播-第二三阶段：商品/无商品 + 按钮高亮
    }

    object WeChatShop {
        const val SMALL_CARD = "wechat_shop_small_card"               // 微信小店-小卡（有商品数据）
        const val BIG_CARD = "wechat_shop_big_card"                   // 微信小店-大卡（有商品数据）
        const val SMALL_CARD_NEW = "wechat_shop_small_card_new"       // 微信小店-新小卡（有商品数据）
        const val BIG_CARD_NEW = "wechat_shop_big_card_new"           // 微信小店-新大卡（有商品数据）

        // 微信小店-三阶段原生卡片
        const val NORMAL_CARD = "wechat_store_normal_card"            // 微信小店-第一阶段：固定icon + 好物热销
        const val ACTION_CARD = "wechat_store_action_card"            // 微信小店-第二三阶段：商品信息 + 按钮高亮
    }

    object Shop618Coupon {
        const val SMALL_CARD = "shop_618_coupon_small_card"         // 618 小店券-一/二段小卡
        const val BIG_CARD = "shop_618_coupon_big_card"             // 618 小店券-三段白色券包大卡
        const val COMPLIANCE_TEXT_CARD = "shop_618_coupon_compliance_text_card" // 618 小店券-免责声明
    }

    object EcommerceGeneral {
        const val SMALL_CARD = "ecommerce_general_small_card"               // 电商-小卡
        const val BIG_CARD = "ecommerce_general_big_card"                   // 电商-大卡
        const val COMPLIANCE_TEXT_CARD = "ecommerce_general_compliance_text_card" // 电商-合规说明
    }

    object ShortDrama {
        const val NORMAL_CARD = "short_video_normal_card"               // 短剧-普通小卡（不带行动按钮）
        const val ACTION_CARD = "short_video_action_card"               // 短剧-行动按钮卡（带行动按钮）
        const val ACTION_CARD_V2 = "short_video_action_card_v2"         // 短剧-行动按钮卡（信息外显）
        const val BIG_CARD = "short_video_big_card"                     // 短剧-三段卡大卡
    }

    object Travel {
        const val NORMAL_CARD = "travel_normal_card"                    // 旅游-第一阶段：固定icon + 引导文案
        const val ACTION_CARD = "travel_action_card"                    // 旅游-第二三阶段：行动按钮高亮
    }

    object Novel {
        const val ACTION_CARD = "novel_action_card"               // 小说-行动按钮高亮
    }

    object Tools {
        const val ACTION_CARD = "tools_action_card"               // 工具-行动按钮高亮
        const val BIG_CARD = "tools_big_card"               // 工具-行动按钮高亮
    }

    object Education {
        const val ACTION_CARD = "education_action_card"           // 教育-行动按钮高亮
    }

    object Download {
        const val SMALL_CARD = "download_small_card"              // 下载行业-一/二段小卡
        const val BIG_CARD = "download_big_card"                  // 下载行业-三段白色大卡
    }

    const val DEFAULT_BIG_CARD = "default_big_card"         // 普通大卡（旧）
    const val DEFAULT_BIG_CARD_V2 = "default_big_card_v2"   // 大卡（数据外显-新）
    const val TITLE_CARD = "title_card"                     // 标题
    const val FLICKER_BTN_CARD = "flicker_btn_card"         // 闪动行动按钮
    const val APP_CHANNEL_INFO_CARD = "ad_app_channel_info" // 下载十要素
    const val AD_HOT_CLICK_AREA = "ad_hot_click_area"       // 广告点击热区

    const val MINI_CARD_PENDANT = "mini_card_pendant"       // 图文组件

    const val COMPANION_VIEW = "companion_view"             // 竖版-视频挂卡

}
