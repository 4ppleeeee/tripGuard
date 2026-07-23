import Foundation
import CocoaLumberjack
import Dispatch
import Photos
import PhotosUI
import UIKit
import CryptoKit
import umbrella
import TDOS_Diagnose

/// iOS 端平台依赖注入入口
enum IOSPlatformInit {

    static func setup() {
        setupAppTask()
        setupAppPageStack()
        setupAppRouter()
        setupComposeBridge()
        setupAppConfig()
        setupAppReport()
        setupAppStatus()
        setupAppDevice()
        setupAppEncoder()
        setupAppAlert()
        setupAppUpload()
        setupResManager()
        setupIOSAdJumpActionIfAvailable()
        setupAppFileManager()
        setupFileCacheManager()
        setupAppWindow()
        setupStatusBarController()
    }
}

// MARK: - ResManager

private func setupResManager() {
    QnPlatformLogic.shared.resManager = IOSResManager()
}

/// iOS 平台资源能力实现，提供系统相册选图能力。
fileprivate class IOSResManager: NSObject, IResManager {
    override init() {
        super.init()
    }

    // MARK: - IResManager 协议实现

    func getAssetJson(fileName: String) -> String { return "" }

    func preloadImage(url: String, onSuccess: (() -> Void)?, onFail: (() -> Void)?) {}

    func saveImage(url: String, metadata: [String: String]?) {}

    func copyToClipboard(content: String) {
        guard !content.isEmpty else { return }
        UIPasteboard.general.string = content
    }

    func doCopyToClipboard(content: String) {
        copyToClipboard(content: content)
    }

    func saveVideo(localFilePath: String) -> Bool {
        guard !localFilePath.trimmingCharacters(in: .whitespaces).isEmpty else {
            return false
        }
        let fileUrl = URL(fileURLWithPath: localFilePath)
        let semaphore = DispatchSemaphore(value: 0)
        var result = false
        PHPhotoLibrary.shared().performChanges({
            PHAssetChangeRequest.creationRequestForAssetFromVideo(atFileURL: fileUrl)
        }) { success, _ in
            result = success
            semaphore.signal()
        }
        semaphore.wait()
        return result
    }

    func selectImage(context: (any IKmmContext)?, callback: @escaping ([String]) -> Void) {
        DispatchQueue.main.async {
            guard let topVC = IOSResManager.topViewController() else {
                callback([])
                return
            }
            if #available(iOS 14, *) {
                var config = PHPickerConfiguration()
                config.selectionLimit = 9
                config.filter = .images
                let picker = PHPickerViewController(configuration: config)
                picker.delegate = IOSPhotoPickerDelegate.shared
                IOSPhotoPickerDelegate.shared.pendingCallback = callback
                topVC.present(picker, animated: true)
            } else {
                let picker = UIImagePickerController()
                picker.sourceType = .photoLibrary
                picker.allowsEditing = false
                picker.delegate = IOSLegacyPickerDelegate.shared
                IOSLegacyPickerDelegate.shared.pendingCallback = callback
                topVC.present(picker, animated: true)
            }
        }
    }

    func selectImage(context: (any IKmmContext)?, callback: @escaping (NSArray) -> Void) {
        selectImage(context: context) { (paths: [String]) in
            callback(paths as NSArray)
        }
    }

    func getPaletteColor(imageUrl: String, param: PaletteParam, defaultColor: KotlinInt?, onGot: @escaping (KotlinInt) -> Void) {
        if let color = defaultColor {
            onGot(color)
        }
    }

    func getPaletteColor(imageUrl: String, param: PaletteParam, defaultColor: Int32?, onGot: @escaping (Int32) -> Void) {
        if let color = defaultColor {
            onGot(color)
        }
    }

    func preloadLottieToMemory(context: (any IKmmContext)?, url: String, status: String, isDay: Bool) {}

    func preloadAlphaVideo(url: String, onSuccess: (() -> Void)?, onFail: (() -> Void)?) {
        onFail?()
    }

    // MARK: - 辅助方法

    /// 获取当前最顶层的 UIViewController
    private static func topViewController() -> UIViewController? {
        guard let windowScene = UIApplication.shared.connectedScenes
            .compactMap({ $0 as? UIWindowScene })
            .first(where: { $0.activationState == .foregroundActive }),
              let rootVC = windowScene.windows.first(where: { $0.isKeyWindow })?.rootViewController
        else { return nil }
        return findTopViewController(from: rootVC)
    }

    private static func findTopViewController(from vc: UIViewController) -> UIViewController {
        if let presented = vc.presentedViewController {
            return findTopViewController(from: presented)
        }
        if let nav = vc as? UINavigationController, let top = nav.topViewController {
            return findTopViewController(from: top)
        }
        if let tab = vc as? UITabBarController, let selected = tab.selectedViewController {
            return findTopViewController(from: selected)
        }
        return vc
    }
}

// MARK: - PHPickerViewController Delegate (iOS 14+)

@available(iOS 14, *)
fileprivate final class IOSPhotoPickerDelegate: NSObject, PHPickerViewControllerDelegate {
    static let shared = IOSPhotoPickerDelegate()
    var pendingCallback: (([String]) -> Void)?

    func picker(_ picker: PHPickerViewController, didFinishPicking results: [PHPickerResult]) {
        picker.dismiss(animated: true)
        guard !results.isEmpty else {
            pendingCallback?([])
            pendingCallback = nil
            return
        }
        let callback = pendingCallback
        pendingCallback = nil
        var paths: [String] = []
        let group = DispatchGroup()
        for result in results {
            guard result.itemProvider.canLoadObject(ofClass: UIImage.self) else { continue }
            group.enter()
            result.itemProvider.loadObject(ofClass: UIImage.self) { object, _ in
                defer { group.leave() }
                guard let image = object as? UIImage,
                      let data = image.jpegData(compressionQuality: 0.9) else { return }
                let fileName = "report_img_\(Int(Date().timeIntervalSince1970 * 1000)).jpg"
                let url = FileManager.default.temporaryDirectory.appendingPathComponent(fileName)
                if (try? data.write(to: url)) != nil {
                    paths.append(url.path)
                }
            }
        }
        group.notify(queue: .main) {
            callback?(paths)
        }
    }
}

// MARK: - UIImagePickerController Delegate (iOS 13 fallback)

fileprivate final class IOSLegacyPickerDelegate: NSObject, UIImagePickerControllerDelegate, UINavigationControllerDelegate {
    static let shared = IOSLegacyPickerDelegate()
    var pendingCallback: (([String]) -> Void)?

    func imagePickerController(
        _ picker: UIImagePickerController,
        didFinishPickingMediaWithInfo info: [UIImagePickerController.InfoKey: Any]
    ) {
        picker.dismiss(animated: true)
        let callback = pendingCallback
        pendingCallback = nil
        guard let image = info[.originalImage] as? UIImage,
              let data = image.jpegData(compressionQuality: 0.9) else {
            callback?([])
            return
        }
        let fileName = "report_img_\(Int(Date().timeIntervalSince1970 * 1000)).jpg"
        let url = FileManager.default.temporaryDirectory.appendingPathComponent(fileName)
        if (try? data.write(to: url)) != nil {
            callback?([url.path])
        } else {
            callback?([])
        }
    }

    func imagePickerControllerDidCancel(_ picker: UIImagePickerController) {
        picker.dismiss(animated: true)
        pendingCallback?([])
        pendingCallback = nil
    }
}

// MARK: - AppEncoder

private func setupAppEncoder() {
    QnPlatformLogic.shared.appEncoder = IOSAppEncoder()
}

// MARK: - AppFileManager

private func setupAppFileManager() {
    IOSAppFileManagerKt.setupIOSAppFileManager()
}

// MARK: - FileCacheManager

private func setupFileCacheManager() {
    QnPlatformLogic.shared.fileCacheManager = IOSFileCacheManager()
}

/// iOS 端音频/本地文件缓存实现。
///
/// `cacheFile` 接收的是 common 层传入的 Base64 文件内容，写入前需要先解码成原始 `Data`。
fileprivate final class IOSFileCacheManager: NSObject, IFileCacheManager {
    private let fileManager = FileManager.default

    func fileAbsolutePath(folderPath: String, fileName: String) -> String? {
        let normalizedFolder = folderPath.trimmingCharacters(in: CharacterSet(charactersIn: "/"))
        let normalizedFileName = fileName.trimmingCharacters(in: CharacterSet(charactersIn: "/"))
        guard !normalizedFolder.isEmpty,
              !normalizedFileName.isEmpty,
              let cacheURL = Self.cacheDirectoryURL else {
            return nil
        }
        let folderURL = cacheURL.appendingPathComponent(normalizedFolder, isDirectory: true)
        return folderURL.appendingPathComponent(normalizedFileName).path
    }

    func cacheFile(filePath: String, data: String) -> Bool {
        guard !filePath.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty,
              !data.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty,
              let decodedData = Data(base64Encoded: data, options: .ignoreUnknownCharacters) else {
            return false
        }
        let fileURL = URL(fileURLWithPath: filePath)
        do {
            try fileManager.createDirectory(
                at: fileURL.deletingLastPathComponent(),
                withIntermediateDirectories: true
            )
            try decodedData.write(to: fileURL, options: .atomic)
            return true
        } catch {
            NSLog("[IOSFileCacheManager] cacheFile failed: \(filePath), error=\(error)")
            return false
        }
    }

    func containsFile(filePath: String) -> Bool {
        guard !filePath.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty else {
            return false
        }
        return fileManager.fileExists(atPath: filePath)
    }

    func removeFile(filePath: String) {
        guard containsFile(filePath: filePath) else {
            return
        }
        do {
            try fileManager.removeItem(atPath: filePath)
        } catch {
            NSLog("[IOSFileCacheManager] removeFile failed: \(filePath), error=\(error)")
        }
    }

    func removeDirPathFile(dirPath: String) {
        guard !dirPath.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty,
              fileManager.fileExists(atPath: dirPath) else {
            return
        }
        do {
            try fileManager.removeItem(atPath: dirPath)
        } catch {
            NSLog("[IOSFileCacheManager] removeDirPathFile failed: \(dirPath), error=\(error)")
        }
    }

    private static var cacheDirectoryURL: URL? {
        FileManager.default.urls(for: .cachesDirectory, in: .userDomainMask).first
    }
}

