package com.tencent.news.core.list.model

import com.tencent.news.core.tads.game.model.IGameDto
import com.tencent.news.core.tads.model.IKmmEmptyAdOrder

interface IFeedsIndexItem {
    val flexDto: IFlexListAdapter   // 隔离一层，防止接口重名
}

interface IFlexListAdapter {
    // 基础信息：
    var idStr: String
    val exposureKey: String
    var picShowType: Int
    var articleType: String
    var title: String
    var url: String
    var fullWidthInGrid: Boolean    // 在网格或瀑布流里，占据一整行

    val singleImageUrl: String

    val moduleNewsList: List<IListItem>?

    // 临时业务依赖，待解耦：
    val eventId: String
    val eventVoteId: String
    val userSuid: String
    var shareImg: String
    var shareUrl: String
    var shareTitle: String

    // 广告：
    val ad: IFlexListAdAdapter
}

// 广告：
interface IFlexListAdAdapter {
    val idStr: String   // 文章id（广告投放定向用）
    val tagId: String
    val mediaId: String
    val picShowType: Int    // 对应业务侧的 picShowType 取值
    val vid: String?    // 视频vid（挂卡等视频相关广告位有用，影响投放定向）
    var skipAdInsertLoc: Boolean    // 强制uiBlockSum返回 0（不占广告槽位）
    var fixAdUiBlockNum: Int        // 固定这个Item的uiBlockSum为特定值
    var closeAllAd: Boolean // 【重要】当前文章是否关闭广告（close_all_ad）
    var uiBlockSeq: Int     // 当前信息流文章的绝对位置（按广告槽位计数口径uiBlockSum算的）
    val uiBlockSum: Int // 当前信息流文章/模块，在列表中占据多少个广告位置；返回 0 表示不算一个位置，插入广告时会跳过该文章
    var emptyAdOrder: IKmmEmptyAdOrder? // 空单信息（kmm解析时如果发现，会给对应seq的业务Item身上绑定）
    var game: List<IGameDto>?
}

open class DefaultFlexListAdAdapter : IFlexListAdAdapter {
    override val idStr: String = ""
    override val tagId: String = ""
    override val mediaId: String = ""
    override val picShowType: Int = 0
    override val vid: String? = null
    override var skipAdInsertLoc: Boolean = false
    override var fixAdUiBlockNum: Int = 1
    override var closeAllAd: Boolean = false
    override var uiBlockSeq: Int = 0
    override val uiBlockSum: Int
        get() {
            if (fixAdUiBlockNum > 0) {
                return fixAdUiBlockNum
            }
            if (skipAdInsertLoc) {
                return 0
            }
            return 1
        }
    override var emptyAdOrder: IKmmEmptyAdOrder? = null
    override var game: List<IGameDto>? = null
}

open class DefaultFlexListAdapter(
    override var idStr: String,
    override var title: String = "",
) : IFlexListAdapter {
    override val exposureKey: String = idStr
    override var picShowType: Int = 0
    override var articleType: String = ""
    override var url: String = ""
    override var fullWidthInGrid: Boolean = false
    override val singleImageUrl: String = ""
    override val moduleNewsList: List<IListItem>? = null
    override val eventId: String = ""
    override val eventVoteId: String = ""
    override val userSuid: String = ""

    override var shareImg: String = ""
    override var shareTitle: String = ""
    override var shareUrl: String = ""

    override val ad: IFlexListAdAdapter = object : DefaultFlexListAdAdapter() {
        override val idStr: String get() = this@DefaultFlexListAdapter.idStr
        override val picShowType: Int get() = this@DefaultFlexListAdapter.picShowType
    }
}