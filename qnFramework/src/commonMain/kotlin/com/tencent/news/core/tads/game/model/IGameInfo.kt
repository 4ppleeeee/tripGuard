package com.tencent.news.core.tads.game.model

import com.tencent.news.core.extension.IKmmKeep
import com.tencent.news.core.tads.model.IAppChannelInfo
import com.tencent.news.core.tads.model.interact.IGameSpecialActivityInfos
import com.tencent.news.core.vm.IGameItemVMStub
import kotlinx.serialization.Transient


// 【重要】游戏信息

interface IGameInfo : IKmmKeep, IGamePageTypeHolder {

    @Transient
    var enableCloudGame: Boolean

    @Transient
    var moduleType: Int   // 游戏所在模块的 @GameModuleType

    val reflowInfo: IGameReflowInfo

    val oid: String

    var newsChannel: String

    val iconUrl: String         // 图标
    val gameId: String          // 游戏id
    val gameName: String        // 游戏名
    val gameDesc: String        // 描述
    val gameScore: Float        // 游戏评分（小数的）
    val valueLabel: String      // 数值标签
    val labels: List<String>?   // 标签

    val appChannelInfo: IAppChannelInfo?    // 下载‘十要素’

    val scheme: String                      // 用于拉起app
    val downloadInfo: IGameDownloadInfo?    // 下载信息
    val editGameInfo: IGameEditJson?
    val gameReserveStatus: String           // 游戏的预约状态
    val canShowGameReserve: Boolean         // 是否展示‘游戏预约’
    var isUserReserved: Boolean             // 用户是否预约此游戏
    val reserveInfo: IGameReserveInfo?      // 游戏的预约信息

    val canShowCloudGame: Boolean           // 是否展示‘云游戏’

    val giftTotalNum: Int
    val giftInfoList: List<IGameGiftInfo>?

    val gamePicture: List<IGamePicture>?

    var gameRankingIndex: Int    // 游戏排序，从1开始（在下发的game_list中的顺序）

    val gameItemVM: IGameItemVMStub?

    val activityInfos: List<IGameSpecialActivityInfos>?

    val calendarCard: IGameCalendarCard?    // 游戏日历卡片

    var reportExposeActId: Int

    var reportClickActId: Int
}
