package com.tencent.news.core.compose.scaffold.widgetbtns

import androidx.compose.runtime.Composable
import com.tencent.kuikly.compose.foundation.layout.Arrangement
import com.tencent.kuikly.compose.foundation.layout.Box
import com.tencent.kuikly.compose.foundation.layout.Column
import com.tencent.kuikly.compose.foundation.layout.Row
import com.tencent.kuikly.compose.foundation.layout.fillMaxSize
import com.tencent.kuikly.compose.foundation.layout.fillMaxWidth
import com.tencent.kuikly.compose.foundation.layout.height
import com.tencent.kuikly.compose.foundation.layout.padding
import com.tencent.kuikly.compose.foundation.layout.wrapContentHeight
import com.tencent.kuikly.compose.foundation.layout.wrapContentWidth
import com.tencent.kuikly.compose.ui.Alignment
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.layout.ContentScale
import com.tencent.kuikly.compose.ui.unit.dp
import com.tencent.news.core.compose.scaffold.modifiers.backgroundColor
import com.tencent.news.core.compose.scaffold.StructPageViewModelEx.pageRootWidget
import com.tencent.news.core.compose.scaffold.registry.LocalStructPageViewModel
import com.tencent.news.core.compose.scaffold.theme.FullWidthThinDivider
import com.tencent.news.core.compose.scaffold.theme.QnColor
import com.tencent.news.core.compose.scaffold.theme.QnSkin
import com.tencent.news.core.compose.view.QnImage
import com.tencent.news.core.compose.view.SpacerHeight
import com.tencent.news.core.extension.isFalseOrNull
import com.tencent.news.core.extension.isNotNullOrEmpty
import com.tencent.news.core.page.model.AskBtnWidget
import com.tencent.news.core.page.model.BottomBarWidget
import com.tencent.news.core.page.model.ColumnPayBtnWidget
import com.tencent.news.core.service.ViewService
import com.tencent.news.core.compose.platform.safeAreaHeight

@Composable
fun StructBottomBar(bottomWidget: BottomBarWidget?) {
    bottomWidget ?: return

    if (bottomWidget.ui?.hide.isFalseOrNull()) {
        Column(
            modifier = Modifier.fillMaxWidth().wrapContentHeight()
                .backgroundColor(
                    if (QnSkin?.barBgImage.isNullOrEmpty()) {
                        QnSkin?.barBgColor ?: QnColor.bgPage
                    } else {
                        QnColor.bgPage
                    }
                )
        ) {

            // topBar 区域（如赠送提示条）
            bottomWidget.topBar?.forEach { topBarWidget ->
                ViewService.btn.Build(topBarWidget)
            }

            Box(modifier = Modifier.height(62.dp).fillMaxWidth()) {
                if (QnSkin?.barBgImage.isNotNullOrEmpty()) {
                    QnSkin?.barBgImage?.let {
                        QnImage(
                            url = it,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.FillWidth
                        )
                    }
                }
                if (QnSkin == null) {
                    FullWidthThinDivider()
                }

                // 判断是否包含宽按钮（ColumnPayBtn / AskBtn），决定布局模式
                // 对应宿主端 StructActionBarHolder.getLayoutMode()：hasInputBtn()(含 AskBtn) || hasColumnPayBtn()
                //   有宽按钮 → HORIZONTAL_LIKE_DETAIL（宽按钮占左侧主空间，小图标在右侧）
                //   无 → HORIZONTAL_CENTER_SAME_GAP（等间距居中）
                val hasWideBtn = bottomWidget.btnList?.any {
                    it is ColumnPayBtnWidget || it is AskBtnWidget
                } == true

                if (hasWideBtn) {
                    // 类详情页布局：宽按钮(weight=1)在左，小图标按钮在右
                    Row(
                        modifier = Modifier.fillMaxSize()
                            .padding(horizontal = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        bottomWidget.btnList?.forEach { btn ->
                            if (btn is ColumnPayBtnWidget || btn is AskBtnWidget) {
                                // 宽按钮（购买 / 提问入口）占据剩余空间
                                Box(
                                    modifier = Modifier.weight(1f),
                                    contentAlignment = Alignment.Center
                                ) {
                                    ViewService.btn.Build(btn)
                                }
                            } else {
                                // 小图标按钮固定宽度
                                Box(
                                    modifier = Modifier.wrapContentWidth()
                                        .padding(horizontal = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    ViewService.btn.Build(btn)
                                }
                            }
                        }
                    }
                } else {
                    // 默认布局：等间距居中
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceEvenly,
                    ) {
                        bottomWidget.btnList?.forEach { btn ->
                            ViewService.btn.Build(btn)
                        }
                    }
                }
            }

            val adaptBottom = LocalStructPageViewModel.current
                ?.pageRootWidget?.pageConfig?.expandBottomSafeAreaForPage ?: false

            if (adaptBottom) {
                SpacerHeight(safeAreaHeight())
            }
        }
    }
}