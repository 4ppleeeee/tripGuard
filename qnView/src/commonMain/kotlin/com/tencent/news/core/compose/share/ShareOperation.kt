package com.tencent.news.core.compose.share

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import com.tencent.kuikly.compose.foundation.background
import com.tencent.kuikly.compose.foundation.layout.Column
import com.tencent.kuikly.compose.foundation.layout.fillMaxWidth
import com.tencent.kuikly.compose.foundation.layout.wrapContentHeight
import com.tencent.kuikly.compose.foundation.lazy.LazyRow
import com.tencent.kuikly.compose.foundation.lazy.items
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.graphics.Color
import com.tencent.kuikly.compose.ui.graphics.painter.Painter
import com.tencent.news.core.app.constants.IconFont
import com.tencent.news.core.compose.scaffold.theme.QNTheme
import com.tencent.news.core.dt.constants.DtElementId

/**
 * 分享面板「操作按钮」数据模型。
 *
 * 用于 [ShareDialog] 的 operationButtons 行，承载诸如「赠送 / 收藏 / 夜间模式」
 * 等与具体业务相关的功能按钮。该行属于可选能力：未传入时不会渲染，避免影响
 * 现有调用方的视觉与行为。
 *
 * 设计取舍：
 * - 不复用 StructBtnWidget 体系：StructBtnWidget 是页面结构数据（来自后端下发或固定结构），
 *   而操作按钮是业务页面在 Compose 端就地组装的临时视图，没有持久化语义；
 *   且复用底部 bar 同名 widget 会导致 ViewModel 双实例订阅（如 FavoriteBtnWidgetViewModel）。
 * - 视觉直接复用 [ShareChannelItem]，与第一行渠道项保持完全一致的 layout、间距、字号。
 * - name/iconFont/icon/iconColor 设计为 @Composable getter，便于在 dialog 弹出后随 State
 *   变化驱动重组（如「收藏」点击后切换为「取消收藏」文案 + 黄色高亮图标）。
 *
 * @param id 唯一标识，用于 LazyRow 的 key
 * @param name 按钮文案 getter（@Composable）
 * @param iconFont 图标字体 getter（与 [icon] 二选一）（@Composable）
 * @param icon 图片资源 getter（与 [iconFont] 二选一）（@Composable）
 * @param iconColor iconFont 专属高亮颜色（如已收藏黄色）；返回 null 时走面板默认色
 * @param dtEid 数据上报元素 ID，传 null 时不上报
 * @param onClick 点击回调（弹窗 dismiss 时机由调用方在回调中决定）
 */
class ShareOperation(
    val id: String,
    val name: () -> String,
    val iconFont: () -> IconFont? = { null },
    val icon: () -> Painter? = { null },
    val iconColor: () -> Color? = { null },
    val dtEid: DtElementId? = null,
    val dismissDialogOnClick: Boolean = false,
    val onClick: () -> Unit,
)

/**
 * 通用分享按钮（widgetbtns/ShareBtn）从此 CompositionLocal 读取
 * 业务侧注入的「操作按钮」清单，并在弹出 [ShareDialog] 时透传给它。
 *
 * 默认 null，即不显示操作按钮行；业务页面（如专栏详情页）通过
 * [androidx.compose.runtime.CompositionLocalProvider] 注入即可生效。
 */
val LocalShareOperationButtons = staticCompositionLocalOf<List<ShareOperation>?> { null }

/**
 * 分享面板「操作按钮」行（可选）。
 *
 * 视觉直接复用 [ShareChannelItem]，与第一行渠道项保持完全一致。
 * 与渠道行的差异：这里不走 [ShareChannelViewModel.channel.isSupported] 过滤，
 * 显示与否完全由调用方决定。
 */
@Composable
internal fun ShareOperationRow(
    operations: List<ShareOperation>,
    modifier: Modifier = Modifier,
    backgroundColor: Color? = null,
    textColor: Color? = null,
    iconBgColor: Color? = null,
    onOperationClick: (ShareOperation) -> Unit = { it.onClick() },
) {
    if (operations.isEmpty()) return

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(backgroundColor ?: QNTheme.colorScheme.bgBlock)
    ) {
        LazyRow(Modifier.fillMaxWidth().wrapContentHeight()) {
            items(
                items = operations,
                key = { it.id }
            ) { op ->
                ShareChannelItem(
                    icon = op.icon(),
                    iconFont = op.iconFont(),
                    name = op.name(),
                    dtEid = op.dtEid,
                    textColor = textColor,
                    iconBgColor = iconBgColor,
                    iconFontColor = op.iconColor(),
                    onShareClick = { onOperationClick(op) },
                )
            }
        }
    }
}