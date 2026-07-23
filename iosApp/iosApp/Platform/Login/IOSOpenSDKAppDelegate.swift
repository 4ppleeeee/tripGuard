import UIKit
import UserNotifications
import umbrella
// TPNS（XGPush / XGPushTokenManager / XGPushDelegate / XGPushTokenManagerDelegate 等）
// 通过 iosApp-Bridging-Header.h 引入，不需要 Swift module import。

final class IOSOpenSDKAppDelegate: NSObject, UIApplicationDelegate, WXApiDelegate, XGPushDelegate, XGPushTokenManagerDelegate {

    /// SwiftUI `@UIApplicationDelegateAdaptor` 持有的实例引用，
    /// 供 `IOSPushPlatformDelegate.start` 把自己设为 `XGPushDelegate` /
    /// `XGPushTokenManagerDelegate`（账号绑定回调走后者）。
    static weak var shared: IOSOpenSDKAppDelegate?

    override init() {
        super.init()
        IOSOpenSDKAppDelegate.shared = self
    }

    /// 全局方向控制标志位（参考旧 app AppDelegate 方案）
    /// 默认仅竖屏，只有代码调用 setScreenOrientation(.landscape) 时才切横屏
    static var isLandscape: Bool = false

    // MARK: - UIApplicationDelegate 方向控制

    func application(
        _ application: UIApplication,
        supportedInterfaceOrientationsFor window: UIWindow?
    ) -> UIInterfaceOrientationMask {
        if Self.isLandscape {
            return .landscapeRight
        }
        return .portrait
    }

    func application(
        _ application: UIApplication,
        open url: URL,
        options: [UIApplication.OpenURLOptionsKey: Any] = [:]
    ) -> Bool {
        handleIncomingURL(url)
    }

    func application(
        _ application: UIApplication,
        continue userActivity: NSUserActivity,
        restorationHandler: @escaping ([UIUserActivityRestoring]?) -> Void
    ) -> Bool {
        handleIncomingUserActivity(userActivity)
    }

    /// 兼容 SwiftUI Scene 生命周期的 URL 回调入口。
    func handleIncomingURL(_ url: URL) -> Bool {
        if url.scheme?.lowercased() == "weishi" {
            handleWeishiScheme(url.absoluteString)
            return true
        }

        return IOSSocialLoginSetup.handleOpenURL(url)
    }

    /// 兼容 SwiftUI Scene 生命周期的 Universal Link 回调入口。
    func handleIncomingUserActivity(_ userActivity: NSUserActivity) -> Bool {
        if IOSSocialLoginSetup.handleContinueUserActivity(userActivity) {
            return true
        }
        guard
            userActivity.activityType == NSUserActivityTypeBrowsingWeb,
            let webpageURL = userActivity.webpageURL,
            isWeishiUniversalLink(webpageURL)
        else {
            return false
        }
        handleWeishiScheme(webpageURL.absoluteString)
        return true
    }

    // MARK: - Weishi Scheme Handling

    private func handleWeishiScheme(_ uri: String) {
        NSLog("[ExternalScheme][iOS] scheme ignored after business router removal: %@", uri)
    }

    private func isWeishiUniversalLink(_ url: URL) -> Bool {
        guard url.scheme?.lowercased() == "https" else {
            return false
        }
        let host = url.host?.lowercased()
        return host == "schema.weishi.qq.com" || host == "isee.weishi.qq.com"
    }

    // MARK: - WXApiDelegate

    func onResp(_ resp: BaseResp) {
    }

    func onReq(_ req: BaseReq) {
    }

    // MARK: - APNs DeviceToken

    func application(
        _ application: UIApplication,
        didRegisterForRemoteNotificationsWithDeviceToken deviceToken: Data
    ) {
        // TPNS SDK 内部会 hook UIApplication.didRegisterForRemoteNotifications 拿到 deviceToken
        // 后自行注册（参考 microvision AppDelegate+Push.m 也没有手动转发），这里仅占位日志。
        IOSPushLog.info("APNs deviceToken received, length=\(deviceToken.count)")
    }

    func application(
        _ application: UIApplication,
        didFailToRegisterForRemoteNotificationsWithError error: Error
    ) {
        IOSPushLog.error("APNs register failed: \(error.localizedDescription)")
    }

    /// 冷启动场景：launchOptions 中可能包含 push userInfo，转给 dispatcher 暂存
    func application(
        _ application: UIApplication,
        didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?
    ) -> Bool {
        IOSPushPlatformDelegate.notifyAppDelegateReady(self)
        if let userInfo = launchOptions?[.remoteNotification] as? [AnyHashable: Any] {
            forwardClick(userInfo: userInfo, launchType: .coldLaunch)
        }
        return true
    }

    // MARK: - XGPushDelegate

    /// TPNS Token 注册成功
    func xgPushDidRegisteredDeviceToken(
        _ deviceToken: String?,
        xgToken: String?,
        error: Error?
    ) {
        if let err = error {
            IOSPushLog.error("xgPushDidRegisteredDeviceToken FAILED err=\(err.localizedDescription)")
            PushNotificationDispatcher.shared.onTokenRegistered(
                success: false,
                tpnsToken: "",
                factoryToken: "",
                error: "\(err.localizedDescription)"
            )
            return
        }
        let resolvedXgToken = xgToken ?? ""
        let resolvedDeviceToken = deviceToken ?? ""
        IOSPushLog.info(
            "xgPushDidRegisteredDeviceToken OK xgTokenLen=\(resolvedXgToken.count) deviceTokenLen=\(resolvedDeviceToken.count) xgToken=\(resolvedXgToken) deviceToken=\(resolvedDeviceToken)"
        )
        PushNotificationDispatcher.shared.onTokenRegistered(
            success: true,
            tpnsToken: resolvedXgToken,
            factoryToken: resolvedDeviceToken,
            error: nil
        )
    }

