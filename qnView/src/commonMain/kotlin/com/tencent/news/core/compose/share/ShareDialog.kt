package com.tencent.news.core.compose.share

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.tencent.kuikly.compose.foundation.background
import com.tencent.kuikly.compose.foundation.clickable
import com.tencent.kuikly.compose.foundation.layout.Arrangement
import com.tencent.kuikly.compose.foundation.layout.Box
import com.tencent.kuikly.compose.foundation.layout.Column
import com.tencent.kuikly.compose.foundation.layout.absoluteOffset
import com.tencent.kuikly.compose.foundation.layout.fillMaxHeight
import com.tencent.kuikly.compose.foundation.layout.fillMaxSize
import com.tencent.kuikly.compose.foundation.layout.fillMaxWidth
import com.tencent.kuikly.compose.foundation.layout.heightIn
import com.tencent.kuikly.compose.foundation.layout.padding
import com.tencent.kuikly.compose.foundation.layout.size
import com.tencent.kuikly.compose.foundation.layout.wrapContentHeight
import com.tencent.kuikly.compose.foundation.layout.wrapContentWidth
import com.tencent.kuikly.compose.foundation.lazy.LazyColumn
import com.tencent.kuikly.compose.foundation.lazy.LazyRow
import com.tencent.kuikly.compose.foundation.lazy.items
import com.tencent.kuikly.compose.foundation.shape.RoundedCornerShape
import com.tencent.kuikly.compose.ui.Alignment
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.draw.clip
import com.tencent.kuikly.compose.ui.graphics.Color
import com.tencent.kuikly.compose.ui.graphics.painter.Painter
import com.tencent.kuikly.compose.ui.platform.LocalConfiguration
import com.tencent.kuikly.compose.ui.text.TextStyle
import com.tencent.kuikly.compose.ui.unit.dp
import com.tencent.kuikly.compose.ui.unit.sp
import com.tencent.kuikly.compose_dsl.kuikly.extension.bouncesEnable
import com.tencent.kuikly.compose_dsl.kuikly.extension.nativeRef
import com.tencent.news.core.app.LocalKmmContext
import com.tencent.news.core.app.constants.IconFont
import com.tencent.news.core.compose.adaptive.AdaptiveDialog
import com.tencent.news.core.compose.platform.QnIconFont
import com.tencent.news.core.compose.platform.fdp
import com.tencent.news.core.compose.platform.pageViewHeightValue
import com.tencent.news.core.compose.scaffold.modifiers.Button
import com.tencent.news.core.compose.scaffold.modifiers.DtCurrentView
import com.tencent.news.core.compose.scaffold.modifiers.DtLogicParentView
import com.tencent.news.core.compose.scaffold.modifiers.dtElement
import com.tencent.news.core.compose.scaffold.modifiers.margin
import com.tencent.news.core.compose.scaffold.modifiers.onSizeChangedDp
import com.tencent.news.core.compose.scaffold.share.getPostSharePanelElementId
import com.tencent.news.core.compose.scaffold.theme.DarkColorScheme
import com.tencent.news.core.compose.scaffold.theme.ForceDarkTheme
import com.tencent.news.core.compose.scaffold.theme.QNTheme
import com.tencent.news.core.compose.view.QnImage
import com.tencent.news.core.compose.view.QnScreenshot
import com.tencent.news.core.compose.view.QnText
import com.tencent.news.core.compose.view.dialog.DialogController
import com.tencent.news.core.compose.view.dialog.DialogShowType
import com.tencent.news.core.compose.view.dialog.IDialog
import com.tencent.news.core.compose.view.rememberScreenshotState
import com.tencent.news.core.dt.constants.DtCardPanelType
import com.tencent.news.core.dt.constants.DtElementId
import com.tencent.news.core.isHarmonyPlatform
import com.tencent.news.core.isIOSPlatform
import com.tencent.news.core.list.trace.ShareLog
import com.tencent.news.core.platform.api.appFile
import com.tencent.news.core.platform.api.dtReport
import com.tencent.news.core.service.ViewService
import com.tencent.news.core.share.IShareChannel
import com.tencent.news.core.share.api.IKmmShareData
import com.tencent.news.core.share.api.ShareChannel
import com.tencent.news.core.share.api.ShareSceneType
import com.tencent.news.core.view.setup.ViewServiceBridge
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import com.tencent.news.core.compose.platform.safeAreaHeight

