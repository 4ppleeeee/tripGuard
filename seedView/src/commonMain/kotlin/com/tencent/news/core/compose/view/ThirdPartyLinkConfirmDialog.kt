package com.tencent.news.core.compose.view

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.tencent.kuikly.compose.foundation.background
import com.tencent.kuikly.compose.foundation.clickable
import com.tencent.kuikly.compose.foundation.layout.Arrangement
import com.tencent.kuikly.compose.foundation.layout.Box
import com.tencent.kuikly.compose.foundation.layout.Column
import com.tencent.kuikly.compose.foundation.layout.Row
import com.tencent.kuikly.compose.foundation.layout.fillMaxSize
import com.tencent.kuikly.compose.foundation.layout.fillMaxWidth
import com.tencent.kuikly.compose.foundation.layout.fillMaxHeight
import com.tencent.kuikly.compose.foundation.layout.height
import com.tencent.kuikly.compose.foundation.layout.padding
import com.tencent.kuikly.compose.foundation.layout.width
import com.tencent.kuikly.compose.foundation.layout.wrapContentHeight
import com.tencent.kuikly.compose.foundation.shape.RoundedCornerShape
import com.tencent.kuikly.compose.ui.Alignment
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.draw.clip
import com.tencent.kuikly.compose.ui.graphics.Color
import com.tencent.kuikly.compose.ui.text.TextStyle
import com.tencent.kuikly.compose.ui.text.font.FontWeight
import com.tencent.kuikly.compose.ui.text.style.TextAlign
import com.tencent.kuikly.compose.ui.unit.dp
import com.tencent.kuikly.compose.ui.unit.sp
import com.tencent.kuikly.compose_dsl.kuikly.extension.setEvent
import com.tencent.news.core.app.constants.QnIconFont
import com.tencent.news.core.compose.scaffold.theme.QNTheme
import com.tencent.news.core.compose.platform.QnIconFont
import com.tencent.news.core.compose.scaffold.theme.LightColorScheme
import com.tencent.news.core.compose.view.dialog.DialogController
import com.tencent.news.core.compose.view.dialog.DialogShowType
import com.tencent.news.core.compose.view.dialog.IDialog
import kotlinx.coroutines.CoroutineScope

class ThirdPartyLinkConfirmDialog(
    private val onConfirm: (skipNext: Boolean) -> Unit,
    private val onCancel: () -> Unit = {},
    private val initialSkipConfirm: Boolean = false,
    override val safeAreaBackgroundColorProvider: (@Composable () -> Color)? = null
) : IDialog() {

    override val showType: DialogShowType = DialogShowType.Center

    override val content: @Composable (pageScope: CoroutineScope, controller: DialogController) -> Unit = { scope, controller ->
        ThirdPartyLinkConfirmContent(
            initialSkipConfirm = initialSkipConfirm,
            onConfirm = { skip ->
                onConfirm(skip)
                controller.dismissDialog(this@ThirdPartyLinkConfirmDialog)
            },
            onCancel = {
                onCancel()
                controller.dismissDialog(this@ThirdPartyLinkConfirmDialog)
            }
        )
    }
}

@Composable
private fun ThirdPartyLinkConfirmContent(
    initialSkipConfirm: Boolean,
    onConfirm: (skipNext: Boolean) -> Unit,
    onCancel: () -> Unit
) {
    var isNoMoreReminder by remember { mutableStateOf(initialSkipConfirm) }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(horizontal = 40.dp)
                .background(
                    color = QNTheme.colorScheme.bgPage,
                    shape = RoundedCornerShape(12.dp)
                )
                .clip(RoundedCornerShape(12.dp))
                .clickable { }
                .setEvent("click") {

                },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            QnText(
                text = "您即将离开腾讯新闻，跳转到第三方网站",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp, start = 20.dp, end = 20.dp),
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                lineHeight = 26f,
                color = QNTheme.colorScheme.t1,
                textAlign = TextAlign.Center
            )

            QnText(
                text = "腾讯新闻出于为您提供便利的目的向您提供第三方链接，我们不对第三方网站的内容负责，请您审慎访问，保护好您的信息及财产安全。",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, start = 20.dp, end = 20.dp),
                fontSize = 16.sp,
                color = QNTheme.colorScheme.t2,
                lineHeight = 26f
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp, start = 20.dp, end = 20.dp)
                    .clickable {
                        isNoMoreReminder = !isNoMoreReminder
                    },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                QnIconFont(
                    name = if (isNoMoreReminder) QnIconFont.XW_GOU_24 else QnIconFont.XW_DAN_XUAN,
                    textStyle = TextStyle(
                        color = if (isNoMoreReminder) QNTheme.colorScheme.t2 else QNTheme.colorScheme.t3,
                        fontSize = 16.sp
                    )
                )

                QnText(
                    text = "下次不再提示",
                    modifier = Modifier.padding(start = 8.dp),
                    fontSize = 14.sp,
                    color = QNTheme.colorScheme.t2
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp)
                    .clip(RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp))
                    .background(QNTheme.colorScheme.bgPage)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(0.5.dp)
                        .background(QNTheme.colorScheme.lineFine)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickable { onCancel() }
                            .background(QNTheme.colorScheme.bgPage),
                        contentAlignment = Alignment.Center
                    ) {
                        QnText(
                            text = "取消",
                            color = QNTheme.colorScheme.t1,
                            fontSize = 16.sp
                        )
                    }
                    Box(
                        modifier = Modifier
                            .width(0.5.dp)
                            .fillMaxHeight()
                            .background(QNTheme.colorScheme.lineFine)
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickable {
                                onConfirm(isNoMoreReminder)
                            }
                            .background(QNTheme.colorScheme.bNormal),
                        contentAlignment = Alignment.Center
                    ) {
                        QnText(
                            text = "继续访问",
                            color = LightColorScheme.t4,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }
    }
}
