import Foundation
import Darwin
import umbrella
import startupUmbrella

enum IOSToggleBridge {
    private static let moduleId = "2"
    private static let productName = "weishi"
    private static let configAppId = "e2a1f61db0"
    private static let configAppKey = "2e34b780-9b8c-4d8b-91cb-37ffffdeb0fd"
    private static let updateInterval: TimeInterval = 4 * 60 * 60
    // RDCONFIG_UPDATE_MODE_APP_START | RDCONFIG_UPDATE_MODE_SCHEDUAL | RDCONFIG_UPDATE_MODE_NETWORK_CHANGE
    private static let updateModeMask = NSNumber(value: 11)
    private static let defaultAnonymousUserId = "999"

    private struct RuntimeConfig {
        let appId: String
        let appKey: String
        /// `true` 时只切到 Shiply 测试逻辑环境；是否按 debug 包上报由 `isDebugPackage` 单独控制。
        let useTestEnv: Bool
        let isDebugPackage: Bool
    }

    private static let lock = NSLock()
    private static var didSetup = false
    private static let runtimeLock = NSLock()
    private static var runtime: RDeliverySDK?
    private static var runtimeSettings: RDeliverySDKSettings?
    private static var runtimeConfig = RuntimeConfig(
        appId: configAppId,
        appKey: configAppKey,
        useTestEnv: false,
        isDebugPackage: false
    )
    private static var runtimeReleaseMode = true
    private static var runtimeQimei = ""
    private static var runtimeUserId = ""

    static func setup(config: ToggleInitConfig? = nil, userId: String = "") -> (appId: String, env: String) {
        setupIfNeeded(config: config, userId: userId, force: true)
        return (snapshotConfig().appId, currentEnv())
    }

    static func getShiplySwitch(key: String, defaultValue: Bool) -> Bool {
        setupIfNeeded(force: false)
        guard let sdk = snapshotRuntime(), !key.isEmpty else {
            return defaultValue
        }
        let selector = NSSelectorFromString("isSwitchOn:defaultValue:")
        guard sdk.responds(to: selector) else {
            return defaultValue
        }
        typealias Function = @convention(c) (AnyObject, Selector, NSString, Bool) -> Bool
        let function = unsafeBitCast(sdk.method(for: selector), to: Function.self)
        return function(sdk, selector, key as NSString, defaultValue)
    }

    static func getShiplyConfig(key: String, defaultValue: String) -> String {
        setupIfNeeded(force: false)
        guard let sdk = snapshotRuntime(), !key.isEmpty else {
            return defaultValue
        }
        let selector = NSSelectorFromString("stringValueWithKey:defaultValue:")
        guard sdk.responds(to: selector) else {
            return defaultValue
        }
        typealias Function = @convention(c) (
            AnyObject,
            Selector,
            NSString,
            NSString
        ) -> Unmanaged<AnyObject>?
        let function = unsafeBitCast(sdk.method(for: selector), to: Function.self)
        let value = function(
            sdk,
            selector,
            key as NSString,
            defaultValue as NSString
        )?.takeUnretainedValue() as? NSString
        return value as String? ?? defaultValue
    }

    static func getShiplyInt(key: String, defaultValue: Int32) -> Int32 {
        setupIfNeeded(force: false)
        guard let sdk = snapshotRuntime(), !key.isEmpty else {
            return defaultValue
        }
        let selector = NSSelectorFromString("intValueWithKey:defaultValue:")
        guard sdk.responds(to: selector) else {
            return defaultValue
        }
        typealias Function = @convention(c) (AnyObject, Selector, NSString, Int) -> Int
        let function = unsafeBitCast(sdk.method(for: selector), to: Function.self)
        return Int32(function(sdk, selector, key as NSString, Int(defaultValue)))
    }

    static func module() -> String {
        return moduleId
    }

    static func name() -> String {
        return productName
    }

    private static func setupIfNeeded(config: ToggleInitConfig? = nil, userId: String = "", force: Bool) {
        lock.lock()
        defer { lock.unlock() }
        if didSetup && !force {
            return
        }
        applyConfig(config)
        let activeConfig = runtimeConfig
        bootstrapRuntime(
            QimeiSetup.currentQimei36(),
            resolveUserId(userId),
            resolveAppVersion(),
            ProcessInfo.processInfo.operatingSystemVersionString,
            resolveDeviceType(),
            !activeConfig.useTestEnv,
            activeConfig
        )
        didSetup = true
    }