// MARK: - AppWindow

private func setupAppWindow() {
    IOSAppWindowKt.setupIOSAppWindow(appWindow: IOSAppWindowImpl())
}

// MARK: - StatusBarController

/// 注入 IStatusBarController，将 iOS 状态栏样式控制桥接到 KMM 公共层。
/// setWhiteBar = 白色图标（适合深色背景），setBlackBar = 深色图标（适合浅色背景）
/// 通过找到当前最顶层的 KuiklyRenderViewController 来调用 wsSetStatusBarLightContent
private func setupStatusBarController() {
    QnPlatformLogic.shared.statusBarController = IOSStatusBarController()
}

private class IOSStatusBarController: NSObject, IStatusBarController {

    func setWhiteBar() {
        setLightContent(true)
    }

    func setBlackBar() {
        setLightContent(false)
    }

    func resetStatusBar() {
        setLightContent(false)
    }

    func setCustomBar(textColor: String, backgroundColor: String) {}

    func setStatusBarVisibility(visible: Bool) {
        guard Thread.isMainThread else {
            DispatchQueue.main.async { self.setStatusBarVisibility(visible: visible) }
            return
        }
        guard let vc = topKuiklyViewController() else { return }
        vc.wsSetStatusBarHidden(!visible)
    }

    private func setLightContent(_ lightContent: Bool) {
        guard Thread.isMainThread else {
            DispatchQueue.main.async { self.setLightContent(lightContent) }
            return
        }
        guard let vc = topKuiklyViewController() else { return }
        vc.wsSetStatusBarLightContent(lightContent)
    }

    /// 找到当前最顶层的 KuiklyRenderViewController
    /// SwiftUI WindowGroup 的 rootViewController 是 UIHostingController，
    /// 需要递归查找 children 中的 UINavigationController 再取 topViewController
    private func topKuiklyViewController() -> KuiklyRenderViewController? {
        var rootVC = UIApplication.shared.windows.first(where: { $0.isKeyWindow })?.rootViewController
        // 穿透 presentedViewController
        while let presented = rootVC?.presentedViewController {
            rootVC = presented
        }
        return findKuiklyVC(in: rootVC)
    }

    private func findKuiklyVC(in vc: UIViewController?) -> KuiklyRenderViewController? {
        guard let vc = vc else { return nil }
        if let nav = vc as? UINavigationController {
            return nav.topViewController as? KuiklyRenderViewController
        }
        if let kuikly = vc as? KuiklyRenderViewController {
            return kuikly
        }
        // 递归查找 children（处理 UIHostingController 等容器）
        for child in vc.children {
            if let found = findKuiklyVC(in: child) {
                return found
            }
        }
        return nil
    }
}

/// iOS 原生侧 IAppWindow 完整实现
/// 直接实现 KMM 导出的 IAppWindow 和 IAppWindowOrientationSensor 协议
private class IOSAppWindowImpl: NSObject, IAppWindow, IAppWindowOrientationSensor {

    /// 记录当前通过代码设置的界面方向
    private var currentOrientation: ScreenOrientation = .portrait

    /// 设备方向监听器集合
    private var deviceOrientationListeners: [IDeviceOrientationListener] = []
    /// 最近一次分发的设备方向，用于去重
    private var latestDeviceOrientation: ScreenOrientation?
    /// 是否已注册系统通知
    private var isObservingDeviceOrientation = false

    func keepScreenOn() {
        guard Thread.isMainThread else {
            DispatchQueue.main.async { self.keepScreenOn() }
            return
        }
        UIApplication.shared.isIdleTimerDisabled = true
    }

    func cancelScreenOn() {
        guard Thread.isMainThread else {
            DispatchQueue.main.async { self.cancelScreenOn() }
            return
        }
        UIApplication.shared.isIdleTimerDisabled = false
    }

    func setScreenOrientation(orientation: ScreenOrientation) {
        guard Thread.isMainThread else {
            DispatchQueue.main.async { self.setScreenOrientation(orientation: orientation) }
            return
        }

        let mask: UIInterfaceOrientationMask
        let targetOrientation: UIInterfaceOrientation

        switch orientation {
        case .landscape:
            mask = .landscapeRight
            targetOrientation = .landscapeRight
        case .auto_:
            mask = .allButUpsideDown
            targetOrientation = .portrait
        default: // .portrait
            mask = .portrait
            targetOrientation = .portrait
        }

        // 记录当前方向
        currentOrientation = (orientation == .landscape) ? .landscape : .portrait

        // 更新当前 ViewController 支持的方向
        guard let topVC = Self.topViewController() else { return }
        if let vc = topVC as? KuiklyRenderViewController {
            vc.wsSetSupportedOrientationMask(mask)
        }

        if #available(iOS 16.0, *) {
            guard let windowScene = Self.activeWindowScene() else { return }
            let preferences = UIWindowScene.GeometryPreferences.iOS(interfaceOrientations: mask)
            windowScene.requestGeometryUpdate(preferences) { _ in }
            topVC.navigationController?.setNeedsUpdateOfSupportedInterfaceOrientations()
            topVC.setNeedsUpdateOfSupportedInterfaceOrientations()
        } else {
            UIDevice.current.setValue(targetOrientation.rawValue, forKey: "orientation")
            UIViewController.attemptRotationToDeviceOrientation()
        }
    }

    func getScreenOrientation() -> ScreenOrientation {
        return currentOrientation
    }

    func enterFullScreen() {
        guard Thread.isMainThread else {
            DispatchQueue.main.async { self.enterFullScreen() }
            return
        }
        guard let topVC = Self.topViewController() as? KuiklyRenderViewController else { return }
        topVC.wsSetStatusBarHidden(true)
    }

    func exitFullScreen() {
        guard Thread.isMainThread else {
            DispatchQueue.main.async { self.exitFullScreen() }
            return
        }
        guard let topVC = Self.topViewController() as? KuiklyRenderViewController else { return }
        topVC.wsSetStatusBarHidden(false)
    }

    // MARK: - Private Helpers

    private static func activeWindowScene() -> UIWindowScene? {
        for scene in UIApplication.shared.connectedScenes {
            guard let windowScene = scene as? UIWindowScene else { continue }
            if windowScene.activationState == .foregroundActive ||
               windowScene.activationState == .foregroundInactive {
                return windowScene
            }
        }
        return nil
    }

    private static func topViewController() -> UIViewController? {
        guard let window = activeWindow() else { return nil }
        return topVisibleController(from: window.rootViewController)
    }

    private static func activeWindow() -> UIWindow? {
        var fallback: UIWindow?
        for scene in UIApplication.shared.connectedScenes {
            guard let windowScene = scene as? UIWindowScene else { continue }
            guard windowScene.activationState == .foregroundActive ||
                  windowScene.activationState == .foregroundInactive else { continue }
            for window in windowScene.windows {
                if window.isKeyWindow { return window }
                if fallback == nil { fallback = window }
            }
        }
        return fallback ?? UIApplication.shared.windows.first
    }

    private static func topVisibleController(from controller: UIViewController?) -> UIViewController? {
        guard let controller else { return nil }
        if let presented = controller.presentedViewController {
            return topVisibleController(from: presented)
        }
        if let nav = controller as? UINavigationController {
            return topVisibleController(from: nav.topViewController ?? nav.visibleViewController)
        }
        if let tab = controller as? UITabBarController {
            return topVisibleController(from: tab.selectedViewController)
        }
        return controller
    }

    // MARK: - IAppWindowOrientationSensor

    func registerDeviceOrientationListener(listener: any IDeviceOrientationListener) -> Bool {
        deviceOrientationListeners.append(listener)
        if !isObservingDeviceOrientation {
            UIDevice.current.beginGeneratingDeviceOrientationNotifications()
            NotificationCenter.default.addObserver(
                self,
                selector: #selector(handleDeviceOrientationChange),
                name: UIDevice.orientationDidChangeNotification,
                object: nil
            )
            isObservingDeviceOrientation = true
        }
        return true
    }

    func unregisterDeviceOrientationListener(listener: any IDeviceOrientationListener) {
        deviceOrientationListeners.removeAll { $0 === listener }
        if deviceOrientationListeners.isEmpty {
            stopObservingDeviceOrientation()
        }
    }

    func unregisterAllDeviceOrientationListeners() {
        guard !deviceOrientationListeners.isEmpty else { return }
        deviceOrientationListeners.removeAll()
        stopObservingDeviceOrientation()
    }

    func isAutoRotationEnabled() -> Bool {
        // iOS 没有公开 API 直接查询旋转锁定状态。
        // 当旋转锁定开启时，UIDevice.current.orientation 仍会变化（反映物理姿态），
        // 但系统不会实际旋转界面。这里通过检查当前设备方向是否为 unknown 来做基本判断：
        // 如果设备方向为 unknown，通常意味着设备平放或无法确定方向。
        // 由于 iOS 无法精确判断旋转锁定，默认返回 true，让业务方自行处理。
        return true
    }

    @objc private func handleDeviceOrientationChange() {
        let deviceOrientation = UIDevice.current.orientation
        let nextOrientation: ScreenOrientation
        switch deviceOrientation {
        case .landscapeLeft, .landscapeRight:
            nextOrientation = .landscape
        case .portrait, .portraitUpsideDown:
            nextOrientation = .portrait
        default:
            // faceUp, faceDown, unknown 等不触发方向变化
            return
        }
        if latestDeviceOrientation == nextOrientation { return }
        latestDeviceOrientation = nextOrientation
        for listener in deviceOrientationListeners {
            listener.onDeviceOrientationChanged(orientation: nextOrientation)
        }
    }

    private func stopObservingDeviceOrientation() {
        guard isObservingDeviceOrientation else { return }
        isObservingDeviceOrientation = false
        latestDeviceOrientation = nil
        NotificationCenter.default.removeObserver(
            self,
            name: UIDevice.orientationDidChangeNotification,
            object: nil
        )
        UIDevice.current.endGeneratingDeviceOrientationNotifications()
    }
}

