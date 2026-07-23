package com.tencent.news.qnchannel.api


annotation class ModifyFrom {
    companion object {
        /**
         * 废弃值
         */
        @Deprecated("原需求[859868111], 6770起废弃此枚举值，部分相关逻辑迁移至{@link ModifyFrom#ALGORITHM_REC}")
        const val ALGORITHM = "alg"

        /**
         * 算法推荐
         * 需求[871409665], 6770起此枚举值生效
         */
        const val ALGORITHM_REC = "alg_rec"

        const val USER = "user"

        const val COMMAND = "cmd"

        /**
         * 频道替换,6800开始生效
         */
        const val REPLACE = "replace"

        /**
         * https://tapd.woa.com/10045201/prong/stories/view/1010045201875787877
         * 运营推荐
         */
        const val OPERATE_REC = "operate_rec"
    }
}