package com.tencent.news.core.list.api

import com.tencent.news.core.annotation.KmmInternalApi
import com.tencent.news.core.audio.model.RadioScene
import com.tencent.news.core.detail.IUnderlineItem
import com.tencent.news.core.extension.IKmmKeep
import com.tencent.news.core.list.constants.NewsSceneType
import com.tencent.news.core.list.model.Divider
import com.tencent.news.core.list.model.IKmmFeedsItem
import com.tencent.news.core.page.model.NewsListSection
import com.tencent.news.core.page.model.PageSkinRes
import com.tencent.news.core.parcel.IKmmParcelable
import com.tencent.news.core.tads.model.IKmmAdFeedsItem
import com.tencent.news.core.tag.model.QnTagInfo
import com.tencent.news.core.video.constants.VideoArticleType
import com.tencent.news.core.vm.IDetailModelStub

enum class ContextListType {
    UNKNOWN,                        // 未知
    CPMEMBERAREA_TAB_ALL,           // 会员专区全部tab
    CPMEMBERAREA_TAB_COLLECTION,    // 会员专区合集tab
    OM_PAGE,                        // 个人页
}

// 客户端本地绑定的参数集合
interface IContextDto : IKmmKeep, IKmmParcelable {

    var articleUUID: String

    // 从0开始，客户端记录。记录文章是第几刷下发下来的，reset时归0，后续上拉或下拉都会+1。默认值-1，非二级频道目前未绑定该值
    var articlePage: Int

    // 在当前这一刷中的位置，从1开始；客户端计算时机：置顶、排重、广告插入之后，模块展开之前（模块内的所有文章算一个位置）
    // todo【注意】产品要求这个位置中，广告【不计算】位置
    var articleRealPos: Int

    // 在整个数据列表中的位置，从1开始；客户端计算时机：置顶、排重、广告插入之后，模块展开之前（模块内的所有文章算一个位置）
    // todo【注意】产品要求这个位置中，广告【需要计算】位置
    var articleListPos: Int

    // 文章在当前模块中的位置，从1开始；
    var articleModulePos: Int

    var ignorePos: Boolean          // 标识不参与Pos位置计算
    var ignoreExpose: Boolean       // 不上报曝光

    var newsChannel: String         // 上报用的二级频道

    // 当前所在页面信息
    var pageArticleId: String
    var pageArticleType: String
    var realArticleType: String     // 数据解析时候，绑定的原始articleType，后面再修改articleType的话，这个也不变

    // 当前所在模块的信息
    var modArticleId: String
    var modArticleType: String
    var modArticleTitle: String
    var modPicShowType: Int

    var videoArticleType: VideoArticleType

    var pageTagType: String         // tag底层列表上报用
    var pageTagId: String

    val jumpStartTime: Long
    var jumpStartScheme: String     // 拉起当前页面时的scheme

    // 标识需要从列表中抠出来，置顶到品字形顶部的文章；目前有该标识的文章，会在普通列表里被隐藏
    var moveToHeader: Boolean

    var relateVideos: List<String>?

    // 相关新闻，从图文进入沉浸式会会携带
    var relateNews: List<IKmmFeedsItem>?

    var closeVideoAd: Boolean

    var newsSceneType: NewsSceneType

    var relatedTagInfo: QnTagInfo?

    var topDivider: Divider?
    var bottomDivider: Divider?

    var fromScheme: Boolean

    var disableClick: Boolean

    // 【qnFeeds】：
    var isDisliked: Boolean                 // 是否被负反馈删除过
    var channelShowType: Int                // 频道双列 408
    var referenceArticlePos: Int            // 当前文章在引用文章列表中的位置
    var hotListIndex: String                // 当前文章在榜单中的

    // 【qnFeeds】长视频底层页简介 VIP Banner 日间图 URL（PicShowType=LONG_VIDEO_VIP_BANNER_INTRO）
    var longVideoIntroVipBannerImageUrl: String
    // 【qnFeeds】长视频底层页简介 VIP Banner 夜间图 URL（PicShowType=LONG_VIDEO_VIP_BANNER_INTRO）
    var longVideoIntroVipBannerNightImageUrl: String
    // 【qnFeeds】长视频底层页简介 VIP Banner 接口下发图片宽度，用于计算宽高比
    var longVideoIntroVipBannerWidth: Int
    // 【qnFeeds】长视频底层页简介 VIP Banner 接口下发图片高度，用于计算宽高比
    var longVideoIntroVipBannerHeight: Int
    // 【qnFeeds】长视频底层页简介 VIP Banner 跳转地址（优先来自 cep_rsp.value_added_content.ad_json_str[].schema）
    var longVideoIntroVipBannerScheme: String

