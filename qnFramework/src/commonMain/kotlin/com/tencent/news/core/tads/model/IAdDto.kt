package com.tencent.news.core.tads.model

import com.tencent.news.core.extension.IItemDtoDoc
import com.tencent.news.core.list.model.IKmmFeedsItem
import com.tencent.news.core.parcel.IKmmParcelable


/**
 * 信息流广告逻辑 依赖的 Item控制参数
 */
interface IAdDto : IItemDtoDoc, IKmmParcelable {

    // 【重要】文章id：影响文章定向投放；另外，如果客户端没拿到 close_all_ad，ssp会用文章id从总库拉取状态
    val idStr: String

    // 【重要】当前文章是否关闭广告（close_all_ad）
    var closeAllAd: Boolean

    // 对应业务侧的 picShowType 取值
    val picShowType: Int

    // 当前信息流文章/模块，在列表中占据多少个广告位置；返回 0 表示不算一个位置，插入广告时会跳过该文章
    val uiBlockSum: Int

    // 固定这个Item的uiBlockSum为特定值
    var fixAdUiBlockNum: Int

    // 强制uiBlockSum返回 0（不占广告槽位）
    var skipAdInsertLoc: Boolean

    // 当前信息流文章的绝对位置（按广告槽位计数口径uiBlockSum算的）
    var uiBlockSeq: Int

    // 文章模块嵌套多篇子文章的情况
    val moduleItemList: List<IKmmFeedsItem>?

    // 视频vid：挂卡等视频相关广告位有用，影响投放定向
    val vid: String?

    // tag_id：普通tag、724tag、tag合集 等场景有
    val tagId: String?

    // 企鹅号id：可能影响投放策略，目前用的比较少
    val mediaId: String?

    // 空单信息（kmm解析时如果发现，会给对应seq的业务Item身上绑定）
    var emptyAdOrder: IKmmEmptyAdOrder?

}