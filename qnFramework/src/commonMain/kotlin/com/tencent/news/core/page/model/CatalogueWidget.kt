@file:Suppress("PropertyName")

package com.tencent.news.core.page.model

import com.tencent.news.core.extension.getNonNull
import com.tencent.news.core.extension.isNotNullOrEmpty
import com.tencent.news.core.extension.safeSize
import com.tencent.news.core.platform.api.getShiplySwitch
import com.tencent.news.core.service.FrameworkServiceBridge
import com.tencent.news.core.user.model.getValidUserInfo
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


annotation class CatalogueShowType {
    companion object {
        const val INDICATOR_STYLE = 1       // 带底部蓝色小条的样式（付费专栏在用）
    }
}

@Serializable
@SerialName(StructWidgetType.CATALOGUE)
class CatalogueWidget : StructWidget() { // 目前这个组件由本地构建，不是直接下发的

    @Serializable(CatalogueWidgetDataWrapperSerializer::class)
    var data: CatalogueWidgetData? = null

    var action: CatalogueWidgetAction = CatalogueWidgetAction()

    override fun getWidgetType() = StructWidgetType.CATALOGUE

    // 页面已构建完毕，能查找pageWidget的情况下使用（例如：buildPageWithManual 调用完之后才行）
    fun buildCatalogueData() {
        val pageWidget = findStructPageWidget() ?: return

        data = innerBuildCatalogueData(
            sectionList = pageWidget.getMainContentListWidgets(),
            pageTheme = pageWidget.getValidPageTheme()
        )
    }

    // 直接用section数据构建（可以在 buildPageWithManual 过程中使用）
    fun buildCatalogueData(sectionList: List<NewsListWidget>, pageTheme: StructPageTheme?) {
        data = innerBuildCatalogueData(sectionList, pageTheme)
    }

    fun isDataValid(): Boolean = data?.catalogueData.isNotNullOrEmpty()

    private fun innerBuildCatalogueData(
        sectionList: List<NewsListWidget>,
        pageTheme: StructPageTheme?
    ): CatalogueWidgetData? {
        // 若页面配置了皮肤，允许通过shiply隐藏导航栏，避免兼容问题
        if (pageTheme != null) {
            if (getShiplySwitch("event_detail_hide_channel_bar_with_skin")) {
                return null
            }
        }

        val result = arrayListOf<CatalogueSection>()

        sectionList.forEach { listWidget ->
            val section = listWidget.data?.section
                ?: return@forEach

            if (section.isCpSection() && !canShowCpSection(listWidget)) {
                return@forEach
            }

            if (listWidget.data?.newslist.isNullOrEmpty()) {
                return@forEach
            }

            if (section.name.isNotNullOrEmpty() && section.catalogueName.isNotNullOrEmpty()) {
                result.add(CatalogueSection().apply {
                    key = section.name
                    catalogueName = section.catalogueName
                    theme = pageTheme
                })
            }
        }

        // 目录<=2时不展示（另见：ThingCataloguePresenter 的逻辑）
        if (!action.catalogueNoCountLimit && result.safeSize() <= 2) {
            return null
        }

        return CatalogueWidgetData().apply {
            catalogueData = result
        }
    }

    private fun canShowCpSection(widget: NewsListWidget): Boolean {
        // 调整‘cp推荐模块’和其在导航目录中 标题展示的逻辑关系：
        // 1.cp推荐模块展示时：cp推荐模块标题在导航栏中同步展示
        // 2.cp推荐模块不展示时（没有配置、后台勾选不展示、未关注cp少于3个时等情况）：cp推荐模块标题在导航栏中不展示
        val canShowCp = widget.data?.newslist?.find { item ->
            val notFocusList = item.moduleDto.newsModule?.newsList
                ?.filter {
                    val suid = it.userDto.getValidUserInfo()?.baseDto?.suid.getNonNull()
                    !FrameworkServiceBridge.impl.isFollowUser(suid)
                }
            notFocusList.safeSize() >= 3 // 未关注cp>=3个时，才展示
        } != null

        return canShowCp
    }

}

class CatalogueWidgetDataWrapperSerializer : DataWrapperSerializer<CatalogueWidgetData>(
    StructWidgetType.CATALOGUE, CatalogueWidgetData.serializer()
)

@Serializable
class CatalogueWidgetData : StructWidgetData() {
    var catalogueData: List<CatalogueSection>? = null   // 目录数据
}

@Serializable
class CatalogueWidgetAction : StructWidgetAction() {
    var catalogueNoCountLimit = false                   // 强制显示目录，不受个数限制
    var showAtRight = false                             // true：选停在屏幕右侧样式
}