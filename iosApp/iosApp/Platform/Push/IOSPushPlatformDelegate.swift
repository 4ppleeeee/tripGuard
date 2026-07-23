import Foundation
import UIKit
import UserNotifications
import umbrella
// TPNS（XGPush / XGPushTokenManager 等）通过 iosApp-Bridging-Header.h 引入

enum IOSPushLog {
    private static let tag = "Push/iOS"

    static func info(_ msg: String) {
        let fileLog = QnPlatformLog.shared.fileLog
        fileLog?.logI(tag: tag, msg: msg)
        writeConsoleIfNeeded(msg: msg, fileLogReady: fileLog != nil) {
            $0.logI(tag: tag, msg: msg)
        }
    }

    static func warn(_ msg: String) {
        let fileLog = QnPlatformLog.shared.fileLog
        fileLog?.logW(tag: tag, msg: msg)
        writeConsoleIfNeeded(msg: msg, fileLogReady: fileLog != nil) {
            $0.logW(tag: tag, msg: msg)
        }
    }

    static func error(_ msg: String) {
        let fileLog = QnPlatformLog.shared.fileLog
        fileLog?.logE(tag: tag, msg: msg, throwable: nil)
        writeConsoleIfNeeded(msg: msg, fileLogReady: fileLog != nil) {
            $0.logE(tag: tag, msg: msg, throwable: nil)
        }
    }

    private static func writeConsoleIfNeeded(
        msg: String,
        fileLogReady: Bool,
        writer: (ICommonLog) -> Void
    ) {
#if DEBUG || RDM_DEBUG
        if let logcat = QnPlatformLog.shared.logcat {
            writer(logcat)
        } else {
            NSLog("%@", "[\(tag)] \(msg)")
        }
#else
        if !fileLogReady {
            NSLog("%@", "[\(tag)] \(msg)")
        }
#endif
    }
}

/// iOS 端 [IPushPlatformDelegate] 实现：直接调 TPNS-iOS SDK。
///
/// 行为对齐 microvision `AppDelegate+Push.m`：
/// - `start` 调用 `[XGPush startXGWithAccessID:accessKey:delegate:]`，使用宿主注入的
///   TPNS appId/appKey（debug / rdm_debug / alpha / release 均来自 KMP `PushConfig`）
/// - `bindUid` 调用 `[XGPushTokenManager.defaultTokenManager upsertAccountsByDict:]`
/// - `clearBadge` 只清本地桌面角标；后台 push 红点数由 KMP `PushServiceImpl` 统一清理
///
/// XGPushDelegate 的回调（注册 token、收到/点击通知）由 `IOSOpenSDKAppDelegate` 实现，
/// 把 userInfo 透传给 KMP `PushNotificationDispatcher`。
final class IOSPushPlatformDelegate: NSObject, IPushPlatformDelegate {

    private static weak var readyAppDelegate: IOSOpenSDKAppDelegate?
    private static weak var pendingStartDelegate: IOSPushPlatformDelegate?

    static func notifyAppDelegateReady(_ appDelegate: IOSOpenSDKAppDelegate) {
        readyAppDelegate = appDelegate
        IOSPushLog.info(
            "IOSOpenSDKAppDelegate didFinishLaunching ready, pendingStart=\(pendingStartDelegate != nil ? "YES" : "NO")"
        )
        pendingStartDelegate?.startXGIfNeeded(appDelegate: appDelegate)
    }

    private static func currentAppDelegate() -> IOSOpenSDKAppDelegate? {
        readyAppDelegate ?? IOSOpenSDKAppDelegate.shared
    }

    private let keys: PushConfig.IosTpnsKeys
    private let environmentName: String

    init(keys: PushConfig.IosTpnsKeys, environmentName: String) {
        self.keys = keys
        self.environmentName = environmentName
        super.init()
    }

    private var hasStarted = false
    private var startRequested = false

