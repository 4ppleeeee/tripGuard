package com.tencent.kmm.demo

import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID

class DemoActivity : Activity() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private lateinit var root: LinearLayout
    private lateinit var input: EditText
    private lateinit var chatInput: EditText
    private lateinit var chatResultContainer: LinearLayout
    private lateinit var listContainer: LinearLayout
    private lateinit var emptyView: TextView
    private var showingChat = false
    private val repository by lazy { TravelSourceRepository(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupHomeView()
        handleIncomingIntent(intent, autoSave = true)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if (intent != null) {
            setIntent(intent)
            handleIncomingIntent(intent, autoSave = true)
        }
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }

    override fun onBackPressed() {
        if (showingChat) {
            setupHomeView()
        } else {
            super.onBackPressed()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_PICK_IMAGE && resultCode == RESULT_OK) {
            val imageUri = data?.data
            if (imageUri != null) {
                analyzeImageBeforeInsert(imageUri)
            } else {
                Toast.makeText(this, "没有选择图片", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupBaseRoot() {
        root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(28), dp(20), dp(20))
            setBackgroundColor(0xFFF4F6FA.toInt())
        }
        setContentView(root)
    }

    private fun setupHomeView() {
        showingChat = false
        setupBaseRoot()

        root.addView(text("旅行资料 MVP", 26f, true, 0xFF171717.toInt()))
        root.addView(text("粘贴链接，或从微信/相册分享长图到 App。AI 会先判断旅行相关性，相关才保存到后端知识库。", 14f, false, 0xFF667085.toInt()).apply {
            setPadding(0, dp(8), 0, dp(18))
        })

        input = EditText(this).apply {
            hint = "粘贴小红书、马蜂窝、网页链接或分享文案"
            minLines = 4
            maxLines = 7
            gravity = Gravity.TOP
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE
            setTextColor(0xFF222222.toInt())
            setHintTextColor(0xFF999999.toInt())
            background = roundedBg(0xFFFFFFFF.toInt(), 0xFFE2E8F0.toInt(), 8f)
            setPadding(dp(14), dp(12), dp(14), dp(12))
        }
        root.addView(input, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)

        val actions = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, dp(12), 0, dp(18))
        }
        actions.addView(primaryButton("识别并入库") { saveInputText() }, LinearLayout.LayoutParams(0, dp(44), 1f))
        actions.addView(space(dp(10), 1))
        actions.addView(secondaryButton("选择长图") {
            openImagePicker()
        }, LinearLayout.LayoutParams(dp(128), dp(44)))
        actions.addView(space(dp(10), 1))
        actions.addView(secondaryButton("问行程") {
            setupChatView()
        }, LinearLayout.LayoutParams(dp(88), dp(44)))
        root.addView(actions)

        emptyView = text("还没有资料。粘贴链接，或分享长图到 Travel MVP Demo。", 14f, false, 0xFF667085.toInt()).apply {
            setPadding(dp(16), dp(18), dp(16), dp(18))
            background = roundedBg(0xFFFFFFFF.toInt(), 0xFFE2E8F0.toInt(), 8f)
        }
        root.addView(emptyView, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)

        val scroll = ScrollView(this)
        listContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }
        scroll.addView(listContainer)
        root.addView(scroll, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f))

        renderSources()
        refreshBackendSources(showToast = false)
    }

    private fun setupChatView() {
        showingChat = true
        setupBaseRoot()

        root.addView(text("行程推荐", 26f, true, 0xFF171717.toInt()))
        root.addView(text("基于已经入库的旅行资料回答，并返回引用来源。", 14f, false, 0xFF667085.toInt()).apply {
            setPadding(0, dp(8), 0, dp(18))
        })

        chatInput = EditText(this).apply {
            hint = "比如：北京周末想看花拍照，怎么安排？"
            minLines = 3
            maxLines = 5
            gravity = Gravity.TOP
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE
            setTextColor(0xFF222222.toInt())
            setHintTextColor(0xFF999999.toInt())
            background = roundedBg(0xFFFFFFFF.toInt(), 0xFFE2E8F0.toInt(), 8f)
            setPadding(dp(14), dp(12), dp(14), dp(12))
        }
        root.addView(chatInput, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)

        val actions = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, dp(12), 0, dp(18))
        }
        actions.addView(primaryButton("生成推荐") { sendTravelQuestion() }, LinearLayout.LayoutParams(0, dp(44), 1f))
        actions.addView(space(dp(10), 1))
        actions.addView(secondaryButton("返回") {
            setupHomeView()
        }, LinearLayout.LayoutParams(dp(96), dp(44)))
        root.addView(actions)

        val scroll = ScrollView(this)
        chatResultContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }
        scroll.addView(chatResultContainer)
        root.addView(scroll, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f))

        chatResultContainer.addView(text("输入问题后，会从后端知识库检索已保存资料再生成推荐。", 14f, false, 0xFF667085.toInt()).apply {
            setPadding(dp(16), dp(18), dp(16), dp(18))
            background = roundedBg(0xFFFFFFFF.toInt(), 0xFFE2E8F0.toInt(), 8f)
        })
    }

    private fun handleIncomingIntent(intent: Intent, autoSave: Boolean) {
        if (intent.action == Intent.ACTION_SEND && intent.type.orEmpty().startsWith("image/")) {
            val imageUri = intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)
            if (imageUri != null && autoSave) {
                analyzeImageBeforeInsert(imageUri)
            }
            return
        }

        val sharedText = when (intent.action) {
            Intent.ACTION_SEND -> intent.getStringExtra(Intent.EXTRA_TEXT)
            Intent.ACTION_VIEW -> intent.dataString
            else -> null
        }?.trim().orEmpty()

        if (sharedText.isBlank()) {
            return
        }
        input.setText(sharedText)
        if (autoSave) {
            saveRawText(sharedText)
        }
    }

    private fun saveInputText() {
        val rawText = input.text?.toString()?.trim().orEmpty()
        saveRawText(rawText)
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "image/*"
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        runCatching {
            startActivityForResult(Intent.createChooser(intent, "选择长图"), REQUEST_PICK_IMAGE)
        }.onFailure {
            Toast.makeText(this, "无法打开图片选择器", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveRawText(rawText: String) {
        if (rawText.isBlank()) {
            Toast.makeText(this, "先粘贴分享文案或 URL", Toast.LENGTH_SHORT).show()
            return
        }
        val url = XhsShareParser.extractFirstUrl(rawText)
        if (url == null) {
            Toast.makeText(this, "没有识别到 URL", Toast.LENGTH_SHORT).show()
            return
        }

        val existing = repository.findByUrl(url)
        if (existing != null) {
            Toast.makeText(this, "这条资料已经保存过", Toast.LENGTH_SHORT).show()
            renderSources()
            return
        }

        val detectedPlatform = XhsShareParser.detectPlatform("$url $rawText")
        input.setText("")
        analyzeCandidateBeforeInsert(rawText, url, detectedPlatform)
    }

    private fun analyzeCandidateBeforeInsert(
        rawText: String,
        url: String,
        initialPlatform: SourcePlatform,
    ) {
        Toast.makeText(this, "正在解析并交给 AI 判断旅行相关性", Toast.LENGTH_SHORT).show()

        scope.launch {
            val metadata = withContext(Dispatchers.IO) {
                runCatching { MetadataParser.parse(url) }
                    .getOrElse { error ->
                        ParsedMetadata(
                            resolvedUrl = url,
                            title = null,
                            description = null,
                            imageUrl = null,
                            warning = "正文解析失败: ${error.message ?: error.javaClass.simpleName}",
                        )
                    }
            }
            val collectResult = withContext(Dispatchers.IO) {
                runCatching { TravelInsightAnalyzer.collect(rawText, metadata) }
            }
            val collect = collectResult.getOrElse {
                input.setText(rawText)
                Toast.makeText(
                    this@DemoActivity,
                    "后端入库失败，确认后端服务和 Lucky 反代可访问",
                    Toast.LENGTH_LONG,
                ).show()
                return@launch
            }

            if (!collect.saved) {
                Toast.makeText(this@DemoActivity, collect.reason ?: "这条内容看起来和旅行无关，已跳过入库", Toast.LENGTH_LONG).show()
                return@launch
            }

            refreshBackendSources(showToast = false)
            Toast.makeText(this@DemoActivity, "已保存到后端知识库", Toast.LENGTH_SHORT).show()
        }
    }

    private fun analyzeImageBeforeInsert(imageUri: Uri) {
        Toast.makeText(this, "正在识别长图并交给 AI 判断旅行相关性", Toast.LENGTH_SHORT).show()

        scope.launch {
            val imageFile = withContext(Dispatchers.IO) {
                runCatching { copyImageToLocalFile(imageUri) }
            }.getOrElse {
                Toast.makeText(this@DemoActivity, "图片读取失败", Toast.LENGTH_LONG).show()
                return@launch
            }

            val collect = withContext(Dispatchers.IO) {
                runCatching { TravelInsightAnalyzer.collectImage(imageFile) }
            }.getOrElse {
                Toast.makeText(
                    this@DemoActivity,
                    "AI 图片识别失败，确认后端服务和 Ollama 隧道可访问",
                    Toast.LENGTH_LONG,
                ).show()
                return@launch
            }

            if (!collect.saved) {
                imageFile.delete()
                Toast.makeText(this@DemoActivity, collect.reason ?: "这张图未识别出有效旅行信息，已跳过入库", Toast.LENGTH_LONG).show()
                return@launch
            }

            imageFile.delete()
            refreshBackendSources(showToast = false)
            Toast.makeText(this@DemoActivity, "长图已保存到后端知识库", Toast.LENGTH_SHORT).show()
        }
    }

    private fun copyImageToLocalFile(uri: Uri): File {
        val dir = File(filesDir, "travel_images").apply { mkdirs() }
        val file = File(dir, "${UUID.randomUUID()}.jpg")
        contentResolver.openInputStream(uri).use { inputStream ->
            requireNotNull(inputStream) { "无法打开图片" }
            file.outputStream().use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }
        return file
    }

    private fun parseSource(sourceId: String) {
        val queued = repository.findById(sourceId) ?: return
        repository.upsert(queued.copy(status = SourceStatus.PARSING, error = null, updatedAt = System.currentTimeMillis()))
        renderSources()

        scope.launch {
            val result = withContext(Dispatchers.IO) {
                runCatching { MetadataParser.parse(queued.originalUrl) }
            }
            val latest = repository.findById(sourceId) ?: return@launch
            val parsed = result.fold(
                onSuccess = { metadata ->
                    val resolvedUrl = XhsShareParser.normalizeResolvedUrl(metadata.resolvedUrl)
                    val platform = XhsShareParser.detectPlatform("$resolvedUrl ${latest.rawText}")
                        .takeIf { it != SourcePlatform.WEB || latest.platform == SourcePlatform.WEB }
                        ?: latest.platform
                    latest.copy(
                        resolvedUrl = resolvedUrl,
                        platform = platform,
                        title = metadata.title
                            ?.takeIf { XhsShareParser.isUsefulParsedTitle(it) }
                            ?: XhsShareParser.deriveTitle(latest.rawText),
                        description = XhsShareParser.selectDescription(
                            platform = platform,
                            parsedDescription = metadata.description,
                            rawText = latest.rawText,
                            resolvedUrl = resolvedUrl,
                        ),
                        imageUrl = metadata.imageUrl?.takeIf { it.isNotBlank() },
                        insight = latest.insight,
                        status = if (metadata.hasUsefulContent()) SourceStatus.PARSED else SourceStatus.PARTIAL,
                        error = metadata.warning,
                        updatedAt = System.currentTimeMillis(),
                    )
                },
                onFailure = { error ->
                    latest.copy(
                        status = SourceStatus.FAILED,
                        error = error.message ?: error.javaClass.simpleName,
                        updatedAt = System.currentTimeMillis(),
                    )
                },
            )
            repository.upsert(parsed)
            renderSources()
        }
    }

    private fun refreshBackendSources(showToast: Boolean) {
        scope.launch {
            val result = withContext(Dispatchers.IO) {
                runCatching { TravelInsightAnalyzer.fetchSources() }
            }
            result.onSuccess { cards ->
                val now = System.currentTimeMillis()
                repository.replaceAll(cards.mapIndexed { index, card -> card.toTravelSource(now - index) })
                renderSources()
                if (showToast) {
                    Toast.makeText(this@DemoActivity, "已刷新后端知识库", Toast.LENGTH_SHORT).show()
                }
            }.onFailure {
                if (showToast) {
                    Toast.makeText(this@DemoActivity, "刷新后端知识库失败", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun sendTravelQuestion() {
        val message = chatInput.text?.toString()?.trim().orEmpty()
        if (message.isBlank()) {
            Toast.makeText(this, "先输入旅行问题", Toast.LENGTH_SHORT).show()
            return
        }
        chatResultContainer.removeAllViews()
        chatResultContainer.addView(text("正在从知识库检索并生成推荐...", 14f, false, 0xFF667085.toInt()).apply {
            setPadding(dp(16), dp(18), dp(16), dp(18))
            background = roundedBg(0xFFFFFFFF.toInt(), 0xFFE2E8F0.toInt(), 8f)
        })
        scope.launch {
            val result = withContext(Dispatchers.IO) {
                runCatching { TravelInsightAnalyzer.recommend(message) }
            }
            result.onSuccess(::renderRecommendation)
                .onFailure {
                    chatResultContainer.removeAllViews()
                    chatResultContainer.addView(text("生成失败，请确认后端服务和 Ollama 隧道可访问。", 14f, false, 0xFFB3261E.toInt()).apply {
                        setPadding(dp(16), dp(18), dp(16), dp(18))
                        background = roundedBg(0xFFFFFFFF.toInt(), 0xFFE2E8F0.toInt(), 8f)
                    })
                }
        }
    }

    private fun renderRecommendation(result: BackendRecommendResult) {
        chatResultContainer.removeAllViews()
        chatResultContainer.addView(text(result.answer.ifBlank { "暂时没有足够资料生成推荐。" }, 16f, false, 0xFF172033.toInt()).apply {
            setPadding(dp(16), dp(14), dp(16), dp(14))
            background = roundedBg(0xFFFFFFFF.toInt(), 0xFFE2E8F0.toInt(), 8f)
        })
        if (result.usedSources.isNotEmpty()) {
            chatResultContainer.addView(text("引用资料", 15f, true, 0xFF344054.toInt()).apply {
                setPadding(0, dp(16), 0, dp(8))
            })
            result.usedSources.forEach { source ->
                chatResultContainer.addView(usedSourceView(source))
                chatResultContainer.addView(space(1, dp(10)))
            }
        }
    }

    private fun usedSourceView(source: BackendUsedSource): View =
        LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(14), dp(12), dp(14), dp(12))
            background = roundedBg(0xFFFFFFFF.toInt(), 0xFFE2E8F0.toInt(), 8f)
            addView(text(source.title, 15f, true, 0xFF172033.toInt()))
            addView(text("${source.destination} · ${TravelCategory.fromModelValue(source.category).displayName}", 12f, false, 0xFF667085.toInt()).apply {
                setPadding(0, dp(6), 0, 0)
            })
            if (source.tags.isNotEmpty()) {
                addView(text(source.tags.take(4).joinToString("  ") { "#$it" }, 12f, true, 0xFF175CD3.toInt()).apply {
                    setPadding(0, dp(6), 0, 0)
                })
            }
            if (!source.originalUrl.isNullOrBlank()) {
                setOnClickListener { openUrl(source.originalUrl) }
            }
        }

    private fun renderSources() {
        val sources = repository.getAll()
            .sortedByDescending { it.createdAt }
        emptyView.visibility = if (sources.isEmpty()) View.VISIBLE else View.GONE
        listContainer.removeAllViews()
        sources.forEach { source ->
            listContainer.addView(sourceCard(source))
            listContainer.addView(space(1, dp(12)))
        }
    }

    private fun sourceCard(source: TravelSource): View {
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(14), dp(16), dp(14))
            background = roundedBg(0xFFFFFFFF.toInt(), 0xFFE2E8F0.toInt(), 8f)
        }
        if (!source.imagePath.isNullOrBlank()) {
            card.addView(localCoverImage(source.imagePath))
        } else if (!source.imageUrl.isNullOrBlank()) {
            card.addView(coverImage(source.imageUrl))
        }
        card.addView(text(source.title ?: "等待解析标题", 19f, true, 0xFF172033.toInt()).apply {
            setPadding(0, dp(10), 0, dp(8))
        })
        source.insight?.let { insight ->
            card.addView(insightBlock(insight))
        }
        card.addView(sourceFooter(source))
        if (!source.error.isNullOrBlank()) {
            card.addView(text("解析提示: ${source.error}", 12f, false, 0xFFB3261E.toInt()).apply {
                setPadding(0, dp(8), 0, 0)
            })
        }

        if (source.sourceKind == SourceKind.LINK) {
            val actions = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                setPadding(0, dp(12), 0, 0)
            }
            actions.addView(secondaryButton("打开原文") {
                openUrl(source.resolvedUrl ?: source.originalUrl)
            }, LinearLayout.LayoutParams(0, dp(40), 1f))
            actions.addView(space(dp(10), 1))
            actions.addView(secondaryButton("重新解析") {
                parseSource(source.id)
            }, LinearLayout.LayoutParams(0, dp(40), 1f))
            card.addView(actions)
        }

        return card
    }

    private fun insightBlock(insight: TravelInsight): View =
        LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(12), dp(10), dp(12), dp(10))
            background = roundedBg(0xFFF7FBFF.toInt(), 0xFFD8EAFE.toInt(), 8f)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
            ).apply {
                topMargin = dp(10)
            }

            addView(LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                addView(insightChip("目的地", insight.destination))
                addView(space(dp(8), 1))
                addView(insightChip("分类", insight.category.displayName))
            })
            if (insight.tags.isNotEmpty()) {
                addView(LinearLayout(context).apply {
                    orientation = LinearLayout.HORIZONTAL
                    setPadding(0, dp(8), 0, 0)
                    insight.tags.take(4).forEachIndexed { index, tag ->
                        if (index > 0) {
                            addView(space(dp(8), 1))
                        }
                        addView(tagChip(tag))
                    }
                })
            }
        }

    private fun sourceFooter(source: TravelSource): View =
        LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, dp(10), 0, 0)
            addView(text(source.status.displayName, 12f, true, source.status.color), LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
            addView(text(source.sourceLabel(), 12f, true, 0xFF475467.toInt()).apply {
                setPadding(dp(10), dp(4), dp(10), dp(4))
                background = roundedBg(0xFFF2F4F7.toInt(), 0xFFD0D5DD.toInt(), 8f)
            })
        }

    private fun insightChip(label: String, value: String): TextView =
        text("$label $value", 12f, true, 0xFF344054.toInt()).apply {
            setPadding(dp(8), dp(4), dp(8), dp(4))
            background = roundedBg(0xFFFFFFFF.toInt(), 0xFFD0D5DD.toInt(), 8f)
        }

    private fun tagChip(value: String): TextView =
        text("#$value", 12f, true, 0xFF175CD3.toInt()).apply {
            setPadding(dp(8), dp(4), dp(8), dp(4))
            background = roundedBg(0xFFEFF8FF.toInt(), 0xFFB2DDFF.toInt(), 8f)
        }

    private fun openUrl(url: String) {
        runCatching {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        }.onFailure {
            Toast.makeText(this, "无法打开链接", Toast.LENGTH_SHORT).show()
        }
    }

    private fun primaryButton(label: String, onClick: () -> Unit): Button =
        Button(this).apply {
            text = label
            isAllCaps = false
            setTextColor(0xFFFFFFFF.toInt())
            background = roundedBg(0xFF226CFF.toInt(), 0x00000000, 8f)
            setOnClickListener { onClick() }
        }

    private fun secondaryButton(label: String, onClick: () -> Unit): Button =
        Button(this).apply {
            text = label
            isAllCaps = false
            val color = 0xFF226CFF.toInt()
            setTextColor(color)
            background = roundedBg(0xFFFFFFFF.toInt(), color, 8f)
            setOnClickListener { onClick() }
        }

    private fun coverImage(url: String): ImageView =
        ImageView(this).apply {
            setBackgroundColor(0xFFE8EAED.toInt())
            scaleType = ImageView.ScaleType.FIT_CENTER
            adjustViewBounds = true
            contentDescription = "资料封面"
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(220),
            ).apply {
                topMargin = dp(10)
            }
            loadCover(url, this)
        }

    private fun localCoverImage(path: String): ImageView =
        ImageView(this).apply {
            setBackgroundColor(0xFFE8EAED.toInt())
            scaleType = ImageView.ScaleType.FIT_CENTER
            adjustViewBounds = true
            contentDescription = "长图快照"
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(280),
            )
            BitmapFactory.decodeFile(path)?.let(::setImageBitmap)
        }

    private fun loadCover(url: String, imageView: ImageView) {
        scope.launch {
            val bitmap = withContext(Dispatchers.IO) {
                runCatching {
                    val connection = (URL(url).openConnection() as HttpURLConnection).apply {
                        connectTimeout = 10000
                        readTimeout = 12000
                        setRequestProperty("User-Agent", "Mozilla/5.0 TravelMvpDemo/0.1")
                        setRequestProperty("Referer", "https://www.xiaohongshu.com/")
                    }
                    try {
                        connection.inputStream.use(BitmapFactory::decodeStream)
                    } finally {
                        connection.disconnect()
                    }
                }.getOrNull()
            }
            if (bitmap != null) {
                imageView.setImageBitmap(bitmap)
            }
        }
    }

    private fun text(value: String, sizeSp: Float, bold: Boolean, color: Int): TextView =
        TextView(this).apply {
            text = value
            textSize = sizeSp
            setTextColor(color)
            if (bold) {
                typeface = Typeface.DEFAULT_BOLD
            }
            setLineSpacing(dp(3).toFloat(), 1.0f)
        }

    private fun space(width: Int, height: Int): View =
        View(this).apply {
            layoutParams = LinearLayout.LayoutParams(width, height)
        }

    private fun roundedBg(fillColor: Int, strokeColor: Int, radiusDp: Float): GradientDrawable =
        GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = dp(radiusDp.toInt()).toFloat()
            setColor(fillColor)
            if (strokeColor != 0x00000000) {
                setStroke(dp(1), strokeColor)
            }
        }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density + 0.5f).toInt()

    private companion object {
        const val REQUEST_PICK_IMAGE = 1001
    }
}

