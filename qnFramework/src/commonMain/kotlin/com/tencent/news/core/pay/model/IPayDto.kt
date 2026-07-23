package com.tencent.news.core.pay.model

import com.tencent.news.core.extension.IDtoDoc
import com.tencent.news.core.extension.IEnumDoc
import com.tencent.news.core.hot.IBoostInfo
import com.tencent.news.core.parcel.IKmmParcelable
import com.tencent.news.core.tag.model.IKmmTagInfo
import com.tencent.news.core.user.model.IUserInfo

// 付费文章类型
object ArticleVipType : IEnumDoc {
    const val NONE = 0          // 非付费文章
    const val CP_MEMBER = 1     // 会员文章
    const val COLUMN = 2        // 专栏文章
    const val ALL = 999         // 全部会员，仅用于发送通知，检查更新，不做具体单个业务
}

// 远端下发的付费状态
object ArticleRemotePayState : IEnumDoc {
    const val UNKNOWN = 0    // 未知
    const val NOT_PAY = 1    // 未付费
    const val PAY = 2        // 已付费
    const val SINGLE_ARTICLE_PAY = 3    // 单篇文章已付费
}

// 付费文章阅读类型
object VipArticleReadableType : IEnumDoc {
    const val UNKNOWN = 0    // 未知
    const val PARTIAL = 1    // 部分试看
    const val FREE = 2       // 免费
    const val LOCK = 3       // 全部锁定，无试看
}

// 文章付费业务数据
interface IPayDto : IDtoDoc, IKmmParcelable {
    var paymentInfo: IPaymentInfo?
    var paymentMemberInfo: IPaymentMemberInfo?  // 付费会员信息
    var paymentColumnInfo: IPaymentColumnInfo?  // 付费专栏
    var boostInfo: IBoostInfo?                  // 加热开关
    val userVipRelateList: QnUserVipRelateList? // 用户VIP关联列表，只有图文底层页用

    val payCmsId: String                        // 付费文章id
    val payArticleType: String                  // 付费文章类型
    val articleVipType: Int                     // 付费类型
    val vipArticleReadableType: Int             // 付费文章阅读类型

    val articleRemotePayState: Int              // 远程下发的会员购买状态

    val payUserInfo: IUserInfo?                 // 作者信息
    var payTagInfo: IKmmTagInfo?                // 专栏tag

    val articleIsPay: Boolean                   // 文章是否付费

    fun isCPMemberCollection(): Boolean         // 是否为会员合集
}