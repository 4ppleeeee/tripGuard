package com.tencent.news.core.tads.model.interact

import com.tencent.news.core.extension.IAdOrderDtoDoc
import com.tencent.news.core.tads.constants.AdDisplayCode
import com.tencent.news.core.tads.model.IAdDynamicInfo
import com.tencent.news.core.tads.model.IAdMiniCardComponent
import com.tencent.news.core.tads.model.IAdVideoTaskInfo
import com.tencent.news.core.tads.model.IKmmAdAnyCounselDto


interface IAdInteractDto : IAdOrderDtoDoc {

    val adInteractType: Int         // 挂件样式 @AdInteractType
    val displayCode: String         // 互动样式（原始字符串）
    val adDisplayCode: AdDisplayCode // 互动样式（枚举类型）
    val richMediaId: String     // 蒙太奇模板id，后续要废弃

    val adVideoGameDto: IAdVideoGameDto             // 视频 游戏礼包信息
    val brokenCreativeInfo: IAdBrokenCreativeInfo?  // 破窗素材
    val adAnyCounselDto: IKmmAdAnyCounselDto?       // 大家问,咨询
    val dynamicInfo: IAdDynamicInfo?                // 动态化模板信息
    val bulletList: List<String>?                   // 弹幕信息
    val miniCard: IAdMiniCardComponent?             // 图文挂件
    val adVideoTask: IAdVideoTaskInfo?              // 竖版视频广告任务信息
    val olympicADDuration: Int                      // 广告时长，目前用于奥运挂件的展示总时长

    var crossInteractive: IAdCrossInteractive?      // 轻互动效果配置（扭/划/摇等）

}

object AdInteractType {
    const val NONE = 0

    // todo 注意：
    //  1. 序号代表优先级（数字越大，优先级越高）
    //  2. 序号值轻易不要改，这个值也用于shiply配置挂件屏蔽（ad_slop_guide_black_list）

    @Deprecated("废弃")
    const val ANY_COUNSEL = 990     // 线索行业挂件（微信客服咨询、问答组件、转化数据）

    @Deprecated("废弃")
    const val GAME_PENDENT = 981    // 游戏礼包挂件：信息流

    @Deprecated("废弃")
    const val GAME_PACK_TAB2 = 980  // 游戏礼包挂件：竖版视频

    const val BROKEN_VIDEO = 970    // 3D破窗
    const val BULLET = 960          // 弹幕

}