private enum class SourceStatus(val displayName: String, val color: Int) {
    QUEUED("等待解析", 0xFF7C5800.toInt()),
    PARSING("解析中", 0xFF226CFF.toInt()),
    PARTIAL("部分解析", 0xFF7C5800.toInt()),
    PARSED("已解析", 0xFF137333.toInt()),
    FAILED("解析失败", 0xFFB3261E.toInt()),
}

private enum class SourceKind(val displayName: String) {
    LINK("链接"),
    IMAGE("长图"),
}

private data class TravelSource(
    val id: String,
    val sourceKind: SourceKind,
    val rawText: String,
    val originalUrl: String,
    val resolvedUrl: String?,
    val platform: SourcePlatform,
    val title: String?,
    val description: String?,
    val imageUrl: String?,
    val imagePath: String?,
    val insight: TravelInsight?,
    val status: SourceStatus,
    val error: String?,
    val createdAt: Long,
    val updatedAt: Long,
) {
    fun toJson(): JSONObject = JSONObject()
        .put("id", id)
        .put("sourceKind", sourceKind.name)
        .put("rawText", rawText)
        .put("originalUrl", originalUrl)
        .put("resolvedUrl", resolvedUrl)
        .put("platform", platform.name)
        .put("title", title)
        .put("description", description)
        .put("imageUrl", imageUrl)
        .put("imagePath", imagePath)
        .put("insight", insight?.toJson())
        .put("status", status.name)
        .put("error", error)
        .put("createdAt", createdAt)
        .put("updatedAt", updatedAt)

    companion object {
        fun fromJson(json: JSONObject): TravelSource {
            val rawText = json.optString("rawText")
            val originalUrl = json.getString("originalUrl")
            val sourceKind = runCatching { SourceKind.valueOf(json.optString("sourceKind")) }
                .getOrDefault(SourceKind.LINK)
            val storedPlatform = runCatching { SourcePlatform.valueOf(json.optString("platform")) }.getOrNull()
            val storedResolvedUrl = json.optNullableString("resolvedUrl")?.let(XhsShareParser::normalizeResolvedUrl)
            val detectedPlatform = XhsShareParser.detectPlatform("$originalUrl ${storedResolvedUrl.orEmpty()} $rawText")
            return TravelSource(
                id = json.getString("id"),
                sourceKind = sourceKind,
                rawText = rawText,
                originalUrl = originalUrl,
                resolvedUrl = storedResolvedUrl,
                platform = if (storedPlatform == SourcePlatform.WEB) detectedPlatform else storedPlatform ?: detectedPlatform,
                title = json.optNullableString("title") ?: XhsShareParser.deriveTitle(rawText),
                description = json.optNullableString("description")
                    ?: storedResolvedUrl?.takeIf { detectedPlatform == SourcePlatform.XIAOHONGSHU }?.let {
                        XhsShareParser.deriveFallbackDescription(rawText, it)
                    },
                imageUrl = json.optNullableString("imageUrl"),
                imagePath = json.optNullableString("imagePath"),
                insight = json.optJSONObject("insight")?.let(TravelInsight::fromJson),
                status = runCatching { SourceStatus.valueOf(json.optString("status")) }.getOrDefault(SourceStatus.QUEUED),
                error = json.optNullableString("error"),
                createdAt = json.optLong("createdAt"),
                updatedAt = json.optLong("updatedAt"),
            )
        }
    }

    fun sourceLabel(): String =
        when (sourceKind) {
            SourceKind.IMAGE -> "来源 长图"
            SourceKind.LINK -> "来源 ${platform.displayName}"
        }
}

