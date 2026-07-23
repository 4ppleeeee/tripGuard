package com.tencent.news.core.compose.scaffold.modifiers

import com.tencent.news.core.dt.constants.IDtElementId

object QnViewDtElementIds {
    val ArticleCard: IDtElementId = QnViewDtElementId("em_item_article")
    val SharePanel: IDtElementId = QnViewDtElementId("em_more_panel")
    val ShareScreenPanel: IDtElementId = QnViewDtElementId("em_share_screenpanel")
    val ShareCardPanel: IDtElementId = QnViewDtElementId("em_more_cardpanel")
    val ShareSystem: IDtElementId = QnViewDtElementId("em_share_system")
    val SharePdf: IDtElementId = QnViewDtElementId("em_share_pdf")
    val ShareFriends: IDtElementId = QnViewDtElementId("em_share_friends")
    val ShareMoments: IDtElementId = QnViewDtElementId("em_share_moments")
    val ShareQQ: IDtElementId = QnViewDtElementId("em_share_qq")
    val ShareQZone: IDtElementId = QnViewDtElementId("em_share_qzone")
    val ShareSina: IDtElementId = QnViewDtElementId("em_share_sina_weibo")
    val ShareWorkWeixin: IDtElementId = QnViewDtElementId("em_share_enterprise_wechat")
    val ShareCopyLink: IDtElementId = QnViewDtElementId("em_share_copy_link")
    val SharePostCard: IDtElementId = QnViewDtElementId("em_share_cardpanel")
    val ShareScreenshot: IDtElementId = QnViewDtElementId("em_share_screenshot")
    val ShareSaveImage: IDtElementId = QnViewDtElementId("em_save_image")
    val ShareSaveVideo: IDtElementId = QnViewDtElementId("em_save_video")
    val EM_ITEM_NAV: IDtElementId = QnViewDtElementId("em_item_nav")
    val EM_ITEM_SUB_NAV: IDtElementId = QnViewDtElementId("em_item_subnav")
}

private data class QnViewDtElementId(
    override val id: String
) : IDtElementId