    func start(context: IKmmContext?) {
        guard !hasStarted else {
            IOSPushLog.info("start ignored: TPNS already started env=\(environmentName) appId=\(keys.appId)")
            return
        }
        startRequested = true

        IOSPushLog.info(
            "start requested env=\(environmentName) appId=\(keys.appId) appDelegateReady=\(Self.currentAppDelegate() != nil ? "YES" : "NO")"
        )

        // iOS 端由 dispatcher 主动分发推送跳转。
        PushNotificationDispatcher.shared.setSchemeAutoDispatch(enabled: true)

        if let appDelegate = Self.currentAppDelegate() {
            startXGIfNeeded(appDelegate: appDelegate)
            return
        }

        // SwiftUI `@UIApplicationDelegateAdaptor` 创建时机不稳定。这里不再轮询，也不再以
        // nil delegate 兜底启动；等 IOSOpenSDKAppDelegate.didFinishLaunchingWithOptions
        // 明确通知就位后再真正 startXG，避免 TPNS SDK 缓存 nil delegate 导致回调丢失。
        Self.pendingStartDelegate = self
        IOSPushLog.warn("startXG pending: IOSOpenSDKAppDelegate not ready")
    }

    private func startXGIfNeeded(appDelegate: IOSOpenSDKAppDelegate) {
        guard startRequested else { return }
        guard !hasStarted else { return }
        hasStarted = true
        if Self.pendingStartDelegate === self {
            Self.pendingStartDelegate = nil
        }
        IOSPushLog.info("startXG env=\(environmentName) appId=\(keys.appId) delegateReady=YES")
        invokeStartXG(appDelegate: appDelegate)
        ensureAuthorizedAndRegister()
    }

    private func invokeStartXG(appDelegate: IOSOpenSDKAppDelegate) {
        // KMP `Long` → Swift `Int64`，TPNS appId 都是 16 亿级别，安全 cast 到 UInt32
        XGPush.defaultManager().startXG(
            withAccessID: UInt32(truncatingIfNeeded: keys.appId),
            accessKey: keys.appKey,
            delegate: appDelegate
        )
    }

    func stop() {
        // TPNS-iOS V1.4.0.5 没提供完整退订接口（microvision 老仓也没调过 stopXG）；
        // 但仅复位本地 flag 会让"app 内关闭推送"在 iOS 上失效——APNs 离线通知由系统弹出，
        // dispatcher 拦不到。最小修复：
        //   1. 解绑 TPNS 账号（清账号即从 TPNS 服务端账号→token 关系中摘掉，后端按账号下发的 push 失败）
        //   2. unregisterForRemoteNotifications：本地停止接收 APNs token 更新；系统下次重新 register
        //      才会签发新 token；与"用户主动关闭推送"的预期一致。
        // 注意：unregister 后服务端仍可向旧 token 下发推送（直至 token 失效），所以这是一个
        // 偏前端的兜底；彻底退订仍依赖服务端按账号关停下发。
        startRequested = false
        if Self.pendingStartDelegate === self {
            Self.pendingStartDelegate = nil
        }
        XGPushTokenManager.default().clearAccounts()
        DispatchQueue.main.async {
            UIApplication.shared.unregisterForRemoteNotifications()
        }
        hasStarted = false
    }

    func bindUid(uid: String) {
        guard !uid.isEmpty else { return }
        // 必须先设置 token manager delegate 才能收到 xgPushDidUpsertAccountsByDict 回调
        // （对齐 microvision AppDelegate+Push.m:417 的 xgPushUpdateBindPid 行为）
        if let appDelegate = Self.currentAppDelegate() {
            XGPushTokenManager.default().delegate = appDelegate
        } else {
            IOSPushLog.warn("bindUid: IOSOpenSDKAppDelegate not ready, callback may be lost")
        }
        XGPushTokenManager.default().upsertAccounts(byDict: [NSNumber(value: 0): uid])
    }