private fun BackendSourceCard.toTravelSource(createdAtMillis: Long): TravelSource {
    val sourceKind = if (originalUrl.isNullOrBlank() || sourcePlatform == "image") SourceKind.IMAGE else SourceKind.LINK
    val sourcePlatformValue = when (sourcePlatform) {
        "xhs" -> SourcePlatform.XIAOHONGSHU
        "mafengwo" -> SourcePlatform.MAFENGWO
        else -> originalUrl?.let { XhsShareParser.detectPlatform(it) } ?: SourcePlatform.WEB
    }
    return TravelSource(
        id = sourceId,
        sourceKind = sourceKind,
        rawText = "",
        originalUrl = originalUrl ?: sourceId,
        resolvedUrl = originalUrl,
        platform = sourcePlatformValue,
        title = title,
        description = null,
        imageUrl = coverImageUrl,
        imagePath = null,
        insight = TravelInsight(
            isTravelRelated = true,
            title = title,
            destination = destination,
            category = TravelCategory.fromModelValue(category),
            tags = tags,
            bodyText = "",
            confidence = 1.0,
        ),
        status = SourceStatus.PARSED,
        error = null,
        createdAt = createdAtMillis,
        updatedAt = createdAtMillis,
    )
}

private class TravelSourceRepository(activity: Activity) {
    private val prefs = activity.getSharedPreferences("travel_sources", Activity.MODE_PRIVATE)

