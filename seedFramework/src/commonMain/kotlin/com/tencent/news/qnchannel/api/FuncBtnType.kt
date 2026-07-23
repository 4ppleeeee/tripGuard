package com.tencent.news.qnchannel.api


annotation class FuncBtnType {
    companion object {
        /**
         * 功能按钮入口位置：1
         */
        const val INDEX_FUNC_1 = "_func1"

        /**
         * 功能按钮入口位置：2
         */
        const val INDEX_FUNC_2 = "_func2"

        fun getAllIndex() = arrayOf(
            INDEX_FUNC_1,
            INDEX_FUNC_2
        )

        /**
         * 功能按钮类型：搜索入口
         */
        const val SEARCH = "search"

        /**
         * 功能按钮类型：发布微博
         */
        const val PUBLISH_WEIBO = "publish_weibo"

        /**
         * 全量功能按钮集合，非该集合里的type_id当做非法值，客户端不展示；
         * 后续新版本扩展了type_id，旧版本不支持的，这里会直接隐藏。
         */
        fun getAllTypes() = arrayOf(
            SEARCH,
            PUBLISH_WEIBO
        )
    }
}
