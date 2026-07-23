package com.tencent.news.core.platform.api

import com.tencent.news.core.app.IKmmContext
import com.tencent.news.core.platform.QnPlatformLogic
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.withContext

fun appMediaPicker(): IAppMediaPicker = AppMediaPickerInterceptor(QnPlatformLogic.appMediaPicker)

/**
 * 图片选择 + 上传能力的宿主接口。
 *
 * 与旧版差异：
 * - [pickImages] 现在**只返回选图结果**，不再在此方法内同步完成上传。这样 UI 可以先把选中的图
 *   作为占位展示，再通过 [uploadMedia] 异步上传每一张。
 * - [uploadMedia] 按 [AppPickedMedia.localId] 触发单张上传。每次调用一次回调。
 * - 如果宿主没有真正分离的能力（比如 demo mock），可直接在 [pickImages] 里就填好 url，
 *   此时 [uploadMedia] 只是把已有 url echo 回去即可。
 */
interface IAppMediaPicker {
    /**
     * 宿主在缩略图（localPath）**异步落盘完成**后推送的 update。
     *
     * ### 为什么要这个 flow
     *
     * [pickImages] 在主线程 finishBlock 里必须迅速返回，否则会造成相册 pop 动画卡顿。
     * 缩略图编码 + 落盘是 CPU + IO，搬到后台队列后，返回给 KMM 的 [AppPickedMedia] 里
     * `localPath` 一开始可能是空的；后台写完后通过这个 flow 推回来，KMM 业务层收到后
     * 补齐对应 `ChatMultiMedia.localPath`，SelectedImagePreview 立刻从灰底 loading
     * 切到真实缩略图。
     *
     * ### 订阅约定
     *
     * - 业务层应在**持有附件列表的 ViewModel 生命周期内**长期订阅这个 flow；
     * - flow 用的是**热流**（SharedFlow with replay=0），订阅前发生的更新会丢失，
     *   所以订阅要在首次调用 [pickImages] 之前完成；
     * - 收到的 [LocalPathUpdate.localId] 在业务列表里找不到时（已被 [releaseMedia]），
     *   直接忽略即可。
     */
    val localPathResolvedFlow: SharedFlow<LocalPathUpdate>
        get() = LocalPathResolvedBus.flow
    /**
     * 弹出选图 / 拍照 UI。选中后应立即回调：
     * 建议返回的每一张媒体都带上宿主分配的 [AppPickedMedia.localId]；此时 `url` 可以为空，
     * 业务层会后续通过 [uploadMedia] 触发上传。
     */
    suspend fun pickImages(
        context: IKmmContext?,
        config: AppMediaPickerConfig
    ): AppMediaPickerResult

    /**
     * 上传之前 [pickImages] 返回的某一张图。
     * @return 带 `url` 的最终 [AppPickedMedia]；失败时 [AppMediaPickerResult.errorMsg] 非空。
     *
     * 实现约定：
     * - 同一个 [localId] 可重复调用（用于重试）；
     * - 宿主内部保留本地图片数据，直到业务层显式调用 [releaseMedia]（目前统一在对话窗口关闭时释放）。
     */
    suspend fun uploadMedia(
        context: IKmmContext?,
        localId: String
    ): AppMediaPickerResult

    /**
     * 取消某张本地图的上传（如果正在进行），并释放相应的 localId 缓存。
     * UI 层移除该附件时调用。
     */
    suspend fun releaseMedia(localId: String)

    /**
     * 跳到宿主的图片浏览页，用户可在那里删除部分图片；关闭时回传「剩余保留」的列表。
     *
     * @param medias 当前 SelectedImagePreview 中的全部图（按顺序），每一项至少需要一个可显示的来源：
     *  - [AppPickedMedia.url] 非空（已上传成功）：宿主按 url 显示远程图
     *  - [AppPickedMedia.localId] 非空：宿主按 localId 从本地缓存里取原图显示
     * @param startIndex 首屏定位的下标
     * @return attachments 为用户在预览页「完成」时保留下来的图；若用户按返回，[AppMediaPickerResult.canceled] = true 且调用方应保留原列表
     */
    suspend fun previewImages(
        context: IKmmContext?,
        medias: List<AppPickedMedia>,
        startIndex: Int
    ): AppMediaPickerResult
}

