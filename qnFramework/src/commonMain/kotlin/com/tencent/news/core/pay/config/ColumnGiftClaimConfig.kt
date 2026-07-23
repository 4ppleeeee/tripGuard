package com.tencent.news.core.pay.config

import com.tencent.news.core.extension.IKmmKeep
import com.tencent.news.core.platform.api.getShiplyParsedConfig
import kotlinx.serialization.Serializable

/**
 * 付费专栏免费领取相关 Shiply 配置
 *
 * 全量配置统一存放于一张 Shiply 表：`claim_column_config`，
 * 表内容为一个 JSON 对象（map），结构与 [ColumnGiftClaimRemoteConfig] 一一对应。
 *
 * 通过懒加载（[LazyThreadSafetyMode.PUBLICATION]）缓存首次解析结果，便于运营动态调整；
 * 任一字段缺失或表本身缺失时，自动回落到客户端默认值（[DEFAULT]），禁止线上出现空文案。
 *
 * 注意：初始化会触发一次 Shiply 配置读取，鸿蒙端为同步跨语言调用，
 * 因此显式使用 PUBLICATION 模式避免持锁阻塞导致的跨线程死锁，详见 [remoteConfig] 注释。
 *
 * 归属说明：放在 qnFramework 层，使 qnUser 业务流程与 qnCompose UI 层都能使用，
 * 避免跨业务模块依赖（qnCompose 不允许直接依赖 qnUser）。
 */
object ColumnGiftClaimConfig {

    /** Shiply 配置表表名 */
    private const val SHIPLY_TABLE_NAME = "claim_column_config"

    /** 主标题模板，%s 为价格（元） */
    const val DEFAULT_TITLE_TEMPLATE: String = "恭喜获得价值 %s 元 专栏"

    /** 价格降级（price <= 0）后的主标题 */
    const val FALLBACK_TITLE_NO_PRICE: String = "恭喜获得专栏"

    /** 副标题模板，第一个 %s 为价格（元），第二个 %s 为专栏名称 */
    const val DEFAULT_SUBTITLE_TEMPLATE: String = "价值%s元的专栏《%s》福利已为您保留，请尽快领取"

    /** 默认按钮文案 */
    const val DEFAULT_BUTTON_TEXT: String = "立即领取"

    /** 默认活动规则入口文案 */
    const val DEFAULT_RULE_TEXT: String = "查看活动规则"

    /** 默认活动规则 H5 链接 */
    const val DEFAULT_RULE_URL: String = "https://rule.tencent.com/rule/202404240004"

    /** 兜底专栏名称 */
    const val FALLBACK_COLUMN_NAME: String = "该专栏"

    /** 客户端默认配置（Shiply 整表缺失或字段缺失时的兜底） */
    private val DEFAULT = ColumnGiftClaimRemoteConfig()

    /**
     * Shiply 远端配置（懒加载，首次访问时解析；解析失败回落到默认值）
     *
     * 【线程安全模式说明】此处显式使用 [LazyThreadSafetyMode.PUBLICATION]，
     * 而非默认的 [LazyThreadSafetyMode.SYNCHRONIZED]。原因：
     *
     * `getShiplyParsedConfig` 在鸿蒙端会走到 `OhosConfig.getShiplyConfig` →
     * `RDService_SyncGetRDeliveryDataByKey` 这条**同步跨语言**配置读取链路。
     * 若使用默认 SYNCHRONIZED 模式，初始化期间会持有 lazy 内部的 monitor 锁
     * 并同步等待鸿蒙侧返回；一旦 JS 线程在该同步调用或其相关回调中再次进入
     * KMM 读取本对象任一字段，就会等待同一把 lazy 锁释放，与持锁线程互相
     * 等待，导致 JS 线程卡住、UI 刷新与后续回调阻塞。
     *
     * 改为 PUBLICATION 模式后：
     *  - 并发首次访问可能各自计算一次（最多重复执行少量解析，无副作用、幂等），
     *    但**不会持锁阻塞**，从根上避免上述死锁路径；
     *  - 第一个完成的结果会被发布，后续访问全部命中缓存。
     */
    private val remoteConfig: ColumnGiftClaimRemoteConfig by lazy(LazyThreadSafetyMode.PUBLICATION) {
        getShiplyParsedConfig(SHIPLY_TABLE_NAME, DEFAULT) ?: DEFAULT
    }

    /** 主标题模板（Shiply 优先，缺省回退默认值） */
    val titleTemplate: String
        get() = remoteConfig.titleTemplate.ifEmpty { DEFAULT_TITLE_TEMPLATE }

    /** 副标题模板（Shiply 优先，缺省回退默认值） */
    val subtitleTemplate: String
        get() = remoteConfig.subtitleTemplate.ifEmpty { DEFAULT_SUBTITLE_TEMPLATE }

    /** 按钮文案（Shiply 优先，缺省回退默认值） */
    val buttonText: String
        get() = remoteConfig.buttonText.ifEmpty { DEFAULT_BUTTON_TEXT }

    /** 活动规则入口文案（Shiply 优先，缺省回退默认值） */
    val ruleText: String
        get() = remoteConfig.ruleText.ifEmpty { DEFAULT_RULE_TEXT }

    /** 活动规则 H5 链接，空字符串表示隐藏入口 */
    val ruleUrl: String
        get() = remoteConfig.ruleUrl

    /** 是否展示活动规则入口（URL 非空才展示） */
    fun isRuleEntryEnabled(): Boolean = ruleUrl.isNotEmpty()
}

/**
 * 付费专栏免费领取相关 Shiply 远端配置数据结构
 *
 * 对应 Shiply 表 `claim_column_config`，表内容为一个 JSON 对象，例如：
 * ```json
 * {
 *   "titleTemplate": "恭喜获得价值 %s 元 专栏",
 *   "subtitleTemplate": "价值%s元的专栏《%s》福利已为您保留，请尽快领取",
 *   "buttonText": "立即领取",
 *   "ruleText": "查看活动规则",
 *   "ruleUrl": "https://rule.tencent.com/rule/202404240004"
 * }
 * ```
 *
 * - 任一字段缺失时，由 [ColumnGiftClaimConfig] 自动回落到 `DEFAULT_*` 客户端默认值。
 * - `ruleUrl` 显式为空字符串可用于线上隐藏"活动规则"入口。
 */
@Serializable
data class ColumnGiftClaimRemoteConfig(
    /** 主标题模板，%s 为价格（元） */
    val titleTemplate: String = ColumnGiftClaimConfig.DEFAULT_TITLE_TEMPLATE,
    /** 副标题模板，第一个 %s 为价格（元），第二个 %s 为专栏名称 */
    val subtitleTemplate: String = ColumnGiftClaimConfig.DEFAULT_SUBTITLE_TEMPLATE,
    /** 按钮文案 */
    val buttonText: String = ColumnGiftClaimConfig.DEFAULT_BUTTON_TEXT,
    /** 活动规则入口文案 */
    val ruleText: String = ColumnGiftClaimConfig.DEFAULT_RULE_TEXT,
    /** 活动规则 H5 链接，空字符串表示隐藏入口 */
    val ruleUrl: String = ColumnGiftClaimConfig.DEFAULT_RULE_URL,
) : IKmmKeep

/**
 * 付费专栏免费领取相关 Toast 文案常量
 *
 * 真实文案优先以后端 `pop_msg` 为准，下方仅作客户端兜底使用。
 */
object ColumnGiftClaimToast {
    const val DEFAULT_CLAIM_SUCCESS_TOAST: String = "领取成功"
    const val DEFAULT_CLAIM_FAILED_TOAST: String = "领取失败"
}