// MARK: - AppAlert

private func setupAppAlert() {
    QnPlatformLogic.shared.appAlert = IOSAppAlert()
}

// MARK: - AppUpload

private func setupAppUpload() {
    QnFrameworkLogic.shared.appUpload = IOSAppUpload()
}

fileprivate final class IOSAppUpload: NSObject, IAppUpload {
    func createVideoUploadTask(input: VideoUploadInput, listener: any UploadTaskListener) -> any UploadTask {
        return IOSVmeVideoUploadTask(input: input, listener: listener)
    }

    func createImageUploadTask(input: ImageUploadInput, listener: any UploadTaskListener) -> any UploadTask {
        return IOSVmeImageUploadTask(input: input, listener: listener)
    }

    func prepareUploadConnection() {
        IOSUploadSdkInvoker.shared.prepareUploadConnection()
    }

    func setAppForegroundState(isForeground: Bool) {
        IOSUploadSdkInvoker.shared.setAppForegroundState(isForeground: isForeground)
    }

    func pickMedia(
        context: (any IKmmContext)?,
        type: PickMediaType,
        source: PickMediaSource,
        cropConfig: PickMediaCropConfig?,
        callback: @escaping ([String]) -> Void
    ) {
        _ = context
        _ = cropConfig
        DispatchQueue.main.async {
            guard let topVC = IOSAppUpload.topViewController() else {
                callback([])
                return
            }

            switch source {
            case .camera:
                IOSAppUpload.presentCamera(from: topVC, type: type, callback: callback)
            case .albumOrCamera:
                IOSAppUpload.presentMediaSourceSheet(from: topVC, type: type, callback: callback)
            default:
                IOSAppUpload.presentAlbum(from: topVC, type: type, callback: callback)
            }
        }
    }

    private static func presentMediaSourceSheet(
        from topVC: UIViewController,
        type: PickMediaType,
        callback: @escaping ([String]) -> Void
    ) {
        let alert = UIAlertController(title: nil, message: nil, preferredStyle: .actionSheet)
        alert.addAction(UIAlertAction(title: "从相册选择", style: .default) { _ in
            IOSAppUpload.presentAlbum(from: topVC, type: type, callback: callback)
        })
        alert.addAction(UIAlertAction(title: "拍照", style: .default) { _ in
            IOSAppUpload.presentCamera(from: topVC, type: type, callback: callback)
        })
        alert.addAction(UIAlertAction(title: "取消", style: .cancel) { _ in
            callback([])
        })
        if let popover = alert.popoverPresentationController {
            popover.sourceView = topVC.view
            popover.sourceRect = CGRect(
                x: topVC.view.bounds.midX,
                y: topVC.view.bounds.maxY,
                width: 0,
                height: 0
            )
            popover.permittedArrowDirections = []
        }
        topVC.present(alert, animated: true)
    }

    private static func presentAlbum(
        from topVC: UIViewController,
        type: PickMediaType,
        callback: @escaping ([String]) -> Void
    ) {
        if #available(iOS 14.0, *) {
            var config = PHPickerConfiguration()
            config.selectionLimit = 9
            switch type {
            case .imageOnly:
                config.filter = .images
            case .videoOnly:
                config.filter = .videos
            default:
                config.filter = .any(of: [.images, .videos])
            }
            let picker = PHPickerViewController(configuration: config)
            IOSAppUploadPhotoPickerDelegate.shared.pendingCallback = callback
            picker.delegate = IOSAppUploadPhotoPickerDelegate.shared
            topVC.present(picker, animated: true)
            return
        }

        let picker = UIImagePickerController()
        picker.sourceType = .photoLibrary
        picker.allowsEditing = false
        picker.mediaTypes = IOSAppUpload.legacyMediaTypes(for: type)
        IOSAppUploadLegacyPickerDelegate.shared.pendingCallback = callback
        picker.delegate = IOSAppUploadLegacyPickerDelegate.shared
        topVC.present(picker, animated: true)
    }

    private static func presentCamera(
        from topVC: UIViewController,
        type: PickMediaType,
        callback: @escaping ([String]) -> Void
    ) {
        guard UIImagePickerController.isSourceTypeAvailable(.camera) else {
            NSLog("[IOSAppUpload] camera source is unavailable")
            callback([])
            return
        }
        let picker = UIImagePickerController()
        picker.sourceType = .camera
        picker.allowsEditing = false
        picker.mediaTypes = IOSAppUpload.legacyMediaTypes(for: type)
        IOSAppUploadLegacyPickerDelegate.shared.pendingCallback = callback
        picker.delegate = IOSAppUploadLegacyPickerDelegate.shared
        topVC.present(picker, animated: true)
    }

    private static func legacyMediaTypes(for type: PickMediaType) -> [String] {
        switch type {
        case .imageOnly:
            return ["public.image"]
        case .videoOnly:
            return ["public.movie"]
        default:
            return ["public.image", "public.movie"]
        }
    }

    fileprivate static func createTempImagePath() -> String {
        let fileName = "upload_img_\(Int(Date().timeIntervalSince1970 * 1000)).jpg"
        return FileManager.default.temporaryDirectory.appendingPathComponent(fileName).path
    }

    fileprivate static func createTempVideoPath(extensionName: String) -> String {
        let ext = extensionName.isEmpty ? "mp4" : extensionName
        let fileName = "upload_video_\(Int(Date().timeIntervalSince1970 * 1000)).\(ext)"
        return FileManager.default.temporaryDirectory.appendingPathComponent(fileName).path
    }

    /// 获取当前最顶层的 UIViewController
    private static func topViewController() -> UIViewController? {
        guard let windowScene = UIApplication.shared.connectedScenes
            .compactMap({ $0 as? UIWindowScene })
            .first(where: { $0.activationState == .foregroundActive }),
              let rootVC = windowScene.windows.first(where: { $0.isKeyWindow })?.rootViewController
        else { return nil }
        return findTopViewController(from: rootVC)
    }

    private static func findTopViewController(from vc: UIViewController) -> UIViewController {
        if let presented = vc.presentedViewController {
            return findTopViewController(from: presented)
        }
        if let nav = vc as? UINavigationController, let top = nav.topViewController {
            return findTopViewController(from: top)
        }
        if let tab = vc as? UITabBarController, let selected = tab.selectedViewController {
            return findTopViewController(from: selected)
        }
        return vc
    }
}

@available(iOS 14.0, *)
fileprivate final class IOSAppUploadPhotoPickerDelegate: NSObject, PHPickerViewControllerDelegate {
    static let shared = IOSAppUploadPhotoPickerDelegate()
    var pendingCallback: (([String]) -> Void)?

    func picker(_ picker: PHPickerViewController, didFinishPicking results: [PHPickerResult]) {
        picker.dismiss(animated: true)
        let callback = pendingCallback
        pendingCallback = nil
        guard let callback else { return }

        if results.isEmpty {
            callback([])
            return
        }

        let group = DispatchGroup()
        var paths: [String] = []
        let pathsLock = NSLock()

        for result in results {
            let provider = result.itemProvider

            if provider.canLoadObject(ofClass: UIImage.self) {
                group.enter()
                provider.loadObject(ofClass: UIImage.self) { object, _ in
                    defer { group.leave() }
                    guard let image = object as? UIImage,
                          let data = image.jpegData(compressionQuality: 0.9) else {
                        return
                    }
                    let path = IOSAppUpload.createTempImagePath()
                    if (try? data.write(to: URL(fileURLWithPath: path))) != nil {
                        pathsLock.lock()
                        paths.append(path)
                        pathsLock.unlock()
                    }
                }
                continue
            }

            if provider.hasItemConformingToTypeIdentifier("public.movie") {
                group.enter()
                provider.loadFileRepresentation(forTypeIdentifier: "public.movie") { url, _ in
                    defer { group.leave() }
                    guard let sourceUrl = url else { return }
                    let ext = sourceUrl.pathExtension
                    let destinationPath = IOSAppUpload.createTempVideoPath(extensionName: ext)
                    let destinationUrl = URL(fileURLWithPath: destinationPath)
                    do {
                        let manager = FileManager.default
                        if manager.fileExists(atPath: destinationPath) {
                            try manager.removeItem(at: destinationUrl)
                        }
                        try manager.copyItem(at: sourceUrl, to: destinationUrl)
                        pathsLock.lock()
                        paths.append(destinationPath)
                        pathsLock.unlock()
                    } catch {
                        NSLog("[IOSAppUpload] copy picked video failed: %@", error.localizedDescription)
                    }
                }
            }
        }

        group.notify(queue: .main) {
            callback(paths)
        }
    }
}

fileprivate final class IOSAppUploadLegacyPickerDelegate: NSObject, UIImagePickerControllerDelegate, UINavigationControllerDelegate {
    static let shared = IOSAppUploadLegacyPickerDelegate()
    var pendingCallback: (([String]) -> Void)?

    func imagePickerController(
        _ picker: UIImagePickerController,
        didFinishPickingMediaWithInfo info: [UIImagePickerController.InfoKey: Any]
    ) {
        picker.dismiss(animated: true)
        let callback = pendingCallback
        pendingCallback = nil
        guard let callback else { return }

        let mediaType = info[.mediaType] as? String
        if mediaType == "public.movie",
           let sourceUrl = info[.mediaURL] as? URL {
            let ext = sourceUrl.pathExtension
            let destinationPath = IOSAppUpload.createTempVideoPath(extensionName: ext)
            let destinationUrl = URL(fileURLWithPath: destinationPath)
            do {
                let manager = FileManager.default
                if manager.fileExists(atPath: destinationPath) {
                    try manager.removeItem(at: destinationUrl)
                }
                try manager.copyItem(at: sourceUrl, to: destinationUrl)
                callback([destinationPath])
            } catch {
                NSLog("[IOSAppUpload] legacy copy picked video failed: %@", error.localizedDescription)
                callback([])
            }
            return
        }

        guard let image = info[.originalImage] as? UIImage,
              let data = image.jpegData(compressionQuality: 0.9) else {
            callback([])
            return
        }
        let path = IOSAppUpload.createTempImagePath()
        if (try? data.write(to: URL(fileURLWithPath: path))) != nil {
            callback([path])
        } else {
            callback([])
        }
    }

    func imagePickerControllerDidCancel(_ picker: UIImagePickerController) {
        picker.dismiss(animated: true)
        pendingCallback?([])
        pendingCallback = nil
    }
}