    fun getAll(): List<TravelSource> {
        val raw = prefs.getString(KEY_SOURCES, "[]").orEmpty()
        val array = runCatching { JSONArray(raw) }.getOrDefault(JSONArray())
        return buildList {
            for (index in 0 until array.length()) {
                val item = array.optJSONObject(index) ?: continue
                runCatching { TravelSource.fromJson(item) }.getOrNull()?.let(::add)
            }
        }
    }

    fun findById(id: String): TravelSource? = getAll().firstOrNull { it.id == id }

    fun findByUrl(url: String): TravelSource? = getAll().firstOrNull {
        it.originalUrl == url || it.resolvedUrl == url
    }

    fun upsert(source: TravelSource) {
        val next = getAll().filterNot { it.id == source.id } + source
        replaceAll(next)
    }

    fun replaceAll(sources: List<TravelSource>) {
        val array = JSONArray()
        sources.forEach { array.put(it.toJson()) }
        prefs.edit().putString(KEY_SOURCES, array.toString()).apply()
    }

    private companion object {
        const val KEY_SOURCES = "sources"
    }
}

internal data class ParsedMetadata(
    val resolvedUrl: String,
    val title: String?,
    val description: String?,
    val imageUrl: String?,
    val warning: String? = null,
) {
    fun hasUsefulContent(): Boolean =
        XhsShareParser.isUsefulParsedTitle(title) || !description.isNullOrBlank() || !imageUrl.isNullOrBlank()
}

