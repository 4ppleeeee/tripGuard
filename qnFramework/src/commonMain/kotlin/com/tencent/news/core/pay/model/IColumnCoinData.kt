package com.tencent.news.core.pay.model

import com.tencent.news.core.extension.IKmmKeep

interface IColumnCoinData : IKmmKeep {
    var balance: Float                          // 账户代币余额
    val totalRechargeCoins: Int                 // 累计充值代币数量
    val coinsList: List<ICoinProduct>?          // 代币物品列表
    val orderCommonParam: IOrderCommonParam?
}