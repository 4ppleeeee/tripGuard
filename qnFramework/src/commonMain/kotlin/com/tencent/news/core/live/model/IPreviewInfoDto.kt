package com.tencent.news.core.live.model

import com.tencent.news.core.extension.IKmmKeep

/**
 * 预约信息 DTO 接口（跨平台暴露）
 */
interface IPreviewInfoDto : IKmmKeep {
    var idStr: Long           // 预约ID，如果没有预约设置，id为0
    var status: Int           // 创建预约状态 0未提交预约;1审核中;2审核通过;3审核未通过
    var orderStatus: Int      // 预约状态 1已预约;2未预约
    var leftUpdateTimes: Int  // 剩余修改和删除次数
    var liveTime: Long        // 秒级时间戳
    var title: String         // 标题
    var icon: String          // 封面
    var publishWeibo: Int     // 是否已发布动态, 0:未发布 1:已发布
    var orderNum: Long        // 预约人数
    var shareUrl: String      // H5分享页地址
}