internal object MetadataParser {
    fun parse(rawUrl: String): ParsedMetadata =
        parse(rawUrl) { url -> url.openConnection() as HttpURLConnection }

    fun parse(rawUrl: String, openConnection: (URL) -> HttpURLConnection): ParsedMetadata {
        return when (val desktopResult = parseAttempt(rawUrl, DESKTOP_USER_AGENT, openConnection)) {
            is ParseAttemptResult.Success -> desktopResult.metadata
            is ParseAttemptResult.LoginRedirect -> {
                when (val mobileResult = runCatching {
                    parseAttempt(desktopResult.retryUrl, MOBILE_USER_AGENT, openConnection)
                }.getOrNull()) {
                    is ParseAttemptResult.Success -> mobileResult.metadata
                    else -> ParsedMetadata(
                        resolvedUrl = desktopResult.resolvedUrl,
                        title = null,
                        description = null,
                        imageUrl = null,
                    )
                }
            }
        }
    }

    private fun parseAttempt(
        rawUrl: String,
        userAgent: String,
        openConnection: (URL) -> HttpURLConnection,
    ): ParseAttemptResult {
        var currentUrl = rawUrl
        repeat(6) {
            val connection = openConnection(URL(currentUrl)).apply {
                instanceFollowRedirects = false
                connectTimeout = 10000
                readTimeout = 12000
                requestMethod = "GET"
                setRequestProperty("User-Agent", userAgent)
                setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                setRequestProperty("Accept-Language", "zh-CN,zh;q=0.9")
                setRequestProperty("Referer", "https://www.xiaohongshu.com/")
            }
            try {
                val status = connection.responseCode
                if (status in 300..399) {
                    val location = connection.getHeaderField("Location")
                    if (location.isNullOrBlank()) {
                        throw IllegalStateException("HTTP $status without Location")
                    }
                    val nextUrl = URL(URL(currentUrl), location).toString()
                    val normalizedUrl = XhsShareParser.normalizeResolvedUrl(nextUrl)
                    if (XhsShareParser.isXhsLoginRedirect(nextUrl)) {
                        return ParseAttemptResult.LoginRedirect(
                            resolvedUrl = normalizedUrl,
                            retryUrl = currentUrl,
                        )
                    }
                    currentUrl = normalizedUrl.takeIf { it != nextUrl || XhsShareParser.isXhsNoteUrl(it) } ?: nextUrl
                    return@repeat
                }
                if (status !in 200..299) {
                    throw IllegalStateException("HTTP $status")
                }
                val html = connection.inputStream.bufferedReader().use { reader ->
                    val builder = StringBuilder()
                    val buffer = CharArray(4096)
                    while (builder.length < MAX_HTML_CHARS) {
                        val read = reader.read(buffer)
                        if (read <= 0) break
                        builder.append(buffer, 0, read)
                    }
                    builder.toString()
                }
                val resolvedUrl = XhsShareParser.normalizeResolvedUrl(connection.url?.toString() ?: currentUrl)
                if (isWafChallenge(connection, html)) {
                    return ParseAttemptResult.Success(
                        ParsedMetadata(
                            resolvedUrl = resolvedUrl,
                            title = null,
                            description = null,
                            imageUrl = null,
                            warning = WAF_WARNING,
                        ),
                    )
                }
                if (XhsShareParser.detectPlatform(resolvedUrl) == SourcePlatform.XIAOHONGSHU) {
                    XhsNoteMetadataParser.parse(resolvedUrl, html)?.let { metadata ->
                        return ParseAttemptResult.Success(
                            ParsedMetadata(
                                resolvedUrl = resolvedUrl,
                                title = metadata.title,
                                description = metadata.description,
                                imageUrl = metadata.imageUrl,
                            ),
                        )
                    }
                }
                return ParseAttemptResult.Success(
                    ParsedMetadata(
                        resolvedUrl = resolvedUrl,
                        title = firstMeta(html, "og:title", "twitter:title") ?: titleTag(html),
                        description = firstMeta(html, "description", "og:description", "twitter:description"),
                        imageUrl = firstMeta(html, "og:image", "twitter:image"),
                    ),
                )
            } finally {
                connection.disconnect()
            }
        }
        throw IllegalStateException("Too many redirects")
    }

