package com.tencent.news.core.user.model

import com.tencent.news.core.extension.getFirstNonNullString
import com.tencent.news.core.parcel.IKmmParcelable


// 用户基础信息
interface IUserInfoBaseDto : IKmmParcelable {

    // 用户id：关注等相关操作都以这个为主，按规范后续应该最优先用suid； 这个方法是为了兼容历史遗留情况
    fun getUserFocusId(): String = getFirstNonNullString(suid, chlid, mediaId)
    // 端内大部分使用Nick都是要先取om用户昵称，再取普通用户昵称
    val nickName: String get() = getFirstNonNullString(chlname, nick)

    var suid: String        // 接入层生成的对用户的唯一ID，包括登录与未登录用户，逐渐替代uid
    var chlid: String       // 企鹅号的omid
    var mediaId: String     // 企鹅号的omid
    var uid: String         // 接入层生成的对用户的唯一ID
    var openId: String      // 用户的openid

    var isAigc: Int         // ai 标识  1--新闻妹 2--较真 3--元宝 4...可扩展
    var isAi: Boolean       // 是否是新闻妹
    var isSeriousAi: Boolean       // 是否是较真AI
    var isYuanbaoAi: Boolean       // 是否是元宝AI
    // 注意确认使用nick还是nickName, 大部分应该使用nickName
    var nick: String
    // 0:普通用户 1:企鹅号 2:达人 3:机构
    val vipType: Int
    var vipIcon: String
    var vipIconNight: String
    var vipPlace: String
    var vipDesc: String
    var vipTypeNew: Int
    var vipSiteIntroduction: String?
    val isOpenPaymentColumn: Boolean // 用户是否开通专栏

    // OM用户对应的昵称
    var chlname: String
    // 头像下方的简介
    var desc: String
}