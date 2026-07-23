package com.tencent.news.core.tads.comment.vm

import com.tencent.news.core.extension.IKmmKeep


// 小店商品信息， TODO： mountainsli 把其它地方的小店信息归拢到这里面来
interface IAdStoreProductInfoVM : IKmmKeep {
    // 评论小店
    val commentProductInfoVM: IAdCommentStoreProductInfoVM?

    /** 标记评论商品信息 Cell 已经绑定数据，后续直播商品异步回包不再改写该订单。 */
    fun markCommentProductInfoCellInitialized()

    /** 更新评论商品信息异步回填后的宿主刷新回调。 */
    fun updateCommentProductInfoReloadAction(action: (() -> Unit)?)
}
