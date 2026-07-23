import Foundation
import MMKV
import Network
import UIKit
import umbrella

/// iOS 端 IAppStatus 实现
/// 核心职责：监听系统深色/浅色模式切换并通知 KMP 侧
final class IOSAppStatus: NSObject, IAppStatus {
    func isSystemNightMode() -> Bool {
        return isNightMode()
    }

    func isSupportFollowSystemBackgroundSetting() -> Bool {
        return false
    }

    // MARK: - 主题订阅

    private var themeCallbackRegistered = false
    private var lastKnownDarkMode: Bool?
    private var themeChangedListeners: [(KotlinBoolean) -> Void] = []
    private var textScaleGradient: DensityScaleGradient = IOSAppStatus.loadStoredTextScaleGradient()
    private var textScaleChangedListeners: [(KotlinDouble) -> Void] = []
    private let networkMonitor = NWPathMonitor()
    private let networkMonitorQueue = DispatchQueue(label: "com.tencent.news.base.app.ios.appStatus.network")
    private let netStatusLock = NSLock()
    private var netStatusListeners: [any NetStateChangeListener] = []
    private var networkMonitorStarted = false
    private var hasReceivedInitialNetworkPath = false
    private var currentNetState: NetState = .inavailable

    func subscribeTheme(onThemeChanged: @escaping (KotlinBoolean) -> Void) {
        themeChangedListeners.append(onThemeChanged)
        ensureThemeCallbackRegistered()
    }

    private func ensureThemeCallbackRegistered() {
        guard !themeCallbackRegistered else { return }

        let currentStyle = currentUserInterfaceStyle()
        lastKnownDarkMode = (currentStyle == .dark)

        // 监听系统深色模式切换通知（iOS 17+ 使用 UITraitChangeHandler，
        // 但为了兼容 iOS 13+，使用 UIScreen.didChangeNotification 或自定义 Window 监听）
        // 这里通过一个轻量级的 TraitChangeHelper 实现
        let helper = TraitChangeHelper { [weak self] isDark in
            guard let self = self else { return }
            guard self.lastKnownDarkMode != isDark else { return }
            self.lastKnownDarkMode = isDark
            let listeners = self.themeChangedListeners
            listeners.forEach { listener in
                listener(KotlinBoolean(bool: isDark))
            }
        }
        // 将 helper 挂载到 keyWindow 上，确保能感知 traitCollection 变化
        DispatchQueue.main.async {
            helper.attachToWindow()
        }
        // 持有引用防止释放
        Self.traitChangeHelper = helper
        themeCallbackRegistered = true
    }

    private static var traitChangeHelper: TraitChangeHelper?

    private func currentUserInterfaceStyle() -> UIUserInterfaceStyle {
        if Thread.isMainThread {
            return UIScreen.main.traitCollection.userInterfaceStyle
        }
        var style: UIUserInterfaceStyle = .light
        DispatchQueue.main.sync {
            style = UIScreen.main.traitCollection.userInterfaceStyle
        }
        return style
    }

    // MARK: - IExportAppStatus

    func getDtSessionId() -> String { "" }
    func getQIMEI36() -> String { QimeiSetup.currentQimei36() }
    func getOAID() -> String { TuringState.shared.oaid }
    func getTOAID() -> String { TuringState.shared.oaid }
    func getTAID() -> String { IOSTuringSetup.currentTaidTicket() }
    func getDevId() -> String { getQIMEI36() }
    func getVersion() -> Int32 { 0 }
    func getVersionName() -> String {
        Bundle.main.object(forInfoDictionaryKey: "CFBundleShortVersionString") as? String ?? ""
    }
    func getAppName() -> String { "腾讯新闻" }
    func getAppBuildNo() -> String { "0" }
    func getQQAppId() -> String { "1101083114" }
    func getWxAppId() -> String { "wx5dfbe0a95623607b" }
    func isDebug() -> Bool { _isDebugAssertConfiguration() }
    func isRdmDebug() -> Bool { false }
    func isGrey() -> Bool { false }
    func isIntegrationMode() -> Bool { false }
    func isTalkbackEnabled() -> Bool { false }
    func isBrowseMode() -> Bool {
        // 与 Android 端对齐：从 MMKV（sp_read_only_mode 表）读取仅浏览模式状态
        guard let mmkv = MMKV(mmapID: "sp_read_only_mode") else { return false }
        return mmkv.string(forKey: "read_only_mode_status") == "1"
    }
    func isNightMode() -> Bool { currentUserInterfaceStyle() == .dark }
//    func isNightMode() -> Bool { false }
    func isInReviewMode() -> Bool { false }
    func isTextMode() -> Bool { false }
    func currentTextScaleGradient() -> DensityScaleGradient { textScaleGradient }