/**
 * 通用取消按钮组件
 * @param onClick 点击回调
 * @param modifier 修饰符
 * @param backgroundColor 背景色，传null时使用主题色
 * @param textColor 文字颜色，传null时使用主题色
 */
@Composable
internal fun CancelButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color? = null,
    textColor: Color? = null
) {
    val safeAreaInsetBottom = safeAreaHeight()
    val bottomPadding = if (safeAreaInsetBottom > 0) 10.dp else 44.dp

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(backgroundColor ?: QNTheme.colorScheme.bgBlock)
    ) {
        Button(
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp, bottom = bottomPadding),
            onClick = onClick
        ) {
            QnText(
                text = "取消",
                color = textColor ?: QNTheme.colorScheme.t1,
                fontSize = 16.sp,
            )
        }
    }
}

sealed class ShareResult {
    object SUCCESS : ShareResult()
    object CANCELED : ShareResult()

    // 海报分享等二次分享
    class CONTINUE(val shareChannel: IShareChannel, val shareData: IKmmShareData) : ShareResult()
}

typealias OnDismissed = (shareResult: ShareResult) -> Unit

/**
 * 根据分享场景获取合适的内容修饰符
 */
@Composable
private fun getContentModifierByShareScene(
    shareData: IKmmShareData,
    defaultModifier: Modifier? = null
): Modifier {
    // 如果提供了默认修饰符，则使用默认修饰符
    if (defaultModifier != null) {
        return defaultModifier
    }

    // 获取分享场景
    val sceneType = shareData.option?.shareScene
    val isMorningOrEveningPostOrChannelShare = sceneType == ShareSceneType.MORNING_POST ||
            sceneType == ShareSceneType.CHANNEL_SHARE ||
            sceneType == ShareSceneType.AIGC_POSTER

    if (isHarmonyPlatform() || !isMorningOrEveningPostOrChannelShare) {
        return Modifier
            .margin(start = 28.dp, end = 28.dp)
            .clip(QNTheme.shape.medium)
    } else {
        // 早晚报场景使用固定宽度
        return Modifier
            .margin(start = 50.fdp, end = 50.fdp)
            // 注意：此处代码会导致海报预览外层有圆角
            .clip(QNTheme.shape.medium)
    }
}

/**
 * 卡片分享弹窗
 * @param shareData 分享数据
 * @param onDismissed 关闭回调
 * @param customBackground 自定义背景内容（可选）
 */
