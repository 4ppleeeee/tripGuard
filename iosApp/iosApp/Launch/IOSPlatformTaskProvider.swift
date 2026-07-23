import Foundation
import Darwin
import umbrella
import CocoaLumberjack
#if canImport(romaabtest)
import romaabtest
#endif
#if canImport(MidasIAPSDK)
import MidasIAPSDK
#endif

private let PROFILE_KEY_APP_VERSION = "appVersion"
private struct IOSRomaABRuntimeConfig {
    var appId: String
    var appKey: String
    var sceneId: String
    var appVersion: String
}

private enum IOSBuglySetup {
    private static let uidKey = "__uidForRDMKey__"

    private static let releaseAppId = "d2deefb3f8"
    private static let releaseAppKey = "10e1da1e-ac80-4f66-bbfd-92bd026f3745"
    private static let alphaAppId = "ea029f30e7"
    private static let alphaAppKey = "bc611f5f-d3f5-40ee-bcf9-0a328bf1fa8d"
    private static let debugAppId = "6195c441a6"
    private static let debugAppKey = "45cd3bfc-257e-422a-a201-022e5796d23e"

    static func setup(userId: String?) -> String {
        let appConfig = resolveAppConfig()
        let config = BuglyConfig(appId: appConfig.appId, appKey: appConfig.appKey)
        let uid = userId ?? UserDefaults.standard.string(forKey: uidKey) ?? ""
        let qimei = resolveQimei()

        config.userIdentifier = uid
        config.deviceIdentifier = qimei
        config.appVersion = resolveAppVersion()
        config.buildConfig = appConfig.buildConfig

        Bugly.start(with: config)

        if qimei.isEmpty {
            updateDeviceIdWhenQimeiReady()
        }
        return appConfig.appId
    }

    private static func resolveQimei() -> String {
        QimeiSetup.currentQimei()
    }

    private static func resolveAppConfig() -> (appId: String, appKey: String, buildConfig: BuglyBuildConfig) {
#if DEBUG
        return (debugAppId, debugAppKey, BuglyBuildConfigDebug)
#elseif ALPHA
        // 对齐老微视：仅 DEBUG 为调试构建，其余（含 ALPHA）按发布构建上报。
        return (alphaAppId, alphaAppKey, BuglyBuildConfigRelease)
#else
        return (releaseAppId, releaseAppKey, BuglyBuildConfigRelease)
#endif
    }

    private static func updateDeviceIdWhenQimeiReady() {
        QimeiSetup.getQimeiWithBlock { qimei in
            guard !qimei.isEmpty else { return }
            Bugly.updateDeviceIdentifier(qimei)
        }
    }

    private static func resolveAppVersion() -> String {
        let info = Bundle.main.infoDictionary ?? [:]
        let shortVersion = info["CFBundleShortVersionString"] as? String ?? "1.0"
        guard let commitId = (info["Git commit id"] as? String), !commitId.isEmpty else {
            return shortVersion
        }
        return shortVersion + "_" + String(commitId.prefix(7))
    }
}

private enum IOSReshubSetup {
    private static let appId = "e2a1f61db0"
    private static let appKey = "2e34b780-9b8c-4d8b-91cb-37ffffdeb0fd"
    private static let reshubDebugSwitchKey = "kSwitchResHubTestServer"

    static func setup() -> (appId: String, env: String) {
        let env = currentEnv()
        guard
            let centerClass = NSClassFromString("ResHubCenter") as? NSObject.Type,
            let paramClass = NSClassFromString("ResHubParam") as? NSObject.Type
        else {
            NSLog("[Startup][ResHub][iOS] ResHub not linked, skip init.")
            return (appId, env)
        }

        let sharedSelector = NSSelectorFromString("sharedInstance")
        let initSelector = NSSelectorFromString("initSDK:")
        guard
            centerClass.responds(to: sharedSelector),
            centerClass.instancesRespond(to: initSelector)
        else {
            NSLog("[Startup][ResHub][iOS] ResHub selector not found, skip init.")
            return (appId, env)
        }

        let paramObject = paramClass.init()
        setValueIfSupported(paramObject, value: resolveAppVersion(), key: "appVersion")
        setValueIfSupported(paramObject, value: resolveQimei(), key: "qimei")
        setValueIfSupported(paramObject, value: resolveDeviceType(), key: "deviceType")
        setValueIfSupported(paramObject, value: resolveSystemVersion(), key: "systemVersion")
        setValueIfSupported(paramObject, value: localPresetResPath(), key: "localPresetResPath")
        setValueIfSupported(paramObject, value: true, key: "enableSampleReport")
        setValueIfSupported(paramObject, value: true, key: "enableOnceForProcessReport")
        setValueIfSupported(paramObject, value: true, key: "enableLoadIndependentProfile")
        guard configureDependsIfNeeded(paramObject) else {
            return (appId, env)
        }
#if ALPHA || RDM_DEBUG || DEBUG
        setValueIfSupported(paramObject, value: true, key: "rdmTest")
#else
        setValueIfSupported(paramObject, value: false, key: "rdmTest")
#endif

        guard let center = centerClass.perform(sharedSelector)?.takeUnretainedValue() as? NSObject else {
            NSLog("[Startup][ResHub][iOS] ResHubCenter sharedInstance failed, skip init.")
            return (appId, env)
        }
        _ = center.perform(initSelector, with: paramObject)
        createResHubIfSupported(center: center, env: env)
        return (appId, env)
    }

    private static func currentEnv() -> String {
        UserDefaults.standard.bool(forKey: reshubDebugSwitchKey) ? "test" : "online"
    }

    private static func localPresetResPath() -> String {
        guard let resourcePath = Bundle.main.resourcePath else {
            return ""
        }
        return resourcePath + "/ShiplyResource"
    }