/**
 * 宿主异步落盘完成后推送的 update 单元。
 *
 * @param localId 对应的图片在宿主 store 里的唯一 id，和 [AppPickedMedia.localId] 一致
 * @param localPath 落盘后的绝对路径；若写盘失败则为空字符串，业务层可据此保留 loading 或展示错误
 */
data class LocalPathUpdate(
    val localId: String,
    val localPath: String,
)

/**
 * 全局 bus：宿主 iosMain 侧把 OC 的 callback 桥到这里 emit，业务层通过
 * [IAppMediaPicker.localPathResolvedFlow] 订阅。
 *
 * extraBufferCapacity = 8 够同时选 6 张图 + 一点余量，不阻塞宿主；replay = 0 避免
 * 新订阅拿到过期数据。
 */
object LocalPathResolvedBus {
    val mutableFlow: MutableSharedFlow<LocalPathUpdate> =
        MutableSharedFlow(replay = 0, extraBufferCapacity = 8)
    val flow: SharedFlow<LocalPathUpdate> = mutableFlow.asSharedFlow()
}

/**
 * 媒体来源类型
 */
enum class MediaSourceType {
    /** 相册 */
    ALBUM,
    /** 拍照 */
    CAMERA
}

data class AppMediaPickerConfig(
    val maxImageSelectCount: Int = 1,
    val allowPreview: Boolean = true,
    /** 媒体来源类型，null 表示由宿主自行决定（如弹出选择框）,拍照or相机 */
    val sourceType: MediaSourceType? = null,
    /**
     * 再次拉起相册时，已经在业务侧选中的 localId 列表。宿主应该：
     * - 在相册 UI 中把对应的图标记为「已选中」并计入最大选择数；
     * - 返回结果里保留这些 localId（按用户在相册中的取消/新增动作做增删）；
     * - 对于本来就已选中且用户没取消的图，**复用原 localId**，不要生成新的，也不要重复上传。
     *
     * 仅当 [sourceType] 为 [MediaSourceType.ALBUM] 或 `null` 时有意义；拍照场景忽略。
     */
    val preSelectedLocalIds: List<String> = emptyList(),
)

data class AppMediaPickerResult(
    val attachments: List<AppPickedMedia> = emptyList(),
    val canceled: Boolean = false,
    val errorMsg: String = ""
)

private class AppMediaPickerInterceptor(
    private val platformMediaPicker: IAppMediaPicker?
) : IAppMediaPicker {
    override suspend fun pickImages(
        context: IKmmContext?,
        config: AppMediaPickerConfig
    ): AppMediaPickerResult = withContext(Dispatchers.Main) {
        platformMediaPicker?.pickImages(context, config)
            ?: AppMediaPickerResult(errorMsg = "当前版本暂不支持图片发送")
    }

    override suspend fun uploadMedia(
        context: IKmmContext?,
        localId: String
    ): AppMediaPickerResult = withContext(Dispatchers.Main) {
        platformMediaPicker?.uploadMedia(context, localId)
            ?: AppMediaPickerResult(errorMsg = "当前版本暂不支持图片发送")
    }

    override suspend fun releaseMedia(localId: String) {
        withContext(Dispatchers.Main) {
            platformMediaPicker?.releaseMedia(localId)
        }
    }

    override suspend fun previewImages(
        context: IKmmContext?,
        medias: List<AppPickedMedia>,
        startIndex: Int
    ): AppMediaPickerResult = withContext(Dispatchers.Main) {
        platformMediaPicker?.previewImages(context, medias, startIndex)
            ?: AppMediaPickerResult(canceled = true)
    }
}
