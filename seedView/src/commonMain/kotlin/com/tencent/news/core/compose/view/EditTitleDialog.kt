package com.tencent.news.core.compose.view

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.tencent.kuikly.compose.foundation.background
import com.tencent.kuikly.compose.foundation.layout.Arrangement
import com.tencent.kuikly.compose.foundation.layout.Box
import com.tencent.kuikly.compose.foundation.layout.Column
import com.tencent.kuikly.compose.foundation.layout.Row
import com.tencent.kuikly.compose.foundation.layout.Spacer
import com.tencent.kuikly.compose.foundation.layout.fillMaxHeight
import com.tencent.kuikly.compose.foundation.layout.fillMaxSize
import com.tencent.kuikly.compose.foundation.layout.fillMaxWidth
import com.tencent.kuikly.compose.foundation.layout.height
import com.tencent.kuikly.compose.foundation.layout.padding
import com.tencent.kuikly.compose.foundation.layout.width
import com.tencent.kuikly.compose.foundation.layout.wrapContentHeight
import com.tencent.kuikly.compose.foundation.shape.RoundedCornerShape
import com.tencent.kuikly.compose.material3.Surface
import com.tencent.kuikly.compose.material3.TextButton
import com.tencent.kuikly.compose.ui.Alignment
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.draw.clip
import com.tencent.kuikly.compose.ui.focus.FocusRequester
import com.tencent.kuikly.compose.ui.text.TextStyle
import com.tencent.kuikly.compose.ui.unit.dp
import com.tencent.kuikly.compose.ui.unit.sp
import com.tencent.news.core.compose.scaffold.modifiers.TextField
import com.tencent.news.core.compose.scaffold.modifiers.backgroundColor
import com.tencent.kuikly.compose_dsl.kuikly.extension.lineSpacing
import com.tencent.news.core.compose.scaffold.modifiers.margin
import com.tencent.news.core.compose.scaffold.theme.QNTheme
import com.tencent.news.core.compose.view.dialog.DialogController
import com.tencent.news.core.compose.view.dialog.DialogShowType
import com.tencent.news.core.compose.view.dialog.IDialog
import kotlinx.coroutines.CoroutineScope

class EditTitleDialog(
    private val dialogTitle: String,
    private val initialTitle: String,
    private val editHint: String = "",
    private val onConfirm: (String) -> Boolean,
) : IDialog() {

    override val showType: DialogShowType = DialogShowType.FullScreen

    override val content: @Composable (pageScope: CoroutineScope, controller: DialogController) -> Unit
        get() = { _, controller ->

            val focusRequester = remember { FocusRequester() }


            var title by remember { mutableStateOf(initialTitle) }
            Box(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .background(QNTheme.colorScheme.bgPage)
                        .width(266.dp)
                        .wrapContentHeight()
                        .clip(QNTheme.shape.small)

                ) {
                    // ... 标题部分相同 ...
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(18.dp)
                    ) {
                        QnText(
                            modifier = Modifier.align(Alignment.Center).lineSpacing(3f),
                            text = dialogTitle,
                            fontSize = 16.sp,
                            color = QNTheme.colorScheme.t1,
                        )
                    }

                    // 可编辑的输入框
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 18.dp),
                        shape = RoundedCornerShape(8.dp),
                        color = QNTheme.colorScheme.bgBlock
                    ) {
                        TextField(
                            value = title,
                            placeholderColor = QNTheme.colorScheme.t3,
                            focusRequester = focusRequester,
                            maxLines = 1,
                            placeholder = editHint,
                            onValueChange = {
                                title = it
                            },
                            modifier = Modifier.padding(12.dp).fillMaxWidth(),
                            textStyle = TextStyle(fontSize = 14.sp, color = QNTheme.colorScheme.t1),
                        )
                    }

                    Spacer(
                        modifier = Modifier.height(0.5.dp).fillMaxWidth()
                            .margin(top = 18.dp)
                            .backgroundColor(QNTheme.colorScheme.lineFine)
                    )

                    // 按钮行保持不变，但传递新标题
                    Row(
                        modifier = Modifier.fillMaxWidth().height(46.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TextButton(
                            onClick = { controller.dismissDialog(this@EditTitleDialog) },
                            modifier = Modifier.weight(1f)
                        ) {
                            QnText(
                                text = "取消",
                                fontSize = 16.sp,
                                color = QNTheme.colorScheme.t2
                            )

                        }
                        Spacer(
                            modifier = Modifier.width(0.5.dp).fillMaxHeight()
                                .backgroundColor(QNTheme.colorScheme.lineFine)
                        )

                        TextButton(
                            onClick = {
                                if (onConfirm(title)) {
                                    controller.dismissDialog(this@EditTitleDialog)
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            QnText(
                                text = "完成",
                                fontSize = 16.sp,
                                color = QNTheme.colorScheme.bNormal
                            )
                        }
                    }
                }
            }
        }
}