    private static func currentEnv() -> String {
        runtimeLock.lock()
        defer { runtimeLock.unlock() }
        return runtimeReleaseMode ? "online" : "test"
    }

    private static func snapshotConfig() -> RuntimeConfig {
        runtimeLock.lock()
        defer { runtimeLock.unlock() }
        return runtimeConfig
    }

    private static func resolveAppVersion() -> String {
        return (Bundle.main.infoDictionary?["CFBundleShortVersionString"] as? String) ?? "1.0"
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

    private static func bootstrapRuntime(
        _ qimei: String,
        _ userId: String,
        _ appVersion: String,
        _ systemVersion: String,
        _ deviceType: String,
        _ releaseMode: Bool,
        _ config: RuntimeConfig
    ) {
        let normalizedQimei = normalize(qimei)
        let normalizedUserId = normalize(userId)
        let normalizedVersion = normalize(appVersion)
        let normalizedSystemVersion = normalize(systemVersion)
        let normalizedDeviceType = normalize(deviceType)

        runtimeLock.lock()
        defer { runtimeLock.unlock() }

        if runtime == nil {
            guard
                let depends = buildDepends(),
                let settings = buildSettings(userId: normalizedUserId, depends: depends, config: config)
            else {
                NSLog("[Startup][Toggle][iOS] init runtime settings failed.")
                return
            }

            applyRuntimeSettings(
                settings: settings,
                qimei: normalizedQimei,
                appVersion: normalizedVersion,
                systemVersion: normalizedSystemVersion,
                deviceType: normalizedDeviceType,
                releaseMode: releaseMode,
                isDebugPackage: config.isDebugPackage
            )

            guard
                let sdk = buildRuntime(settings: settings)
            else {
                NSLog("[Startup][Toggle][iOS] init runtime failed.")
                return
            }

            runtimeSettings = settings
            runtime = sdk
            runtimeConfig = config
            runtimeReleaseMode = releaseMode
            runtimeQimei = normalizedQimei
            runtimeUserId = normalizedUserId
            return
        }

        guard let sdk = runtime else {
            return
        }

        if runtimeUserId != normalizedUserId {
            switchUser(sdk: sdk, userId: normalizedUserId)
            runtimeUserId = normalizedUserId
        }
        if !normalizedQimei.isEmpty && runtimeQimei != normalizedQimei {
            updateQimei(sdk: sdk, qimei: normalizedQimei)
            runtimeQimei = normalizedQimei
        }
        if runtimeReleaseMode != releaseMode {
            switchEnv(sdk: sdk, releaseMode: releaseMode)
            runtimeReleaseMode = releaseMode
        }
        if let settings = runtimeSettings {
            applyRuntimeSettings(
                settings: settings,
                qimei: normalizedQimei,
                appVersion: normalizedVersion,
                systemVersion: normalizedSystemVersion,
                deviceType: normalizedDeviceType,
                releaseMode: releaseMode,
                isDebugPackage: config.isDebugPackage
            )
        }
        runtimeConfig = config
    }

    private static func snapshotRuntime() -> RDeliverySDK? {
        runtimeLock.lock()
        defer { runtimeLock.unlock() }
        return runtime
    }

    private static func normalize(_ value: String) -> String {
        return value.isEmpty ? "" : value
    }

    private static func buildDepends() -> RDeliveryDepends? {
        let depends = RDeliveryDepends()
        depends.httpImpl = RDNetworkImpl.sharedInstance()
        depends.kvImpl = RDMMKVFactoryImpl.sharedInstance()
        depends.logImpl = RDLoggerImpl.sharedInstance()
        depends.jsonModelImpl = RDeliveryJsonModelImpl.sharedInstance()
        return depends
    }

    private static func buildSettings(
        userId: String,
        depends: RDeliveryDepends,
        config: RuntimeConfig
    ) -> RDeliverySDKSettings? {
        let selector = NSSelectorFromString("settingWithAppId:appKey:guid:depends:")
        guard RDeliverySDKSettings.responds(to: selector) else {
            return nil
        }
        typealias Function = @convention(c) (
            AnyObject,
            Selector,
            NSString,
            NSString,
            NSString,
            AnyObject
        ) -> Unmanaged<AnyObject>?
        let function = unsafeBitCast(RDeliverySDKSettings.method(for: selector), to: Function.self)
        let result = function(
            RDeliverySDKSettings.self,
            selector,
            config.appId as NSString,
            config.appKey as NSString,
            userId as NSString,
            depends
        )?.takeUnretainedValue()
        return result as? RDeliverySDKSettings
    }

    private static func buildRuntime(settings: RDeliverySDKSettings) -> RDeliverySDK? {
        let selector = NSSelectorFromString("createSDKWithSettings:")
        guard RDeliverySDK.responds(to: selector) else {
            return nil
        }
        typealias Function = @convention(c) (AnyObject, Selector, AnyObject) -> Unmanaged<AnyObject>?
        let function = unsafeBitCast(RDeliverySDK.method(for: selector), to: Function.self)
        let result = function(
            RDeliverySDK.self,
            selector,
            settings
        )?.takeUnretainedValue()
        return result as? RDeliverySDK
    }

    private static func applyRuntimeSettings(
        settings: RDeliverySDKSettings,
        qimei: String,
        appVersion: String,
        systemVersion: String,
        deviceType: String,
        releaseMode: Bool,
        isDebugPackage: Bool
    ) {
        settings.qimei = qimei
        settings.appVersion = appVersion
        settings.systemVersion = systemVersion
        settings.deviceType = deviceType
        settings.envId = releaseMode ? RDeliveryReleaseEnvId : RDeliveryTestEnvId
        settings.isDebugPackage = isDebugPackage
        settings.updateDuration = updateInterval
        settings.setValue(updateModeMask, forKey: "updateMode")
    }

    private static func switchUser(sdk: RDeliverySDK, userId: String) {
        let selector = NSSelectorFromString("switchGuid:")
        guard sdk.responds(to: selector) else {
            return
        }
        typealias Function = @convention(c) (AnyObject, Selector, NSString) -> Void
        let function = unsafeBitCast(sdk.method(for: selector), to: Function.self)
        function(sdk, selector, userId as NSString)
        requestFullRemoteDataIfSupported(sdk: sdk)
    }

    private static func updateQimei(sdk: RDeliverySDK, qimei: String) {
        let selector = NSSelectorFromString("setQimei:")
        guard sdk.responds(to: selector) else {
            return
        }
        typealias Function = @convention(c) (AnyObject, Selector, NSString) -> Void
        let function = unsafeBitCast(sdk.method(for: selector), to: Function.self)
        function(sdk, selector, qimei as NSString)
    }

    private static func switchEnv(sdk: RDeliverySDK, releaseMode: Bool) {
        let selector = NSSelectorFromString("switchEnvironment:")
        guard sdk.responds(to: selector) else {
            return
        }
        typealias Function = @convention(c) (AnyObject, Selector, NSString) -> Void
        let function = unsafeBitCast(sdk.method(for: selector), to: Function.self)
        let envId = releaseMode ? RDeliveryReleaseEnvId : RDeliveryTestEnvId
        function(sdk, selector, envId as NSString)
    }

    private static func requestFullRemoteDataIfSupported(sdk: RDeliverySDK) {
        // 老版 RDeliverySDK 使用 nullable block 作为刷新回调；这里只触发刷新，不持有回调状态。
        let selector = NSSelectorFromString("updateConfigWithCompleteHandler:")
        guard sdk.responds(to: selector) else {
            return
        }
        typealias Function = @convention(c) (AnyObject, Selector, AnyObject?) -> Void
        let function = unsafeBitCast(sdk.method(for: selector), to: Function.self)
        function(sdk, selector, nil)
    }

    private static func updateUserId(_ userId: String) {
        runtimeLock.lock()
        defer { runtimeLock.unlock() }
        guard let sdk = runtime else {
            return
        }
        if runtimeUserId != userId {
            switchUser(sdk: sdk, userId: userId)
            runtimeUserId = userId
            return
        }
        requestFullRemoteDataIfSupported(sdk: sdk)
    }

    private static func applyConfig(_ config: ToggleInitConfig?) {
        guard let config = config else {
            return
        }
        runtimeConfig = RuntimeConfig(
            appId: normalize(config.appId).isEmpty ? configAppId : config.appId,
            appKey: normalize(config.appKey).isEmpty ? configAppKey : config.appKey,
            useTestEnv: config.useTestEnv,
            isDebugPackage: config.isDebug
        )
    }

    private static func resolveUserId(_ userId: String) -> String {
        let configuredUserId = normalize(userId)
        if !configuredUserId.isEmpty {
            return configuredUserId
        }
        return defaultAnonymousUserId
    }
}
