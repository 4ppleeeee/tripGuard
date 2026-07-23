// 业务层导出
export { loadKnoi, Kommon } from './src/main/ets/kmm/Kommon'
export { KuiklyViewDelegate } from './src/main/ets/kuikly/KuiklyViewDelegate'
export { default as globalNativeManager } from './src/main/ets/kuikly/WsNativeManager'
export { COMPOSE_EVENT_ON_PAGE_NEW_INTENT, OhosComposePageRegistry } from './src/main/ets/kuikly/OhosComposePageRegistry'
export { LogAdapter } from './src/main/ets/kuikly/adapter/LogAdapter'
export { RouterAdapter } from './src/main/ets/kuikly/adapter/RouterAdapter'
export { LoginSDKManager } from './src/main/ets/startup/LoginSDKManager'
export { AppThemeManager } from './src/main/ets/utils/AppThemeManager'
export { WsKmmContext } from './src/main/ets/WsKmmContext'

// kniogen 契约层导出（entry 与第三方 ets 可能用到）
export * from './src/main/ets/kniogen/consumer'
export * from './src/main/ets/kniogen/provider'
export * from './src/main/ets/kniogen/callback'

export * from '@kuikly/render'
