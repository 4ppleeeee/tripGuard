package com.tencent.kmm.demo.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.tencent.kuikly.core.annotations.Page
import com.tencent.kuikly.compose.foundation.background
import com.tencent.kuikly.compose.foundation.layout.Arrangement
import com.tencent.kuikly.compose.foundation.layout.Box
import com.tencent.kuikly.compose.foundation.layout.Column
import com.tencent.kuikly.compose.foundation.layout.PaddingValues
import com.tencent.kuikly.compose.foundation.layout.Row
import com.tencent.kuikly.compose.foundation.layout.Spacer
import com.tencent.kuikly.compose.foundation.layout.fillMaxSize
import com.tencent.kuikly.compose.foundation.layout.fillMaxWidth
import com.tencent.kuikly.compose.foundation.layout.height
import com.tencent.kuikly.compose.foundation.layout.heightIn
import com.tencent.kuikly.compose.foundation.layout.padding
import com.tencent.kuikly.compose.foundation.lazy.LazyColumn
import com.tencent.kuikly.compose.foundation.shape.RoundedCornerShape
import com.tencent.kuikly.compose.ui.Alignment
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.draw.clip
import com.tencent.kuikly.compose.ui.graphics.Color
import com.tencent.kuikly.compose.ui.text.font.FontWeight
import com.tencent.kuikly.compose.ui.unit.dp
import com.tencent.kuikly.compose.ui.unit.sp
import com.tencent.news.core.compose.platform.emptyPageArgs
import com.tencent.news.core.compose.scaffold.ComposePage
import com.tencent.news.core.compose.scaffold.registry.LocalDialogController
import com.tencent.news.core.compose.scaffold.theme.QNTheme
import com.tencent.news.core.compose.view.QnText
import com.tencent.news.core.compose.view.SpacerHeight
import com.tencent.news.core.compose.view.dialog.DialogController
import com.tencent.news.core.compose.view.dialog.DialogShowType
import com.tencent.news.core.compose.view.dialog.IDialog
import com.tencent.news.core.compose.view.extension.preciseClickable
import com.tencent.news.core.isIOSPlatform
import com.tencent.news.core.platform.extension.AppRouterEx
import kotlinx.coroutines.CoroutineScope

@Page(name = DemoRoutes.HOME)
class DemoHomePage : ComposePage() {

    override fun enableGlobalDebugFloatingEntry(): Boolean = false

    @Composable
    override fun OnSetContent() {
        DemoHomeContent()
    }
}

@Composable
private fun DemoHomeContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(QNTheme.colorScheme.bgPage)
            .padding(horizontal = 24.dp, vertical = 32.dp),
    ) {
        QnText(
            text = "KMM Base Demo",
            color = QNTheme.colorScheme.t1,
            fontSize = 28.sp,
            fontWeight = FontWeight.W600,
        )
        SpacerHeight(8.dp)
        QnText(
            text = "选择一个入口，开始验证跨端基础能力",
            color = QNTheme.colorScheme.t3,
            fontSize = 15.sp,
            lineHeight = 22f,
        )
        SpacerHeight(28.dp)
        buildDemoEntries().forEach { entry ->
            DemoEntryCard(entry)
            SpacerHeight(16.dp)
        }
    }
}

@Composable
private fun DemoEntryCard(entry: DemoEntry) {
    val isPlatformEntry = entry.pageName == DemoRoutes.PLATFORM_CAPABILITIES
    val accentColor = if (isPlatformEntry) Color(0xFF7C5CFC) else Color(0xFF3377FF)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(16.dp))
            .preciseClickable(onClickLabel = "打开${entry.title}") {
                AppRouterEx.toComposePage(
                    pageName = entry.pageName,
                    pageArgs = emptyPageArgs(),
                )
            }
            .padding(20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Box(
            modifier = Modifier
                .background(accentColor, RoundedCornerShape(12.dp))
                .padding(horizontal = 12.dp, vertical = 10.dp),
            contentAlignment = Alignment.Center,
        ) {
            QnText(
                text = if (isPlatformEntry) "API" else "UI",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.W600,
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            QnText(
                text = entry.title,
                color = QNTheme.colorScheme.t1,
                fontSize = 17.sp,
                fontWeight = FontWeight.W500,
            )
            SpacerHeight(6.dp)
            QnText(
                text = entry.desc,
                color = QNTheme.colorScheme.t3,
                fontSize = 13.sp,
                lineHeight = 19f,
            )
        }
        QnText(
            text = "›",
            color = accentColor,
            fontSize = 26.sp,
            fontWeight = FontWeight.W500,
        )
    }
}

