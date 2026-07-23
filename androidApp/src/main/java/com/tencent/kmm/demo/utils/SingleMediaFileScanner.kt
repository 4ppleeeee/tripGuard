package com.tencent.kmm.demo.utils

import android.content.Context
import android.media.MediaScannerConnection
import android.media.MediaScannerConnection.MediaScannerConnectionClient
import android.net.Uri
import com.tencent.news.core.extension.isFalseOrNull
import com.tencent.news.core.extension.isTrue
import java.io.File

class SingleMediaFileScanner
    (context: Context?, file: File?) : MediaScannerConnectionClient {
    private var msConn: MediaScannerConnection? = null
    private var mFile: File? = null

    init {
        if (file != null && file.exists() && !file.isDirectory()) {
            mFile = file
            msConn = MediaScannerConnection(context, this)
        }
    }

    fun start() {
        if (msConn?.isConnected.isFalseOrNull()) {
            msConn?.connect()
        }
    }

    override fun onMediaScannerConnected() {
        try {
            msConn?.scanFile(mFile?.getAbsolutePath(), null)
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    override fun onScanCompleted(path: String?, uri: Uri?) {
        if (msConn?.isConnected().isTrue()) {
            msConn?.disconnect()
        }
        msConn = null
    }

}