    private static func resolveQimei() -> String {
        let selector = NSSelectorFromString("getQIMEI")
        guard
            let qimeiClass = NSClassFromString("WSQuickQIMEI") as? NSObject.Type,
            qimeiClass.responds(to: selector),
            let qimei = qimeiClass.perform(selector)?.takeUnretainedValue() as? String
        else {
            return ""
        }
        return qimei
    }

    private static func resolveDeviceType() -> String {
        var systemInfo = utsname()
        uname(&systemInfo)
        return withUnsafePointer(to: &systemInfo.machine) { ptr in
            ptr.withMemoryRebound(to: CChar.self, capacity: 1) {
                String(cString: $0)
            }
        }
    }

    private static func resolveSystemVersion() -> String {
        ProcessInfo.processInfo.operatingSystemVersionString
    }

    private static func resolveAppVersion() -> String {
        (Bundle.main.infoDictionary?["CFBundleShortVersionString"] as? String) ?? "1.0"
    }

    private static func createResHubIfSupported(center: NSObject, env: String) {
        let selector = NSSelectorFromString("resHubWithAppId:appKey:env:")
        guard center.responds(to: selector) else {
            return
        }
        let imp = center.method(for: selector)
        typealias Function = @convention(c) (
            AnyObject,
            Selector,
            NSString,
            NSString,
            NSString
        ) -> AnyObject?
        let function = unsafeBitCast(imp, to: Function.self)
        _ = function(
            center,
            selector,
            appId as NSString,
            appKey as NSString,
            env as NSString
        )
    }

    private static func setValueIfSupported(_ object: NSObject, value: Any, key: String) {
        let selectorName = "set\(key.prefix(1).uppercased())\(key.dropFirst()):"
        let selector = NSSelectorFromString(selectorName)
        if object.responds(to: selector) {
            object.setValue(value, forKey: key)
        }
    }

    /// Inject the required `depends` object into ResHubParam.
    /// ResHub SDK requires `param.depends` to be set before `initSDK:`, otherwise it crashes
    /// with "外部配置错误：未设置depends".
    ///
    /// Returns `true` if depends was successfully injected, `false` otherwise.
    /// When returning `false`, the caller should skip `initSDK:` to avoid crash.
    @discardableResult
    private static func configureDependsIfNeeded(_ paramObject: NSObject) -> Bool {
        let dependsSelector = NSSelectorFromString("setDepends:")
        guard paramObject.responds(to: dependsSelector) else {
            NSLog("[Startup][ResHub][iOS] ResHubParam does not respond to setDepends:, skip depends injection.")
            return true // No depends needed, safe to proceed
        }

        let dependsObject = IOSResHubDependImpl()
        paramObject.setValue(dependsObject, forKey: "depends")
        NSLog("[Startup][ResHub][iOS] Injected IOSResHubDependImpl as depends.")
        return true
    }
}

private enum IOSBeaconSetup {
    private static let fallbackAppKey = "0S000EAOIR2GPC95"
    private static let lock = NSLock()
    private static var didInit = false

    static func setup(
        userId: String?,
        appVersion: String,
        channelId: String,
        userAgreePrivacy: Bool,
        enableLog: Bool
    ) -> String {
        let appKey = resolveAppKey()
        lock.lock()
        if didInit {
            lock.unlock()
            return appKey
        }
        didInit = true
        lock.unlock()

        let report = BeaconReport.sharedInstance()
        let config = createConfig()

        setAuditEnable(userAgreePrivacy)
        config.realTimeEventPollingInterval = 0
        if shouldDisableConfigQuery() {
            config.configQueryEnabled = false
        }

        let normalizedUserId = (userId ?? "").trimmingCharacters(in: .whitespacesAndNewlines)
        if !normalizedUserId.isEmpty {
            report.setOpenId(normalizedUserId, forAppKey: appKey)
        }

        let normalizedChannelId = channelId.trimmingCharacters(in: .whitespacesAndNewlines)
        if !normalizedChannelId.isEmpty {
            report.channelId = normalizedChannelId
        }

        report.logLevel = enableLog ? 10 : 0
        report.start(withAppkey: appKey, config: config)

        _ = appVersion // 对齐老微视初始化链路，Beacon iOS 侧不单独透传 appVersion。
        return appKey
    }

    private static func resolveAppKey() -> String {
        if let path = Bundle.main.path(forResource: "BeaconInfo", ofType: "plist"),
           let dict = NSDictionary(contentsOfFile: path) as? [String: Any] {
            for key in ["beacon_main_appkey", "beacon_appkey", "appKey", "appkey"] {
                if let value = dict[key] as? String,
                   !value.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty {
                    return value
                }
            }
        }

        if let info = Bundle.main.infoDictionary {
            for key in ["BEACON_APP_ID", "beacon_main_appkey", "beacon_appkey"] {
                if let value = info[key] as? String,
                   !value.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty {
                    return value
                }
            }
        }
        return fallbackAppKey
    }

    private static func createConfig() -> BeaconReportConfig {
        BeaconReportConfig()
    }

    private static func shouldDisableConfigQuery() -> Bool {
        let version = ProcessInfo.processInfo.operatingSystemVersion
        return version.majorVersion == 14 && version.minorVersion >= 5 && version.minorVersion < 7
    }

    private static func setAuditEnable(_ enabled: Bool) {
        BeaconAuditInterface.setAuditEnable(enabled)
    }
}

private enum IOSMidasSetup {
    private static let offerId = "1450016110"
    private static let sandBoxAppKey = "bEsdG3H5S5k3dTi72X8BM6cmwhHCYHI8"
    private static let releaseAppKey = "Dky2vZx4Q05WrIZsytTTkTPZoKZRDb2L"
    private static let zoneId = "1"
    private static let guestOpenKey = "openKey"
    private static let envStoreKey = "kMidasEnvKey"

