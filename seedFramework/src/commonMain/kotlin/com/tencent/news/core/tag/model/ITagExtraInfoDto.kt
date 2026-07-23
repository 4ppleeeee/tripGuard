package com.tencent.news.core.tag.model

import com.tencent.news.core.parcel.IKmmParcelable

interface ITagExtraInfoDto : IKmmParcelable {

    var insertContentId: String

    val showTimestamp: Long

    val lightIcon: String
    val darkIcon: String

    val postBgLightImage: String
    val postBgDarkImage: String
    val postXiaomiBgUrl: String

    val postBgLottie: String
    val postReadCompleteLottie: String
    val postReadProgressLottie: String

    val radioPlaceholder: String    // 早报tag 音频的默认文案

}