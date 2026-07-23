package com.tencent.news.core.compose.scaffold

import androidx.compose.runtime.Composable
import com.tencent.kuikly.compose.foundation.background
import com.tencent.kuikly.compose.foundation.clickable
import com.tencent.kuikly.compose.foundation.layout.Box
import com.tencent.kuikly.compose.foundation.layout.Column
import com.tencent.kuikly.compose.foundation.layout.height
import com.tencent.kuikly.compose.foundation.layout.offset
import com.tencent.kuikly.compose.foundation.layout.padding
import com.tencent.kuikly.compose.foundation.layout.width
import com.tencent.kuikly.compose.ui.Alignment
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.draw.clip
import com.tencent.kuikly.compose.ui.graphics.Color
import com.tencent.kuikly.compose.ui.graphics.painter.Painter
import com.tencent.kuikly.compose.ui.unit.dp
import com.tencent.kuikly.compose.ui.unit.sp
import com.tencent.news.core.compose.scaffold.theme.ForceDarkTheme
import com.tencent.news.core.compose.scaffold.theme.QNTheme
import com.tencent.news.core.compose.scaffold.theme.QnColor
import com.tencent.news.core.compose.view.QnImage
import com.tencent.news.core.compose.view.QnText
import com.tencent.news.core.resources.Res
import com.tencent.news.core.view.setup.ViewServiceBridge
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch


@Composable
fun StructPageErrorView(
    modifier: Modifier = Modifier,
    pageScope: CoroutineScope,
    background: Color? = null,
    onRefresh: (suspend () -> Unit)? = null,
    forceDarkTheme: Boolean = false,
    errorImagePainter: Painter? = null,
) {
    val defaultError = ViewServiceBridge.impl.defaultErrorView()
    if (defaultError != null) {
        defaultError()
        return
    }

    val content: @Composable () -> Unit = {
        StructCenterLayerContainer(modifier.background(background ?: QnColor.bgPage)) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {

                QnImage(
                    painter = errorImagePainter ?: Res.drawable.no_network_placeholder,
                    contentDescription = null,
                    modifier = Modifier.width(160.dp).height(160.dp)
                )

                QnText(
                    text = "内容加载失败",
                    color = QNTheme.colorScheme.t3,
                    fontSize = 14.sp
                )

                if (onRefresh != null) {
                    Box(
                        modifier = Modifier
                            .offset(y = 28.dp)
                            .clickable { pageScope.launch { onRefresh() } }
                            .background(background ?: QNTheme.colorScheme.bLight)
                            .clip(QNTheme.shape.extraLarge)
                            .padding(45.dp, 10.dp, 45.dp, 10.dp)
                    ) {
                        QnText(
                            text = "重试",
                            color = QNTheme.colorScheme.bNormal,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }

    if (forceDarkTheme) {
        ForceDarkTheme {
            content()
        }
    } else {
        content()
    }
}