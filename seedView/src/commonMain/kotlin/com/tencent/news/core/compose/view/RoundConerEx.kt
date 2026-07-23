package com.tencent.news.core.compose.view

import com.tencent.kuikly.compose.foundation.shape.RoundedCornerShape
import com.tencent.kuikly.compose.ui.unit.dp


// 【全端圆角统一】从7160版本开始，圆角收敛为以下三档： 2/4/8, 后续涉及到圆角， 都让设计师按新定的几档调整
//  https://tapd.woa.com/newsapp/prong/stories/view/1010045201877148385
fun noCorner(): RoundedCornerShape = RoundedCornerShape(0.dp)
fun smallConer(): RoundedCornerShape = RoundedCornerShape(2.dp)
fun normalConer(): RoundedCornerShape = RoundedCornerShape(4.dp)
fun bigConer(): RoundedCornerShape = RoundedCornerShape(8.dp)
fun roundConer(): RoundedCornerShape = RoundedCornerShape(200.dp) // 整体都是圆角，这里用一个比较大的值实现