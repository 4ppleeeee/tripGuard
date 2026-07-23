package com.tencent.news.qnchannel.api


annotation class ChannelTabId {
    companion object {
        /**
         * 特殊tabId：用户当前选中的右导航数据
         */
        const val USER_CHANNELS = "user_channels"

        /**
         * 特殊tabId：用户顶层导航数据
         */
        @Deprecated("")
        const val TOP_USER_CHANNELS = "top_user_channels"

        /**
         * 特殊tabId：当前推荐的地方站城市
         */
        const val RECOMMEND_CITY = "recommend_city"

        // 以下为大圣系统配置的正规tabId
        const val NORMAL_CHANNELS = "normal_channels"
        const val CITY_CHANNELS = "city_channels"
        const val LEFT_CHANNELS = "left_channels"
        const val LEFT_TOP_CHANNELS = "left_top_channels"
        const val RIGHT_TOP_CHANNELS_1 = "right_top_channels1"
        const val RIGHT_TOP_CHANNELS_2 = "right_top_channels2"
        const val TAB_2 = "tab2"
        const val TAB_3 = "tab3"
        const val TAB_4 = "tab4"
        const val TAB_MIDDLE = "tab_middle"
        const val TAB_TOP = "tab_top"
        const val USER_CHANNELS_PRO = "user_channels_pro"
        const val TAB_EXT1 = "tab_ext1"
        const val TAB_EXT2 = "tab_ext2"
        const val TAB_EXT3 = "tab_ext3"
        const val TAB_EXT4 = "tab_ext4"
        const val TAB_EXT5 = "tab_ext5"
        const val LEFT_CHANNELS_EXT = "left_ext"
        const val LEFT_TOP_CHANNELS_EXT = "left_top_ext"
        const val RIGHT_TOP_CHANNELS_1_EXT = "right_top1_ext"
        const val RIGHT_TOP_CHANNELS_2_EXT = "right_top2_ext"
        const val CHANNELS_MENU = "channels_menu" // 频道管理按钮的气泡配置
        const val TAB_LIKE_RADIO_MOCK_ID = "tab_like_radio_mock"

        const val LOCAL_GROUP_TAB = "local_group_tab"

        // 标识tab1，等同于大圣配置的normal_channels，语义上更好理解，添加于6410，仅用于scheme跳转参数
        const val TAB_1 = "tab1"

        fun getAllEntries() = arrayOf( // todo 这个全量集合入口还没有包含二级的tab
            NORMAL_CHANNELS,
            CITY_CHANNELS,
            LEFT_CHANNELS,
            LEFT_TOP_CHANNELS,
            RIGHT_TOP_CHANNELS_1,
            RIGHT_TOP_CHANNELS_2,
            TAB_2,
            TAB_3,
            TAB_4,
            TAB_MIDDLE,
            TAB_TOP
        )

        /**
         * 全部EXT入口集合
         */
        fun getAllExtEntries() = arrayOf(
            TAB_EXT1,
            TAB_EXT2,
            TAB_EXT3,
            TAB_EXT4,
            TAB_EXT5,
            LEFT_CHANNELS_EXT,
            LEFT_TOP_CHANNELS_EXT,
            RIGHT_TOP_CHANNELS_1_EXT,
            RIGHT_TOP_CHANNELS_2_EXT,
            CITY_CHANNELS
        )

        /**
         * 下发数据不能为空的集合（如果后台下发为空，则不更新数据，继续使用本地的）
         */
        fun getEntriesCanNotEmpty() = arrayOf(
            NORMAL_CHANNELS,
            CITY_CHANNELS,
            TAB_TOP,
            TAB_2,
            TAB_3,
            TAB_4
        )

        fun getEntriesCanNotEmptyExt() = arrayOf(
            TAB_EXT1,
            CITY_CHANNELS,
            TAB_EXT2,
            TAB_EXT4,
            TAB_EXT5
        )

        /**
         * 顶部ChannelBar的入口
         */
        fun getTopTabEntries() = arrayOf(
            LEFT_TOP_CHANNELS,
            RIGHT_TOP_CHANNELS_1,
            RIGHT_TOP_CHANNELS_2,
            TAB_TOP
        )

        /**
         * 新闻底部导航 的几个入口位置
         */
        fun getBottomTabEntries() = arrayOf(
            NORMAL_CHANNELS,
            TAB_2,
            TAB_3,
            TAB_4,
            TAB_MIDDLE
        )
    }
}