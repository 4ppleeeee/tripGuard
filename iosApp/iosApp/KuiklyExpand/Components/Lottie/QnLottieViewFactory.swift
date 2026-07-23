import Foundation
import UIKit
import Lottie

// MARK: - 带内存缓存的图片提供者
// 参照 QQNews QNCachedFilepathImageProvider，避免重复磁盘 IO

/// 带内存缓存的文件路径图片提供者
/// 适用于 json 格式动画（带 images/ 子目录），缓存已加载的图片避免重复磁盘读取
final class QnCachedFilepathImageProvider: AnimationImageProvider {

    private let directoryPath: String

    // 全局图片缓存（NSCache 自动管理内存，支持内存压力自动清理）
    private static let imageCache = NSCache<NSString, UIImage>()
    private static let cacheQueue = DispatchQueue(
        label: "com.qn.lottie.image.cache",
        attributes: .concurrent
    )

    private static let setupOnce: Void = {
        imageCache.countLimit = 300          // 最多 300 张
        imageCache.totalCostLimit = 80 * 1024 * 1024  // 最大 80MB
    }()

    init(filepath: String) {
        self.directoryPath = filepath
        _ = QnCachedFilepathImageProvider.setupOnce
    }

    // MARK: - AnimationImageProvider

    func imageForAsset(asset: ImageAsset) -> CGImage? {
        return loadImage(asset: asset)?.cgImage
    }

    // MARK: - 图片加载

    private func loadImage(asset: ImageAsset) -> UIImage? {
        let cacheKey = Self.cacheKey(directory: directoryPath, name: asset.name)

        // 1. 内存缓存命中
        if let cached = Self.cacheQueue.sync(execute: { Self.imageCache.object(forKey: cacheKey) }) {
            return cached
        }

        // 2. data URI 内联图片
        if asset.name.hasPrefix("data:"),
           let url = URL(string: asset.name),
           let data = try? Data(contentsOf: url),
           let image = UIImage(data: data) {
            Self.store(image: image, key: cacheKey)
            return image
        }

        // 3. 磁盘加载（先尝试直接路径，再尝试带 asset.directory 的路径）
        let directPath = (directoryPath as NSString).appendingPathComponent(asset.name)
        if let image = UIImage(contentsOfFile: directPath) {
            Self.store(image: image, key: cacheKey)
            return image
        }

        let pathWithDir = ((directoryPath as NSString)
            .appendingPathComponent(asset.directory) as NSString)
            .appendingPathComponent(asset.name)
        if let image = UIImage(contentsOfFile: pathWithDir) {
            Self.store(image: image, key: cacheKey)
            return image
        }

        return nil
    }

    // MARK: - 缓存管理

    private static func cacheKey(directory: String, name: String) -> NSString {
        return "\(directory.hash)_\(name)" as NSString
    }

    private static func store(image: UIImage, key: NSString) {
        let cost = Int(image.size.width * image.size.height * 4)  // RGBA 估算
        cacheQueue.async(flags: .barrier) {
            imageCache.setObject(image, forKey: key, cost: cost)
        }
    }

    /// 清除所有图片缓存（内存压力时可主动调用）
    static func clearAll() {
        cacheQueue.async(flags: .barrier) {
            imageCache.removeAllObjects()
        }
    }
}

/// ObjC 可用的 Lottie 动画视图协议
@objc
public protocol QnLottieAnimatable: AnyObject {
    var currentProgress: CGFloat { get set }
    /// -1 = 无限循环，0 = 播放一次
    var loopAnimationCount: CGFloat { get set }
    var isAnimationPlaying: Bool { get }
    func play()
    func playFromProgress(_ fromProgress: CGFloat, toProgress: CGFloat, completion: ((Bool) -> Void)?)
    func stop()
    func setColorValue(_ color: UIColor, forKeypath keypath: CompatibleAnimationKeypath)
    func setTextProvider(_ values: [String: String])
}

/// 带图片资源目录支持的 Lottie 动画视图
/// - zip/lottie 文件：通过 DotLottieFile 加载，lottie-ios 内部自行解压，无需 SSZipArchive
/// - json 文件：通过 LottieAnimation + FilepathImageProvider 加载，支持同目录 images/
@objc
public final class QnLottieAnimationView: UIView, QnLottieAnimatable {