fileprivate enum IOSUploadFileType: String {
    case avatar = "avatar"
    case cover = "cover"
    case video = "video"
}

fileprivate final class IOSUploadSdkInvoker {
    static let shared = IOSUploadSdkInvoker()

    private init() {}

    private func uploadManager() -> NSObject? {
        guard
            let managerClass = NSClassFromString("BDHUploadManager") as? NSObject.Type
        else {
            return nil
        }
        let selector = NSSelectorFromString("instance")
        guard
            managerClass.responds(to: selector),
            let manager = managerClass.perform(selector)?.takeUnretainedValue() as? NSObject
        else {
            return nil
        }
        return manager
    }

    func prepareUploadConnection() {
        guard let manager = uploadManager() else {
            NSLog("[IOSAppUpload] BDHUploadManager not found when prepareUploadConnection")
            return
        }
        invokeNoArgIfPossible(target: manager, selectorNames: ["preBuildConnection", "prepareConnect"])
    }

    func setAppForegroundState(isForeground: Bool) {
        guard let manager = uploadManager() else {
            NSLog("[IOSAppUpload] BDHUploadManager not found when setAppForegroundState")
            return
        }
        if isForeground {
            invokeNoArgIfPossible(target: manager, selectorNames: ["applicationWillActive", "appForeGround"])
        } else {
            invokeNoArgIfPossible(target: manager, selectorNames: ["applicationWillSuspend", "appBackGround"])
        }
    }

    func startUpload(fileObject: NSObject) -> Bool {
        guard let manager = uploadManager() else {
            return false
        }
        return invokeOneObjectArgIfPossible(
            target: manager,
            selectorNames: ["startUpload:", "submitTransaction:"],
            arg: fileObject
        )
    }

    func cancelUpload(taskId: Int64) -> Bool {
        guard let manager = uploadManager() else {
            return false
        }
        let didCancelByTaskId = invokeOneIntegerArgIfPossible(
            target: manager,
            selectorNames: ["cancelUpload:"],
            arg: taskId
        )
        if didCancelByTaskId {
            return true
        }
        return invokeOneIntegerArgIfPossible(
            target: manager,
            selectorNames: ["stopTransaction:"],
            arg: taskId
        )
    }

    func createUploadFile(path: String, taskId: Int64, fileType: IOSUploadFileType) -> NSObject? {
        guard let uploadFileClass = NSClassFromString("BDHUploadFile") as? NSObject.Type else {
            return nil
        }
        let uploadFile = uploadFileClass.init()
        uploadFile.setValue(path, forKey: "filePath")
        uploadFile.setValue(NSNumber(value: taskId), forKey: "taskId")
        uploadFile.setValue(QimeiSetup.currentQimei(), forKey: "guid")

        let reqId = IOSUploadAuthBuilder.createRequestId()
        uploadFile.setValue(reqId, forKey: "reqId")
        uploadFile.setValue(NSNumber(value: IOSUploadAuthBuilder.bizId), forKey: "bizId")
        uploadFile.setValue(["skipAudit": 1], forKey: "cosParamsDic")
        uploadFile.setValue(NSNumber(value: IOSUploadAuthBuilder.connectTimeoutSeconds), forKey: "connTimeout")
        uploadFile.setValue(NSNumber(value: IOSUploadAuthBuilder.uploadTimeoutSeconds), forKey: "uploadTimeout")

        if let serviceId = IOSUploadAuthBuilder.serviceId(for: fileType) {
            uploadFile.setValue(serviceId, forKey: "serviceId")
            let bizToken = IOSUploadAuthBuilder.bizToken(serviceId: serviceId, reqId: reqId)
            uploadFile.setValue(bizToken.data(using: .utf8), forKey: "bizToken")
        }
        if let serverKey = IOSUploadAuthBuilder.serverKey(for: fileType) {
            uploadFile.setValue(serverKey, forKey: "serverkey")
        }

        if let metaData = IOSUploadAuthBuilder.buildUploadMeta(
            filePath: path,
            isVideo: fileType == .video,
            videoMeta: nil
        ) {
            setValueIfPossible(metaData, forKey: "ukey", target: uploadFile)
            setValueIfPossible(metaData, forKey: "extendInfo", target: uploadFile)
            setValueIfPossible(metaData, forKey: "tickets", target: uploadFile)
        }

        calculateFileInfo(uploadFile)
        return uploadFile
    }

    func createUploadFile(path: String, taskId: Int64, input: VideoUploadInput) -> NSObject? {
        guard let uploadFileClass = NSClassFromString("BDHUploadFile") as? NSObject.Type else {
            return nil
        }
        let uploadFile = uploadFileClass.init()
        uploadFile.setValue(path, forKey: "filePath")
        uploadFile.setValue(NSNumber(value: taskId), forKey: "taskId")
        uploadFile.setValue(QimeiSetup.currentQimei(), forKey: "guid")

        let reqId = IOSUploadAuthBuilder.createRequestId()
        uploadFile.setValue(reqId, forKey: "reqId")
        uploadFile.setValue(NSNumber(value: IOSUploadAuthBuilder.bizId), forKey: "bizId")
        uploadFile.setValue(["skipAudit": 1], forKey: "cosParamsDic")
        uploadFile.setValue(NSNumber(value: IOSUploadAuthBuilder.connectTimeoutSeconds), forKey: "connTimeout")
        uploadFile.setValue(NSNumber(value: IOSUploadAuthBuilder.uploadTimeoutSeconds), forKey: "uploadTimeout")

        if let serviceId = IOSUploadAuthBuilder.serviceId(for: .video) {
            uploadFile.setValue(serviceId, forKey: "serviceId")
            let bizToken = IOSUploadAuthBuilder.bizToken(serviceId: serviceId, reqId: reqId)
            uploadFile.setValue(bizToken.data(using: .utf8), forKey: "bizToken")
        }
        if let serverKey = IOSUploadAuthBuilder.serverKey(for: .video) {
            uploadFile.setValue(serverKey, forKey: "serverkey")
        }

        let videoMeta = IOSUploadAuthBuilder.VideoMeta(
            width: Int(input.width),
            height: Int(input.height),
            duration: Int(input.duration),
            bitrate: Int(input.bitrate),
            templateBusinessType: Int(input.videoType),
            transcodingPriority: Int(input.encodePriority),
            keyFrame: (input.keyFrame as [Any]).compactMap {
                if let value = $0 as? NSNumber { return value.intValue }
                if let value = $0 as? Int32 { return Int(value) }
                if let value = $0 as? Int { return value }
                return nil
            },
            source: input.source
        )

        if let metaData = IOSUploadAuthBuilder.buildUploadMeta(
            filePath: path,
            isVideo: true,
            videoMeta: videoMeta
        ) {
            setValueIfPossible(metaData, forKey: "ukey", target: uploadFile)
            setValueIfPossible(metaData, forKey: "extendInfo", target: uploadFile)
            setValueIfPossible(metaData, forKey: "tickets", target: uploadFile)
        }
        calculateFileInfo(uploadFile)
        return uploadFile
    }

    private func calculateFileInfo(_ uploadFile: NSObject) {
        invokeNoArgIfPossible(target: uploadFile, selectorNames: ["calcFileSize"])
        invokeNoArgIfPossible(target: uploadFile, selectorNames: ["calcMD5"])
        invokeNoArgIfPossible(target: uploadFile, selectorNames: ["calcSha1"])
    }

    private func setValueIfPossible(_ value: Any, forKey key: String, target: NSObject) {
        let selectorName = "set\(key.prefix(1).uppercased())\(key.dropFirst()):"
        guard target.responds(to: NSSelectorFromString(selectorName)) else {
            NSLog("[IOSAppUpload] skip unsupported BDHUploadFile key: %@", key)
            return
        }
        target.setValue(value, forKey: key)
    }

    private func invokeNoArgIfPossible(target: NSObject, selectorNames: [String]) {
        for selectorName in selectorNames {
            let selector = NSSelectorFromString(selectorName)
            guard target.responds(to: selector) else {
                continue
            }
            _ = target.perform(selector)
            return
        }
    }

    private func invokeOneObjectArgIfPossible(target: NSObject, selectorNames: [String], arg: NSObject) -> Bool {
        for selectorName in selectorNames {
            let selector = NSSelectorFromString(selectorName)
            guard target.responds(to: selector) else {
                continue
            }
            _ = target.perform(selector, with: arg)
            return true
        }
        return false
    }

    private func invokeOneIntegerArgIfPossible(target: NSObject, selectorNames: [String], arg: Int64) -> Bool {
        for selectorName in selectorNames {
            let selector = NSSelectorFromString(selectorName)
            guard target.responds(to: selector) else {
                continue
            }
            typealias Function = @convention(c) (AnyObject, Selector, Int64) -> Void
            let imp = target.method(for: selector)
            let function = unsafeBitCast(imp, to: Function.self)
            function(target, selector, arg)
            return true
        }
        return false
    }
}

