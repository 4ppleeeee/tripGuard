package com.tencent.kmm.startup.std.tasks

import com.tencent.kmm.startup.std.trace.TuringLog
import kotlin.concurrent.Volatile

/**
 * 启动阶段产出的图灵盾结果缓存。
 *
 * 线程安全：使用不可变数据类 + @Volatile 保证多线程读取一致性。
 */
object TuringState {

    /**
     * 不可变快照，保证多线程读取时不会出现部分更新的中间状态。
     */
    private data class Snapshot(
        val openIdTicket: String = "",
        val aidTicket: String = "",
        val taidTicket: String = "",
        val oaid: String = "",
    )

    @Volatile
    private var snapshot = Snapshot()

    val openIdTicket: String
        get() = snapshot.openIdTicket

    val aidTicket: String
        get() = snapshot.aidTicket

    val taidTicket: String
        get() = snapshot.taidTicket

    val oaid: String
        get() = snapshot.oaid

    /**
     * 原子性更新全部四个字段。
     */
    fun update(result: TuringInitResult) {
        TuringLog.debug("State") {
            "update() openIdTicket=${result.openIdTicket.isNotEmpty()}" +
            " aidTicket=${result.aidTicket.isNotEmpty()}" +
            " taidTicket=${result.taidTicket.isNotEmpty()}" +
            " oaid=${result.toaid.isNotEmpty()}"
        }
        TuringLog.fileLog("State", "update() 四字段已更新")
        snapshot = Snapshot(
            openIdTicket = result.openIdTicket,
            aidTicket = result.aidTicket,
            taidTicket = result.taidTicket,
            oaid = result.toaid,
        )
    }

    /**
     * 单独更新 oaid（后端 stWsGetTuringIDReq 返回后调用）。
     */
    fun updateOaid(oaid: String) {
        TuringLog.debug("State") { "updateOaid() oaid=${oaid.isNotEmpty()}" }
        TuringLog.fileLog("State", "updateOaid() 已更新")
        snapshot = snapshot.copy(oaid = oaid)
    }
}

object TuringOaidBridge {
    private var fetcher: (suspend (String) -> String)? = null

    fun setFetcher(fetcher: suspend (String) -> String) {
        this.fetcher = fetcher
    }

    suspend fun fetchOaid(aidTicket: String): String {
        if (aidTicket.isEmpty()) {
            return ""
        }
        return runCatching { fetcher?.invoke(aidTicket).orEmpty() }
            .getOrElse { error ->
                TuringLog.error("OaidBridge", "fetchOaid异常", error)
                ""
            }
    }
}
