package com.tencent.news.core.compose.view

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import com.tencent.kuikly.compose.foundation.background
import com.tencent.kuikly.compose.foundation.clickable
import com.tencent.kuikly.compose.foundation.layout.Box
import com.tencent.kuikly.compose.foundation.layout.Column
import com.tencent.kuikly.compose.foundation.layout.fillMaxWidth
import com.tencent.kuikly.compose.foundation.layout.height
import com.tencent.kuikly.compose.foundation.layout.width
import com.tencent.kuikly.compose.foundation.shape.RoundedCornerShape
import com.tencent.kuikly.compose.ui.Alignment
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.draw.clip
import com.tencent.kuikly.compose.ui.platform.LocalConfiguration
import com.tencent.kuikly.compose.ui.text.font.FontWeight
import com.tencent.kuikly.compose.ui.unit.dp
import com.tencent.kuikly.compose.ui.unit.sp
import com.tencent.news.core.compose.scaffold.modifiers.Button
import com.tencent.news.core.compose.scaffold.modifiers.margin
import com.tencent.news.core.app.LocalKmmContext
import com.tencent.news.core.compose.scaffold.theme.QNTheme
import com.tencent.news.core.platform.api.appRouter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.tencent.news.core.compose.utils.ComposeUtils

/**
 * 法务同意协议弹窗
 */
@Composable
fun AgreementDialog(
    modifier: Modifier = Modifier,
    title: String = "请阅读并同意以下条款",
    linkName: String,
    link: String,
    agreeBtnText: String = "同意",
    disAgreeBtnText: String = "不同意",
    onAgree: () -> Unit,
    onDisagree: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val safeAreaInsetBottom = ComposeUtils.rememberSafeAreaBottomHeight()
    Column(
        modifier = modifier
            .background(QNTheme.colorScheme.bgBlock)
            .fillMaxWidth()
            .clip(
                RoundedCornerShape(
                    topStart = 8.dp,
                    topEnd = 8.dp,
                    bottomEnd = 0.dp,
                    bottomStart = 0.dp
                )
            )
            .clickable { }
    ) {
        QnText(
            text = title,
            modifier = Modifier
                .margin(start = 24.dp, top = 28.dp)
                .fillMaxWidth(),
            fontSize = 24.sp,
            color = QNTheme.colorScheme.t1,
            fontWeight = FontWeight.SemiBold
        )

        Box(
            modifier = Modifier
                .margin(start = 24.dp, top = 28.dp)
                .width(30.dp)
                .height(4.dp)
                .background(QNTheme.colorScheme.t1)
        )

        QnText(
            text = linkName,
            modifier = Modifier
                .margin(start = 24.dp, end = 24.dp, top = 24.dp)
                .fillMaxWidth()
                .clickable {
                    scope.launch(Dispatchers.Main) {
                        LocalKmmContext?.let { context ->
                            appRouter().to(
                                context,
                                link
                            )
                        }
                    }
                },
            fontSize = 16.sp,
            color = QNTheme.colorScheme.tlink
        )

        Button(
            onClick = onAgree,
            modifier = Modifier
                .margin(start = 24.dp, end = 24.dp, top = 24.dp)
                .background(QNTheme.colorScheme.bNormal)
                .fillMaxWidth()
                .height(48.dp)
                .clip(RoundedCornerShape(24.dp))
        ) {
            QnText(
                text = agreeBtnText,
                color = QNTheme.colorScheme.t4,
                fontSize = 16.sp
            )
        }

        Button(
            onClick = onDisagree,
            modifier = Modifier
                .margin(
                    start = 24.dp,
                    end = 24.dp,
                    top = 16.dp,
                    bottom = if (safeAreaInsetBottom > 0f) {
                        0.dp // iOS外层套了一个safeArea了，这里再加看着高了
                    } else {
                        16.dp
                    }
                )
                .align(Alignment.CenterHorizontally),
        ) {
            QnText(
                text = disAgreeBtnText,
                color = QNTheme.colorScheme.t2,
                fontSize = 14.sp
            )
        }
    }
}