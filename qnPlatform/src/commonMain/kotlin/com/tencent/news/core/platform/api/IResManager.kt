package com.tencent.news.core.platform.api

import com.tencent.news.core.app.IKmmContext
import com.tencent.news.core.platform.Lock
import com.tencent.news.core.platform.QnPlatformLogic
import com.tencent.news.core.platform.synchronized

interface IResManager {

    // 读取宿主app内置的json文件
    fun getAssetJson(fileName: String): String

    // 通过emoji的名字获取图片地址
    fun getEmoji(name: String): String

    fun preloadImage(
        url: String,
        onSuccess: (() -> Unit)? = null,
        onFail: (() -> Unit)? = null
    )

    fun copyToClipboard(content: String)

    fun copyHtmlToClipboard(html: String, plainText: String) {
        copyToClipboard(plainText)
    }

    // 保存图片，可能是网图也可能是缓存图
    fun saveImage(url: String, metadata: Map<String, String>?)

    // 保存网络视频到系统相册
    fun saveVideo(
        url: String,
        taskId: String? = null,
        stageCallback: ((stage: String, message: String?, error: String?) -> Unit)? = null,
        completion: ((success: Boolean, message: String?) -> Unit)? = null
    )


    fun sharePdf(context: IKmmContext?, url: String)

    fun getPaletteColor(
        imageUrl: String,
        param: PaletteParam,
        defaultColor: Int?,
        onGot: (color: Int) -> Unit
    ) {
    }

    fun preloadLottieToMemory(context: IKmmContext?, url: String, status: String, isDay: Boolean)

    fun preloadAlphaVideo(
        url: String,
        onSuccess: (() -> Unit)? = null,
        onFail: (() -> Unit)? = null
    )

}


fun resManager(): IResManager? {
    return QnPlatformLogic.resManager
}

data class PaletteParam(
    val imageSize: Int,
    val colorCount: Int,
    val imageRect: String
)

object PaletteCache {
    private val lock = Lock()
    private val map = mutableMapOf<String, MutableMap<PaletteParam, Int?>?>()

    private fun getMap(scene: String): MutableMap<PaletteParam, Int?>? {
        return map.getOrPut(scene) { mutableMapOf() }
    }

    fun get(url: String, key: PaletteParam): Int? {
        return synchronized(lock) { getMap(url)?.get(key) }
    }

    fun put(url: String, param: PaletteParam, value: Int) {
        synchronized(lock) { getMap(url)?.set(param, value) }
    }

    fun clear(url: String) {
        synchronized(lock) { map.remove(url) }
    }
}
