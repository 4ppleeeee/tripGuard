package com.tencent.news.core.list.model

import com.tencent.news.core.detail.model.IDetailAttribute
import com.tencent.news.core.extension.ICmsModelDoc
import com.tencent.news.core.list.api.IContextDtoHolder
import com.tencent.news.core.list.api.IExportModel
import com.tencent.news.core.list.api.IExposure
import com.tencent.news.core.list.controller.IFeedsItemValidator
import com.tencent.news.core.parcel.IKmmParcelable
import com.tencent.news.core.view.ILogicContextHolder
import com.tencent.news.core.vm.IFeedsVMItemStub

interface IFrameworkFeedsItem :
    IKmmIndexItem,          // 信息流业务统一的标记接口，目前无实际功能意义
    IFeedsIndexItem,        // 信息流框架依赖的数据实现
    IFeedsVMItemStub,       // 所有UI层的vm放这里
    ILogicContextHolder,    // 支持逻辑层数据绑定
    IExposure,              // 支持曝光排重
    IContextDtoHolder,      // 支持客户端本地参数绑定
    IFeedsItemValidator,    // 支持列表数据过滤
    IOriginJson,            // 解析后保留原始json
    ICmsModelDoc,
    IKmmParcelable,
    IDetailAttribute,
    IExportModel