    static func setup() -> (initialized: Bool, environment: String, offerId: String) {
#if canImport(MidasIAPSDK)
        let env = resolveEnv()
        let req = MidasIAPBaseReq()
        req.offerId = offerId
        req.openId = resolveOpenId()
        req.openKey = guestOpenKey
        req.sessionId = "hy_gameid"
        req.sessionType = "st_dummy"
        req.zoneId = zoneId
        req.pf = "qq_m_guest-2001-iap-2001"
        req.pfKey = "pfKey"

        MidasIAPApi.setProcess(MIDAS_IAP_PROCESS_LOCAL)
        MidasIAPApi.updateShouldUseGetTransactionInfoSwitch(true)
        MidasIAPApi.enableLog(false)
        MidasIAPApi.setRuntimeDelegate(IOSMidasDelegate.shared)
        let initialized = MidasIAPApi.initialize(
            withReq: req,
            environment: env.name,
            locale: MIDAS_IAP_LOCALE_LOCAL,
            extra: [MIDAS_IAP_APP_EXTRA: "1"],
            reprovide: IOSMidasDelegate.shared
        )
        NSLog(
            "[Startup][Midas][iOS] initialized=\(initialized), offerId=\(offerId), env=\(env.name), appKey=\(env.appKey)"
        )
        return (initialized, env.name, offerId)
#else
        NSLog("[Startup][Midas][iOS] MidasIAPSDK not linked, skip init.")
        return (false, "unavailable", offerId)
#endif
    }

    private static func resolveOpenId() -> String {
        let bundleId = Bundle.main.bundleIdentifier ?? "com.tencent.news.base.app"
        return "guest_\(bundleId)"
    }

    private static func resolveEnv() -> (name: String, appKey: String) {
#if canImport(MidasIAPSDK)
        let rawValue = UserDefaults.standard.integer(forKey: envStoreKey)
        if rawValue == 1 {
            return (MIDAS_IAP_ENV_SANDBOX, sandBoxAppKey)
        }
        return (MIDAS_IAP_ENV_RELEASE, releaseAppKey)
#else
        return ("unavailable", releaseAppKey)
#endif
    }
}

/// iOS 端图灵盾参数缓存管理。
///
/// 本次 SDK 回调完成前，`currentXxxTicket()` 从 commonMain `TuringState`（MMKV）读取上次缓存值兜底。
/// 三端共用同一套持久化逻辑，iOS 端不再单独维护 UserDefaults 缓存。
enum IOSTuringSetup {

    private static let lock = NSLock()
    private static var lastUserId: String = ""
    private static var cachedOpenIdTicket: String = ""
    private static var cachedTaidTicket: String = ""

    static func setup(userId: String = "") {
        let normalizedUserId = normalize(userId)
        NSLog("[Startup][Turing][iOS] setup() userId=%@", normalizedUserId.isEmpty ? "(empty)" : "(non-empty)")

        if normalizedUserId != lastUserId {
            NSLog("[Startup][Turing][iOS] userId变更, 重启风控检测")
            lastUserId = normalizedUserId
            restartRiskDetecting(userId: normalizedUserId)
        }
        refreshFingerprintAsync()
    }

    static func currentOpenIdTicket() -> String {
        lock.lock()
        let cached = cachedOpenIdTicket
        lock.unlock()
        if !cached.isEmpty {
            return cached
        }
        // 从 TuringState（MMKV）读取上次缓存值兜底
        let stored = TuringState.shared.openIdTicket
        NSLog("[Startup][Turing][iOS] currentOpenIdTicket 内存为空, MMKV兜底=%@", stored.isEmpty ? "空" : "有值")
        if !stored.isEmpty {
            lock.lock()
            cachedOpenIdTicket = stored
            lock.unlock()
        }
        return stored
    }

    static func currentTaidTicket() -> String {
        lock.lock()
        let cached = cachedTaidTicket
        lock.unlock()
        if !cached.isEmpty {
            return cached
        }
        // 从 TuringState（MMKV）读取上次缓存值兜底
        let stored = TuringState.shared.taidTicket
        NSLog("[Startup][Turing][iOS] currentTaidTicket 内存为空, MMKV兜底=%@", stored.isEmpty ? "空" : "有值")
        if !stored.isEmpty {
            lock.lock()
            cachedTaidTicket = stored
            lock.unlock()
        }
        return stored
    }

    private static func restartRiskDetecting(userId: String) {
        guard let service = turingService() else { return }
        DispatchQueue.global(qos: .background).async {
            let stopSelector = NSSelectorFromString("stopRiskDetecting")
            if service.responds(to: stopSelector) {
                _ = service.perform(stopSelector)
            }
            let startSelector = NSSelectorFromString("startRiskDetectingWithUserID:withPostRule:")
            if service.responds(to: startSelector) {
                _ = service.perform(startSelector, with: userId as NSString, with: nil)
            }
        }
    }

    private static func refreshFingerprintAsync() {
        guard let service = turingService() else { return }

        let selector = NSSelectorFromString("getFingerprintWithCompletionHandler:")
        guard service.responds(to: selector) else { return }

        let callback: @convention(block) (Any?) -> Void = { content in
            guard let fingerprint = content as? NSObject else {
                NSLog("[Startup][Turing][iOS] fingerprint回调内容为空")
                return
            }
            let openIdTicket = value(of: fingerprint, key: "ticket")
            let taidTicket = value(of: fingerprint, key: "TAIDTicket")

            NSLog("[Startup][Turing][iOS] fingerprint回调 openIdTicket=%@ taidTicket=%@",
                  openIdTicket.isEmpty ? "空" : "有值",
                  taidTicket.isEmpty ? "空" : "有值")

            if !openIdTicket.isEmpty {
                lock.lock()
                cachedOpenIdTicket = openIdTicket
                lock.unlock()
            }

            if !taidTicket.isEmpty {
                lock.lock()
                cachedTaidTicket = taidTicket
                lock.unlock()
            }
        }

        _ = service.perform(selector, with: callback as AnyObject)
    }

