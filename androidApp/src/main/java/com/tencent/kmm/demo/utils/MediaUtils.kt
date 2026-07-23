@file:JvmName("MediaUtils")

package com.tencent.kmm.demo.utils

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.text.TextUtils
import com.tencent.news.core.platform.qnFileLog
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException

/**
 * @Description: 媒体文件插入相册统一收归
 * @author rayerning
 * @date 2024/3/22
 * @Copyright (c) 2024 Tencent. All rights reserved.
 */
private const val TAG = "MediaUtils"

private const val VIDEO_MINE_TYPE = "video/mp4"
private const val IMAGE_MINE_TYPE = "image/jpeg"

fun isAfterQ() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q

fun insertVideoToAlbum(context: Context, path: String, title: String): Boolean {
    return if (isAfterQ()) {
        insertMediaToAlbumAfterQ(context, path, title, VIDEO_MINE_TYPE)
    } else {
        insertMediaToAlbumLessThanAndroidQ(context, path, VIDEO_MINE_TYPE)
    }
}

fun insertImageToAlbum(context: Context, path: String, title: String): Boolean {
    return if (isAfterQ()) {
        insertMediaToAlbumAfterQ(context, path, title, IMAGE_MINE_TYPE)
    } else {
        insertMediaToAlbumLessThanAndroidQ(context, path, IMAGE_MINE_TYPE)
    }
}

private fun insertMediaToAlbumLessThanAndroidQ(context: Context, path: String, type: String): Boolean {
    val videoFile = File(path)
    val modified = System.currentTimeMillis()
    val values = ContentValues()
    var isSuccess = true
    values.put(MediaStore.Video.Media.DATA, path)
    values.put(MediaStore.Video.Media.SIZE, videoFile.length())
    values.put(MediaStore.Video.Media.DATE_ADDED, modified / 1000)
    values.put(MediaStore.Video.Media.DATE_MODIFIED, modified / 1000)
    values.put(MediaStore.Video.Media.DATE_TAKEN, modified)
    values.put(MediaStore.Video.Media.MIME_TYPE, type)
    if (type == VIDEO_MINE_TYPE) {
        values.put(MediaStore.Video.Media.DURATION, getDurationImmediately(path))
    }
    try {
        context.contentResolver
                .insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values)
    } catch (e: Exception) {
        qnFileLog()?.logE(TAG, "insert external content uri error", e)
        try {
            context.contentResolver
                    .insert(MediaStore.Video.Media.INTERNAL_CONTENT_URI, values)
        } catch (e2: Exception) {
            qnFileLog()?.logE(TAG, "insert internal content uri error", e2)
            isSuccess = false
        }
    }

    scanNewFile(context, path)
    return isSuccess
}


private fun insertMediaToAlbumAfterQ(context: Context, path: String, title: String, type: String): Boolean {
    val file = File(path)
    val modified = System.currentTimeMillis()
    val duration = getDurationImmediately(path)
    var isSuccess = true
    val contentValues = ContentValues().apply {
        put(MediaStore.Images.Media.SIZE, file.length())
        put(MediaStore.Video.Media.DISPLAY_NAME, title)
        put(MediaStore.Video.Media.TITLE, title)
        put(MediaStore.Video.Media.DATE_ADDED, modified / 1000)
        put(MediaStore.Video.Media.DATE_MODIFIED, modified / 1000)
        put(MediaStore.Video.Media.DATE_TAKEN, modified)
        put(MediaStore.Video.Media.MIME_TYPE, type)
        put(MediaStore.Video.Media.ARTIST, "shanka")
        put(MediaStore.Images.Media.IS_PENDING, 1)
        if (type == VIDEO_MINE_TYPE) {
            put(MediaStore.Video.Media.DURATION, duration)
        }
    }
    val mediaUri = when (type) {
        VIDEO_MINE_TYPE -> MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        IMAGE_MINE_TYPE -> MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        else -> null
    }
    val contentResolver = context.contentResolver
    val videoUri = mediaUri?.let { contentResolver.insert(it, contentValues) }
    qnFileLog()?.logI(TAG, "insertVideoToAlbum path:$path, duration:$duration, title:$title, videoUri:$videoUri")
    videoUri?.let { uri ->
        try {
            contentResolver.openOutputStream(uri)?.use { outputStream ->
                val inputStream = FileInputStream(file)
                val readContent = ByteArray(1024)
                var len: Int?
                do {
                    len = inputStream.read(readContent)
                    outputStream.write(readContent)
                } while (len != -1)

                inputStream.close()
                outputStream.close()
            }
            contentValues.clear()
            contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
            contentResolver.update(uri, contentValues, null, null)
            getReadPathFromUri(context, uri)
        } catch (e: FileNotFoundException) {
            qnFileLog()?.logE(TAG, "insertVideoToAlbum failed", e)
            isSuccess = false
        } finally {
            file.delete()
        }
    }
    return isSuccess
}

fun getReadPathFromUri(context: Context?, uri: Uri): String {
    if (context == null) {
        return ""
    }
    var filePath = ""
    val scheme = uri.scheme
    if (scheme.isNullOrEmpty()) {
        filePath = uri.path.orEmpty()
    } else if (ContentResolver.SCHEME_FILE == scheme) {
        filePath = uri.path.orEmpty()
    } else if (ContentResolver.SCHEME_CONTENT == scheme) {
        val proj = arrayOf(MediaStore.Images.Media.DATA)
        val cursor = context.contentResolver.query(uri, proj, null, null, null)
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                val coIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                filePath = cursor.getString(coIndex)
            }
            cursor.close()
        }
        if (filePath.isNullOrEmpty()) {
            filePath = getFilePathFOrNonMediaUri(context, uri)
        }
    }
    return filePath
}

private fun getFilePathFOrNonMediaUri(context: Context, uri: Uri): String {
    var filePath = ""
    val cursor = context.contentResolver.query(uri, null, null, null, null)
    if (cursor != null) {
        if (cursor.moveToFirst()) {
            val index = cursor.getColumnIndexOrThrow("_data")
            filePath = cursor.getString(index)
        }
        cursor.close()
    }
    return filePath
}

/**
 * 快速获取视频时长，相册容错时获取视频时长要用，VideoTrackExtractor获取的方式如果遇到视频文件损坏的状况，会卡很久
 */
fun getDurationImmediately(path: String?): Long {
    var durations = 0L
    if (!TextUtils.isEmpty(path)) {
        var retriever: MediaMetadataRetriever? = null
        try {
            retriever = MediaMetadataRetriever()
            retriever.setDataSource(path)
            val duration = retriever
                    .extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            durations = duration?.toLong() ?: 0
        } catch (e: java.lang.Exception) {
            qnFileLog()?.logE(TAG, e.toString())
        } finally {
            retriever?.release()
        }
    }
    return durations
}

private fun scanNewFile(context: Context, path: String) {
    SingleMediaFileScanner(context.applicationContext, File(path)).start();
}
