import Foundation

enum IOSSocialLoginSetup {
    private static let qqAppId: String = {
#if ALPHA || DEBUG
        return "101868496"
#else
        return "1101083114"
#endif
    }()

    private static let wxAppId: String = {
#if ALPHA || DEBUG
        return "wx8fcf169ee9630741"
#else
        return "wx5dfbe0a95623607b"
#endif
    }()

    private static let wxUniversalLink: String = {
#if ALPHA || DEBUG
        return "https://isee.weishi.qq.com/wx8fcf169ee9630741/"
#else
        return "https://schema.weishi.qq.com/"
#endif
    }()

    private static let qqScheme: String = "tencent\(qqAppId)"
    private static let weiboScheme: String = "wb"
    private static let weiboAppKey: String = "1269698370"
    // Note: Weibo SDK requires universalLink WITHOUT "https://" prefix (same as microvision)
    private static let weiboUniversalLink: String = "schema.weishi.qq.com"

    private static let weComShareAppId: String = {
#if DEBUG
        return "wwauth40f89068e85f7d48000004"
#else
        return "wwauthc8d2d7a989d28694000026"
#endif
    }()

    private static let weComCorpId: String = {
#if DEBUG
        return "ww40f89068e85f7d48"
#else
        return "wwc8d2d7a989d28694"
#endif
    }()

    private static let weComAgentId: String = {
#if DEBUG
        return "1000004"
#else
        return "1000026"
#endif
    }()

    private static let lock = NSLock()
    private static var didInitQQ = false
    private static var didInitWX = false
    private static var didInitWeibo = false
    private static var didInitWeCom = false

    static var currentQQAppId: String { qqAppId }
    static var currentWXAppId: String { wxAppId }
    static var currentWXUniversalLink: String { wxUniversalLink }
    static var currentWeiboAppKey: String { weiboAppKey }
    static var currentWeiboUniversalLink: String { weiboUniversalLink }
    static var currentWeComShareAppId: String { weComShareAppId }
    static var currentWeComCorpId: String { weComCorpId }
    static var currentWeComAgentId: String { weComAgentId }

    static func setupQQ() -> String {
        lock.lock()
        if didInitQQ {
            lock.unlock()
            return qqAppId
        }
        didInitQQ = true
        lock.unlock()

        ThirdPartyLoginBridge.setupQQ(withAppId: qqAppId)
        return qqAppId
    }

    static func setupWX() -> String {
        lock.lock()
        if didInitWX {
            lock.unlock()
            return wxAppId
        }
        didInitWX = true
        lock.unlock()

        ThirdPartyLoginBridge.setupWX(withAppId: wxAppId, universalLink: wxUniversalLink)
        return wxAppId
    }

    static func setupWeibo() -> String {
        lock.lock()
        if didInitWeibo {
            lock.unlock()
            return weiboAppKey
        }
        didInitWeibo = true
        lock.unlock()

        ThirdPartyLoginBridge.setupWeibo(withAppKey: weiboAppKey, universalLink: weiboUniversalLink)
        return weiboAppKey
    }

    static func setupWeCom() -> String {
        lock.lock()
        if didInitWeCom {
            lock.unlock()
            return weComShareAppId
        }
        didInitWeCom = true
        lock.unlock()

        ThirdPartyLoginBridge.setupWeCom(
            withShareAppId: weComShareAppId,
            corpId: weComCorpId,
            agentId: weComAgentId
        )
        return weComShareAppId
    }

    static func handleOpenURL(_ url: URL) -> Bool {
        ensureInitialized()
        let scheme = url.scheme?.lowercased() ?? ""

        if scheme == wxAppId.lowercased() {
            return ThirdPartyLoginBridge.handleWXOpen(url)
        }

        if scheme == qqScheme.lowercased() {
            return ThirdPartyLoginBridge.handleQQOpen(url)
        }

        if scheme.hasPrefix(weiboScheme) {
            return ThirdPartyLoginBridge.handleWeiboOpen(url)
        }

        if scheme.hasPrefix(weComShareAppId.lowercased()) {
            return ThirdPartyLoginBridge.handleWeComOpen(url)
        }

        return false
    }

    static func handleContinueUserActivity(_ userActivity: NSUserActivity) -> Bool {
        ensureInitialized()

        if ThirdPartyLoginBridge.handleWXUniversalLink(userActivity) {
            return true
        }

        if ThirdPartyLoginBridge.handleWeiboUniversalLink(userActivity) {
            return true
        }

        guard
            userActivity.activityType == NSUserActivityTypeBrowsingWeb,
            let url = userActivity.webpageURL
        else {
            return false
        }

        return ThirdPartyLoginBridge.handleQQUniversalLink(url)
    }

    private static func ensureInitialized() {
        _ = setupQQ()
        _ = setupWX()
        _ = setupWeibo()
        _ = setupWeCom()
    }
}
