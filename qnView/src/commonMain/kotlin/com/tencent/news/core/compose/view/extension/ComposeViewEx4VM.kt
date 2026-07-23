package com.tencent.news.core.compose.view.extension

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.tencent.kuikly.compose.foundation.clickable
import com.tencent.kuikly.compose.foundation.layout.padding
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.geometry.Offset
import com.tencent.kuikly.compose.ui.input.pointer.PointerEventType
import com.tencent.kuikly.compose.ui.input.pointer.pointerInput
import com.tencent.kuikly.compose.ui.semantics.Role
import com.tencent.kuikly.compose.ui.unit.dp
import com.tencent.kuikly.compose_dsl.kuikly.extension.setEvent
import com.tencent.news.core.isHarmonyPlatform
import com.tencent.news.core.isIOSPlatform
import com.tencent.news.core.list.trace.ComposeViewLog
import com.tencent.news.core.list.vm.ClickAction
import com.tencent.news.core.list.vm.IClickVM
import com.tencent.news.core.list.vm.createValidAction
import com.tencent.news.core.list.vm.runAll

@Composable
fun Modifier.setClickVM(
    vm: IClickVM?,
    replaceClickAction: ClickAction? = null,
    preciseClick: Boolean = true,
    beforeClickAction: (() -> Unit)? = null,
    afterClickAction: (() -> Unit)? = null
): Modifier {
    val clickAction = vm.createValidAction(replaceClickAction = replaceClickAction)
        ?: return this

    val onClick = {
        beforeClickAction?.invoke()
        vm.runAll(clickAction)
        afterClickAction?.invoke()
    }

    return if (preciseClick) {
        preciseClickable {
            onClick.invoke()
        }
    } else {
        clickable {
            onClick.invoke()
        }
    }
}

// 【坑】点击误触保护措施：
// compose 目前的手势事件，如果 Press 和 Release 派发到同一个view上，中途没被别的view抢到，则会触发clickable；
// （在pager滑动到边界、或者横向滑动list时，都能复现该问题）
//
// 【改法】：监听手势事件，如果手指移动>20dp，或被别人抢走事件，不触发点击
//  阈值能力已经下沉到kuikly侧，这里仅处理透传点击错误
@Composable
fun Modifier.preciseClickable(
    enabled: Boolean = true,
    onClickLabel: String? = null,
    role: Role? = null,
    onClick: () -> Unit
): Modifier {

    return clickable(
        enabled = enabled,
        onClickLabel = onClickLabel,
        role = role
    ) {
        onClick()
    }.compatPenetrateClickEvent() // 兼容iOS的手势点击错误，这里加一个空实现，让手势不往业务侧传递
}

// 兼容：kuiklyView和宿主混用时，点击事件会穿透到宿主的问题
@Composable
fun Modifier.compatPenetrateClickEvent(): Modifier {
    return if (isIOSPlatform() || isHarmonyPlatform()) { // 鸿蒙和iOS一样处理
        this.setEvent("click") {
            // 兼容iOS的手势点击错误，这里加一个空实现，让手势不往业务侧传递
        }
    } else {
        this // 不要给安卓用这个，发现1.3.26版本起，会让安卓首次点击失效
    }
}

// fix 弹窗 和 LinkAnnotation.Clickable会出现点击穿透
@Composable
fun Modifier.dialogPenetrateClickEvent(need: Boolean = true): Modifier {
    return if (need) {
        this.setEvent("click") { }
    } else {
        this
    }
}

// 兼容鸿蒙平台文字垂直居中问题，需要添加 top padding
@Composable
fun Modifier.compatVerticalMargin(): Modifier {
    return if (isHarmonyPlatform()) {
        this.padding(top = 2.dp)
    } else {
        this
    }
}