fileprivate enum IOSUploadAuthBuilder {
    static let bizId: Int = 1047
    static let appKey = "367bc74f0b235f36b87c4cdfedf01745"
    static let connectTimeoutSeconds: Double = 3 * 60
    static let uploadTimeoutSeconds: Double = 8 * 60

    private static let defaultServiceId: [IOSUploadFileType: String] = [
        .avatar: "1047_20201119113717_7U2JFbgu",
        .cover: "1047_20201119113646_GUmyF6ND",
        .video: "1047_20201119113606_q0hKym5b",
    ]

    private static let defaultServerKey: [IOSUploadFileType: String] = [
        .avatar: "8b0a55727ad1cb8b63912c82b74c8001",
        .cover: "b1bfd7a760da49b3542549796a52f237",
        .video: "0b3179d6ff6fb6cb07cb62b8f70c1841",
    ]

    struct VideoMeta {
        var width: Int
        var height: Int
        var duration: Int
        var bitrate: Int
        var templateBusinessType: Int
        var transcodingPriority: Int
        var keyFrame: [Int]
        var source: String
    }

    static func serviceId(for fileType: IOSUploadFileType) -> String? {
        return defaultServiceId[fileType]
    }

    static func serverKey(for fileType: IOSUploadFileType) -> String? {
        return defaultServerKey[fileType]
    }

    static func createRequestId() -> String {
        return md5("\(Date().timeIntervalSince1970)-\(Int.random(in: 0...Int(Int32.max)))")
    }

    static func bizToken(serviceId: String, reqId: String) -> String {
        return md5(appKey + serviceId + reqId)
    }

    static func buildUploadMeta(filePath: String, isVideo: Bool, videoMeta: VideoMeta?) -> Data? {
        let authInfo = buildAuthInfo()
        var meta = Data()
        appendBytesField(tag: 1, value: authInfo, to: &meta)
        appendStringField(tag: 2, value: buildQua(), to: &meta)
        if isVideo, let videoMeta = videoMeta, let videoParam = buildVideoInfo(videoMeta: videoMeta) {
            appendBytesField(tag: 3, value: videoParam, to: &meta)
        }
        appendInt32Field(tag: 4, value: 17, to: &meta)
        appendStringField(tag: 5, value: (filePath as NSString).lastPathComponent, to: &meta)
        appendStringField(tag: 6, value: IOSUploadNetworkResolver.networkType(), to: &meta)
        appendStringField(tag: 7, value: IOSUploadNetworkResolver.networkCarrier(), to: &meta)
        appendStringField(tag: 8, value: videoMeta?.source ?? "", to: &meta)
        return meta.isEmpty ? nil : meta
    }

    private static func buildAuthInfo() -> Data {
        var auth = Data()
        appendInt32Field(tag: 1, value: 0, to: &auth)
        appendStringField(tag: 2, value: "", to: &auth)
        appendStringField(tag: 3, value: "", to: &auth)
        appendStringField(tag: 4, value: "", to: &auth)
        appendStringField(tag: 5, value: "", to: &auth)
        appendStringField(tag: 6, value: "", to: &auth)
        return auth
    }

    private static func buildVideoInfo(videoMeta: VideoMeta) -> Data? {
        var videoInfo = Data()
        appendUInt32Field(tag: 1, value: videoMeta.width, to: &videoInfo)
        appendUInt32Field(tag: 2, value: videoMeta.height, to: &videoInfo)
        appendUInt32Field(tag: 3, value: videoMeta.duration, to: &videoInfo)
        appendUInt32Field(tag: 4, value: videoMeta.bitrate, to: &videoInfo)
        appendUInt32Field(tag: 5, value: videoMeta.templateBusinessType, to: &videoInfo)
        appendUInt32Field(tag: 6, value: videoMeta.transcodingPriority, to: &videoInfo)
        appendPackedUInt32Field(tag: 7, values: videoMeta.keyFrame, to: &videoInfo)
        return videoInfo.isEmpty ? nil : videoInfo
    }

    private static func buildQua() -> String {
        let version = Bundle.main.infoDictionary?["CFBundleShortVersionString"] as? String ?? "1.0"
        return "V1_IOS_WEISHI_\(version)_10_RDM_T"
    }

    private static func appendInt32Field(tag: UInt64, value: Int, to data: inout Data) {
        guard value != 0 else { return }
        appendVarint((tag << 3) | 0, to: &data)
        appendVarint(UInt64(UInt32(bitPattern: Int32(value))), to: &data)
    }

    private static func appendUInt32Field(tag: UInt64, value: Int, to data: inout Data) {
        guard value > 0 else { return }
        appendVarint((tag << 3) | 0, to: &data)
        appendVarint(UInt64(UInt32(value)), to: &data)
    }

    private static func appendStringField(tag: UInt64, value: String, to data: inout Data) {
        guard !value.isEmpty, let bytes = value.data(using: .utf8) else { return }
        appendBytesField(tag: tag, value: bytes, to: &data)
    }

    private static func appendBytesField(tag: UInt64, value: Data, to data: inout Data) {
        guard !value.isEmpty else { return }
        appendVarint((tag << 3) | 2, to: &data)
        appendVarint(UInt64(value.count), to: &data)
        data.append(value)
    }

    private static func appendPackedUInt32Field(tag: UInt64, values: [Int], to data: inout Data) {
        var packed = Data()
        for value in values where value > 0 {
            appendVarint(UInt64(UInt32(value)), to: &packed)
        }
        appendBytesField(tag: tag, value: packed, to: &data)
    }

    private static func appendVarint(_ value: UInt64, to data: inout Data) {
        var current = value
        while current >= 0x80 {
            data.append(UInt8(current & 0x7F) | 0x80)
            current >>= 7
        }
        data.append(UInt8(current))
    }

    private static func md5(_ input: String) -> String {
        let digest = Insecure.MD5.hash(data: Data(input.utf8))
        return digest.map { String(format: "%02x", $0) }.joined()
    }
}

private extension KotlinByteArray {
    func toData() -> Data {
        let size = Int(self.size)
        var bytes = [UInt8](repeating: 0, count: size)
        for index in 0..<size {
            bytes[index] = UInt8(bitPattern: self.get(index: Int32(index)))
        }
        return Data(bytes)
    }
}

fileprivate enum IOSUploadNetworkResolver {
    static func networkType() -> String {
        let status = reachabilityStatus()
        switch status {
        case "wifi":
            return "wifi"
        case "wwan":
            return "mobile"
        default:
            return ""
        }
    }

    static func networkCarrier() -> String {
        return ""
    }

    private static func reachabilityStatus() -> String {
        guard
            let reachabilityClass = NSClassFromString("WSTelephoneNetworkInfo") as? NSObject.Type,
            reachabilityClass.responds(to: NSSelectorFromString("shareInstance")),
            let shared = reachabilityClass.perform(NSSelectorFromString("shareInstance"))?.takeUnretainedValue() as? NSObject
        else {
            return ""
        }

        let wifiSelector = NSSelectorFromString("isWiFi")
        if shared.responds(to: wifiSelector), let value = shared.perform(wifiSelector)?.takeUnretainedValue() as? Bool, value {
            return "wifi"
        }

        let wwanSelector = NSSelectorFromString("isWWAN")
        if shared.responds(to: wwanSelector), let value = shared.perform(wwanSelector)?.takeUnretainedValue() as? Bool, value {
            return "wwan"
        }
        return ""
    }
}

fileprivate final class IOSUploadSdkCallbackProxy: NSObject {
    weak var owner: IOSBaseVmeUploadTask?

    init(owner: IOSBaseVmeUploadTask) {
        self.owner = owner
    }

    @objc func notifyBDHPrepareResult(_ prepareResult: NSObject?) {
        _ = prepareResult
    }

    @objc func notifyBDHProgress(_ progress: NSObject?) {
        guard let owner else { return }
        let current = IOSUploadSdkCallbackProxy.int64Value(for: progress, keys: ["recvLen", "current", "currentLen"])
        let total = IOSUploadSdkCallbackProxy.int64Value(for: progress, keys: ["totalLen", "total", "fileLen"])
        if total > 0 {
            owner.onProgress(current: current, total: total)
        }
    }

    @objc func notifyBDHResult(_ result: NSObject?) {
        guard let owner else { return }
        let success = IOSUploadSdkCallbackProxy.boolValue(for: result, keys: ["result", "success"])
        if success {
            owner.onSuccess(result: result)
            return
        }
        let code = IOSUploadSdkCallbackProxy.int32Value(for: result, keys: ["busiErrCode", "errorCode"])
        let message = IOSUploadSdkCallbackProxy.stringValue(for: result, keys: ["busiErrMsg", "errMsg"])
        owner.onFailure(code: code == 0 ? -20002 : code, message: message)
    }

    @objc func notifyBDHCancel(_ uploadFile: NSObject?, cancelType: Int32) {
        _ = uploadFile
        _ = cancelType
        owner?.onFailure(code: -20005, message: "Upload cancelled")
    }

    @objc func onUpdateProgress(_ result: NSObject?, file: NSObject?) {
        let progressObj = result ?? file
        notifyBDHProgress(progressObj)
    }

    @objc func onSuccess(_ result: NSObject?, file: NSObject?) {
        _ = file
        owner?.onSuccess(result: result)
    }

    @objc func onFailed(_ result: NSObject?, file: NSObject?) {
        guard let owner else { return }
        let codeFromResult = IOSUploadSdkCallbackProxy.int32Value(for: result, keys: ["buErrorCode", "errorCode", "busiErrCode"])
        let messageFromResult = IOSUploadSdkCallbackProxy.stringValue(for: result, keys: ["buErrInfo", "errMsg", "busiErrMsg"])
        if codeFromResult != 0 || messageFromResult != nil {
            owner.onFailure(code: codeFromResult == 0 ? -20002 : codeFromResult, message: messageFromResult)
            return
        }
        owner.onFailure(code: -20002, message: "Upload failed")
    }

    fileprivate static func int64Value(for object: NSObject?, keys: [String]) -> Int64 {
        for key in keys {
            if let number = value(for: object, key: key) as? NSNumber {
                return number.int64Value
            }
            if let string = value(for: object, key: key) as? String, let number = Int64(string) {
                return number
            }
        }
        return 0
    }

    private static func int32Value(for object: NSObject?, keys: [String]) -> Int32 {
        for key in keys {
            if let number = value(for: object, key: key) as? NSNumber {
                return number.int32Value
            }
            if let string = value(for: object, key: key) as? String, let number = Int32(string) {
                return number
            }
        }
        return 0
    }

    private static func boolValue(for object: NSObject?, keys: [String]) -> Bool {
        for key in keys {
            if let number = value(for: object, key: key) as? NSNumber {
                return number.boolValue
            }
            if let string = value(for: object, key: key) as? String {
                return (string as NSString).boolValue
            }
        }
        return false
    }

    fileprivate static func stringValue(for object: NSObject?, keys: [String]) -> String? {
        for key in keys {
            if let string = value(for: object, key: key) as? String, !string.isEmpty {
                return string
            }
            if let data = value(for: object, key: key) as? Data, let string = String(data: data, encoding: .utf8), !string.isEmpty {
                return string
            }
        }
        return nil
    }

    fileprivate static func value(for object: NSObject?, key: String) -> Any? {
        guard let object else { return nil }
        guard object.responds(to: NSSelectorFromString(key)) ||
              object.responds(to: NSSelectorFromString("is\(key.prefix(1).uppercased())\(key.dropFirst())")) ||
              object.responds(to: NSSelectorFromString("get\(key.prefix(1).uppercased())\(key.dropFirst())")) else {
            return nil
        }
        return object.value(forKey: key)
    }
}