    private let animView: LottieAnimationView

    // MARK: - 同步初始化（仅用于 json，主线程安全）

    /// 从本地 JSON 文件路径创建（主线程安全）
    @objc
    public init(jsonFilePath: String) {
        let animation = (try? Data(contentsOf: URL(fileURLWithPath: jsonFilePath)))
            .flatMap { try? LottieAnimation.from(data: $0) }
        let imageDir = (jsonFilePath as NSString).deletingLastPathComponent
        // 使用带内存缓存的图片提供者，避免重复磁盘 IO（参照 QQNews QNCachedFilepathImageProvider）
        let imageProvider = QnCachedFilepathImageProvider(filepath: imageDir)
        animView = LottieAnimationView(animation: animation, imageProvider: imageProvider)
        super.init(frame: .zero)
        setup()
    }

    /// 从已解析好的 DotLottieFile 创建（主线程安全，解析在后台完成）
    @objc
    public init(dotLottieFile: AnyObject) {
        if let file = dotLottieFile as? DotLottieFile {
            animView = LottieAnimationView(dotLottie: file)
        } else {
            animView = LottieAnimationView()
        }
        super.init(frame: .zero)
        setup()
    }

    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }

    private func setup() {
        animView.translatesAutoresizingMaskIntoConstraints = false
        addSubview(animView)
        NSLayoutConstraint.activate([
            animView.topAnchor.constraint(equalTo: topAnchor),
            animView.bottomAnchor.constraint(equalTo: bottomAnchor),
            animView.leadingAnchor.constraint(equalTo: leadingAnchor),
            animView.trailingAnchor.constraint(equalTo: trailingAnchor),
        ])
    }

    // MARK: - 异步工厂（后台解析，主线程回调）

    /// 异步从本地文件路径加载动画视图，始终在主线程回调
    ///
    /// 格式处理策略（路径由 QnLottieDownloader 提供）：
    /// - `.lottie`：标准 dotLottie 格式（含 manifest.json），DotLottieFile 内部解压，后台线程解析
    /// - `.zip`：Android 格式 zip（含普通 json，无 manifest.json），后台解压找 json 文件后加载
    /// - `.json`：直接在主线程加载（QnCachedFilepathImageProvider 支持同目录 images/）
    ///
    /// 注意：QnLottieDownloader 对 zip/lottie/json 均直接缓存原始文件，不做任何解压处理
    @objc
    public static func loadAsync(
        filePath: String,
        completion: @escaping (QnLottieAnimationView) -> Void
    ) {
        let ext = (filePath as NSString).pathExtension.lowercased()

        if ext == "lottie" {
            // 标准 dotLottie 格式（含 manifest.json），交给 DotLottieFile 处理
            DispatchQueue.dotLottie.async {
                let result = DotLottieFile.SynchronouslyBlockingCurrentThread
                    .loadedFrom(filepath: filePath)
                DispatchQueue.main.async {
                    switch result {
                    case .success(let dotLottie):
                        completion(QnLottieAnimationView(dotLottieFile: dotLottie))
                    case .failure(let error):
                        NSLog("[QnLottieAnimationView] lottie 解析失败: %@", error.localizedDescription)
                        completion(QnLottieAnimationView(jsonFilePath: ""))
                    }
                }
            }
        } else if ext == "zip" {
            // Android 格式 zip（无 manifest.json），后台解压后找 json 文件加载
            DispatchQueue.global(qos: .userInitiated).async {
            let jsonPath = QnLottieAnimationView.extractJsonFromZip(filePath)
                DispatchQueue.main.async {
                    if let jsonPath = jsonPath {
                        completion(QnLottieAnimationView(jsonFilePath: jsonPath))
                    } else {
                        NSLog("[QnLottieAnimationView] zip 解压失败，找不到 json 文件: %@", filePath)
                        completion(QnLottieAnimationView(jsonFilePath: ""))
                    }
                }
            }
        } else {
            // .json 直接主线程加载
            completion(QnLottieAnimationView(jsonFilePath: filePath))
        }
    }

    /// 解压 Android 格式 zip，返回其中第一个 json 文件的路径
    /// 解压目录与 zip 同级，以 zip 文件名（不含后缀）命名，避免重复解压
    private static func extractJsonFromZip(_ zipPath: String) -> String? {
        let fm = FileManager.default
        let zipDir = (zipPath as NSString).deletingLastPathComponent
        let zipName = ((zipPath as NSString).lastPathComponent as NSString).deletingPathExtension
        let extractDir = (zipDir as NSString).appendingPathComponent(zipName + "_extracted")

        // 已解压过，直接找 json
        if fm.fileExists(atPath: extractDir) {
            if let found = findFirstJson(in: extractDir) {
                return found
            }
            // 目录存在但找不到 json，说明上次解压失败，删掉重试
            try? fm.removeItem(atPath: extractDir)
        }

        // 解压（使用 SSZipArchive）
        try? fm.createDirectory(atPath: extractDir, withIntermediateDirectories: true)

        // 打印 zip 文件大小，确认文件完整性
        let fileSize = (try? fm.attributesOfItem(atPath: zipPath))?[.size] as? Int ?? 0
        NSLog("[QnLottieAnimationView] 开始解压 zip，文件大小: %d bytes，路径: %@", fileSize, zipPath)

        // 使用 LVMiniZipArchive（通过 WSMinizipHelper 封装）解压，支持 Data Descriptor 格式
        let success = WSMinizipHelper.unzipFile(atPath: zipPath, toDestination: extractDir)

        if !success {
            NSLog("[QnLottieAnimationView] LVMiniZipArchive 解压失败: %@", zipPath)
            // 即使返回 false，也尝试找 json（部分 zip 解压会返回 false 但文件已写出）
            if let found = findFirstJson(in: extractDir) {
                NSLog("[QnLottieAnimationView] 解压虽失败但找到 json: %@", found)
                return found
            }
            try? fm.removeItem(atPath: extractDir)
            return nil
        }

        let contents = (try? fm.contentsOfDirectory(atPath: extractDir)) ?? []
        NSLog("[QnLottieAnimationView] 解压成功，目录内容: %@", contents)
        return findFirstJson(in: extractDir)
    }

    /// 递归查找目录中第一个 json 文件
    private static func findFirstJson(in directory: String) -> String? {
        let fm = FileManager.default
        guard let enumerator = fm.enumerator(atPath: directory) else { return nil }
        for case let file as String in enumerator {
            if (file as NSString).pathExtension.lowercased() == "json" {
                return (directory as NSString).appendingPathComponent(file)
            }
        }
        return nil
    }

    // MARK: - QnLottieAnimatable

    @objc public var currentProgress: CGFloat {
        get { animView.currentProgress }
        set { animView.currentProgress = newValue }
    }

    @objc public var loopAnimationCount: CGFloat = 0 {
        didSet { animView.loopMode = loopAnimationCount == -1 ? .loop : .playOnce }
    }

    @objc public var isAnimationPlaying: Bool {
        animView.isAnimationPlaying
    }

    @objc public override var contentMode: UIView.ContentMode {
        get { animView.contentMode }
        set { animView.contentMode = newValue }
    }

    @objc public func play() {
        animView.play()
    }

    @objc public func playFromProgress(
        _ fromProgress: CGFloat,
        toProgress: CGFloat,
        completion: ((Bool) -> Void)?
    ) {
        animView.play(fromProgress: fromProgress, toProgress: toProgress, loopMode: nil, completion: completion)
    }

    @objc public func stop() {
        animView.stop()
    }

    @objc public func setColorValue(_ color: UIColor, forKeypath keypath: CompatibleAnimationKeypath) {
        var r: CGFloat = 0, g: CGFloat = 0, b: CGFloat = 0, a: CGFloat = 0
        color.getRed(&r, green: &g, blue: &b, alpha: &a)
        let provider = ColorValueProvider(LottieColor(r: Double(r), g: Double(g), b: Double(b), a: Double(a)))
        animView.setValueProvider(provider, keypath: keypath.animationKeypath)
    }

    @objc public func setTextProvider(_ values: [String: String]) {
        animView.textProvider = DictionaryTextProvider(values)
    }
}
