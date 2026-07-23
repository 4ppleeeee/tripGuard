package com.tencent.news.core.tads.model.interact

import com.tencent.news.core.channel.constants.NewsChannel
import com.tencent.news.core.extension.IAdOrderDtoDoc
import com.tencent.news.core.extension.isNotNullOrEmpty
import com.tencent.news.core.tads.constants.AdLoid
import com.tencent.news.core.tads.model.IKmmAdOrder


interface IAdVideoGameDto : IAdOrderDtoDoc {

    /** 礼包码*/
    val appGameGiftCode: String

    /** 礼包描述*/
    val appGameGiftPackDesc: String

    /** 礼包icon*/
    val appGameGiftImageUrl: String

    /** 编译pass 透传到微信小游戏-path字段拼接*/
    val appGameGiftByPass: String

}

/** 有效的游戏礼包数据*/
fun IAdVideoGameDto.validAdGameGiftPackInfo(): Boolean {
    return this.appGameGiftPackDesc.isNotNullOrEmpty() && this.appGameGiftByPass.isNotNullOrEmpty()
}

fun IKmmAdOrder?.canShowAdGamePendent(): Boolean {
    val curAdIndex = this?.adIndex
        ?: return false
    // 二级视频频道
    if (curAdIndex.adChannel == NewsChannel.VIDEO_TOP && curAdIndex.loid == AdLoid.STREAM) {
        return false
    }
    // 西瓜流
    if (AdLoid.isVideoStreamLoid(curAdIndex.loid)) {
        return false
    }
    return true
}