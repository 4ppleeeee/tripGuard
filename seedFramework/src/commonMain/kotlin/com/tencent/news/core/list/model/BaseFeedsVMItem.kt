package com.tencent.news.core.list.model

import com.tencent.news.core.extension.ResultEx
import com.tencent.news.core.list.api.DefaultContextDto
import com.tencent.news.core.list.api.IContextDtoBase
import com.tencent.news.core.list.constants.ExportModelType
import com.tencent.news.core.list.trace.getLogStr
import com.tencent.news.core.parcel.IKmmParcel
import com.tencent.news.core.view.LogicContext
import com.tencent.news.core.vm.IFeedsDtoItemStub
import kotlinx.serialization.Serializable

abstract class BaseFeedsVMItem : BaseExposureKmmModel(), IKmmFeedsItem, IFeedsDtoItemStub {

    override val logicContext by lazy { LogicContext() }

    override fun bindingContext(action: LogicContext.() -> Unit) {
        logicContext.action()
    }

    override var ctxDto: IContextDtoBase = DefaultContextDto()

    override fun isForbidInsert() = ResultEx(false)

    override var originJson: String = ""

    override fun writeToKmmParcel(dest: IKmmParcel) {
    }

    override fun readFromKmmParcel(from: IKmmParcel) {
    }

    override val exportModelType = ExportModelType.ITEM
    override val exportUniqueKey = ""

    override fun buildExportPrimitiveMap() = mapOf<String, Any>()

    override fun toString(): String = getLogStr()

}

@Serializable
class DefaultVMItem : BaseFeedsVMItem() {
    override val flexDto by lazy { DefaultFlexListAdapter("", "") }
}