    // MARK: - IAppStatus

    func setDarkMode(isDark: Bool) {}
    func isSupportFollowSystemColorMode() -> Bool { true }

    func getScaleRatioByGradient(gradient: DensityScaleGradient) -> Double {
        switch gradient {
        case .l0: return 0.9
        case .l1: return 1.0
        case .l2: return 1.11
        case .l3: return 1.22
        case .l4: return 1.33
        case .l5: return 1.67
        default: return 1.0
        }
    }

    func getSystemFontScale() -> Float {
        // TODO: 支持 iOS 跟随系统字号时，基于 Dynamic Type 映射到与 Android fontScale 对齐的比例。
        return 1.0
    }

    func setScaleRatio(level: DensityScaleGradient) {
        textScaleGradient = level
        IOSAppStatus.storeTextScaleGradient(level)
        let ratio = KotlinDouble(value: getScaleRatioByGradient(gradient: level))
        let listeners = textScaleChangedListeners
        listeners.forEach { callback in
            callback(ratio)
        }
    }

    func subscribeTextScaleRatio(onTextScaleRatioChanged: @escaping (KotlinDouble) -> Void) {
        textScaleChangedListeners.append(onTextScaleRatioChanged)
        let ratio = KotlinDouble(value: getScaleRatioByGradient(gradient: textScaleGradient))
        onTextScaleRatioChanged(ratio)
    }

    func getDefaultFontFamily() -> String { "" }

    func subscribeFontFamily(onFontFamilyChanged: @escaping (String) -> Void) {
        // iOS 端暂未实现字体变化订阅
    }

    func getBottomBarHeight() -> Int32 { 0 }

    func getNotificationAuthorizationStatus(
        guideConfigIfDenied: (any INotificationGuideConfig)?,
        callback: @escaping (NotificationAuthorizationStatus) -> Void
    ) {}

    func netState() -> NetState {
        if !networkMonitorStarted {
            // 未启动过网络监听，触发启动以初始化网络状态，后续也无需重复启动
            ensureNetworkMonitorStarted()
        }
        netStatusLock.lock()
        let state = currentNetState
        netStatusLock.unlock()
        return state
    }

    func addNetStatusChangeListener(netStatusListener: any NetStateChangeListener) {
        netStatusLock.lock()
        if !netStatusListeners.contains(where: { ($0 as AnyObject) === (netStatusListener as AnyObject) }) {
            netStatusListeners.append(netStatusListener)
        }
        netStatusLock.unlock()
        ensureNetworkMonitorStarted()
    }

    func removeNetStatusChangeListener(netStatusListener: any NetStateChangeListener) {
        netStatusLock.lock()
        netStatusListeners.removeAll { ($0 as AnyObject) === (netStatusListener as AnyObject) }
        netStatusLock.unlock()
    }

    func getLaunchFrom() -> String { "icon" }
    func enableSenor() -> Bool { false }

    func getScreenWidth() -> Int32 {
        Int32(UIScreen.main.bounds.width * UIScreen.main.scale)
    }

    func getScreenHeight() -> Int32 {
        Int32(UIScreen.main.bounds.height * UIScreen.main.scale)
    }

    func getScreenWidthInch() -> Float {
        // 简化实现，返回 pt 宽度
        Float(UIScreen.main.bounds.width)
    }

    func getScreenHeightInch() -> Float {
        Float(UIScreen.main.bounds.height)
    }

    func getDpi() -> Int32 {
        Int32(UIScreen.main.scale * 160)
    }

    func getPackageName() -> String {
        Bundle.main.bundleIdentifier ?? ""
    }

    func getPackageFirstInstallTime() -> Int64 {
        let defaults = UserDefaults.standard
        let key = "ws_app_first_install_time_millis"
        if let value = defaults.object(forKey: key) as? NSNumber {
            return value.int64Value
        }
        let now = Int64(Date().timeIntervalSince1970 * 1000)
        defaults.set(NSNumber(value: now), forKey: key)
        return now
    }

    func getAppLaunchTimes() -> Int32 {
        let defaults = UserDefaults.standard
        return Int32(defaults.integer(forKey: Self.appLaunchTimesKey))
    }

    func recordAppLaunchIfNeeded() {
        let defaults = UserDefaults.standard
        let now = Date()
        let savedMillis = defaults.double(forKey: Self.appLaunchDateKey)
        let isSameDay = savedMillis > 0 &&
            Calendar.current.isDate(Date(timeIntervalSince1970: savedMillis / 1000), inSameDayAs: now)
        let next = isSameDay ? defaults.integer(forKey: Self.appLaunchTimesKey) + 1 : 1
        defaults.set(now.timeIntervalSince1970 * 1000, forKey: Self.appLaunchDateKey)
        defaults.set(next, forKey: Self.appLaunchTimesKey)
    }