class PostShareDialog(
    private val shareData: IKmmShareData,
    private val onDismissed: OnDismissed,
    private val modifier: Modifier? = null,
    private val shareDtParams: ShareDtParams = ShareDtParams(DtElementId.ShareCardPanel),
    private val logicParent: DtLogicParentView? = null,
    private val customBackground: (@Composable () -> Unit)? = null,
    private val screenshotContent: (@Composable () -> Unit)? = null,
    private val screenshotModifier: (@Composable () -> Modifier)? = null,
    private val channels: List<ShareChannel>? = null,
    private val beforeShare: (IShareChannel) -> Boolean = { true },
    private val shareContent: @Composable () -> Unit,
) : IDialog() {

    override val showType: DialogShowType = DialogShowType.BottomSheet

    override val customBackgroundContent: (@Composable () -> Unit)? = customBackground

    override val content: @Composable (pageScope: CoroutineScope, controller: DialogController) -> Unit
        get() = { pageScope, controller ->
            ShareLog.verbose("海报分享") { "PostShareDialog-pageScope:${pageScope}" }
            val shareViewModel =
                if (channels != null) buildShareChannels(channels) else buildCardShareChannels()
            val state by rememberScreenshotState()

            val statusHeight = LocalConfiguration.current.statusBarHeight
            var sheetHeight by remember { mutableStateOf(0F) }
            val height = pageViewHeightValue() - statusHeight
            val scope = rememberCoroutineScope()
            // 海报的父层面板
            val shareScene = shareData.option?.shareScene
            val elementParams =
                remember(shareScene, shareDtParams.cardPanelType, shareDtParams.eType) {
                    val eType = shareDtParams.eType
                    if (eType != null) {
                        mapOf(
                            "e_type" to eType,
                            "cardpanel_type" to shareDtParams.cardPanelType
                        )
                    } else {
                        mapOf(
                            "cardpanel_type" to shareDtParams.cardPanelType
                        )
                    }
                }
            Column(
                modifier = Modifier.margin(top = statusHeight).fillMaxSize()
                    .dtElement(
                        elementId = shareDtParams.elementId,
                        elementParams = elementParams,
                        enableClick = false,
                        enableExposure = true,
                        logicParentView = logicParent
                    ),
                verticalArrangement = Arrangement.Bottom
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement
                        .Center,
                    horizontalAlignment = Alignment.CenterHorizontally, // 添加水平居中对齐
                ) {
                    // 根据分享场景获取合适的内容修饰符
                    val contentModifier = getContentModifierByShareScene(shareData, modifier)

                    // 如果传入了自定义 modifier，说明需要固定宽度（如直播卡片），使用 wrapContentWidth
                    // 否则使用 fillMaxWidth 保持原有行为（如早晚报）
                    val widthModifier = if (modifier != null) {
                        Modifier.wrapContentWidth()
                    } else {
                        Modifier.fillMaxWidth()
                    }
                    // 分享内容
                    val hasCustomScreenshot = screenshotContent != null
                    val customScreenshotModifier = screenshotModifier?.invoke()
                    LazyColumn(
                        modifier = contentModifier
                            .heightIn(max = (height - sheetHeight).dp)
                            .then(widthModifier)
                            .wrapContentHeight()
                            .bouncesEnable(false),
                    ) {
                        item {
                            val previewContainerModifier = widthModifier.wrapContentHeight()
                            val screenshotContainerModifier = customScreenshotModifier ?: Modifier.wrapContentWidth().wrapContentHeight()
                            if (!hasCustomScreenshot) {
                                QnScreenshot(
                                    modifier = previewContainerModifier,
                                    state = state
                                ) {
                                    shareContent()
                                }
                            } else {
                                Box(
                                    modifier = previewContainerModifier,
                                    contentAlignment = Alignment.TopCenter
                                ) {
                                    // 截图内容 - 移到屏幕外很远的地方，用户看不见
                                    QnScreenshot(
                                        modifier = screenshotContainerModifier
                                            .align(Alignment.TopCenter)
                                            .absoluteOffset(x = 10000.dp, y = 0.dp), // 向右偏移到屏幕外
                                        state = state
                                    ) {
                                        screenshotContent?.invoke()
                                    }

                                    // 用户看到的展示内容 - 保持在屏幕内正常位置
                                    Box(
                                        modifier = Modifier.align(Alignment.TopCenter)
                                    ) {
                                        shareContent()
                                    }
                                }
                            }
                        }
                    }
                }

                val context = LocalKmmContext
                // 分享渠道
                ShareChannelList(
                    modifier = Modifier.margin(top = 20.dp).onSizeChangedDp {
                        sheetHeight = it.height + 20f
                    },
                    viewModel = shareViewModel,
                    onItemClicked = { shareChannel ->
                        if (!beforeShare(shareChannel)) {
                            return@ShareChannelList
                        }
                        scope.launch {
                            state.take(scope).collectLatest { path ->
                                if (!path.isNullOrEmpty()) {
                                    val metadata =
                                        ViewServiceBridge.impl.createShareMetaData(shareData)
                                    if (metadata != null) {
                                        appFile()?.writeMetadata4Image(path, mapOf(metadata))
                                    }
                                    shareChannel.share(
                                        context = context,
                                        shareContent = ViewServiceBridge.impl.buildImageShareContent(
                                            path,
                                            shareData,
                                            shareChannel.channel
                                        ),
                                        shareData = shareData
                                    )
                                }
                                controller.dismissDialog(this@PostShareDialog)
                                onDismissed(ShareResult.SUCCESS)
                            }
                        }
                    }
                )

                // 取消按钮
                CancelButton(
                    onClick = {
                        controller.dismissDialog(this@PostShareDialog)
                        onDismissed(ShareResult.CANCELED)
                    }
                )
            }

        }
}