    private sealed interface ParseAttemptResult {
        data class Success(val metadata: ParsedMetadata) : ParseAttemptResult

        data class LoginRedirect(
            val resolvedUrl: String,
            val retryUrl: String,
        ) : ParseAttemptResult
    }

    private fun firstMeta(html: String, vararg names: String): String? {
        names.forEach { name ->
            metaContent(html, "property", name)?.let { return htmlDecode(it) }
            metaContent(html, "name", name)?.let { return htmlDecode(it) }
        }
        return null
    }

    private fun metaContent(html: String, attrName: String, attrValue: String): String? {
        val regex = Regex(
            "<meta[^>]+$attrName=[\"']${Regex.escape(attrValue)}[\"'][^>]+content=[\"']([^\"']+)[\"'][^>]*>",
            setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL),
        )
        val reverseRegex = Regex(
            "<meta[^>]+content=[\"']([^\"']+)[\"'][^>]+$attrName=[\"']${Regex.escape(attrValue)}[\"'][^>]*>",
            setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL),
        )
        return regex.find(html)?.groupValues?.getOrNull(1)
            ?: reverseRegex.find(html)?.groupValues?.getOrNull(1)
    }

    private fun titleTag(html: String): String? =
        Regex("<title[^>]*>(.*?)</title>", setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL))
            .find(html)
            ?.groupValues
            ?.getOrNull(1)
            ?.let { htmlDecode(it.trim()) }

    private fun isWafChallenge(connection: HttpURLConnection, html: String): Boolean =
        html.contains("probe.js") ||
            connection.getHeaderField("Set-Cookie").orEmpty().contains("x-waf-captcha-referer")

    private fun htmlDecode(value: String): String = value
        .replace("&amp;", "&")
        .replace("&quot;", "\"")
        .replace("&#39;", "'")
        .replace("&lt;", "<")
        .replace("&gt;", ">")
        .trim()

    private const val MAX_HTML_CHARS = 1_200_000
    private const val WAF_WARNING = "平台返回 WAF/验证码探针页，当前 HTTP 解析器拿不到正文。"
    private const val DESKTOP_USER_AGENT = "Mozilla/5.0 TravelMvpDemo/0.1"
    private const val MOBILE_USER_AGENT =
        "Mozilla/5.0 (Linux; Android 14; Pixel 8) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Mobile Safari/537.36"
}

private fun JSONObject.optNullableString(name: String): String? =
    if (has(name) && !isNull(name)) optString(name).takeIf { it.isNotBlank() } else null

internal fun String.compact(maxLength: Int): String =
    replace(Regex("\\s+"), " ").let { if (it.length <= maxLength) it else it.take(maxLength - 1) + "…" }
