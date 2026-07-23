package com.tencent.news.core.tads.constants


object AdLabelType {
    const val TYPE_GAME = 1000          // 游戏标签
    const val GAME_PACK = 1000002       // 游戏礼包（客户端自己定义的）
}

object AdWeChatGameType {
    const val GAME_TYPE_LIST = 1        // 榜单标签，例如：‘畅销榜TOP10’
    const val GAME_TYPE_ACTION = 2      // 行为标签，例如：‘100万+人在玩’
    const val GAME_TYPE_CATEGORY = 3    // 品类标签，例如：‘竞技’，‘对战’
}

object AdWeChatGameBulletType {
    const val BULLET_GAME_TYPE_COMMEN = 1       // 普通文本标签
    const val BULLET_GAME_TYPE_CATEGORY = 2     // 品类标签，例如：‘竞技’，‘对战’
}

object AdWeChatGameBulletText {
    const val BULLET_GAME_TEXT = "爆款游戏免费开玩"
}

object AdNativeGameLabelType {
    val RANKING_TYPES = listOf(1, 2, 6, 7) // 游戏榜单标签，按优先级取第一个
    const val ACTIVE = 3                   // 活跃人数
    const val COLLECT = 4                  // 收藏人数
    const val SHARE = 5                    // 分享人数
}