/**
 * 分享弹窗样式配置
 * @param backgroundColor 背景色，传null时使用主题色
 * @param textColor 文字颜色，传null时使用主题色
 * @param iconBgColor 图标背景色，传null时使用主题色
 */
data class ShareDialogStyle(
    val backgroundColor: Color? = null,
    val textColor: Color? = null,
    val iconBgColor: Color? = null
)

/**
 * 分享弹窗
 * @param forceDarkMode 是否强制使用夜间模式，默认为 false
 */
class ShareDialog(
    private val channels: List<ShareChannel>,
    private val shareData: IKmmShareData,
    private val onDismissed: OnDismissed,
    private val logicParent: DtLogicParentView? = null,
    private val style: ShareDialogStyle? = null,
    private val forceDarkMode: Boolean = false,
    /**
     * 可选：第一行渠道下面的「操作按钮」行（如 收藏 / 赠送 / 夜间模式 等）。
     * 默认 null，不渲染该行；保持现有调用方视觉与行为完全一致。
     */
    private val operationButtons: List<ShareOperation>? = null,
    private val beforeShare: (IShareChannel) -> Boolean = { true },
) : IDialog() {


    var useExternalTwiceShare: Boolean = false

    override val forceProtectClickEvent: Boolean = true

    override val showType: DialogShowType = DialogShowType.BottomSheet

    override val displayType: AdaptiveDialog.DisplayType = AdaptiveDialog.DisplayType.BottomSheetLarge

    override val safeAreaBackgroundColorProvider: (@Composable () -> Color) = {
        if (forceDarkMode) {
            style?.backgroundColor ?: DarkColorScheme.bgBlock
        } else {
            style?.backgroundColor ?: QNTheme.colorScheme.bgBlock
        }
    }

    override val content: @Composable (pageScope: CoroutineScope, controller: DialogController) -> Unit
        get() = { pageScope, controller ->
            ShareLog.verbose(subTag = "海报分享") { "content-pageScope:${pageScope}" }
            val shareViewModel = buildShareChannels(channels)
            // 检查是否支持海报预览
            val supportsPostPreview = shareData.option?.showPosterPreview == true
            val statusHeight = LocalConfiguration.current.statusBarHeight
            // 创建PostPreviewManager用于数据处理
            val postPreviewManager = PostPreviewManager(
                shareData = shareData,
                onDismissed = onDismissed
            )

            // 将预览数据提升为Compose状态，确保UI能够响应数据变化
            var postPreviewData by remember { mutableStateOf<PostPreviewData?>(null) }
            var nativeRef by remember { mutableStateOf<DtCurrentView?>(null) }
            // 初始化预览数据
            if (supportsPostPreview) {
                // 使用带有默认本地资源的预览数据
                val initialData = postPreviewManager.createInitialPreviewDataWithPlaceholder()
                LaunchedEffect(Unit) {
                    postPreviewData = initialData
                }
            }

            // 如果支持海报预览，自动生成海报预览数据
            if (supportsPostPreview) {
                postPreviewData?.let { previewData ->
                    // 同步到ShareViewModel（保持向后兼容）
                    shareViewModel.setPostPreviewData(previewData)

                    // 渲染隐藏的截图组件
                    postPreviewManager.renderHiddenScreenshotComponent(
                        postPreviewData = previewData,
                        onPreviewDataUpdated = { newData ->
                            postPreviewData = newData
                            shareViewModel.setPostPreviewData(newData)
                        }
                    )
                }
            }
            var eType: ShareDtEType? = null
            if (shareData.option?.shareScene == ShareSceneType.CHANNEL_SHARE) {
                eType = ShareDtEType.ShareChannel
            }
            // 点击海报后，调用 PostShareDialog
            val showPostShareDialog: (ShareResult.CONTINUE) -> Unit = { continueShareInfo ->
                pageScope.launch {
                    ShareLog.verbose("海报分享") {
                        "即将延迟处理：${continueShareInfo}"
                    }.toString()
                    var cardPanelType = DtCardPanelType.DEFAULT

                    if (shareData.option?.shareScene != ShareSceneType.CHANNEL_SHARE) {
                        delay(500)
                    } else {
                        cardPanelType = DtCardPanelType.POSTER

                    }

                    ViewServiceBridge.impl.fetchShareMetaData(shareData)

                    controller.showDialog(
                        PostShareDialog(
                            shareData = shareData,
                            onDismissed = onDismissed,
                            shareDtParams = ShareDtParams(
                                elementId = getPostSharePanelElementId(continueShareInfo.shareChannel),
                                cardPanelType = cardPanelType,
                                eType = eType
                            ),
                            shareContent = {
                                ViewService.share.PostShareContent(
                                    continueShareInfo.shareChannel,
                                    continueShareInfo.shareData,
                                    null
                                )
                            },
                            logicParent = logicParent,
                            beforeShare = beforeShare,
                        )
                    )
                }
            }
            val shareScene = shareData.option?.shareScene
            val elementParams = remember(shareScene) {
                if (shareScene == ShareSceneType.CHANNEL_SHARE) {
                    // 第一层分享面板参数
                    mapOf("e_type" to ShareDtEType.ShareChannel.name)
                } else {
                    // FIXME: 其它分享不需要 e_type?
                    null
                }
            }

            // 根据 forceDarkMode 参数决定是否强制使用夜间模式
            val dialogContent: @Composable () -> Unit = {
                Column(
                    modifier = Modifier
                        .needFixHeight(supportsPostPreview, statusHeight)
                        .fillMaxWidth()
                        .fillMaxHeight()
                        .dtElement(
                            elementId = DtElementId.SharePanel,
                            elementParams = elementParams,
                            enableExposure = true,
                            logicParentView = logicParent
                        ).nativeRef { nativeRef = it },
                    verticalArrangement = Arrangement.Bottom
                ) {
                    val context = LocalKmmContext
                    // 第一行：分享渠道
                    ShareChannelList(
                        modifier = Modifier,
                        viewModel = shareViewModel,
                        backgroundColor = style?.backgroundColor,
                        textColor = style?.textColor,
                        iconBgColor = style?.iconBgColor,
                        onItemClicked = { shareChannel ->
                            if (!beforeShare(shareChannel)) {
                                return@ShareChannelList
                            }
                            if (shareChannel.needTwiceShare()) {
                                ShareLog.verbose("开始二次分享渠道") { "$shareChannel" }

                                // 关闭当前弹窗 FIXME: 关闭弹窗后，早晚报页面是如何实现延迟展示海报的？
                                if (autoDismissOnTwiceClick) {
                                    controller.dismissDialog(null)
                                }
                                if (this@ShareDialog.useExternalTwiceShare) {
                                    onDismissed(ShareResult.CONTINUE(shareChannel, shareData))
                                } else {
                                    showPostShareDialog(
                                        ShareResult.CONTINUE(shareChannel, shareData)
                                    )
                                }
                            } else {
                                val shareContent = ViewServiceBridge.impl.buildPageShareContent(
                                    shareData, shareChannel.channel
                                )
                                ShareLog.verbose("开始分享内容") { "$shareContent" }

                                if (shareContent != null) {
                                    shareChannel.share(context, shareContent, shareData)
                                }
                                controller.dismissDialog(null)
                                onDismissed(ShareResult.SUCCESS)
                            }
                        }
                    )

                    // 第二行：海报预览组件（如果启用）
                    if (supportsPostPreview) {
                        postPreviewData?.let { previewData ->
                            ViewServiceBridge.impl.ShowPostPreviewComponent(
                                previewData,
                                shareData
                            ) { continueShareInfo ->
                                if (autoDismissOnTwiceClick) {
                                    controller.dismissDialog(null)
                                }
                                if (useExternalTwiceShare) {
                                    // 外部二次分享模式：通过onDismissed回调，由外部处理
                                    onDismissed(continueShareInfo)
                                } else {
                                    // 默认模式：在内部直接弹出PostShareDialog
                                    showPostShareDialog(continueShareInfo)
                                }
                            }
                        }
                    }

                    // 操作按钮行（可选）：仅当业务方传入 operationButtons 时渲染
                    // 例：专栏页的「赠送 / 收藏 / 夜间模式」
                    operationButtons?.takeIf { it.isNotEmpty() }?.let { ops ->
                        ShareOperationRow(
                            operations = ops,
                            backgroundColor = style?.backgroundColor,
                            textColor = style?.textColor,
                            iconBgColor = style?.iconBgColor,
                            onOperationClick = { operation ->
                                if (operation.dismissDialogOnClick) {
                                    controller.dismissDialog(null)
                                }
                                operation.onClick()
                            }
                        )
                    }

                    // 第三行：取消按钮
                    CancelButton(
                        backgroundColor = style?.backgroundColor,
                        textColor = style?.textColor,
                        onClick = {
                            controller.dismissDialog(null)
                            onDismissed(ShareResult.CANCELED)
                        }
                    )
                    LaunchedEffect(nativeRef) {
                        nativeRef?.let {
                            if (isIOSPlatform()) {
                                delay(1000)
                                dtReport()?.traversePage(it)
                            }
                        }
                    }
                }
            }

            // 如果强制使用夜间模式，则使用 ForceDarkTheme 包裹内容
            if (forceDarkMode) {
                ForceDarkTheme {
                    dialogContent()
                }
            } else {
                dialogContent()
            }
        }
}