    private static func turingService() -> NSObject? {
        guard let serviceClass = NSClassFromString("TuringShield") as? NSObject.Type else {
            NSLog("[Startup][Turing][iOS] TuringShield not linked, skip init.")
            return nil
        }

        let selector = NSSelectorFromString("standardService")
        guard
            serviceClass.responds(to: selector),
            let service = serviceClass.perform(selector)?.takeUnretainedValue() as? NSObject
        else {
            NSLog("[Startup][Turing][iOS] standardService unavailable, skip init.")
            return nil
        }
        return service
    }

    private static func value(of object: NSObject, key: String) -> String {
        let selector = NSSelectorFromString(key)
        guard
            object.responds(to: selector),
            let value = object.perform(selector)?.takeUnretainedValue() as? String
        else {
            return ""
        }
        return normalize(value)
    }

    private static func normalize(_ value: String?) -> String {
        value?.trimmingCharacters(in: .whitespacesAndNewlines) ?? ""
    }
}

#if canImport(MidasIAPSDK)
private final class IOSMidasDelegate: NSObject, MidasIAPPayDelegate, MidasIAPRuntimeDelegate {
    static let shared = IOSMidasDelegate()

    private override init() {}

    func needLogin() {
        NSLog("[Startup][Midas][iOS] login expired, skip interactive relogin at startup.")
    }

    func onResp(_ resp: MidasIAPPayResp!) {
        // 启动阶段仅做 SDK 预初始化，不处理支付回调。
    }

    func onLogOutput(_ log: String) {
        NSLog("[Startup][Midas][iOS] \(log)")
    }
}
#endif

enum IOSRomaABSetup {
    private static let fallbackAppId = "8801"
    private static let qimei36Key = "qimei36"
    private static let lock = NSLock()
    private static var didInit = false
    private static var runtimeConfig = IOSRomaABRuntimeConfig(
        appId: fallbackAppId,
        appKey: "",
        sceneId: "",
        appVersion: ""
    )
#if canImport(romaabtest)
    private static var deviceSDK: RomaABSDK?
#endif

    static func configure(with config: TabExpInitConfig) {
        lock.lock()
        defer { lock.unlock() }
        guard !didInit else {
            return
        }
        runtimeConfig = IOSRomaABRuntimeConfig(
            appId: config.appId.isEmpty ? fallbackAppId : config.appId,
            appKey: config.appKey,
            sceneId: config.sceneId,
            appVersion: config.appVersion
        )
    }

    static func setup() -> String {
        lock.lock()
        let currentConfig = runtimeConfig
        if didInit {
            lock.unlock()
            return currentConfig.appId
        }
        didInit = true
        lock.unlock()

#if canImport(romaabtest)
        let config = RomaABConfig()
        config.userUitType = .UserUnitType_DeviceId
        config.appid = currentConfig.appId
        config.guid = QimeiSetup.currentQimei()
        config.onMonitor = true
        config.userProfiles = [PROFILE_KEY_APP_VERSION: resolveAppVersion(defaultValue: currentConfig.appVersion)]
        if !currentConfig.appKey.isEmpty || !currentConfig.sceneId.isEmpty {
            NSLog("[Startup][RomaAB][iOS] appKey/sceneId are provided but not required by romaabtest iOS sdk.")
        }

        let qimei36 = QimeiSetup.currentQimei36()
        if !qimei36.isEmpty {
            config.extParams = [qimei36Key: qimei36]
        }

        let sdk = RomaABSDK.createSDK(with: config)
        lock.lock()
        deviceSDK = sdk
        lock.unlock()

        sdk.start { success in
            NSLog("[Startup][RomaAB][iOS] start=\(success)")
            sdk.forceUpdateExps { forceSuccess in
                NSLog("[Startup][RomaAB][iOS] forceUpdateExps=\(forceSuccess)")
            }
        }
#else
        NSLog("[Startup][RomaAB][iOS] sdk not linked in current build env, skip init.")
#endif
        return currentConfig.appId
    }

    static func getTabExpInt(key: String, defaultValue: Int) -> Int {
#if canImport(romaabtest)
        lock.lock()
        let sdk = deviceSDK
        lock.unlock()
        guard let sdk else {
            return defaultValue
        }

        let exp = sdk.getCachedExpAndReport(key)
        return parseIntValue(exp: exp, key: key) ?? defaultValue
#else
        return defaultValue
#endif
    }

#if canImport(romaabtest)
    private static func parseIntValue(exp: RomaExp, key: String) -> Int? {
        if let value = valueFromParams(exp: exp, key: key) {
            return normalizeInt(value)
        }
        return nil
    }

    private static func valueFromParams(exp: RomaExp, key: String) -> Any? {
        let rawParams = exp.params
        let params: [String: Any]? = {
            if let map = rawParams as? [String: Any] {
                return map
            }
            if let map = rawParams as? [AnyHashable: Any] {
                return map.reduce(into: [String: Any]()) { result, entry in
                    result[String(describing: entry.key)] = entry.value
                }
            }
            if let json = rawParams as? String,
               let data = json.data(using: .utf8),
               let object = try? JSONSerialization.jsonObject(with: data) as? [String: Any] {
                return object
            }
            return nil
        }()
        guard let params else { return nil }
        if let direct = params[key] {
            return direct
        }
        if let fallback = params["value"] {
            return fallback
        }
        return params.values.first
    }
#endif

    private static func normalizeInt(_ value: Any) -> Int? {
        if let number = value as? NSNumber {
            return number.intValue
        }
        if let string = value as? String {
            return Int(string.trimmingCharacters(in: .whitespacesAndNewlines))
        }
        return nil
    }

    private static func resolveAppVersion(defaultValue: String) -> String {
        if !defaultValue.isEmpty {
            return defaultValue
        }
        return (Bundle.main.infoDictionary?["CFBundleShortVersionString"] as? String) ?? "1.0"
    }
}

