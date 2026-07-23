package com.tencent.news.core.platform.api

import com.squareup.wire.kmm.Message
import com.squareup.wire.kmm.ProtoAdapter

/**
 * 通过 INetwork 体系发送 PB 请求（协程挂起，抛异常）
 *
 * 通过 Wire ProtoAdapter 自动完成请求编码和响应解码，业务层只需传入 Wire Message 对象。
 *
 * 用法示例：
 * ```kotlin
 * val rsp: StGetRecommendFeedRsp = quickPBSuspend(
 *     url = "trpc.demo.feed/GetRecommendFeed",
 *     requestBody = StGetRecommendFeedReq(attach_info = "xxx"),
 *     requestAdapter = StGetRecommendFeedReq.ADAPTER,
 *     responseAdapter = StGetRecommendFeedRsp.ADAPTER,
 * )
 * ```
 *
 * @param REQ 请求体的 Wire Message 类型
 * @param RSP 响应体的 Wire Message 类型
 */
suspend fun <REQ : Message<REQ, *>, RSP> quickPBSuspend(
    url: String,
    requestBody: REQ,
    requestAdapter: ProtoAdapter<REQ>,
    responseAdapter: ProtoAdapter<RSP>,
    extra: Map<String, String>? = null,
    headers: Map<String, String>? = null,
    connectTimeout: Long = -1,
    readTimeout: Long = -1,
): RSP = quickPBRequestSuspend(
    url = url,
    bodyEncoder = { requestAdapter.encode(requestBody) },
    bodyDecoder = { responseAdapter.decode(it) },
    extra = extra,
    headers = headers,
    connectTimeout = connectTimeout,
    readTimeout = readTimeout,
)

/**
 * 通过 INetwork 体系发送 PB 请求（协程挂起，返回 Result，不抛异常）
 *
 * 用法示例：
 * ```kotlin
 * val result: Result<StGetRecommendFeedRsp> = quickPB(
 *     url = "trpc.demo.feed/GetRecommendFeed",
 *     requestBody = StGetRecommendFeedReq(attach_info = "xxx"),
 *     requestAdapter = StGetRecommendFeedReq.ADAPTER,
 *     responseAdapter = StGetRecommendFeedRsp.ADAPTER,
 * )
 * result.onSuccess { rsp -> ... }
 * result.onFailure { e -> ... }
 * ```
 */
suspend fun <REQ : Message<REQ, *>, RSP> quickPB(
    url: String,
    requestBody: REQ,
    requestAdapter: ProtoAdapter<REQ>,
    responseAdapter: ProtoAdapter<RSP>,
    extra: Map<String, String>? = null,
    headers: Map<String, String>? = null,
    connectTimeout: Long = -1,
    readTimeout: Long = -1,
): Result<RSP> = quickPBRequest(
    url = url,
    bodyEncoder = { requestAdapter.encode(requestBody) },
    bodyDecoder = { responseAdapter.decode(it) },
    extra = extra,
    headers = headers,
    connectTimeout = connectTimeout,
    readTimeout = readTimeout,
)