@Page(name = DemoRoutes.PLATFORM_CAPABILITIES)
class PlatformCapabilitiesPage : ComposePage() {

    override fun enableGlobalDebugFloatingEntry(): Boolean = false

    @Composable
    override fun OnSetContent() {
        PlatformCapabilitiesContent()
    }
}

@Composable
private fun PlatformCapabilitiesContent() {
    val groups = remember { buildPlatformCapabilityTestGroups() }
    val dialogController = LocalDialogController.current
    val scope = rememberCoroutineScope()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7F8FA)),
    ) {
        DemoTopAppBar(title = "平台能力")
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, top = 18.dp, end = 20.dp, bottom = 28.dp),
        ) {
            item {
                PlatformOverviewCard(groups)
                SpacerHeight(18.dp)
                groups.chunked(2).forEach { rowGroups ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        rowGroups.forEach { group ->
                            PlatformCapabilityCard(
                                group = group,
                                modifier = Modifier.weight(1f),
                            ) {
                                dialogController.showDialog(scope, PlatformCapabilitySheet(group))
                            }
                        }
                        repeat(2 - rowGroups.size) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                    SpacerHeight(12.dp)
                }
            }
        }
    }
}

@Composable
private fun DemoTopAppBar(title: String) {
    val topPadding = if (isIOSPlatform()) 74.dp else 34.dp
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(start = 20.dp, top = topPadding, end = 20.dp, bottom = 16.dp),
        contentAlignment = Alignment.Center,
    ) {
        QnText(
            text = title,
            color = Color(0xFF1D1B20),
            fontSize = 18.sp,
            fontWeight = FontWeight.W600,
        )
    }
}

@Composable
private fun PlatformOverviewCard(groups: List<PlatformCapabilityTestGroup>) {
    val methodCount = groups.sumOf { it.methodNames.size }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White)
            .padding(22.dp),
    ) {
        QnText(
            text = "qnPlatform 能力测试台",
            color = Color(0xFF1D1B20),
            fontSize = 24.sp,
            fontWeight = FontWeight.W700,
            lineHeight = 31f,
        )
        SpacerHeight(8.dp)
        QnText(
            text = "覆盖 ${groups.size} 个注入点，${methodCount} 个具体能力。点击卡片进入浮层逐项验证。",
            color = Color(0xFF625B71),
            fontSize = 14.sp,
            lineHeight = 21f,
        )
    }
}

@Composable
private fun PlatformCapabilityCard(
    group: PlatformCapabilityTestGroup,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White)
            .preciseClickable(onClickLabel = "测试${group.title}", onClick = onClick)
            .padding(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(platformAccentColor(group.key))
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center,
            ) {
                QnText(
                    text = group.title.take(1),
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.W700,
                )
            }
            QnText(
                text = "›",
                color = Color(0xFF6750A4),
                fontSize = 26.sp,
                fontWeight = FontWeight.W500,
            )
        }
        SpacerHeight(14.dp)
        QnText(
            text = group.title,
            color = Color(0xFF1D1B20),
            fontSize = 17.sp,
            fontWeight = FontWeight.W600,
            lineHeight = 22f,
        )
        SpacerHeight(5.dp)
        QnText(
            text = "${group.methodNames.size} 项能力",
            color = Color(0xFF625B71),
            fontSize = 13.sp,
            lineHeight = 18f,
        )
    }
}

private class PlatformCapabilitySheet(
    private val group: PlatformCapabilityTestGroup,
) : IDialog() {

    override val showType: DialogShowType = DialogShowType.BottomSheet

    override val contentHandlesBottomSafeArea: Boolean = true

    override val content: @Composable (pageScope: CoroutineScope, controller: DialogController) -> Unit
        get() = { _, controller ->
            PlatformCapabilitySheetContent(group, controller, this)
        }
}