private final class IOSNSLogLogger: NSObject, ICommonLog {
    func logV(tag: String, msg: String) {
        log(level: "V", tag: tag, msg: msg)
    }

    func logD(tag: String, msg: String) {
        log(level: "D", tag: tag, msg: msg)
    }

    func logI(tag: String, msg: String) {
        log(level: "I", tag: tag, msg: msg)
    }

    func logW(tag: String, msg: String) {
        log(level: "W", tag: tag, msg: msg)
    }

    func logE(tag: String, msg: String, throwable: KotlinThrowable?) {
        let errorSuffix: String
        if let throwable {
            errorSuffix = " \(String(describing: throwable))"
        } else {
            errorSuffix = ""
        }
        log(level: "E", tag: tag, msg: msg + errorSuffix)
    }

    private func log(level: String, tag: String, msg: String) {
        NSLog("[KMM][%@][%@] %@", level, tag, msg)
    }
}

/// 自定义日志文件管理器，将日志写入 Library/WSFolder 目录
final class WSFolderLogFileManager: DDLogFileManagerDefault {
    convenience init() {
        let libraryDir = NSSearchPathForDirectoriesInDomains(.libraryDirectory, .userDomainMask, true).first!
        let wsFolder = (libraryDir as NSString).appendingPathComponent("WSFolder")
        self.init(logsDirectory: wsFolder)
    }
}

/// 视频日志专用文件管理器，写入 Library/WSFolder/video 子目录（与主日志同父目录）
final class WSVideoFolderLogFileManager: DDLogFileManagerDefault {
    convenience init() {
        let libraryDir = NSSearchPathForDirectoriesInDomains(.libraryDirectory, .userDomainMask, true).first!
        let videoFolder = (libraryDir as NSString).appendingPathComponent("WSFolder/video")
        self.init(logsDirectory: videoFolder)
    }
}

/// 重组分析日志专用文件管理器，写入 Library/WSFolder/rcprofiler 子目录（与主日志同父目录）
final class WSRcProfilerFolderLogFileManager: DDLogFileManagerDefault {
    convenience init() {
        let libraryDir = NSSearchPathForDirectoriesInDomains(.libraryDirectory, .userDomainMask, true).first!
        let rcFolder = (libraryDir as NSString).appendingPathComponent("WSFolder/rcprofiler")
        self.init(logsDirectory: rcFolder)
    }
}

/// 基于 CocoaLumberjack 的文件日志实现，日志写入 Library/WSFolder 目录
private final class IOSDDFileLogger: NSObject, ICommonLog {

    override init() {
        super.init()
        let fileLogger = DDFileLogger(logFileManager: WSFolderLogFileManager())
        // 按天滚动，最多保留 7 天
        fileLogger.rollingFrequency = 60 * 60 * 24
        fileLogger.logFileManager.maximumNumberOfLogFiles = 7
        DDLog.add(fileLogger)
    }

    func logV(tag: String, msg: String) { DDLogVerbose("[\(tag)] \(msg)") }
    func logD(tag: String, msg: String) { DDLogDebug("[\(tag)] \(msg)") }
    func logI(tag: String, msg: String) { DDLogInfo("[\(tag)] \(msg)") }
    func logW(tag: String, msg: String) { DDLogWarn("[\(tag)] \(msg)") }

    func logE(tag: String, msg: String, throwable: KotlinThrowable?) {
        let suffix = throwable.map { " \(String(describing: $0))" } ?? ""
        DDLogError("[\(tag)] \(msg)\(suffix)")
    }
}

/// 视频专用文件日志实现：独立的 DDLog 实例 + 独立 DDFileLogger，
/// 落盘到 Library/WSFolder/video，避免与主日志相互污染。
private final class IOSDDVideoFileLogger: NSObject, ICommonLog {

    /// 独立的 DDLog 实例，仅用于视频日志；不与主 `DDLog.sharedInstance` 共享
    private let videoLog: DDLog = DDLog()

    override init() {
        super.init()
        let fileLogger = DDFileLogger(logFileManager: WSVideoFolderLogFileManager())
        fileLogger.rollingFrequency = 60 * 60 * 24
        fileLogger.logFileManager.maximumNumberOfLogFiles = 7
        videoLog.add(fileLogger)
    }

    private func write(_ flag: DDLogFlag, tag: String, msg: String) {
        let message = DDLogMessage(
            message: "[\(tag)] \(msg)",
            level: .all,
            flag: flag,
            context: 0,
            file: #file,
            function: #function,
            line: #line,
            tag: nil,
            options: [],
            timestamp: Date()
        )
        videoLog.log(asynchronous: true, message: message)
    }

    func logV(tag: String, msg: String) { write(.verbose, tag: tag, msg: msg) }
    func logD(tag: String, msg: String) { write(.debug, tag: tag, msg: msg) }
    func logI(tag: String, msg: String) { write(.info, tag: tag, msg: msg) }
    func logW(tag: String, msg: String) { write(.warning, tag: tag, msg: msg) }
    func logE(tag: String, msg: String, throwable: KotlinThrowable?) {
        let suffix = throwable.map { " \(String(describing: $0))" } ?? ""
        write(.error, tag: tag, msg: msg + suffix)
    }
}

/// 重组分析专用文件日志实现：独立的 DDLog 实例 + 独立 DDFileLogger，
/// 落盘到 Library/WSFolder/rcprofiler，避免与主日志、视频日志相互污染。
private final class IOSDDRcProfilerFileLogger: NSObject, ICommonLog {

    /// 独立的 DDLog 实例，仅用于重组分析日志
    private let rcLog: DDLog = DDLog()

    override init() {
        super.init()
        let fileLogger = DDFileLogger(logFileManager: WSRcProfilerFolderLogFileManager())
        fileLogger.rollingFrequency = 60 * 60 * 24
        fileLogger.logFileManager.maximumNumberOfLogFiles = 7
        rcLog.add(fileLogger)
    }

