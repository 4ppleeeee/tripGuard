package com.tencent.news.core.page.config

import com.tencent.news.core.extension.IKmmKeep
import com.tencent.news.core.platform.api.getShiplyParsedConfig
import kotlinx.serialization.Serializable

/**
 * 问答专题底部「问新闻妹」提问入口的 Shiply 下发配置。
 *
 * 对齐原 TencentNews `RDConfig.getConfig("QAAIConfig", AskBottomBarConfig::class.java)`：
 * 同一张 Shiply 表 `QAAIConfig`，表内容为 JSON 对象，字段与本数据类一一对应；
 * 任一字段缺失或整表缺失时回落到客户端默认值（[DEFAULT]）。
 *
 * - [enable]：是否启用 AI 问答入口样式/跳转（原 `showAiStyleConfig.enable`）。
 * - [scheme]：AI 模式跳转 scheme；为空时由调用方兜底站内 AI 问答页。
 * - [placeHolder]：按钮提示文案（原 `tvBtnInput` 文案）。
 * - [rightText]：右侧「去提问」文案。
 * - [leftIcon] / [leftIconNight] / [leftIconWidth]：AI 模式左侧图标（网络图，日 / 夜）。
 */
@Serializable
data class AskBottomBarConfig(
    val enable: Boolean = true,
    val scheme: String = "",
    val placeHolder: String = "",
    val rightText: String = "",
    val leftIcon: String = "",
    val leftIconNight: String = "",
    val leftIconWidth: Int = 0,
) : IKmmKeep {

    companion object {
        /** Shiply 配置表名，与原 RDConfig key 保持一致 */
        private const val SHIPLY_TABLE = "QAAIConfig"

        /** 客户端默认配置（Shiply 整表缺失或字段缺失时的兜底） */
        private val DEFAULT = AskBottomBarConfig()

        /**
         * Shiply 下发配置（全局懒加载一次；解析失败回落默认值）。
         *
         * 用 [LazyThreadSafetyMode.PUBLICATION] 避免鸿蒙端同步跨语言读取时持锁阻塞导致的
         * 跨线程死锁，详见 [com.tencent.news.core.pay.config.ColumnGiftClaimConfig] 注释。
         */
        private val cached: AskBottomBarConfig by lazy(LazyThreadSafetyMode.PUBLICATION) {
            getShiplyParsedConfig(SHIPLY_TABLE, DEFAULT) ?: DEFAULT
        }

        /** 读取 QAAIConfig 下发配置（对齐原 `RDConfig.getConfig("QAAIConfig", ...)`） */
        fun shiply(): AskBottomBarConfig = cached
    }
}
