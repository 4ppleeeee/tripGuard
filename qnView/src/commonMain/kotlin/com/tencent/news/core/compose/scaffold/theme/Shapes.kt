package com.tencent.news.core.compose.scaffold.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import com.tencent.kuikly.compose.foundation.shape.CircleShape
import com.tencent.kuikly.compose.foundation.shape.RoundedCornerShape
import com.tencent.kuikly.compose.ui.unit.dp
import com.tencent.news.core.compose.platform.fdp

val LocalShapes = staticCompositionLocalOf { Shapes() }

@Immutable
data class Shapes(
    val extraLarge: RoundedCornerShape = RoundedCornerShape(28.0.dp),
    val large: RoundedCornerShape = RoundedCornerShape(16.0.dp),
    val medium: RoundedCornerShape = RoundedCornerShape(12.0.dp),
    val small: RoundedCornerShape = RoundedCornerShape(8.0.dp),
    val extraSmall: RoundedCornerShape = RoundedCornerShape(4.0.dp),

    val circle: RoundedCornerShape = CircleShape,
    val rect: RoundedCornerShape = RoundedCornerShape(0.dp),

    val aigcSendBg: RoundedCornerShape = RoundedCornerShape(12.dp, 2.dp, 12.dp, 12.dp),
    val aigcReceiveBg: RoundedCornerShape = RoundedCornerShape(12.fdp, 12.fdp, 12.fdp, 12.fdp)

)