    private func write(_ flag: DDLogFlag, tag: String, msg: String) {
        let message = DDLogMessage(
            message: "[\(tag)] \(msg)",
            level: .all,
            flag: flag,
            context: 0,
            file: #file,
            function: #function,
            line: #line,
            tag: nil,
            options: [],
            timestamp: Date()
        )
        rcLog.log(asynchronous: true, message: message)
    }

    func logV(tag: String, msg: String) { write(.verbose, tag: tag, msg: msg) }
    func logD(tag: String, msg: String) { write(.debug, tag: tag, msg: msg) }
    func logI(tag: String, msg: String) { write(.info, tag: tag, msg: msg) }
    func logW(tag: String, msg: String) { write(.warning, tag: tag, msg: msg) }
    func logE(tag: String, msg: String, throwable: KotlinThrowable?) {
        let suffix = throwable.map { " \(String(describing: $0))" } ?? ""
        write(.error, tag: tag, msg: msg + suffix)
    }
}

/// 封装 Logger 初始化任务的 KotlinSuspendFunction2 实现
private class LoggerInitSuspendFunction: KotlinSuspendFunction2 {
    func invoke(p1: Any?, p2: Any?, completionHandler: @escaping (Any?, Error?) -> Void) {
        QnPlatformLog.shared.logcat = IOSNSLogLogger()
        QnPlatformLog.shared.fileLog = IOSNSLogLogger()
        QnPlatformLog.shared.videoFileLog = IOSDDVideoFileLogger()
        QnPlatformLog.shared.rcProfilerFileLog = IOSDDRcProfilerFileLogger()
        completionHandler(nil, nil)
    }
}

/// 封装 KuiklyAdapter 初始化任务的 KotlinSuspendFunction2 实现
private class KuiklyAdapterInitSuspendFunction: KotlinSuspendFunction2 {

    func invoke(p1: Any?, p2: Any?, completionHandler: @escaping (Any?, Error?) -> Void) {
        QNKuiklyRenderBridge().setup()
        completionHandler(nil, nil)
    }
}

/// 封装 Qimei 初始化任务的 KotlinSuspendFunction2 实现
private class QimeiInitSuspendFunction: KotlinSuspendFunction2 {

    func invoke(p1: Any?, p2: Any?, completionHandler: @escaping (Any?, Error?) -> Void) {
        QimeiSetup.setup()
        // 通过 onResult 回调返回 QimeiInitResult
        if let onResult = p2 as? OnReceiveStartupTaskResult {
            onResult.invoke(result:
                QimeiInitResult(
                    qimei: QimeiSetup.currentQimei(),
                    qimei36: QimeiSetup.currentQimei36()
                )
            )
        }
        completionHandler(nil, nil)
    }
}

/// 封装图灵盾初始化任务的 KotlinSuspendFunction2 实现
private class TuringInitSuspendFunction: KotlinSuspendFunction2 {

    func invoke(p1: Any?, p2: Any?, completionHandler: @escaping (Any?, Error?) -> Void) {
        // 从 StartupContext 中获取 TuringInitConfig，检查 userAgreePrivacy（与 Android 端对齐）
        let userAgreePrivacy: Bool = {
            guard let context = p1 as? StartupContext else { return true }
            for config in context.configs.values {
                if let turingConfig = config as? TuringInitConfig {
                    return turingConfig.userAgreePrivacy
                }
            }
            return true
        }()

        guard userAgreePrivacy else {
            // 未同意隐私协议，跳过图灵初始化，返回空结果
            NSLog("[Startup][Turing][iOS] 用户未同意隐私协议, 跳过初始化")
            if let onResult = p2 as? OnReceiveStartupTaskResult {
                onResult.invoke(result: TuringInitResult(openIdTicket: "", aidTicket: "", taidTicket: "", toaid: ""))
            }
            completionHandler(nil, nil)
            return
        }

        NSLog("[Startup][Turing][iOS] TuringInitSuspendFunction 开始初始化")
        IOSTuringSetup.setup()
        let openIdTicket = IOSTuringSetup.currentOpenIdTicket()
        let taidTicket = IOSTuringSetup.currentTaidTicket()
        NSLog("[Startup][Turing][iOS] 初始化结果 openIdTicket=%@ taidTicket=%@",
              openIdTicket.isEmpty ? "空" : "有值",
              taidTicket.isEmpty ? "空" : "有值")
        if let onResult = p2 as? OnReceiveStartupTaskResult {
            onResult.invoke(result:
                TuringInitResult(
                    openIdTicket: openIdTicket,
                    // iOS 端图灵 SDK 不提供 AIDTicket，固定传空
                    aidTicket: "",
                    taidTicket: taidTicket,
                    // toaid 由 commonMain 中 stWsGetTuringIDReq 后端请求异步获取
                    toaid: ""
                )
            )
        }
        completionHandler(nil, nil)
    }
}

/// 封装 TAB/Roma AB 初始化任务的 KotlinSuspendFunction2 实现
private class TabExpInitSuspendFunction: KotlinSuspendFunction2 {

    func invoke(p1: Any?, p2: Any?, completionHandler: @escaping (Any?, Error?) -> Void) {
        let appId = IOSRomaABSetup.setup()
        if let onResult = p2 as? OnReceiveStartupTaskResult {
            onResult.invoke(result: TabExpInitResult(appId: appId))
        }
        completionHandler(nil, nil)
    }
}

/// 封装 QQ 登录 SDK 初始化任务的 KotlinSuspendFunction2 实现
private class QQLoginInitSuspendFunction: KotlinSuspendFunction2 {

    func invoke(p1: Any?, p2: Any?, completionHandler: @escaping (Any?, Error?) -> Void) {
        let appId = ((p1 as? StartupContext)?.configs.values.first { $0 is QQLoginInitConfig } as? QQLoginInitConfig)?.appId ?? ""
        if let onResult = p2 as? OnReceiveStartupTaskResult {
            onResult.invoke(result: QQLoginInitResult(appId: appId))
        }
        completionHandler(nil, nil)
    }
}