    func getHardware() -> String {
        var systemInfo = utsname()
        uname(&systemInfo)
        return withUnsafePointer(to: &systemInfo.machine) {
            $0.withMemoryRebound(to: CChar.self, capacity: 1) {
                String(cString: $0)
            }
        }
    }

    func getRomType() -> String { "Apple" }
    func getOsVs() -> String { UIDevice.current.systemVersion }

    func getTerm() -> String {
        UIDevice.current.model
    }

    func getStore() -> String { "appstore" }
    func getFixedStore() -> String { "" }
}

private extension IOSAppStatus {
    func ensureNetworkMonitorStarted() {
        netStatusLock.lock()
        let shouldStart = !networkMonitorStarted
        if shouldStart {
            networkMonitorStarted = true
        }
        netStatusLock.unlock()
        guard shouldStart else { return }

        // 同步读取当前路径，确保 netState() 首次调用即可返回准确值
        let initialPath = networkMonitor.currentPath
        netStatusLock.lock()
        currentNetState = resolveNetState(initialPath)
        hasReceivedInitialNetworkPath = true
        netStatusLock.unlock()

        networkMonitor.pathUpdateHandler = { [weak self] path in
            self?.handleNetworkPathUpdate(path)
        }
        networkMonitor.start(queue: networkMonitorQueue)
    }

    func handleNetworkPathUpdate(_ path: NWPath) {
        let newState = resolveNetState(path)

        netStatusLock.lock()
        if !hasReceivedInitialNetworkPath {
            hasReceivedInitialNetworkPath = true
            currentNetState = newState
            netStatusLock.unlock()
            return
        }

        let oldState = currentNetState
        guard oldState != newState else {
            netStatusLock.unlock()
            return
        }
        currentNetState = newState
        let listeners = netStatusListeners
        netStatusLock.unlock()

        DispatchQueue.main.async {
            listeners.forEach { listener in
                listener.netStateChanged(old: oldState, new: newState)
            }
        }
    }

    func resolveNetState(_ path: NWPath) -> NetState {
        guard path.status == .satisfied else { return .inavailable }
        if path.usesInterfaceType(.cellular) {
            return .wwan
        }
        if path.usesInterfaceType(.wifi) || path.usesInterfaceType(.wiredEthernet) {
            return .wifi
        }
        return .wifi
    }

    static let textScaleStorageKey = "kmm_text_scale_gradient_level"
    static let appLaunchDateKey = "key_of_date"
    static let appLaunchTimesKey = "key_of_launch_times"

    static func loadStoredTextScaleGradient() -> DensityScaleGradient {
        guard UserDefaults.standard.object(forKey: textScaleStorageKey) != nil else {
            return .l1
        }
        let level = UserDefaults.standard.integer(forKey: textScaleStorageKey)
        switch level {
        case 0: return .l0
        case 1: return .l1
        case 2: return .l2
        case 3: return .l3
        case 4: return .l4
        case 5: return .l5
        default: return .l1
        }
    }

    static func storeTextScaleGradient(_ level: DensityScaleGradient) {
        UserDefaults.standard.set(Int(level.level), forKey: textScaleStorageKey)
    }
}

// MARK: - TraitChangeHelper

/// 通过一个隐藏的 UIView 挂载到 keyWindow 上，监听 traitCollection 变化
private final class TraitChangeHelper: UIView {

    private let onDarkModeChanged: (Bool) -> Void

    init(onDarkModeChanged: @escaping (Bool) -> Void) {
        self.onDarkModeChanged = onDarkModeChanged
        super.init(frame: .zero)
        isHidden = true
        isUserInteractionEnabled = false
    }

    @available(*, unavailable)
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }

    func attachToWindow() {
        guard let scene = UIApplication.shared.connectedScenes.first as? UIWindowScene,
              let window = scene.windows.first else {
            // 延迟重试，app 可能还没有 window
            DispatchQueue.main.asyncAfter(deadline: .now() + 0.5) { [weak self] in
                self?.attachToWindow()
            }
            return
        }
        window.addSubview(self)
    }

    override func traitCollectionDidChange(_ previousTraitCollection: UITraitCollection?) {
        super.traitCollectionDidChange(previousTraitCollection)
        guard let previous = previousTraitCollection,
              previous.userInterfaceStyle != traitCollection.userInterfaceStyle else {
            return
        }
        let isDark = traitCollection.userInterfaceStyle == .dark
        onDarkModeChanged(isDark)
    }
}