fileprivate class IOSBaseVmeUploadTask: NSObject, UploadTask {
    private let listener: any UploadTaskListener
    fileprivate let filePath: String
    private let createUploadFile: (Int64) -> NSObject?
    private let lock = NSLock()
    private let taskId: Int64 = Int64(Date().timeIntervalSince1970 * 1000) + Int64(Int.random(in: 1...999))

    private var uploadFile: NSObject?
    private var callbackProxy: IOSUploadSdkCallbackProxy?
    private var cancelled = false

    init(
        filePath: String,
        listener: any UploadTaskListener,
        createUploadFile: @escaping (Int64) -> NSObject?
    ) {
        self.filePath = filePath
        self.listener = listener
        self.createUploadFile = createUploadFile
    }

    func upload() {
        lock.lock()
        cancelled = false
        lock.unlock()

        refreshWsTokenThenUpload()
    }

    private func refreshWsTokenThenUpload() {
        NSLog("[IOSAppUpload] refreshWsTokenThenUpload: auth removed, skip refresh")
        doUploadIfNeeded()
    }

    private func doUploadIfNeeded() {
        lock.lock()
        let shouldUpload = !cancelled
        lock.unlock()
        guard shouldUpload else { return }
        doUpload()
    }

    private func doUpload() {
        let file = createUploadFile(taskId)
        guard let uploadFile = file else {
            listener.onFailure(code: -20000, message: "createUploadFile failed")
            return
        }
        let callbackProxy = IOSUploadSdkCallbackProxy(owner: self)
        self.callbackProxy = callbackProxy
        uploadFile.setValue(callbackProxy, forKey: "delegate")
        self.uploadFile = uploadFile
        listener.onProgress(current: 0, total: IOSUploadSdkCallbackProxy.int64Value(for: uploadFile, keys: ["fileSize"]))

        let didStart = IOSUploadSdkInvoker.shared.startUpload(fileObject: uploadFile)
        if !didStart {
            listener.onFailure(code: -20001, message: "submit upload failed")
        }
    }

    func cancel_() -> Bool {
        lock.lock()
        cancelled = true
        lock.unlock()
        guard let uploadFile else {
            return false
        }
        _ = uploadFile
        return IOSUploadSdkInvoker.shared.cancelUpload(taskId: taskId)
    }

    func resume_() -> Bool {
        upload()
        return true
    }

    fileprivate func onProgress(current: Int64, total: Int64) {
        lock.lock()
        let shouldCallback = !cancelled
        lock.unlock()
        guard shouldCallback else { return }
        listener.onProgress(current: current, total: total)
    }

    fileprivate func onSuccess(result: NSObject?) {
        lock.lock()
        let shouldCallback = !cancelled
        lock.unlock()
        guard shouldCallback else { return }
        let uploadResult = mapSuccessResult(result: result)
        listener.onSuccess(result: uploadResult)
    }

    fileprivate func onFailure(code: Int32, message: String?) {
        lock.lock()
        let shouldCallback = !cancelled
        lock.unlock()
        guard shouldCallback else { return }
        listener.onFailure(code: code, message: message)
    }

    fileprivate func mapSuccessResult(result: NSObject?) -> UploadResult {
        return UploadResult(filePath: filePath, url: "", videoId: "", fileId: "")
    }
}

fileprivate final class IOSVmeImageUploadTask: IOSBaseVmeUploadTask {
    init(input: ImageUploadInput, listener: any UploadTaskListener) {
        super.init(
            filePath: input.filePath,
            listener: listener,
            createUploadFile: { taskId in
                let fileType: IOSUploadFileType
                switch input.scene {
                case .avatar:
                    fileType = .avatar
                case .image:
                    // 对齐 Android：举报证据图走 avatar 通道，避免服务端鉴权拒绝
                    fileType = .avatar
                default:
                    // 兜底按 cover 通道处理（当前实际覆盖 ImageUploadScene.COVER）
                    fileType = .cover
                }
                return IOSUploadSdkInvoker.shared.createUploadFile(
                    path: input.filePath,
                    taskId: taskId,
                    fileType: fileType
                )
            }
        )
    }

    override fileprivate func mapSuccessResult(result: NSObject?) -> UploadResult {
        let url = IOSUploadSdkCallbackProxy.stringValue(for: result, keys: ["downUrl", "url"])
            ?? firstString(from: IOSUploadSdkCallbackProxy.value(for: result, key: "cosUrl"))
            ?? ""
        let fileId = IOSUploadSdkCallbackProxy.stringValue(for: result, keys: ["fileId"]) ?? url
        return UploadResult(filePath: filePath, url: url, videoId: "", fileId: fileId)
    }

    private func firstString(from any: Any?) -> String? {
        if let list = any as? [String] {
            return list.first
        }
        if let nsArray = any as? NSArray, let first = nsArray.firstObject as? String {
            return first
        }
        return nil
    }
}

fileprivate final class IOSVmeVideoUploadTask: IOSBaseVmeUploadTask {
    init(input: VideoUploadInput, listener: any UploadTaskListener) {
        super.init(
            filePath: input.filePath,
            listener: listener,
            createUploadFile: { taskId in
                return IOSUploadSdkInvoker.shared.createUploadFile(path: input.filePath, taskId: taskId, input: input)
            }
        )
    }

    override fileprivate func mapSuccessResult(result: NSObject?) -> UploadResult {
        let videoId = IOSUploadSdkCallbackProxy.stringValue(for: result, keys: ["sid", "videoId"])
            ?? IOSUploadSdkCallbackProxy.stringValue(for: result, keys: ["fileId"])
            ?? ""
        // 对齐 Android：发布请求中的 file_id 使用 videoId，而非 BDH 回调中的 fileId
        return UploadResult(filePath: filePath, url: "", videoId: videoId, fileId: videoId)
    }
}

// MARK: - AdJumpAction

private func setupIOSAdJumpActionIfAvailable() {
    let installerSelector = NSSelectorFromString("install")
    guard
        let installerClass = NSClassFromString("IOSAdJumpActionInstaller") as? NSObject.Type,
        installerClass.responds(to: installerSelector)
    else {
        NSLog("[Startup][Ad][iOS] IOSAdJumpActionInstaller not found, skip adJumpAction setup.")
        return
    }
    _ = installerClass.perform(installerSelector)
}

// MARK: - ComposeBridge

private func setupComposeBridge() {
    IIOSComposeBridgeKt.iosComposeBridge = IOSComposeBridge()
}

// MARK: - AppTask

private func setupAppTask() {
    QnPlatformLogic.shared.task = IOSAppTask()
}

fileprivate class IOSAppTask: ITask {

    func postAction(action: @escaping () -> Void, delayTime: Int64) -> any IKmmActionResult {
        let delaySeconds = max(0, Double(delayTime) / 1000.0)
        DispatchQueue.main.asyncAfter(deadline: .now() + delaySeconds) {
            action()
        }
        return DefaultKmmActionResult()
    }

    func runIOAction(action: @escaping () -> Void) {
        DispatchQueue.global(qos: .utility).async {
            action()
        }
    }

    func runMainAction(action: @escaping () -> Void) {
        if Thread.isMainThread {
            action()
        } else {
            DispatchQueue.main.async {
                action()
            }
        }
    }

    func runCpuAction(action: @escaping () -> Void) {
        DispatchQueue.global(qos: .userInitiated).async {
            action()
        }
    }
}

// MARK: - AppPageStack

private func setupAppPageStack() {
    QnPlatformLogic.shared.appPageStack = IOSHostAppPageStack()
}

fileprivate final class IOSHostAppPageStack: NSObject, IAppPageStack {
    private let pageLock = NSRecursiveLock()
    private var pages: [any IKmmContext] = []

    func onPageCreated(stack: Any?, context: any IKmmContext) {
        pageLock.lock()
        defer { pageLock.unlock() }
        if !pages.contains(where: { isSameContext($0, context) }) {
            pages.append(context)
        }
        IOSPageStackStore.onPageCreated(withStack: stack, context: context)
    }

    func onPageDestroyed(stack: Any?, context: any IKmmContext) {
        pageLock.lock()
        defer { pageLock.unlock() }
        pages.removeAll { isSameContext($0, context) }
        IOSPageStackStore.onPageDestroyed(withStack: stack, context: context)
    }

    func getAllPages() -> [any IKmmContext] {
        pageLock.lock()
        defer { pageLock.unlock() }
        return pages
    }

    func getActivePages() -> [any IKmmContext] {
        IOSPageStackStore.activePages()
    }

    func getTopValidPage() -> (any IKmmContext)? {
        IOSPageStackStore.topValidPage() ?? getAllPages().last
    }

    func isPageActive(context: any IKmmContext) -> Bool {
        IOSPageStackStore.isPageActive(context)
    }

    func applicationStateActive() -> Bool {
        IOSPageStackStore.applicationStateActive()
    }

    func getPageLifecycleState(context: any IKmmContext) -> PageLifecycleState {
        IOSPageStackStore.pageLifecycleState(for: context)
    }

    private func isSameContext(_ lhs: any IKmmContext, _ rhs: any IKmmContext) -> Bool {
        (lhs as AnyObject) === (rhs as AnyObject)
    }
}