@Composable
private fun PlatformCapabilitySheetContent(
    group: PlatformCapabilityTestGroup,
    controller: DialogController,
    dialog: IDialog,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
            .background(Color.White)
            .padding(start = 20.dp, top = 18.dp, end = 20.dp, bottom = 18.dp),
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .clip(RoundedCornerShape(2.dp))
                .background(Color(0xFFD0D0D0))
                .fillMaxWidth(0.12f)
                .height(4.dp),
        )
        SpacerHeight(18.dp)
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                QnText(
                    text = group.title,
                    color = Color(0xFF1D1B20),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.W700,
                    lineHeight = 28f,
                )
                SpacerHeight(4.dp)
                QnText(
                    text = group.detail.subtitle,
                    color = Color(0xFF625B71),
                    fontSize = 13.sp,
                    lineHeight = 18f,
                )
            }
            if (group.detail.showCloseAction) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color(0xFFEADDFF))
                        .preciseClickable(onClickLabel = "关闭") {
                            controller.dismissDialog(dialog)
                        }
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    QnText(
                        text = "关闭",
                        color = Color(0xFF4F378B),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.W600,
                    )
                }
            }
        }
        SpacerHeight(16.dp)
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 560.dp),
        ) {
            when (val detail = group.detail) {
                is PlatformCapabilityDeviceInfoDetail -> {
                    if (detail.displayItems.isEmpty()) {
                        item {
                            PlatformCapabilityEmptyRow("当前平台无需展示设备信息")
                        }
                    } else {
                        detail.displayItems.forEach { displayItem ->
                            item {
                                PlatformCapabilityDisplayRow(displayItem)
                                SpacerHeight(10.dp)
                            }
                        }
                    }
                }
                is PlatformCapabilityAppStatusDetail -> {
                    if (detail.displayItems.isEmpty()) {
                        item {
                            PlatformCapabilityEmptyRow("当前平台未注入 IAppStatus")
                        }
                    } else {
                        detail.displayItems.forEach { displayItem ->
                            item {
                                PlatformCapabilityDisplayRow(displayItem)
                                SpacerHeight(10.dp)
                            }
                        }
                    }
                }
                is PlatformCapabilityTestListDetail -> {
                    detail.testCases.forEach { testCase ->
                        item {
                            PlatformCapabilityTestCaseRow(testCase)
                            SpacerHeight(10.dp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PlatformCapabilityDisplayRow(item: PlatformCapabilityDisplayItem) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFF7F2FA))
            .padding(14.dp),
    ) {
        QnText(
            text = item.title,
            color = Color(0xFF1D1B20),
            fontSize = 15.sp,
            fontWeight = FontWeight.W600,
            lineHeight = 20f,
        )
        SpacerHeight(6.dp)
        QnText(
            text = item.value.ifBlank { "-" },
            color = Color(0xFF49454F),
            fontSize = 14.sp,
            lineHeight = 20f,
        )
    }
}

@Composable
private fun PlatformCapabilityEmptyRow(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFF7F2FA))
            .padding(14.dp),
        contentAlignment = Alignment.Center,
    ) {
        QnText(
            text = text,
            color = Color(0xFF625B71),
            fontSize = 14.sp,
            lineHeight = 20f,
        )
    }
}

@Composable
private fun PlatformCapabilityTestCaseRow(testCase: PlatformCapabilityTestCase) {
    var result by remember(testCase.name) { mutableStateOf<String?>(null) }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFF7F2FA))
            .padding(14.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            QnText(
                text = testCase.name,
                color = Color(0xFF1D1B20),
                fontSize = 15.sp,
                fontWeight = FontWeight.W600,
                lineHeight = 20f,
                modifier = Modifier.weight(1f),
            )
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color(0xFF6750A4))
                    .preciseClickable(onClickLabel = "运行${testCase.name}") {
                        result = testCase.run()
                    }
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center,
            ) {
                QnText(
                    text = "运行",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.W600,
                )
            }
        }
        val currentResult = result
        if (currentResult != null) {
            SpacerHeight(10.dp)
            QnText(
                text = currentResult,
                color = platformResultColor(currentResult),
                fontSize = 12.sp,
                lineHeight = 17f,
            )
        }
    }
}

private fun platformResultColor(result: String): Color =
    when {
        result.contains(": PASS ") -> Color(0xFF146C2E)
        result.contains(": NOOP ") -> Color(0xFF825500)
        result.contains(": UNSUPPORTED ") -> Color(0xFF625B71)
        result.contains(": ERROR ") -> Color(0xFFB3261E)
        else -> Color(0xFF49454F)
    }

private fun platformAccentColor(key: String): Color =
    when (key.hashCode().mod(5)) {
        0 -> Color(0xFF6750A4)
        1 -> Color(0xFF006A6A)
        2 -> Color(0xFFB3261E)
        3 -> Color(0xFF386A20)
        else -> Color(0xFF825500)
    }
