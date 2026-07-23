package com.tencent.kmm.startup.std.tasks

import com.tencent.kmm.startup.std.PlatformTask
import com.tencent.kmm.startup.StartupContext
import com.tencent.kmm.startup.StartupScope
import com.tencent.kmm.startup.StartupTask
import com.tencent.kmm.startup.std.trace.TuringLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

data class TuringInitResult(
    val openIdTicket: String = "",
    val aidTicket: String = "",
    val taidTicket: String = "",
    val toaid: String = "",
)

class TuringInitTask(private val initTuring: PlatformTask<TuringInitResult>) : StartupTask {
    override val taskId: String = TASK_ID

    override fun dependencies(): List<String> = listOf(QimeiInitTask.TASK_ID)

    override fun scope(): StartupScope = StartupScope.ASYNC

    override suspend fun execute(context: StartupContext) {
        TuringLog.fileLog("Init", "execute() 开始")
        initTuring(context) { result ->
            TuringLog.debug("Init") {
                "SDK回调 openIdTicket=${result.openIdTicket.isNotEmpty()}" +
                " aidTicket=${result.aidTicket.isNotEmpty()}" +
                " taidTicket=${result.taidTicket.isNotEmpty()}" +
                " oaid=${result.toaid.isNotEmpty()}"
            }
            TuringLog.fileLog("Init", "SDK回调完成, 更新TuringState")
            TuringState.update(result)
            // 异步通过后端接口将 aidTicket 转换为真实 oaid，不阻塞启动流程
            if (result.aidTicket.isNotEmpty()) {
                TuringLog.debug("Init") { "aidTicket非空, 发起fetchOaid后端请求" }
                oaidScope.launch {
                    val oaid = TuringOaidBridge.fetchOaid(result.aidTicket)
                    if (oaid.isNotEmpty()) {
                        TuringLog.fileLog("Init", "fetchOaid成功, 更新oaid")
                        TuringState.updateOaid(oaid)
                    } else {
                        TuringLog.debug("Init") { "fetchOaid返回空, oaid保持不变" }
                    }
                }
            } else {
                TuringLog.debug("Init") { "aidTicket为空, 跳过fetchOaid" }
            }
        }
    }

    companion object {
        const val TASK_ID = "turing"

        /** 独立协程作用域，后端请求失败不影响启动流程 */
        private val oaidScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    }
}