/// 封装微信登录 SDK 初始化任务的 KotlinSuspendFunction2 实现
private class WXLoginInitSuspendFunction: KotlinSuspendFunction2 {

    func invoke(p1: Any?, p2: Any?, completionHandler: @escaping (Any?, Error?) -> Void) {
        let appId = ((p1 as? StartupContext)?.configs.values.first { $0 is WXLoginInitConfig } as? WXLoginInitConfig)?.appId ?? ""
        if let onResult = p2 as? OnReceiveStartupTaskResult {
            onResult.invoke(result: WXLoginInitResult(appId: appId))
        }
        completionHandler(nil, nil)
    }
}

/// 封装新浪微博分享 SDK 初始化任务的 KotlinSuspendFunction2 实现
private class WeiboShareInitSuspendFunction: KotlinSuspendFunction2 {

    func invoke(p1: Any?, p2: Any?, completionHandler: @escaping (Any?, Error?) -> Void) {
        let appKey = ((p1 as? StartupContext)?.configs.values.first { $0 is WeiboShareInitConfig } as? WeiboShareInitConfig)?.appKey ?? ""
        if let onResult = p2 as? OnReceiveStartupTaskResult {
            onResult.invoke(result: WeiboShareInitResult(appKey: appKey))
        }
        completionHandler(nil, nil)
    }
}

/// 封装企业微信分享 SDK 初始化任务的 KotlinSuspendFunction2 实现
private class WeComShareInitSuspendFunction: KotlinSuspendFunction2 {

    func invoke(p1: Any?, p2: Any?, completionHandler: @escaping (Any?, Error?) -> Void) {
        let shareAppId = ((p1 as? StartupContext)?.configs.values.first { $0 is WeComShareInitConfig } as? WeComShareInitConfig)?.shareAppId ?? ""
        if let onResult = p2 as? OnReceiveStartupTaskResult {
            onResult.invoke(result:
                WeComShareInitResult(
                    shareAppId: shareAppId
                )
            )
        }
        completionHandler(nil, nil)
    }
}

/// 封装 Bugly 初始化任务的 KotlinSuspendFunction2 实现
private class BuglyInitSuspendFunction: KotlinSuspendFunction2 {

    func invoke(p1: Any?, p2: Any?, completionHandler: @escaping (Any?, Error?) -> Void) {
        let appId = IOSBuglySetup.setup(userId: nil)
        if let onResult = p2 as? OnReceiveStartupTaskResult {
            onResult.invoke(result: BuglyInitResult(appId: appId))
        }
        completionHandler(nil, nil)
    }
}

/// 封装 Beacon 初始化任务的 KotlinSuspendFunction2 实现
private class BeaconInitSuspendFunction: KotlinSuspendFunction2 {

    func invoke(p1: Any?, p2: Any?, completionHandler: @escaping (Any?, Error?) -> Void) {
        let appVersion = (Bundle.main.infoDictionary?["CFBundleShortVersionString"] as? String) ?? "1.0"
        // 与 Android 端对齐：从 StartupContext 读取 BeaconInitConfig 中的 userAgreePrivacy，
        // 仅浏览模式下 userAgreePrivacy=false，关闭审计数据采集
        let userAgreePrivacy: Bool = {
            guard let context = p1 as? StartupContext else { return true }
            for config in context.configs.values {
                if let beaconConfig = config as? BeaconInitConfig {
                    return beaconConfig.userAgreePrivacy
                }
            }
            return true
        }()
        let appKey = IOSBeaconSetup.setup(
            userId: nil,
            appVersion: appVersion,
            channelId: "appstore",
            userAgreePrivacy: userAgreePrivacy,
            enableLog: _isDebugAssertConfiguration()
        )
        if let onResult = p2 as? OnReceiveStartupTaskResult {
            onResult.invoke(result: BeaconInitResult(appKey: appKey))
        }
        completionHandler(nil, nil)
    }
}

/// 封装 Reshub 初始化任务的 KotlinSuspendFunction2 实现
private class ReshubInitSuspendFunction: KotlinSuspendFunction2 {
    func invoke(p1: Any?, p2: Any?, completionHandler: @escaping (Any?, Error?) -> Void) {
        let result = IOSReshubSetup.setup()
        if let onResult = p2 as? OnReceiveStartupTaskResult {
            onResult.invoke(result: ReshubInitResult(appId: result.appId, env: result.env))
        }
        completionHandler(nil, nil)
    }
}

/// 封装 Midas 初始化任务的 KotlinSuspendFunction2 实现
private class MidasInitSuspendFunction: KotlinSuspendFunction2 {
    func invoke(p1: Any?, p2: Any?, completionHandler: @escaping (Any?, Error?) -> Void) {
        let result = IOSMidasSetup.setup()
        if let onResult = p2 as? OnReceiveStartupTaskResult {
            onResult.invoke(result:
                MidasInitResult(
                    initialized: result.initialized,
                    platform: "ios"
                )
            )
        }
        completionHandler(nil, nil)
    }
}

/// 封装 Shiply/Toggle 初始化任务的 KotlinSuspendFunction2 实现
private class ToggleInitSuspendFunction: KotlinSuspendFunction2 {
    func invoke(p1: Any?, p2: Any?, completionHandler: @escaping (Any?, Error?) -> Void) {
        let config: ToggleInitConfig? = {
            guard let context = p1 as? StartupContext else { return nil }
            for config in context.configs.values {
                if let toggleConfig = config as? ToggleInitConfig {
                    return toggleConfig
                }
            }
            return nil
        }()
        let result = IOSToggleBridge.setup(config: config)
        if let onResult = p2 as? OnReceiveStartupTaskResult {
            onResult.invoke(result: ToggleInitResult(appId: result.appId, env: result.env))
        }
        completionHandler(nil, nil)
    }
}