    var isFlexList: Boolean                 // kmm重构后的flex列表（debug信息里展示使用）
    var isInsertedItem: Boolean             // 标识当前item是客户端插入内容，不属于服务端当前刷数据

    @KmmInternalApi
    var tmpIsRefreshNewData: Boolean        // 新一刷拉的数据（内部使用的临时标记，外部不要调用）


    // 【qnMedia】：
    var canSkipAudio: Boolean               // 是否能够跳过音频

    var isCacheAudio: Boolean             // 是否是缓存音频

    var lastAudioScene: RadioScene?       // 上次播放场景


    // 【qnAd】：
    var midInsertGameAd: String?            // 【图文】中插游戏广告
    var relateAd: IKmmAdFeedsItem?          // 【早晚报】合约广告
    var isFirstVideoAd: Boolean             // 【竖版视频】是否忽略挂卡广告频控
    var nativeAd: IKmmAdFeedsItem?          // 【竖版视频】竞价原生广告
    var hasCompanionAd: Boolean             // 是否有挂卡广告（loid105）
    var gameReportScene: Int                // 游戏上报场景，默认GameReportSceneId.OTHER


    // 【qnDetail】：
    // 标识当前卡片是否在Kuikly视图中 如：【脉络】
    var isInKuiklyView: Boolean
    // 标识当前 article 关联的脉络节点是否含人工摘要（manualSummary 非空）
    // 写入时机：脉络节点 cell 构造时（TimeLineDetailDataRepo.toTimelineBodyItem）；用于大同上报 article_bool_params 第30位
    var hasTimelineSummary: Boolean
    var isEventTimelineModuleItem: Boolean  // 是否是事件脉络模块展开的item（应该废弃，对应到picShowType）
    var canShowEventTimelineModuleItem: Boolean // （应该废弃，对应到picShowType）
    var embedInHotModule: Boolean           // 【专题】嵌入到热点精选里的专题
    var isComponent: Boolean                // 【专题】在宿主以一个组件而非页面的形式出现（例如：嵌入tabM），影响UI结构
    var isEventChannel: Boolean             // 【专题】tabM嵌入专题（大圣配置ext_info.scene=channel），影响请求参数
    var pageBusinessType: String?           // 【专题】结构化页面的 StructPageBusinessType
    var eventVoteId: String?                // 【专题】投票的ID（话题专题 && 投票场景）
    var pageSkinRes: PageSkinRes?           // 【专题】页面皮肤
    var isSectionHeader: Boolean            // 【专题】1. 目录导航的定位，是以Header为锚点的
    var isSectionFooter: Boolean            // 【专题】2. 页面有皮肤时，会给Header、Footer卡片加圆角
    var belongSection: NewsListSection?     // 【专题】文章所在的模块分区信息（模块头尾item会绑定）
    var sectionName: String                 // 【专题】模块分区标识（目录导航索引使用）

    var isFocusItem: Boolean                // 【免费专栏】是否是焦点文章，焦点文章需要高亮3s

    var detailModel: IDetailModelStub?      // 【图文底层页】详情数据

    // 【qnUser】：
    var enableCommentSkin: Boolean          // 专题下，是否开启‘网友热议’皮肤
    var enableOriginCommentTopDiv: Boolean  // 展示评论顶部分割线
    var isFakeComment: Boolean              // 本地构造的假评论
    var isOutOfStyle: Boolean              // 标识是否是过时文章
    var underlineItem: IUnderlineItem?      // 划线数据，用与tab4划线列表
    var scanTimestamp: String?              // 浏览历史时间戳
    var eventBusinessType: String?          // 收藏、点赞历史页下发的businessType，可用来判断是否为ip专题
    var eventId: String?                    // 收藏、点赞历史页下发的eventId，可用来判断是否为ip专题
    var inListType: ContextListType?        // 所在列表类型

    // 【云重排】
    var cloudRerankExposed: Boolean         // 标识当前卡片是否已经曝光，用于只替换仍未曝光的槽位
    var cloudRerankClicked: Boolean         // 标识当前卡片是否点击过
    var cloudRerankArticlePage: Int         // 从0开始，客户端记录。记录【原始】文章是第几刷下发下来的，其他业务不要使用
    var isCloudRerankNewItem: Boolean       // 标识当前卡片是否是云重排新item
    var recTraceId: String?                 // 推荐traceId，17xxx开头，对应header的Traceid字段，这个一般是给推荐查问题用

    // todo 【规范】ctxDto 新增本地字段，必须注释说明作用
}

interface IContextDtoHolder {
    var ctxDto: IContextDto
}

interface IContextDtoBinding {
    fun getContextDtoBindingTargets(): List<IContextDtoHolder>?
}