// MARK: - AppRouter

private func setupAppRouter() {
    QnFrameworkLogic.shared.appRouter = IOSHostAppRouter()
}

fileprivate final class IOSHostAppRouter: NSObject, IAppRouterBase {
    func to(context: (any IKmmContext)?, request: ComponentRequest, completionHandler: @escaping (Error?) -> Void) {
        let route = request.item?.flexDto.url.trimmingCharacters(in: .whitespacesAndNewlines) ?? ""
        if !route.isEmpty {
            _ = IOSNativeRouter.openRoute(route, context: context, animated: true)
        }
        completionHandler(nil)
    }

    func to(context: (any IKmmContext)?, scheme: String, completionHandler: @escaping (Error?) -> Void) {
        _ = IOSNativeRouter.openRoute(scheme, context: context, animated: true)
        completionHandler(nil)
    }

    func toComposePage(context: (any IKmmContext)?, pageName: String, pageArgs: any IComposePageArgs, completionHandler: @escaping (Error?) -> Void) {
        // MODAL_BOTTOM 走 presentViewController，从底部向上覆盖全屏；其它走默认 push。
        // 对应 Android `KuiklyRenderActivity.start` 里的同名分支。
        if pageArgs.transition == PageTransition.modalBottom {
            _ = IOSNativeRouter.presentPage(withName: pageName, pageData: pageArgs.pushPageArgsToMap, context: context, animated: true)
        } else if pageArgs.launchType == ComposePageLaunchType.singleInstance {
            _ = IOSNativeRouter.openSingleInstancePage(withName: pageName, pageData: pageArgs.pushPageArgsToMap, context: context, animated: true)
        } else {
            _ = IOSNativeRouter.openPage(withName: pageName, pageData: pageArgs.pushPageArgsToMap, context: context, animated: true)
        }
        completionHandler(nil)
    }

    func toComposeDialog(context: (any IKmmContext)?, popType: any IPopType, pageName: String, pageArgs: any IComposePageArgs, completionHandler: @escaping (Error?) -> Void) {
        _ = popType
        _ = IOSNativeRouter.presentDialogPage(withName: pageName, pageData: pageArgs.pushPageArgsToMap, context: context, animated: false)
        completionHandler(nil)
    }

    func goBack(context: (any IKmmContext)?, completionHandler: @escaping (Error?) -> Void) {
        _ = IOSNativeRouter.goBack(with: context, animated: true)
        completionHandler(nil)
    }

    func moveTaskToBack(context: (any IKmmContext)?, completionHandler: @escaping (Error?) -> Void) {
        completionHandler(nil)
    }

    func replace(context: (any IKmmContext)?, pushAnimation: Bool, scheme: String, completionHandler: @escaping (Error?) -> Void) {
        _ = IOSNativeRouter.goBack(with: context, animated: false)
        _ = IOSNativeRouter.openRoute(scheme, context: nil, animated: pushAnimation)
        completionHandler(nil)
    }

    func quit(context: (any IKmmContext)?, completionHandler: @escaping (Error?) -> Void) {
        _ = IOSNativeRouter.quit(with: context, animated: true)
        completionHandler(nil)
    }
}

// MARK: - AppConfig

private func setupAppConfig() {
    QnPlatformLogic.shared.appConfig = IOSAppConfig()
}

fileprivate class IOSAppConfig: IAppConfig {

    func getShiplyConfig(key: String, defaultValue: String) -> String {
        return IOSToggleBridge.getShiplyConfig(key: key, defaultValue: defaultValue)
    }

    func getShiplySwitch(key: String, defaultValue: Bool) -> Bool {
        return IOSToggleBridge.getShiplySwitch(key: key, defaultValue: defaultValue)
    }

    func getTabExpInt(key: String, defaultValue: Int32) -> Int32 {
        IOSRomaABSetup.setup()
        let value = IOSRomaABSetup.getTabExpInt(key: key, defaultValue: Int(defaultValue))
        return Int32(value)
    }

    func getTabExpInt(key: String, defaultValue: Int) -> Int {
        IOSRomaABSetup.setup()
        return IOSRomaABSetup.getTabExpInt(key: key, defaultValue: defaultValue)
    }
}

// MARK: - AppReport

private func setupAppReport() {
    QnPlatformLogic.shared.appReport = IOSAppReport()
}

// MARK: - AppStatus

private func setupAppStatus() {
    let appStatus = IOSAppStatus()
    appStatus.recordAppLaunchIfNeeded()
    QnPlatformLogic.shared.appStatus = appStatus
}

// MARK: - AppDevice

private func setupAppDevice() {
    QnPlatformLogic.shared.appDevice = IOSAppDevice()
}

/// iOS 端 `IAppDevice` 实现，只暴露当前平台的 `IIOSDevice` 能力。
fileprivate final class IOSAppDevice: NSObject, IAppDevice, IIOSDevice {

    func getAndroidRom() -> IAndroidDevice? {
        nil
    }

    func getHarmonyRom() -> IHarmonyDevice? {
        nil
    }

    func getIOSRom() -> IIOSDevice? {
        self
    }

    func getScreenType() -> ScreenType {
        UIDevice.current.userInterfaceIdiom == .pad ? .pad : .phone
    }
}

class IOSAppReport: IAppReport {

    /// 共享的 DDFileLogger 实例，仅用于获取日志文件路径（与 IOSDDFileLogger 使用相同的 WSFolderLogFileManager）
    private static let sharedFileLogger = DDFileLogger(logFileManager: WSFolderLogFileManager())

    /// TDLogSDK 懒初始化（仅初始化一次）
    private static let tdLogSDKReady: Bool = {
        print("[IOSAppReport] TDLogSDK 开始初始化...")
        let dataSource = TDLogDataSourceImpl.shared
        // 先为 TDOSLoggerProxy（NSProxy 子类）注入实际的日志实现，
        // 否则 proxy 转发消息时 logger 为 nil 会导致 ___forwarding___ 崩溃
        let loggerProxy = TDOSLoggerProxy.default()
        let logger = TDOSLogger(config: TDOSLoggerConfig.default())
        loggerProxy.setLogger(logger)
        print("[IOSAppReport] TDOSLoggerProxy 已注入")
        let filePacker = WSLogFilePacker.sharedInstance()
        print("[IOSAppReport] WSLogFilePacker 单例已创建: \(filePacker)")
        let depends = TDIAGDepends(
            logImp: loggerProxy,
            kvFactoryImp: TDMMKVFactoryImpl.sharedInstance(),
            andFilePackerImp: filePacker
        )
        // appId/appKey 与 Bugly 配置保持一致，按构建类型区分
        let (tdLogAppId, tdLogAppKey) = IOSAppReport.resolveTDLogAppConfig()
        let config = TDLogSDKConfig(
            appId: tdLogAppId,
            appKey: tdLogAppKey,
            dataSource: dataSource,
            depends: depends
        )
        print("[IOSAppReport] TDLogSDK config: appId=\(config.appId ?? ""), guid=\(dataSource.guidForTDLog())")
        let started = TDLogSDK.sharedInstance().start(with: config)
        print("[IOSAppReport] TDLogSDK 初始化\(started ? "成功" : "失败")")
        return started
    }()

    /// 根据构建类型返回 TDLog 的 appId/appKey，与 Bugly 配置保持一致
    private static func resolveTDLogAppConfig() -> (appId: String, appKey: String) {
#if DEBUG
        return ("6195c441a6", "45cd3bfc-257e-422a-a201-022e5796d23e")
#elseif ALPHA
        return ("ea029f30e7", "bc611f5f-d3f5-40ee-bcf9-0a328bf1fa8d")
#else
        return ("d2deefb3f8", "10e1da1e-ac80-4f66-bbfd-92bd026f3745")
#endif
    }

    func reportBeacon(event: String, params: [String: String]?) {
        IOSBeaconReporter.report(event: event, params: params)
    }

    func reportBugly(msg: String, error: KotlinThrowable?) {
        IOSBuglyReporter.report(msg: msg, error: error)
    }

    func reportDt(event: String, params: [String: String]?) {
    }

    func setPageStartFrom(from: String) {
    }

    func resetPageStartFrom() {
    }

    /// 随机后缀，用于标识本次上传（与 QQNews 保持一致）
    private static var randomSuffix: UInt = 0

    func uploadLogToBugly(onResult: @escaping (KotlinBoolean) -> Void) {
        print("[IOSAppReport] uploadLogToBugly() 调用")
        guard IOSAppReport.tdLogSDKReady else {
            print("[IOSAppReport] TDLogSDK 未初始化，跳过上传")
            onResult(KotlinBoolean(value: false))
            return
        }

        if IOSAppReport.randomSuffix == 0 {
            IOSAppReport.randomSuffix = UInt(arc4random_uniform(10000))
        }

        let fileLogger = IOSAppReport.sharedFileLogger
        let logsDir = fileLogger.logFileManager.logsDirectory
        print("[IOSAppReport] 日志目录: \(logsDir)")
        // 先滚动日志文件，关闭当前正在写入的文件句柄，
        // 确保所有日志文件都已关闭，可以安全读取
        print("[IOSAppReport] 开始 rollLogFile...")
        fileLogger.rollLogFile(withCompletion: {
            print("[IOSAppReport] rollLogFile 完成，开始收集日志文件")
            let logFiles = IOSAppReport.collectLogFiles(fileLogger: fileLogger)
            guard !logFiles.isEmpty else {
                print("[IOSAppReport] 无有效日志文件可上传")
                onResult(KotlinBoolean(value: false))
                return
            }

            print("[IOSAppReport] 已生成分层级日志 zip（内部含 business/ video/ rcprofiler/），交给 TDLogSDK 上传")
            for file in logFiles {
                let size = (try? FileManager.default.attributesOfItem(atPath: file)[.size] as? Int) ?? -1
                print("[IOSAppReport]   \((file as NSString).lastPathComponent) (size=\(size))")
            }

            let tag = String(format: "用户日志[%04lu]", IOSAppReport.randomSuffix)
            let summary = UIDevice.current.name
            print("[IOSAppReport] 上传参数: tag=\(tag), summary=\(summary), fileCount=\(logFiles.count)")

            TDLogSDK.sharedInstance().uploadFiles(
                logFiles,
                withTag: tag,
                summary: summary,
                andExtendInfo: nil
            ) { success, errorMsg in
                if success {
                    print("[IOSAppReport] TDLogSDK 日志上传成功")
                } else {
                    print("[IOSAppReport] TDLogSDK 日志上传失败: \(errorMsg ?? "unknown")")
                }
                onResult(KotlinBoolean(value: success))
            }
        })
    }

