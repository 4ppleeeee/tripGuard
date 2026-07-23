package com.tencent.news.core.tads.tab2.vm

import com.tencent.news.core.extension.IVMDoc
import com.tencent.news.core.list.api.IExportModelData
import com.tencent.news.core.tads.tab2.config.AdVideoCardConfig
import com.tencent.news.core.tads.vm.VMHolder
import com.tencent.news.core.tads.vm.VMHolder2
import com.tencent.news.core.tads.vm.VMHolder3
import com.tencent.news.core.tads.vm.VMHolder4


typealias CardVH<VM> = VMHolder3<AdVideoCardConfig, VM>         // 可能返回空vm
typealias CardVH2<VM> = VMHolder4<AdVideoCardConfig, VM>        // 非空

// todo【架构说明】新增vm应遵循：(doc/【规范】模块化架构.md)
interface IAdVideoVMHolder : IExportModelData, IVMDoc {

    val trinityCard: VMHolder2<IAdVideoTrinityCardVM>           // 【旧】三段卡
    val templateCard: VMHolder2<IAdVideoTemplateCardVM>         // 【新】模板卡
    val nativeCard: VMHolder2<IAdVideoNativeCardVM>             // 【新】原生卡
    val companionViewCard: VMHolder<IAdVideoCompanionCardVM>    // 视频挂卡
    val finishCardVM: VMHolder<IAdVideoFinishCardVM>            // 视频 finish 卡

    // 客服：
    val consultMarqueeCard: CardVH<IAdConsultMarqueeVM>
    val consultBigCard: CardVH<IAdConsultBigCardVM>
    val consultButtonCard: CardVH<IAdConsultButtonVM>
    val consultLocationCard: CardVH<IAdConsultLocationCardVM>
    val consultNewBigCard: CardVH<IAdConsultNewBigCardVM>

    // 小游戏：
    val miniGameSmallCard: CardVH<IAdMiniGameSmallCardVM>
    val miniGameMiddleCard: CardVH<IAdMiniGameMiddleCardVM>
    val miniGameBigCard: CardVH<IAdMiniGameBigCardVM>
    
    // 小游戏新原生卡
    val miniGameNormalCard: CardVH<IAdMiniGameNormalCardVM>     // 第一阶段
    val miniGameActionCard: CardVH<IAdMiniGameActionCardVM>     // 第二三阶段
    val miniGameIntroBigCard: CardVH<IAdMiniGameIntroBigCardVM> // 三元组信息大卡

    // 直播小店
    val liveShopData: VMHolder2<IAdLiveShopDataVM>
    val liveShopSmallCard: CardVH2<IAdLiveShopSmallCardVM>
    val liveShopBigCard: CardVH<IAdLiveShopBigCardVM>
    val liveShopNewSmallCard: CardVH2<IAdProductSmallCardVM>
    val liveShopNewBigCard: CardVH<IAdLiveShopBigCardVM>

    // 直播原生卡
    val liveStreamNormalCard: CardVH<IAdLiveStreamNormalCardVM>     // 直播第一阶段：固定icon
    val liveStreamActionCard: CardVH<IAdLiveStreamActionCardVM>     // 直播第二三阶段：商品/无商品 + 按钮高亮

    // 微信小店
    val weChatStoreSmallCard: CardVH2<IAdWeChatStoreSmallCardVM>
    val weChatStoreBigCard: CardVH<IAdWeChatStoreBigCardVM>
    val weChatStoreNewSmallCard: CardVH2<IAdWeChatStoreSmallCardVM>
    val weChatStoreNewBigCard: CardVH<IAdWeChatStoreBigCardVM>

    val defaultBigCard: CardVH2<IAdDefaultBigCardVM>            // 默认大卡
    val defaultBigCardV2: CardVH2<IAdDefaultBigCardV2VM>               // 默认大卡V2
    val titleCard: CardVH<IAdVideoTitleCardVM>                  // 标题组件
    val appChannelInfo: CardVH<IAdAppChannelInfoVM>             // 下载十要素
    val videoHotClickArea: VMHolder<IAdVideoHotClickAreaVM>     // 半屏点击热区
    val fullScreenPage: VMHolder2<IAdVideoFullScreenPageVM>        // 全屏页面 VM
    val flickerButtonCard: CardVH<IAdVideoFlickerButtonCardVM>  // 大行动按钮（带闪光）

    val miniCard: CardVH<IAdMiniCardPendantVM>                  // 图文组件
    
    // 短剧
    val shortDramaSmallCard: CardVH<IAdShortDramaSmallCardVM>   // 短剧小卡
    val shortDramaActionCard: CardVH<IAdShortDramaActionCardVM> // 短剧行动卡
    val shortDramaActionCardV2: CardVH<IAdShortDramaActionCardV2VM> // 短剧行动卡V2
    val shortDramaBigCard: CardVH<IAdShortDramaBigCardVM>       // 短剧三段卡大卡

    // 旅游原生卡
    val travelNormalCard: CardVH<IAdTravelNormalCardVM>             // 旅游第一阶段：固定icon + 引导文案
    val travelActionCard: CardVH<IAdTravelActionCardVM>             // 旅游第二三阶段：行动按钮高亮

    // 微信小店原生卡
    val wechatStoreNormalCard: CardVH<IAdWechatStoreNormalCardVM>   // 微信小店第一阶段：固定icon + 引导文案
    val wechatStoreActionCard: CardVH<IAdWechatStoreActionCardVM>   // 微信小店第二三阶段：商品信息 + 行动按钮高亮

    // 原生行业小卡
    val novelCard: CardVH<IAdNovelCardVM>                            // 小说-按钮高亮小卡
    val toolsCard: CardVH<IAdToolsCardVM>                            // 工具-第一二阶段，按钮高亮小卡
    val toolsBigCard: CardVH<IAdToolsBigCardVM>                      // 工具-第三阶段，大卡样式
    val educationCard: CardVH<IAdEducationCardVM>                    // 教育-按钮高亮小卡
    val downloadIndustrySmallCard: CardVH<IAdDownloadIndustrySmallCardVM> // 下载行业-一/二段小卡
    val downloadIndustryBigCard: CardVH<IAdDownloadIndustryBigCardVM>     // 下载行业-第三段大卡

    // 电商通用
    val ecommerceGeneralSmallCard: CardVH<IAdEcommerceGeneralSmallCardVM>  // 电商通用-小卡：icon + 文案 + 倒计时
    val ecommerceGeneralBigCard: CardVH<IAdEcommerceGeneralBigCardVM>     // 电商通用-大卡：头像 + 标题 + 券面 + 倒计时

    // 618 小店券
    val shop618CouponSmallCard: CardVH<IAdShop618CouponSmallCardVM>       // 618 小店券-小卡：券文案 + 倒计时
    val shop618CouponBigCard: CardVH<IAdShop618CouponBigCardVM>           // 618 小店券-大卡：白色券包

}
