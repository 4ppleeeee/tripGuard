package com.tencent.news.core.list.model

import com.tencent.news.core.extension.IItemDtoDoc
import com.tencent.news.core.extension.IKmmKeep
import com.tencent.news.core.parcel.IKmmParcelable


interface IBaseDto : IItemDtoDoc, IKmmKeep, IKmmParcelable {

    var idStr: String
    var title: String
    var tltitle: String                 // 在列表上外显的文章标题，只在列表cell上用，优先级高于title
    var longTitle: String
    var articleType: String
    var entityType: String
    var subAType: String                 // 用于区分文章子类型，比如视频文章的子类型：视频形式的音频文章
    var picShowType: Int
    var abstract: String
    var flag: String
    var subTitle: String

    var thumbnailsNormal: List<String>  // 常规信息流图片（接入层会根据picShowType装入不同尺寸图片）
    var thumbnailsLarge: List<String>   // 原始大图，基本是上游数据源尺寸最大的；一般不用这个
    var thumbnailsSmall: List<String>   // 最小尺寸的图，普遍小图标等情况；一般不用这个
    var explicitImageUrl: String     // 专题类型需要的图片

    var imageMore: IImageMore?   // 补充的图片，不同场景的取不同值，一般取上面的

    var nodeContents: List<KmmNodeContents>?    // 节点信息（@cell618 公告 marquee / 专题节点外显），@since 6170

    var briefAbstract: String

    val exposureInfo: IExposureInfo?

    var url: String

    @Deprecated(
        "后续不要用，这个属于偷懒，破坏pb的可读性；本地绑定的参数都放到 ctxDto 里",
        ReplaceWith("item.ctxDto")
    )
    var extraProperty: MutableMap<String, String>

    var replacedId: String      // 【云排】被替换的文章id

    /**
     * 专题/事件等集合形式的文章，在信息流上还有一篇外显的焦点文章，这个focusId是焦点文章的id；
     * 这个字段改过名字，之前叫 focusNewsId 但废弃很久了，接入层重新启用后改了个字段名（@since 7120）
     */
    @Deprecated("用 eventDto 的")
    var focusId: String

    @Deprecated("用 eventDto 的")
    var thingDisplayCmsId: String

    var interactionInfo: IInteractionInfo?

    var calendarInfo: ICalendarInfo?

    var readCount: Long
    var commentNum: Long
    var timestamp: Long
    var labelList: List<IItemLabel>?
    var extraLabelList: List<IItemLabel>?
    var upLabelList: List<IItemLabel>?

    var directScheme: String   // 接入层下发，articleType=700 时需要跳转的scheme
    var scheme: String           // 接入层下发，用于模块跳转，可能是url

    /** 分类标签（长视频频道分类入口卡片 articleType=570 使用） */
    var customTags: List<QnCustomTag>

    var articleVer: String
    var chlid: String
    var time: String
    var origUrl: String
    var source: String
    var pubInfo: IPubInfo?
    var qishu: String
    var isSensitive: Int
    var fadCid: String
    var gesture: String // 是否支持手势，默认支持

    var imageCount: String // 图片数量
    var targetId: String // 目标ID

    var zjTitle: String // 专辑标题
    var videoNum: String // 视频数量

    var isHotNews: Int // 是否是热门新闻（0：不是，1：是）
    var origSpecialID: String // 决定普通新闻底层页，是否显示专题入口
    var sportsExt: String
    var articlePos: Int // 从1开始，后台下发，前端透传。文章在当前刷次中的位置
    var actionbarTitleScheme: String
    var actionbarTitle: String
    var pageJumpType: String
    var weiboStatus: Int // 微博的状态，0已审核,1未审核正在发送，2未审核发送失败,3未审核发送成功
    var hotScore: String // 热度值
    var weiboType: String // 微博类型，weibo_type=6就是点评的微博
    var weiboTag: String // 微博伪标签内容
    var weiboEnableDelete: Int // weiboEnableDelete = 1 出，其它情况都不出
    var nlpContentAbstract: String // 算法取正文兜底
    var isCrossArticle: Int // 是否是CROSS文章 0：不是，1 是
    var extraData: String // 额外数据
    var alginfo: String // 上报用，不解析
    var cctvNewsReportUrl: String // 央视新闻上报URL
    var transparam: String // 通用透传字段
    var switchControl: SwitchControl?
}