@Composable
internal fun ShareChannelList(
    modifier: Modifier = Modifier,
    viewModel: ShareViewModel,
    backgroundColor: Color? = null,
    textColor: Color? = null,
    iconBgColor: Color? = null,
    onItemClicked: (IShareChannel) -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(backgroundColor ?: QNTheme.colorScheme.bgBlock)
            .clip(
                RoundedCornerShape(
                    topStart = 8.dp,
                    topEnd = 8.dp,
                    bottomEnd = 0.dp,
                    bottomStart = 0.dp
                )
            )
    ) {
        LazyRow(Modifier.fillMaxWidth().wrapContentHeight()) {
            items(
                items = viewModel.channels,
                key = { item -> item.name }
            ) {
                ShareChannelItem(
                    icon = it.icon,
                    iconFont = it.iconFont,
                    name = it.name,
                    dtEid = it.dtEid,
                    textColor = textColor,
                    iconBgColor = iconBgColor,
                    onShareClick = { onItemClicked(it.channel) }
                )
            }
        }
    }
}

@Composable
internal fun ShareChannelItem(
    icon: Painter?,
    iconFont: IconFont?,
    name: String,
    dtEid: DtElementId?,
    textColor: Color? = null,
    iconBgColor: Color? = null,
    /**
     * iconFont 的专属颜色覆盖。传 null 时走默认逻辑（textColor 或主题 t1）。
     * 用于分享面板「操作按钮」场景（如已收藏态图标需高亮为 yNormal 黄色）。
     */
    iconFontColor: Color? = null,
    onShareClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .dtElement(dtEid)
            .clickable { onShareClick() }
            .padding(start = 8.dp, end = 8.dp, top = 16.dp, bottom = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Box(
            modifier = Modifier.size(46.dp)
                .background(iconBgColor ?: QNTheme.colorScheme.bgPage)
                .clip(QNTheme.shape.circle),
            contentAlignment = Alignment.Center
        ) {
            if (icon != null) {
                QnImage(
                    painter = icon,
                    contentDescription = null,
                    modifier = Modifier.size(46.dp)
                )
            } else if (iconFont != null) {

                QnIconFont(
                    name = iconFont,
                    textStyle = TextStyle(
                        fontSize = 24.sp,
                        color = iconFontColor ?: textColor ?: QNTheme.colorScheme.t1
                    )
                )
            }
        }

        QnText(
            text = name,
            color = textColor ?: QNTheme.colorScheme.t1,
            fontSize = 12.sp,
            modifier = Modifier.margin(top = 8.dp),
        )
    }
}

/**
 * 有海报预览和无海报预览的情况，某些机型会出现高度算不对，需要补一下stateHeight
 */
private fun Modifier.needFixHeight(supportsPostPreview: Boolean, stateHeight: Float): Modifier {
    return if (!supportsPostPreview) {
        this.margin(top = stateHeight)
    } else {
        this
    }
}