    /// 收到通知（前台 / 离线推送）
    func xgPushDidReceiveRemoteNotification(
        _ notification: Any,
        withCompletionHandler completionHandler: ((UInt) -> Void)?
    ) {
        let userInfo = Self.userInfo(from: notification)
        let userInfoKeys = userInfo.keys.map { "\($0)" }.joined(separator: ",")
        IOSPushLog.info("xgPushDidReceiveRemoteNotification userInfoKeys=\(userInfoKeys)")
        let raw = Self.flatten(userInfo: userInfo)
        PushNotificationDispatcher.shared.onMessageShown(
            rawMap: raw,
            pushTimeMillis: Int64(Date().timeIntervalSince1970 * 1000)
        )
        // 前台展示 banner + 声音 + 角标。如果产品要求前台不展示通知，
        // 把下面的 options 改为 [] 或屏蔽掉 completionHandler 调用。
        let options: UNNotificationPresentationOptions
        if #available(iOS 14.0, *) {
            options = [.banner, .list, .sound, .badge]
        } else {
            options = [.alert, .sound, .badge]
        }
        completionHandler?(UInt(options.rawValue))
    }

    /// 通知点击回调
    func xgPushDidReceiveNotificationResponse(
        _ response: Any,
        withCompletionHandler completionHandler: @escaping () -> Void
    ) {
        let userInfo = Self.userInfo(from: response)
        let userInfoKeys = userInfo.keys.map { "\($0)" }.joined(separator: ",")
        IOSPushLog.info("xgPushDidReceiveNotificationResponse userInfoKeys=\(userInfoKeys)")
        forwardClick(userInfo: userInfo, launchType: .hotLaunch)
        completionHandler()
    }

    /// TPNS 服务端下发角标设置结果回调，对齐旧版 iOS `xgPushDidSetBadge:error:` 监控。
    func xgPushDidSetBadge(_ isSuccess: Bool, error: Error?) {
        if isSuccess {
            IOSPushLog.info("xgPushDidSetBadge success")
        } else {
            IOSPushLog.error("xgPushDidSetBadge failed err=\(error?.localizedDescription ?? "nil")")
        }
    }

    /// 账号绑定回调
    func xgPushDidUpsertAccounts(byDict accountsDict: [AnyHashable: Any], error: Error?) {
        let xgToken = XGPushTokenManager.default().xgTokenString ?? ""
        let factoryToken = XGPushTokenManager.default().deviceTokenString ?? ""
        IOSPushLog.info(
            "xgPushDidUpsertAccounts success=\(error == nil ? "YES" : "NO") err=\(error?.localizedDescription ?? "nil") xgTokenLen=\(xgToken.count) deviceTokenLen=\(factoryToken.count) xgToken=\(xgToken) deviceToken=\(factoryToken)"
        )
        PushNotificationDispatcher.shared.onAccountBound(
            success: error == nil,
            tpnsToken: xgToken,
            factoryToken: factoryToken,
            error: error?.localizedDescription
        )
    }

    // MARK: - Helpers

    private func forwardClick(userInfo: [AnyHashable: Any], launchType: PushLaunchType) {
        let raw = Self.flatten(userInfo: userInfo)
        PushNotificationDispatcher.shared.onMessageClicked(rawMap: raw, launchType: launchType)
    }

    /// TPNS 的 notification / response 既可能是 NSDictionary 也可能是 UNNotificationResponse
    private static func userInfo(from raw: Any) -> [AnyHashable: Any] {
        if let response = raw as? UNNotificationResponse {
            return response.notification.request.content.userInfo
        }
        if let notification = raw as? UNNotification {
            return notification.request.content.userInfo
        }
        if let dict = raw as? [AnyHashable: Any] {
            return dict
        }
        return [:]
    }

    /// 把 NSDictionary 拍平成 KMP 期望的 [String: String]
    /// 嵌套 dict / array 用 JSON 字符串序列化（PushPayloadParser 会再解析 `custom` 字段）。
    private static func flatten(userInfo: [AnyHashable: Any]) -> [String: String] {
        var result: [String: String] = [:]
        for (key, value) in userInfo {
            guard let keyStr = key as? String else { continue }
            switch value {
            case let s as String:
                result[keyStr] = s
            case let n as NSNumber:
                result[keyStr] = n.stringValue
            case let dict as [AnyHashable: Any]:
                if let data = try? JSONSerialization.data(withJSONObject: dict),
                   let json = String(data: data, encoding: .utf8) {
                    result[keyStr] = json
                }
            case let arr as [Any]:
                if let data = try? JSONSerialization.data(withJSONObject: arr),
                   let json = String(data: data, encoding: .utf8) {
                    result[keyStr] = json
                }
            default:
                result[keyStr] = "\(value)"
            }
        }
        return result
    }
}