    func prepareFeedbackLogZipBase64(onResult: @escaping (FeedbackLogZipPayload?) -> Void) {
        print("[IOSAppReport] prepareFeedbackLogZipBase64() 调用")
        IOSAppReport.prepareShareLogData { data, _ in
            guard let data, !data.isEmpty else {
                print("[IOSAppReport] prepareFeedbackLogZipBase64: 无有效日志 zip")
                onResult(nil)
                return
            }
            DispatchQueue.global(qos: .utility).async {
                let base64 = data.base64EncodedString()
                DispatchQueue.main.async {
                    onResult(FeedbackLogZipPayload(base64: base64, fileSuffix: "zip"))
                }
            }
        }
    }

    // MARK: - 分享日志：数据准备（供 IOSAppShare 跨文件复用）

    /// 复用 `collectLogFiles` 产出的 zip，作为分享文件数据源。
    /// 由于 `collectLogFiles` 本身异步依赖 `rollLogFile`，这里同样先 roll 再读。
    static func prepareShareLogData(completion: @escaping (Data?, String?) -> Void) {
        let fileLogger = IOSAppReport.sharedFileLogger
        fileLogger.rollLogFile(withCompletion: {
            let logFiles = IOSAppReport.collectLogFiles(fileLogger: fileLogger)
            guard let zipPath = logFiles.first,
                  let data = try? Data(contentsOf: URL(fileURLWithPath: zipPath)) else {
                print("[IOSAppReport] prepareShareLogData: zip 读取失败")
                DispatchQueue.main.async { completion(nil, nil) }
                return
            }
            DispatchQueue.main.async {
                completion(data, (zipPath as NSString).lastPathComponent)
            }
        })
    }

    /// 生成前缀 `[IDFV-xxxxxx]-` 方便服务端区分设备
    static func logSharePrefix() -> String {
        let idfv = UIDevice.current.identifierForVendor?.uuidString ?? "unknown"
        return "[IDFV-\(idfv)]-"
    }

    /// 微信文件消息大小上限 10MB，参照旧版 `QNShareLog`
    static let LOG_FILE_MAX_UPLOAD_TO_WX: Int = 10 * 1024 * 1024

    // MARK: - 日志文件收集

    /// 为 video / rcprofiler 两个子目录各自 roll 一次，确保异步缓冲已刷到磁盘。
    /// 同步等待，超时 1s。
    private static func flushSubLoggers() {
        let libraryDir = NSSearchPathForDirectoriesInDomains(.libraryDirectory, .userDomainMask, true).first!
        let subDirs = [
            (libraryDir as NSString).appendingPathComponent("WSFolder/video"),
            (libraryDir as NSString).appendingPathComponent("WSFolder/rcprofiler"),
        ]
        for dir in subDirs {
            var isDir: ObjCBool = false
            guard FileManager.default.fileExists(atPath: dir, isDirectory: &isDir), isDir.boolValue else { continue }
            let mgr = DDLogFileManagerDefault(logsDirectory: dir)
            let tmpLogger = DDFileLogger(logFileManager: mgr)
            let sema = DispatchSemaphore(value: 0)
            tmpLogger.rollLogFile(withCompletion: {
                sema.signal()
            })
            _ = sema.wait(timeout: .now() + .seconds(1))
        }
    }

    /// 收集所有日志并打包为单个 zip（内部带 business/video/rcprofiler 三级目录层级），
    /// 返回 zip 文件路径的单元素数组，交给 TDLogSDK 上传（SDK 会再套一层压缩，里层 zip 内目录结构保留）。
    private static func collectLogFiles(fileLogger: DDFileLogger) -> [String] {
        flushSubLoggers()
        let fm = FileManager.default
        let logsDir = fileLogger.logFileManager.logsDirectory
        print("[IOSAppReport] collectLogFiles() 日志目录: \(logsDir)")

        let businessFiles = collectValidFiles(inDirectory: logsDir, subName: "business")
        let videoDir = (logsDir as NSString).appendingPathComponent("video")
        let rcDir = (logsDir as NSString).appendingPathComponent("rcprofiler")
        let videoFiles = collectValidFiles(inDirectory: videoDir, subName: "video")
        let rcFiles = collectValidFiles(inDirectory: rcDir, subName: "rcprofiler")

        let total = businessFiles.count + videoFiles.count + rcFiles.count
        print("[IOSAppReport] 日志统计: business=\(businessFiles.count), video=\(videoFiles.count), rcprofiler=\(rcFiles.count), total=\(total)")
        guard total > 0 else {
            print("[IOSAppReport] 无任何可上传日志文件")
            return []
        }

        // 清理上一次生成的同名 zip
        let libraryDir = NSSearchPathForDirectoriesInDomains(.libraryDirectory, .userDomainMask, true).first!
        let zipPath = (libraryDir as NSString).appendingPathComponent("wesee_logs_\(Int(Date().timeIntervalSince1970)).zip")
        if fm.fileExists(atPath: zipPath) {
            try? fm.removeItem(atPath: zipPath)
        }

        let subdirFiles: [String: [String]] = [
            "business": businessFiles,
            "video": videoFiles,
            "rcprofiler": rcFiles,
        ]
        let ok = WSMinizipHelper.createZipFile(
            atPath: zipPath,
            withSubdirectoryFiles: subdirFiles
        )
        guard ok, fm.fileExists(atPath: zipPath) else {
            print("[IOSAppReport] 分层级 zip 打包失败: \(zipPath)")
            return []
        }
        let zipSize = (try? fm.attributesOfItem(atPath: zipPath)[.size] as? Int) ?? -1
        print("[IOSAppReport] 分层级 zip 打包完成: \(zipPath), size=\(zipSize)")
        return [zipPath]
    }

    /// 收集某个目录下的有效（非空、非目录）文件绝对路径。
    /// - Parameters:
    ///   - directory: 目录绝对路径
    ///   - subName: 仅用于日志打印
    private static func collectValidFiles(inDirectory directory: String, subName: String) -> [String] {
        let fm = FileManager.default
        var isDir: ObjCBool = false
        guard fm.fileExists(atPath: directory, isDirectory: &isDir), isDir.boolValue else {
            print("[IOSAppReport] \(subName) 目录不存在: \(directory)")
            return []
        }
        let items = (try? fm.contentsOfDirectory(atPath: directory)) ?? []
        var result: [String] = []
        for item in items {
            let full = (directory as NSString).appendingPathComponent(item)
            var itemIsDir: ObjCBool = false
            guard fm.fileExists(atPath: full, isDirectory: &itemIsDir), !itemIsDir.boolValue else { continue }
            let size = (try? fm.attributesOfItem(atPath: full)[.size] as? Int) ?? 0
            guard size > 0 else { continue }
            result.append(full)
        }
        print("[IOSAppReport] \(subName) 子目录收集: \(result.count) 个文件")
        return result
    }
}

// MARK: - TDLogSDK DataSource

/// TDLogSDK 所需的数据源实现（单例）
fileprivate class TDLogDataSourceImpl: NSObject, TDLogSDKDataSource {
    static let shared = TDLogDataSourceImpl()
    private override init() { super.init() }

    func guidForTDLog() -> String {
        // TDLogSDK 要求 GUID 为 QIMEI，与 Android 端保持一致
        let qimei = QimeiSetup.currentQimei()
        if !qimei.isEmpty {
            print("[IOSAppReport] guidForTDLog() 使用 QIMEI: \(qimei.prefix(8))...")
            return qimei
        }
        // Qimei 尚未就绪时降级使用 IDFV
        let idfv = UIDevice.current.identifierForVendor?.uuidString ?? "unknown"
        print("[IOSAppReport] guidForTDLog() QIMEI 为空，降级使用 IDFV: \(idfv.prefix(8))...")
        return idfv
    }
}

private enum IOSBuglyReporter {
    private static let reportCategory: UInt = 5

    static func report(msg: String, error: KotlinThrowable?) {
        let reason = normalizedReason(msg: msg, error: error)
        let name = normalizedName(error: error)
        let callStack = Thread.callStackSymbols
        let extraInfo: [String: String] = ["source": "wesee-core"]

        BuglyCrashMonitorPlugin.reportException(
            withCategory: reportCategory,
            name: name,
            reason: reason,
            callStack: callStack,
            extraInfo: extraInfo,
            terminateApp: false
        )
    }

    private static func normalizedReason(msg: String, error: KotlinThrowable?) -> String {
        let trimmed = msg.trimmingCharacters(in: .whitespacesAndNewlines)
        if !trimmed.isEmpty {
            return trimmed
        }
        if let error {
            let description = String(describing: error)
            if !description.isEmpty {
                return description
            }
        }
        return "bugly report"
    }

    private static func normalizedName(error: KotlinThrowable?) -> String {
        if let error {
            return String(describing: type(of: error))
        }
        return "BuglyException"
    }
}

private enum IOSBeaconReporter {
    private static let fallbackAppKey = "0S000EAOIR2GPC95"

    static func report(event: String, params: [String: String]?) {
        let beaconEvent = BeaconEvent(
            appKey: resolveAppKey(),
            code: event,
            type: .realTime,
            success: true,
            params: params
        )
        BeaconReport.sharedInstance().report(beaconEvent)
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
}
