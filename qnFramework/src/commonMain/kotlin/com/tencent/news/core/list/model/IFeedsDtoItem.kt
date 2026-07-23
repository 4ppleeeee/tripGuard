package com.tencent.news.core.list.model

import com.tencent.news.core.audio.model.IAudioDto
import com.tencent.news.core.comment.model.IItemCommentDto
import com.tencent.news.core.event.model.IEventDto
import com.tencent.news.core.extension.ICmsModelDtoItemDoc
import com.tencent.news.core.live.model.ILiveDto
import com.tencent.news.core.pay.model.IPayDto
import com.tencent.news.core.qa.model.IItemQADto
import com.tencent.news.core.share.model.IItemShareDto
import com.tencent.news.core.tads.game.model.IGameDto
import com.tencent.news.core.tads.model.IAdDto
import com.tencent.news.core.tag.model.ITagDto
import com.tencent.news.core.user.model.IItemUserDto
import com.tencent.news.core.video.model.IItemVideoDto


/**
 * 降原始item数据结构，按业务模块分散为多个dto；
 * 建议：按业务场景需求，仅依赖用到的dto，后续会与接入层商定，对应场景/文章类型，只下发特定dto里的字段
 */
interface IFeedsDtoItem : ICmsModelDtoItemDoc {
    val baseDto: IBaseDto               // 基础信息（一般直接来源于总库）
    val moduleDto: IModuleDto           // 模块结构（item嵌套子item）
    val labelDto: ILabelDto             // 标题/左下角标签
    val uiDto: IUIDto                   // ui、交互控制

    val tagDto: ITagDto                 // tag/早晚报/724
    val eventDto: IEventDto             // 专题/事件
    val qaDto: IItemQADto               // 问答

    val userDto: IItemUserDto           // 用户/互动
    val commentDto: IItemCommentDto     // 评论
    val shareDto: IItemShareDto         // 分享
    val payDto: IPayDto                 // 付费

    val videoDto: IItemVideoDto         // 视频
    val liveDto: ILiveDto               // 直播
    val audioDto: IAudioDto             // 音频

    val traceDto: ITraceDto             // 算法/透传信息

    val adDto: IAdDto                   // 广告
    var gameDto: List<IGameDto>?        // 游戏相关：非后台直接下发，客户端绑定到这个dto里

    val reportDto: IItemReportDto       // 上报相关
    val aiDto: IAiDto                   // 文章带的一些AI相关的数据
    val testDto: ITestDto?              // 本地测试配置（非业务逻辑）
}