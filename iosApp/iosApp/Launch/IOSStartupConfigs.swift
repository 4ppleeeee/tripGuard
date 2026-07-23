import Foundation
import umbrella
import startupUmbrella

enum IOSStartupConfigs {

    static let appId = Bundle.main.bundleIdentifier ?? "com.tencent.news.base.app"
    static let packageName = appId

    static var appVersion: String {
        Bundle.main.infoDictionary?["CFBundleShortVersionString"] as? String ?? "1.0.0"
    }

    static var buildNumber: String {
        Bundle.main.infoDictionary?["CFBundleVersion"] as? String ?? ""
    }

    static var isDebug: Bool {
        _isDebugAssertConfiguration()
    }

    static func all() -> [any SdkConfig] {
        [
            QimeiInitConfig(
                appKey: "0S000EAOIR285SV7",
                appVersion: appVersion,
                channelId: "appstore",
                isDebug: isDebug,
                enableLog: isDebug,
                userAgreePrivacy: true
            ),
            TuringInitConfig(
                appId: "2082423467",
                channelId: 105428,
                userId: "",
                userAgreePrivacy: true,
                isDebug: isDebug,
                enableLog: isDebug
            ),
            ToggleInitConfig(
                appId: "b0c0957519",
                appKey: "8d278764-9a1a-45e6-bfa7-ca72d2241181",
                appVersion: appVersion,
                userId: "",
                deviceId: "qimei36",
                useTestEnv: false,
                isDebug: false
            ),
            TabExpInitConfig(
                appId: "8801",
                appKey: "97ea3cfb64eeaa1edba65501d0bb3c86",
                sceneId: "",
                appVersion: appVersion
            ),
            BuglyInitConfig(
                appId: "c91a33d0e8",
                appKey: "970e6776-0a85-426e-a97a-742dd4b28556",
                appVersion: appVersion,
                buildNumber: buildNumber,
                appChannel: "appstore",
                userId: "",
                isDebug: isDebug
            ),
            BeaconInitConfig(
                appKey: "0S000EAOIR285SV7",
                appVersion: appVersion,
                channelId: "appstore",
                userId: "",
                userAgreePrivacy: true,
                enableLog: isDebug
            ),
            QQLoginInitConfig(appId: "1101083114"),
            WXLoginInitConfig(appId: "wx5dfbe0a95623607b", universalLink: ""),
            WeiboShareInitConfig(appKey: "1269698370", universalLink: ""),
            WeComShareInitConfig(shareAppId: "wwauthc8d2d7a989d28694000026"),
            ReshubInitConfig(
                appId: "b0c0957519",
                appKey: "8d278764-9a1a-45e6-bfa7-ca72d2241181",
                appVersion: appVersion,
                deviceId: "qimei36",
                useTestEnv: false,
                forceOnlineEnv: false,
                localPresetResPath: "",
                isDebug: isDebug
            )
        ]
    }
}