    func unbindUid() {
        if let appDelegate = Self.currentAppDelegate() {
            XGPushTokenManager.default().delegate = appDelegate
        } else {
            IOSPushLog.warn("unbindUid: IOSOpenSDKAppDelegate not ready, clear accounts without delegate")
        }
        XGPushTokenManager.default().clearAccounts()
    }

    func clearBadge() {
        DispatchQueue.main.async {
            if #available(iOS 16.0, *) {
                UNUserNotificationCenter.current().setBadgeCount(0) { _ in }
            } else {
                UIApplication.shared.applicationIconBadgeNumber = 0
            }
        }
    }

    func isSystemPushOpen(context: IKmmContext?, callback: @escaping (KotlinBoolean) -> Void) {
        UNUserNotificationCenter.current().getNotificationSettings { settings in
            let granted = settings.authorizationStatus == .authorized ||
                          settings.authorizationStatus == .provisional
            DispatchQueue.main.async {
                callback(KotlinBoolean(value: granted))
            }
        }
    }

    func requestSystemAuthorization(callback: @escaping (KotlinBoolean) -> Void) {
        let center = UNUserNotificationCenter.current()
        center.requestAuthorization(options: [.alert, .badge, .sound]) { granted, _ in
            DispatchQueue.main.async {
                callback(KotlinBoolean(value: granted))
                if granted {
                    UIApplication.shared.registerForRemoteNotifications()
                }
            }
        }
    }

    func openSystemNotificationSettings(showDialog: Bool) {
        // showDialog 由业务侧自行处理引导弹窗（默认直接跳）
        DispatchQueue.main.async {
            guard let url = URL(string: UIApplication.openSettingsURLString),
                  UIApplication.shared.canOpenURL(url) else { return }
            UIApplication.shared.open(url, options: [:], completionHandler: nil)
        }
    }

    /// 启动时确保拿到通知权限并完成 APNs 注册。
    ///
    /// 与 microvision 老仓 `AppDelegate+Push.m` 行为对齐：首次启动如果系统授权还是 `.notDetermined`，
    /// 必须主动 `requestAuthorization` 弹一次系统对话框，否则 APNs 永远不会签发 deviceToken，
    /// TPNS 服务端也拿不到 token → 设备收不到任何 push。
    private func ensureAuthorizedAndRegister() {
        let center = UNUserNotificationCenter.current()
        center.getNotificationSettings { settings in
            let status = settings.authorizationStatus
            IOSPushLog.info("ensureAuthorizedAndRegister status=\(status.rawValue)")
            switch status {
            case .notDetermined:
                IOSPushLog.info("notDetermined -> requestAuthorization")
                center.requestAuthorization(options: [.alert, .badge, .sound]) { granted, error in
                    IOSPushLog.info(
                        "requestAuthorization result granted=\(granted ? "YES" : "NO") err=\(error?.localizedDescription ?? "nil")"
                    )
                    guard granted else { return }
                    DispatchQueue.main.async {
                        IOSPushLog.info("registerForRemoteNotifications after first authorization")
                        UIApplication.shared.registerForRemoteNotifications()
                    }
                }
            case .authorized, .provisional:
                DispatchQueue.main.async {
                    IOSPushLog.info("already authorized, registerForRemoteNotifications")
                    UIApplication.shared.registerForRemoteNotifications()
                }
            case .denied:
                IOSPushLog.warn("notification permission denied, skip registerForRemoteNotifications")
            default:
                if #available(iOS 14.0, *), status == .ephemeral {
                    DispatchQueue.main.async {
                        IOSPushLog.info("ephemeral authorization, registerForRemoteNotifications")
                        UIApplication.shared.registerForRemoteNotifications()
                    }
                } else {
                    IOSPushLog.warn("unknown authorizationStatus=\(status.rawValue), skip")
                }
            }
        }
    }
}
