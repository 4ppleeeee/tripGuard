package com.tencent.news.core.page.model

import com.tencent.news.core.extension.ResultEx
import com.tencent.news.core.list.controller.FeedsProcessResult
import com.tencent.news.core.list.controller.FeedsRequestEnv
import com.tencent.news.core.tads.constants.INVALID_NUM

sealed class StructPageUiState {
    data class Loading(
        val delayMs: Int = 300,
        val viewType: StructPageLoadingViewType = StructPageLoadingViewType.NORMAL_LOTTIE
    ) : StructPageUiState()

    data class Error(val errorInfo: ErrorInfo) : StructPageUiState()
    data class Success<T>(val response: T) : StructPageUiState()
}

data class StructPageData constructor(
    val pageWidget: StructPageWidget2,
    val feedsResult: FeedsProcessResult,
)

data class StructPageProcessResult(
    val requestEnv: FeedsRequestEnv,
    val newPageWidget: StructPageWidget? = null,
    val feedsResult: FeedsProcessResult? = null,
    val result: ResultEx? = null
)

data class ErrorInfo(
    val code: Int,
    val msg: String? = null,
    val throwable: Throwable? = null,
) {
    companion object {
        fun create(result: ResultEx?): ErrorInfo {
            return ErrorInfo(
                code = result?.errorCode ?: INVALID_NUM,
                msg = result?.msg,
                throwable = result?.error
            )
        }
    }
}

enum class StructPageLoadingViewType {
    EMPTY,
    NORMAL_LOTTIE
}