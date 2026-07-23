package com.tencent.news.core.page.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


annotation class SearchBtnShowType {

    companion object {

        // 带灰色背景框的样式 https://universal-1258344701.shiply-cdn.qq.com/config_template/183/1712494325273/rc-upload-1712494310475-3.png
        const val CORNER_BG_STYLE = 2
    }

}

@Serializable
@SerialName(StructWidgetType.SEARCH_BTN)
open class SearchBtnWidget : StructBtnWidget<SearchBtnData>() {

    @Serializable(SearchBtnDataWrapperSerializer::class)
    override var data: SearchBtnData? = null

    override fun getWidgetType() = StructWidgetType.SEARCH_BTN
}

@Serializable
class SearchBtnData : StructBtnWidgetData()

class SearchBtnDataWrapperSerializer : DataWrapperSerializer<SearchBtnData>(
    StructWidgetType.SEARCH_BTN, SearchBtnData.serializer()
)

interface ISearchBtnWidgetViewModel : StructWidgetViewModel {
    var showBtnBg: Boolean
}