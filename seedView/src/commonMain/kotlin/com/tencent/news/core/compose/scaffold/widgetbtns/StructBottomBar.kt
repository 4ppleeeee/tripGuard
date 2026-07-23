package com.tencent.news.core.compose.scaffold.widgetbtns

import androidx.compose.runtime.Composable
import com.tencent.kuikly.compose.foundation.layout.Arrangement
import com.tencent.kuikly.compose.foundation.layout.Box
import com.tencent.kuikly.compose.foundation.layout.Column
import com.tencent.kuikly.compose.foundation.layout.Row
import com.tencent.kuikly.compose.foundation.layout.fillMaxSize
import com.tencent.kuikly.compose.foundation.layout.fillMaxWidth
import com.tencent.kuikly.compose.foundation.layout.height
import com.tencent.kuikly.compose.foundation.layout.wrapContentHeight
import com.tencent.kuikly.compose.ui.Alignment
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.layout.ContentScale
import com.tencent.kuikly.compose.ui.platform.LocalConfiguration
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
import com.tencent.news.core.page.model.BottomBarWidget
import com.tencent.news.core.service.ViewService
import com.tencent.news.core.compose.utils.ComposeUtils

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

            val adaptBottom = LocalStructPageViewModel.current
                ?.pageRootWidget?.pageConfig?.expandBottomSafeAreaForPage ?: false

            if (adaptBottom) {
                SpacerHeight(ComposeUtils.rememberSafeAreaBottomHeight())
            }
        }
    }
}