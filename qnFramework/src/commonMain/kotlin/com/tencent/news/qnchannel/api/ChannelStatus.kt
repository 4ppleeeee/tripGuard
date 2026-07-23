package com.tencent.news.qnchannel.api


annotation class ChannelStatus {

    companion object {

        /**
         * 正常状态，可以刷新网络数据
         */
        const val NORMAL = 0

        /**
         * 频道冻结（有缓存则显示，没缓存则显示空页面），禁掉上下拉
         */
        const val FREEZE = 1

        /**
         * 频道冻结且清空数据，直接显示空页面，禁掉上下拉
         */
        const val FREEZE_CLEAR = 2

        /**
         * 仅显示置顶池，逻辑由接入层做，客户端透传 channel_status 字段
         */
        const val STICK_ONLY = 3

    }

}