/// 封装 MMKV 初始化任务的 KotlinSuspendFunction2 实现
private class KmkvInitSuspendFunction: KotlinSuspendFunction2 {
    func invoke(p1: Any?, p2: Any?, completionHandler: @escaping (Any?, Error?) -> Void) {
        IOSMmkvSetup.setup()
        completionHandler(nil, nil)
    }
}

/// 封装 Lottie 初始化任务的 KotlinSuspendFunction2 实现（iOS no-op）
private class LottieInitSuspendFunction: KotlinSuspendFunction2 {
    func invoke(p1: Any?, p2: Any?, completionHandler: @escaping (Any?, Error?) -> Void) {
        completionHandler(nil, nil)
    }
}

/// 封装设备 OAID 初始化任务的 KotlinSuspendFunction2 实现（iOS no-op，iOS 系统无设备 OAID 概念）
private class OaidInitSuspendFunction: KotlinSuspendFunction2 {
    func invoke(p1: Any?, p2: Any?, completionHandler: @escaping (Any?, Error?) -> Void) {
        if let onResult = p2 as? OnReceiveStartupTaskResult {
            onResult.invoke(result: "")
        }
        completionHandler(nil, nil)
    }
}

/// 封装 VME 上传中台 SDK 初始化任务的 KotlinSuspendFunction2 实现
private class UploadSdkInitSuspendFunction: KotlinSuspendFunction2 {
    func invoke(p1: Any?, p2: Any?, completionHandler: @escaping (Any?, Error?) -> Void) {
        let config: UploadSdkInitConfig? = {
            guard let context = p1 as? StartupContext else { return nil }
            for config in context.configs.values {
                if let uploadConfig = config as? UploadSdkInitConfig {
                    return uploadConfig
                }
            }
            return nil
        }()
        let bizAppId = Int(config?.bizAppId ?? 1047)
        let bizDomain = config?.bizDomain ?? "upload-vma-proxy-wesee.3g.qq.com"
        IOSUploadSdkSetup.setup(bizAppId: bizAppId, bizDomain: bizDomain)
        if let onResult = p2 as? OnReceiveStartupTaskResult {
            onResult.invoke(result: UploadSdkInitResult(bizAppId: Int32(bizAppId), bizDomain: bizDomain))
        }
        completionHandler(nil, nil)
    }
}

private enum IOSUploadSdkSetup {
    static func setup(bizAppId: Int, bizDomain: String) {
        guard let managerClass = NSClassFromString("BDHUploadManager") else {
            NSLog("[UploadSdkInitTask] BDHUploadManager not found")
            return
        }
        let selector = NSSelectorFromString("instance")
        guard (managerClass as AnyObject).responds(to: selector),
              let manager = (managerClass as AnyObject).perform(selector)?.takeUnretainedValue() as? NSObject else {
            NSLog("[UploadSdkInitTask] BDHUploadManager.instance unavailable")
            return
        }
        manager.setValue(NSNumber(value: bizAppId), forKey: "bizAppId")
        manager.setValue(bizDomain, forKey: "bizDomain")
    }
}

/// iOS 平台的 PlatformTaskProvider 实现，提供平台相关的启动任务
class IOSPlatformTaskProvider: PlatformTaskProvider {

    /// Logger 初始化任务
    let loggerInitTask: any KotlinSuspendFunction2 = LoggerInitSuspendFunction()

    /// Kuikly Adapter 初始化任务
    let kuiklyAdapterInitTask: any KotlinSuspendFunction2 = KuiklyAdapterInitSuspendFunction()

    /// Qimei 初始化任务
    let qimeiInitTask: any KotlinSuspendFunction2 = QimeiInitSuspendFunction()

    /// 图灵盾初始化任务
    let turingInitTask: any KotlinSuspendFunction2 = TuringInitSuspendFunction()

    /// TAB/Roma AB 实验 SDK 初始化任务
    let tabExpInitTask: any KotlinSuspendFunction2 = TabExpInitSuspendFunction()

    /// QQ 登录 SDK 初始化任务
    let qqLoginInitTask: any KotlinSuspendFunction2 = QQLoginInitSuspendFunction()

    /// 微信登录 SDK 初始化任务
    let wxLoginInitTask: any KotlinSuspendFunction2 = WXLoginInitSuspendFunction()

    /// 新浪微博分享 SDK 初始化任务
    let weiboShareInitTask: any KotlinSuspendFunction2 = WeiboShareInitSuspendFunction()

    /// 企业微信分享 SDK 初始化任务
    let weComShareInitTask: any KotlinSuspendFunction2 = WeComShareInitSuspendFunction()

    /// Bugly 初始化任务
    let buglyInitTask: any KotlinSuspendFunction2 = BuglyInitSuspendFunction()

    /// Beacon 初始化任务
    let beaconInitTask: any KotlinSuspendFunction2 = BeaconInitSuspendFunction()

    /// Reshub 初始化任务
    let reshubInitTask: any KotlinSuspendFunction2 = ReshubInitSuspendFunction()

    /// Midas 初始化任务
    let midasInitTask: any KotlinSuspendFunction2 = MidasInitSuspendFunction()

    /// MMKV 初始化任务
    let kmkvInitTask: any KotlinSuspendFunction2 = KmkvInitSuspendFunction()

    /// Lottie 初始化任务（iOS no-op）
    let lottieInitTask: any KotlinSuspendFunction2 = LottieInitSuspendFunction()

    /// Shiply/Toggle 初始化任务
    let toggleInitTask: any KotlinSuspendFunction2 = ToggleInitSuspendFunction()

    /// VME 上传中台 SDK 初始化任务
    let uploadSdkInitTask: any KotlinSuspendFunction2 = UploadSdkInitSuspendFunction()

    /// 设备 OAID 初始化任务（iOS no-op，iOS 系统无设备 OAID 概念）
    let oaidInitTask: any KotlinSuspendFunction2 = OaidInitSuspendFunction()
}
