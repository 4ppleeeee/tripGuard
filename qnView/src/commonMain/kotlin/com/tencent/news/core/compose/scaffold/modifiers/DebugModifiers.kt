package com.tencent.news.core.compose.scaffold.modifiers

import com.tencent.kuikly.compose.foundation.background
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.graphics.Color
import com.tencent.kuikly.compose.ui.unit.dp
import com.tencent.kuikly.core.base.BorderStyle
import com.tencent.news.core.platform.api.appStatus
import kotlin.random.Random

private const val debugUi = false

fun Modifier.debugBackground(color: Color): Modifier {
    return if (debugUi && appStatus().isDebug()) {
        this.background(color)
    } else {
        this
    }
}

fun Modifier.debugRandomBorder(): Modifier {
    return if (debugUi && appStatus().isDebug()) {
        this.border(
            Border(
                lineWidth = 1.dp,
                lineStyle = BorderStyle.DASHED,
                color = Color(
                    red = Random.nextInt(255),
                    green = Random.nextInt(255),
                    blue = Random.nextInt(255)
                ),
            )
        )
    } else {
        this
    }
}
