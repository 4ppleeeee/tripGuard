package com.tencent.news.core.compose.scaffold.widgetbtns

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import com.tencent.kuikly.compose.foundation.clickable
import com.tencent.kuikly.compose.foundation.layout.Column
import com.tencent.kuikly.compose.foundation.layout.padding
import com.tencent.kuikly.compose.ui.Alignment
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.text.TextStyle
import com.tencent.kuikly.compose.ui.text.font.FontWeight
import com.tencent.kuikly.compose.ui.unit.dp
import com.tencent.kuikly.compose.ui.unit.sp
import com.tencent.news.core.app.LocalKmmContext
import com.tencent.news.core.app.constants.IconFont
import com.tencent.news.core.compose.platform.QnIconFont
import com.tencent.news.core.compose.platform.fsp
import com.tencent.news.core.compose.scaffold.registry.LocalHeaderCollapseStatus
import com.tencent.news.core.platform.api.appRouter
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.launch

@Composable
fun BackBtn(
    backCallback: Runnable? = null,
    ignoreTextScale: Boolean = false,
    iconSize: Float = DEFAULT_BACK_BTN_ICON_SIZE,
    fontWeight: Int = FontWeight.Normal.weight
) {
    val scope = rememberCoroutineScope()

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable {
                if (backCallback != null) {
                    backCallback.run()
                    return@clickable
                }
                val context = LocalKmmContext
                scope.launch {
                    appRouter().goBack(context)
                }
            },
    ) {

        val isHeaderCollapsed by LocalHeaderCollapseStatus.current


        QnIconFont(
            modifier = Modifier.padding(3.dp),
            name = IconFont.BACKR_REGULAR,
            textStyle = TextStyle(
                color = getTitleBarWidgetColor(
                    isHeaderCollapsed = isHeaderCollapsed,
                    defaultColor = currentTitleBarTheme.widgetFgColor
                ),
                fontWeight = FontWeight(fontWeight),
                fontSize = if (ignoreTextScale) iconSize.fsp else iconSize.sp
            )
        )
    }
}

private const val DEFAULT_BACK_BTN_ICON_SIZE = 24f