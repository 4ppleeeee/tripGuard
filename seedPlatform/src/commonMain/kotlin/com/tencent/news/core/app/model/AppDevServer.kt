package com.tencent.news.core.app.model

import com.tencent.news.core.extension.IKmmPure
import com.tencent.news.core.platform.api.HeaderParams
import kotlinx.serialization.Serializable

@Serializable
data class AppDevServer(
    val name: String = "",      // 服务器名，例如：广告开发环境
    val host: String = "",      // 请求的域名，例如：https://dev.example.com/
    val domain: String = "",    // 对应Header里的 Request-Domain，例如：ad_dev_jinkuangyan.epc.webdev.com
    val ip: String = "",        // 对应Header里的 Request-Ip，例如：9.*.*.27
    val envName: String = "",   // 对应Header里的 env-name（后来新增的能力）
) : IKmmPure {

    fun isValid(): Boolean = !(host.isEmpty() && domain.isEmpty() && ip.isEmpty())

    fun toRequestHeaders(): HeaderParams {
        val headers = mutableMapOf<String, String>()
        if (domain.isNotEmpty()) {
            headers[HEADER_REQUEST_DOMAIN] = domain
        }
        if (ip.isNotEmpty()) {
            headers[HEADER_REQUEST_IP] = ip
        }
        if (envName.isNotEmpty()) {
            headers[HEADER_ENV_NAME] = envName
        }
        return headers
    }

    companion object {
        const val HEADER_REQUEST_DOMAIN = "Request-Domain"
        const val HEADER_REQUEST_IP = "Request-Ip"
        const val HEADER_ENV_NAME = "env-name"
    }

}
