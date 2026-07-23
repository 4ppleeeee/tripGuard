package com.tencent.news.core.live.model

import com.tencent.news.core.extension.ICmsModelDtoItemDoc

/**
 * 直播信息 DTO 接口
 * 用于在 KMM 和宿主平台间共享直播数据
 * 目前只提供基本框架，具体字段后续实现
 */
interface IKmmNewsRoomInfoDtoItem : ICmsModelDtoItemDoc {

    val baseDto: IKmmNewsRoomInfoBaseDto        // 基础信息
    val matchDto: IKmmNewsRoomInfoMatchDto      // 比赛信息
    val roomDto: IKmmNewsRoomInfoRoomDto        // 房间信息
    val shareDto: IKmmNewsRoomInfoShareDto      // 分享信息
    val playDto: IKmmNewsRoomInfoPlayDto        // 播放信息
    val anchorDto: IKmmNewsRoomInfoAnchorDto    // 主播信息
    val accessDto: IKmmNewsRoomInfoAccessDto    // 访问信息
    val previewInfo: IPreviewInfoDto?           // 预约信息（为了兼容宿主调用，这个命名和其他dto不同）

}
