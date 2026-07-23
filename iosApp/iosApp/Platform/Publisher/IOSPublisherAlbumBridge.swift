import AVFoundation
import CommonCrypto
import Foundation
import Photos
import UIKit
import umbrella

private let publisherAlbumAllFolderId = "all"
private let publisherAlbumCollectionPrefix = "collection:"
private let publisherAlbumImagePrefix = "image:"
private let publisherAlbumVideoPrefix = "video:"
private let publisherAlbumMaxImageSide: CGFloat = 3000
private let publisherAlbumMaxImagePixels: Int = 9_000_000
private let publisherAlbumMaxVideoSide: Int32 = 1920

func setupIOSPublisherAlbumBridge() {
    PublisherAlbumPlatformBridgeRegistry.shared.register(bridge: IOSPublisherAlbumBridge())
}

/// iOS 端发布器相册桥，基于 PhotoKit 读取系统相册并转换为 KMM 统一模型。
final class IOSPublisherAlbumBridge: NSObject, PublisherAlbumPlatformBridge, PHPhotoLibraryChangeObserver {

    private let imageManager = PHCachingImageManager()

    /// 相册变更事件发射器，由 observeAlbumChanges 的 callbackFlow 注入，变更时调用以发射事件。
    private var albumChangeEmitter: (() -> Void)?

    // MARK: - 全量缓存

    /// 串行队列，保护 allAssetsCache / collectionsCache 的读写，
    /// 避免 loadFolders、loadMedia、loadMonths 并行执行时重复构建缓存。
    private let cacheQueue = DispatchQueue(label: "com.weishi.publisher.album.cache")

    /// 全量 PHAsset 元数据缓存，首次查询后缓存，后续 loadMedia/loadMonths 从内存过滤。
    /// 仿照 Android 端 allRecordsCache 设计，避免每次操作都重新查询 PhotoKit。
    private var allAssetsCache: [CachedAssetRecord]? = nil
    /// 相册集合缓存，避免 loadFolders 每次都重新 fetch 所有 collection。
    private var collectionsCache: [(id: String, collection: PHAssetCollection)]? = nil
    /// 保存 buildCacheIfNeeded 中的 PHFetchResult，用于 photoLibraryDidChange 中
    /// 通过 changeDetails(for:) 判断素材列表是否真正发生了增删移动。
    private var lastFetchResult: PHFetchResult<PHAsset>? = nil

    /// 轻量级素材元数据缓存记录，只保留构建 PublisherAlbumMedia 所需的字段，不持有 PHAsset 引用。
    private struct CachedAssetRecord {
        let localIdentifier: String
        let mediaType: PHAssetMediaType
        let pixelWidth: Int
        let pixelHeight: Int
        let duration: TimeInterval
        let creationDate: Date?
        /// 素材所属的 collection localIdentifier 集合，用于按相册过滤。
        let collectionIds: Set<String>

        var isVideo: Bool { mediaType == .video }
        var isImage: Bool { mediaType == .image }
    }

    /// 构建全量缓存：一次性查询所有 PHAsset 并记录每个素材所属的相册。
    ///
    /// 优化策略（对比旧版）：
    /// - 旧版为每个 collection 调用 fetchAssets(in:) 得到素材 ID 集合，再遍历全量素材反查归属 → O(N×M)。
    /// - 新版反转方向：先为每个 collection 构建 assetId→collectionId 的反向索引（仍需 fetchAssets(in:)，
    ///   但只做 insert 不做 contains），再遍历全量素材时直接 O(1) 查 Dictionary → 总体 O(N+M×K)。
    /// - 通过 cacheQueue 串行保护，避免并行调用时重复构建。
    private func buildCacheIfNeeded() {
        // 在 cacheQueue 上同步执行，保证只有一个线程能进入构建逻辑。
        cacheQueue.sync {
            guard allAssetsCache == nil else { return }
            let totalStart = CFAbsoluteTimeGetCurrent()

            // 1. 查询所有素材（按创建时间倒序）
            let fetchStart = CFAbsoluteTimeGetCurrent()
            let options = PHFetchOptions()
            options.sortDescriptors = [NSSortDescriptor(key: "creationDate", ascending: false)]
            let allAssets = PHAsset.fetchAssets(with: options)
            let fetchCost = Int((CFAbsoluteTimeGetCurrent() - fetchStart) * 1000)
            NSLog("[PublisherAlbum][PERF] buildCacheIfNeeded fetchAssets: %d assets, cost=%dms", allAssets.count, fetchCost)

            // 2. 收集所有相册集合
            let collStart = CFAbsoluteTimeGetCurrent()
            var collections: [(id: String, collection: PHAssetCollection)] = []
            let smartAlbums = PHAssetCollection.fetchAssetCollections(with: .smartAlbum, subtype: .any, options: nil)
            let userAlbums = PHAssetCollection.fetchAssetCollections(with: .album, subtype: .any, options: nil)
            for i in 0..<smartAlbums.count {
                let c = smartAlbums.object(at: i)
                collections.append((id: c.localIdentifier, collection: c))
            }
            for i in 0..<userAlbums.count {
                let c = userAlbums.object(at: i)
                collections.append((id: c.localIdentifier, collection: c))
            }
            let collCost = Int((CFAbsoluteTimeGetCurrent() - collStart) * 1000)
            NSLog("[PublisherAlbum][PERF] buildCacheIfNeeded fetchCollections: %d collections, cost=%dms", collections.count, collCost)

            // 3. 构建反向索引：assetLocalIdentifier → 所属 collectionId 集合。
            //    遍历每个 collection 的素材列表，将 assetId 插入 Dictionary，
            //    总操作量 = 所有 collection 的素材数之和（≈ N×平均归属数），远小于旧版 O(N×M)。
            let mapStart = CFAbsoluteTimeGetCurrent()
            var assetToCollections: [String: Set<String>] = [:]
            assetToCollections.reserveCapacity(allAssets.count)
            for (collId, collection) in collections {
                let collAssets = PHAsset.fetchAssets(in: collection, options: nil)
                for j in 0..<collAssets.count {
                    let assetId = collAssets.object(at: j).localIdentifier
                    assetToCollections[assetId, default: []].insert(collId)
                }
            }
            let mapCost = Int((CFAbsoluteTimeGetCurrent() - mapStart) * 1000)
            NSLog("[PublisherAlbum][PERF] buildCacheIfNeeded reverseIndexing: %d assets indexed, cost=%dms", assetToCollections.count, mapCost)

            // 4. 构建缓存记录：遍历全量素材，直接从反向索引 O(1) 查归属。
            let buildStart = CFAbsoluteTimeGetCurrent()
            var records: [CachedAssetRecord] = []
            records.reserveCapacity(allAssets.count)
            for i in 0..<allAssets.count {
                let asset = allAssets.object(at: i)
                let belongCollections = assetToCollections[asset.localIdentifier] ?? []
                records.append(CachedAssetRecord(
                    localIdentifier: asset.localIdentifier,
                    mediaType: asset.mediaType,
                    pixelWidth: asset.pixelWidth,
                    pixelHeight: asset.pixelHeight,
                    duration: asset.duration,
                    creationDate: asset.creationDate,
                    collectionIds: belongCollections
                ))
            }

            allAssetsCache = records
            collectionsCache = collections
            lastFetchResult = allAssets
            let buildCost = Int((CFAbsoluteTimeGetCurrent() - buildStart) * 1000)
            let totalCost = Int((CFAbsoluteTimeGetCurrent() - totalStart) * 1000)
            NSLog("[PublisherAlbum][PERF] buildCacheIfNeeded DONE: %d records, buildRecordsCost=%dms, totalCost=%dms", records.count, buildCost, totalCost)
        }
    }

    /// 确保缓存已构建，返回缓存记录数组。
    /// 调用后 allAssetsCache 一定非 nil（最差情况为空数组），调用方可安全使用 allAssetsCache!。
    @discardableResult
    private func ensureCacheBuilt() -> [CachedAssetRecord] {
        buildCacheIfNeeded()
        return allAssetsCache ?? []
    }

    /// 清除元数据缓存，相册内容变化时由外部触发。
    private func clearCache() {
        cacheQueue.sync {
            allAssetsCache = nil
            collectionsCache = nil
            lastFetchResult = nil
        }
    }

    func getPermissionState(
        completionHandler: @escaping (PublisherAlbumPermissionState?, Error?) -> Void
    ) {
        completionHandler(currentPermissionState(), nil)
    }

    func requestPermission(
        completionHandler: @escaping (PublisherAlbumPermissionState?, Error?) -> Void
    ) {
        DispatchQueue.main.async {
            if #available(iOS 14.0, *) {
                // 发布器需要读取已有素材，并为后续保存创作结果预留写权限。
                PHPhotoLibrary.requestAuthorization(for: .readWrite) { status in
                    completionHandler(self.mapAuthorizationStatus(status), nil)
                }
            } else {
                PHPhotoLibrary.requestAuthorization { status in
                    completionHandler(self.mapAuthorizationStatus(status), nil)
                }
            }
        }
    }

    func openPermissionSettings() {
        DispatchQueue.main.async {
            guard let url = URL(string: UIApplication.openSettingsURLString) else { return }
            UIApplication.shared.open(url, options: [:], completionHandler: nil)
        }
    }

    func loadFolders(
        completionHandler: @escaping ([PublisherAlbumFolder]?, Error?) -> Void
    ) {
        DispatchQueue.global(qos: .userInitiated).async {
            let start = CFAbsoluteTimeGetCurrent()
            guard self.currentPermissionState().isReadable else {
                completionHandler([], nil)
                return
            }

            self.buildCacheIfNeeded()
            guard let allRecords = self.allAssetsCache else {
                completionHandler([], nil)
                return
            }

            var folders: [PublisherAlbumFolder] = []

            // "所有视频"文件夹：直接从缓存统计，O(N) 单次遍历同时统计 video/photo 数量
            if !allRecords.isEmpty {
                var videoCount = 0
                var photoCount = 0
                for record in allRecords {
                    if record.isVideo { videoCount += 1 }
                    else if record.isImage { photoCount += 1 }
                }
                folders.append(
                    PublisherAlbumFolder(
                        id: publisherAlbumAllFolderId,
                        title: "所有视频",
                        count: Int32(allRecords.count),
                        coverUrl: "",  // 延迟生成，由 ViewModel 层异步解析
                        videoCount: Int32(videoCount),
                        photoCount: Int32(photoCount)
                    )
                )
            }

            // 各相册文件夹：从缓存过滤统计，不再逐个 fetchAssets
            if let collections = self.collectionsCache {
                var seen = Set(folders.map { $0.id })
                for (collId, collection) in collections {
                    let folderId = publisherAlbumCollectionPrefix + collId
                    guard !seen.contains(folderId) else { continue }
                    // 从缓存中过滤属于该相册的素材
                    let folderRecords = allRecords.filter { $0.collectionIds.contains(collId) }
                    guard !folderRecords.isEmpty else { continue }
                    seen.insert(folderId)
                    var videoCount = 0
                    var photoCount = 0
                    for record in folderRecords {
                        if record.isVideo { videoCount += 1 }
                        else if record.isImage { photoCount += 1 }
                    }
                    folders.append(
                        PublisherAlbumFolder(
                            id: folderId,
                            title: collection.localizedTitle ?? "未命名相册",
                            count: Int32(folderRecords.count),
                            coverUrl: "",  // 延迟生成，由 ViewModel 层异步解析
                            videoCount: Int32(videoCount),
                            photoCount: Int32(photoCount)
                        )
                    )
                }
            }

            completionHandler(folders, nil)
            let cost = Int((CFAbsoluteTimeGetCurrent() - start) * 1000)
            NSLog("[PublisherAlbum][PERF] loadFolders: %d folders, cost=%dms", folders.count, cost)
        }
    }

    func loadMedia(
        folderId: String,
        tab: PublisherAlbumMediaTab,
        monthId: String?,
        completionHandler: @escaping ([PublisherAlbumMedia]?, Error?) -> Void
    ) {
        DispatchQueue.global(qos: .userInitiated).async {
            let start = CFAbsoluteTimeGetCurrent()
            guard self.currentPermissionState().isReadable else {
                completionHandler([], nil)
                return
            }
            // 确保缓存已构建，统一走缓存过滤路径
            let allRecords = self.ensureCacheBuilt()
            let filtered = self.filterRecords(allRecords, folderId: folderId, tab: tab, monthId: monthId)
            let medias = self.mediaList(from: filtered)
            let cost = Int((CFAbsoluteTimeGetCurrent() - start) * 1000)
            NSLog("[PublisherAlbum][PERF] loadMedia: folderId=%@, tab=%@, monthId=%@, %d items, cost=%dms", folderId, String(describing: tab), monthId ?? "nil", medias.count, cost)
            completionHandler(medias, nil)
        }
    }

    func loadMediaDetail(
        mediaId: String,
        completionHandler: @escaping (PublisherAlbumMedia?, Error?) -> Void
    ) {
        let sourceId = mediaId
            .replacingOccurrences(of: publisherAlbumImagePrefix, with: "")
            .replacingOccurrences(of: publisherAlbumVideoPrefix, with: "")
        let assets = PHAsset.fetchAssets(withLocalIdentifiers: [sourceId], options: nil)
        guard assets.count > 0 else {
            completionHandler(nil, nil)
            return
        }
        completionHandler(media(from: assets.object(at: 0)), nil)
    }

    func loadMonths(
        folderId: String,
        tab: PublisherAlbumMediaTab,
        completionHandler: @escaping ([PublisherAlbumMonth]?, Error?) -> Void
    ) {
        DispatchQueue.global(qos: .userInitiated).async {
            guard self.currentPermissionState().isReadable else {
                completionHandler([PublisherAlbumMonth(id: "all", title: "全部")], nil)
                return
            }
            // 确保缓存已构建，统一走缓存过滤路径提取月份
            let allRecords = self.ensureCacheBuilt()
            var seen = Set<String>()
            var months = [PublisherAlbumMonth(id: "all", title: "全部")]
            let filtered = self.filterRecords(allRecords, folderId: folderId, tab: tab, monthId: nil)
            for record in filtered {
                guard let date = record.creationDate else { continue }
                let id = Self.monthIdFormatter.string(from: date)
                guard !seen.contains(id) else { continue }
                seen.insert(id)
                months.append(PublisherAlbumMonth(id: id, title: Self.monthTitleFormatter.string(from: date)))
            }
            completionHandler(months, nil)
        }
    }

    func observeAlbumChanges() -> any Kotlinx_coroutines_coreFlow {
        return PublisherAlbumPlatformBridgeKt.publisherAlbumCallbackChangeFlow(
            onRegister: { [weak self] emitter in
                guard let self = self else { return }
                self.albumChangeEmitter = { _ = emitter() }
                PHPhotoLibrary.shared().register(self)
            },
            onUnregister: { [weak self] in
                guard let self = self else { return }
                PHPhotoLibrary.shared().unregisterChangeObserver(self)
                self.albumChangeEmitter = nil
            }
        )
    }

    // MARK: - PHPhotoLibraryChangeObserver

    func photoLibraryDidChange(_ changeInstance: PHChange) {
        // iOS 的 PHPhotoLibraryChangeObserver 会在很多场景下触发回调，
        // 包括缩略图请求完成、iCloud 同步进度更新等，这些并不意味着素材列表发生了变化。
        // 如果无条件清缓存 + 通知 ViewModel 刷新，会导致：
        //   数据展示 → 收到变更 → 清空列表 → loading → 重新加载 → 数据展示 → 又收到变更 → 循环
        //
        // 优化策略：利用 PHChange.changeDetails(for:) 检查之前 fetch 的素材结果是否真的发生了变化
        // （新增/删除/移动），只有真正有变化时才清缓存并通知 ViewModel。
        let hasRealChange: Bool = cacheQueue.sync { () -> Bool in
            // 如果缓存还没建立或没有保存的 fetchResult，说明还没加载过，不需要响应变更
            guard let fetchResult = lastFetchResult else {
                NSLog("[PublisherAlbum][PERF] photoLibraryDidChange: no fetchResult yet, skip")
                return false
            }
            // 用之前保存的 PHFetchResult 检查是否有真正的素材增删移动
            guard let details = changeInstance.changeDetails(for: fetchResult) else {
                NSLog("[PublisherAlbum][PERF] photoLibraryDidChange: no changeDetails, skip")
                return false
            }
            let changed = details.hasIncrementalChanges &&
                (details.insertedIndexes != nil ||
                 details.removedIndexes != nil ||
                 details.hasMoves)
            return changed
        }

        if hasRealChange {
            NSLog("[PublisherAlbum][PERF] photoLibraryDidChange: real change detected, clearing cache")
            clearCache()
            albumChangeEmitter?()
        } else {
            NSLog("[PublisherAlbum][PERF] photoLibraryDidChange: no real asset change, skip refresh")
        }
    }

    func shouldTranscode(
        media: PublisherAlbumMedia,
        completionHandler: @escaping (KotlinBoolean?, Error?) -> Void
    ) {
        let width = media.width
        let height = media.height
        guard width > 0, height > 0 else {
            completionHandler(KotlinBoolean(value: false), nil)
            return
        }
        let needTranscode: Bool
        if media.isVideo {
            needTranscode = width > publisherAlbumMaxVideoSide ||
                height > publisherAlbumMaxVideoSide ||
                width % 2 != 0 ||
                height % 2 != 0
        } else {
            needTranscode = width > Int32(publisherAlbumMaxImageSide) ||
                height > Int32(publisherAlbumMaxImageSide) ||
                Int(width) * Int(height) > publisherAlbumMaxImagePixels
        }
        completionHandler(KotlinBoolean(value: needTranscode), nil)
    }

    func transcodeMedia(
        media: PublisherAlbumMedia,
        completionHandler: @escaping (PublisherAlbumTranscodeResult?, Error?) -> Void
    ) {
        guard let asset = asset(for: media) else {
            completionHandler(PublisherAlbumPlatformBridgeKt.publisherAlbumTranscodeFailed(message: "素材不存在"), nil)
            return
        }
        if media.isVideo {
            exportVideo(asset: asset, media: media, completionHandler: completionHandler)
        } else {
            compressImage(asset: asset, media: media, completionHandler: completionHandler)
        }
    }

    func cleanupSessionCache(
        sessionId: String,
        committedIds: Set<String>,
        completionHandler: @escaping (Error?) -> Void
    ) {
        // 只清理缩略图和转码临时文件，保留元数据缓存（allAssetsCache）。
        // 元数据缓存构建耗时 3s+，退出时清除会导致每次进入都重新构建并展示 loading。
        // 相册内容变化时由 photoLibraryDidChange 触发 clearCache 即可保证数据新鲜度。
        let hadCache = allAssetsCache != nil
        NSLog("[PublisherAlbum][PERF] cleanupSessionCache: hadCache=%@, sessionId=%@, keepMetadataCache=true", hadCache ? "true" : "false", sessionId)
        [thumbnailCacheDir(), transcodeCacheDir()].forEach { cacheDir in
            if let files = try? FileManager.default.contentsOfDirectory(at: cacheDir, includingPropertiesForKeys: nil) {
                for file in files {
                    try? FileManager.default.removeItem(at: file)
                }
            }
        }
        completionHandler(nil)
    }

    func resolveThumbnailUrl(
        media: PublisherAlbumMedia,
        completionHandler: @escaping (String?, Error?) -> Void
    ) {
        // 已有缩略图则直接返回。
        if !media.thumbnailUrl.isEmpty {
            completionHandler(media.thumbnailUrl, nil)
            return
        }
        // 尝试通过 sourceId 查找原始 PHAsset 并生成缩略图。
        guard let asset = asset(for: media) else {
            completionHandler("", nil)
            return
        }
        DispatchQueue.global(qos: .userInitiated).async {
            let start = CFAbsoluteTimeGetCurrent()
            let url = self.thumbnailPath(for: asset)
            let cost = Int((CFAbsoluteTimeGetCurrent() - start) * 1000)
            if cost > 100 {
                NSLog("[PublisherAlbum][PERF] resolveThumbnailUrl SLOW: mediaId=%@, cost=%dms", media.id, cost)
            }
            completionHandler(url, nil)
        }
    }

    func doCopyMediaToPrivateDir(
        media: PublisherAlbumMedia,
        completionHandler: @escaping @Sendable (CopyMediaResult?, (any Error)?) -> Void
    ) {
        guard let asset = asset(for: media) else {
            NSLog("[PublisherAlbum] copyMediaToPrivateDir: asset not found, mediaId=%@", media.id)
            completionHandler(nil, nil)
            return
        }
        DispatchQueue.global(qos: .userInitiated).async {
            let start = CFAbsoluteTimeGetCurrent()
            let filePrefix = "publish_\(abs(media.sourceId.hashValue))"
            if asset.mediaType == .video {
                // 优先使用 media.path（转码后的本地文件），避免回溯 PHAsset 导致压缩流程失效
                if !media.path.isEmpty,
                   FileManager.default.isReadableFile(atPath: media.path),
                   !media.path.hasPrefix("ph://") {
                    NSLog("[PublisherAlbum] copyVideoToPrivateDir: using transcoded local path=%@", media.path)
                    self.copyLocalVideoToPrivateDir(localPath: media.path, filePrefix: filePrefix) { videoPath in
                        guard let videoPath = videoPath else {
                            let cost = Int((CFAbsoluteTimeGetCurrent() - start) * 1000)
                            NSLog("[PublisherAlbum][PERF] copyMediaToPrivateDir video (local) FAILED: mediaId=%@, cost=%dms",
                                  media.id, cost)
                            completionHandler(nil, nil)
                            return
                        }
                        let coverPath = self.copyCoverToPrivateDir(asset: asset, filePrefix: filePrefix)
                        let cost = Int((CFAbsoluteTimeGetCurrent() - start) * 1000)
                        NSLog("[PublisherAlbum][PERF] copyMediaToPrivateDir video (local): mediaId=%@, cost=%dms, success=true",
                              media.id, cost)
                        let fileSize = (try? FileManager.default.attributesOfItem(atPath: videoPath)[.size] as? Int64) ?? 0
                        let (md5Hex, sha1Hex) = self.calculateFileDigests(filePath: videoPath)
                        completionHandler(CopyMediaResult(videoPath: videoPath, coverPath: coverPath, videoFileSize: fileSize, videoMd5: md5Hex, videoSha1: sha1Hex), nil)
                    }
                    return
                }
                self.copyVideoToPrivateDir(asset: asset, media: media, filePrefix: filePrefix) { videoPath in
                    guard let videoPath = videoPath else {
                        let cost = Int((CFAbsoluteTimeGetCurrent() - start) * 1000)
                        NSLog("[PublisherAlbum][PERF] copyMediaToPrivateDir video FAILED: mediaId=%@, cost=%dms",
                              media.id, cost)
                        completionHandler(nil, nil)
                        return
                    }
                    // 封面复制
                    let coverPath = self.copyCoverToPrivateDir(asset: asset, filePrefix: filePrefix)
                    let cost = Int((CFAbsoluteTimeGetCurrent() - start) * 1000)
                    NSLog("[PublisherAlbum][PERF] copyMediaToPrivateDir video: mediaId=%@, cost=%dms, success=true",
                          media.id, cost)
                    let fileSize = (try? FileManager.default.attributesOfItem(atPath: videoPath)[.size] as? Int64) ?? 0
                    let (md5Hex, sha1Hex) = self.calculateFileDigests(filePath: videoPath)
                    completionHandler(CopyMediaResult(videoPath: videoPath, coverPath: coverPath, videoFileSize: fileSize, videoMd5: md5Hex, videoSha1: sha1Hex), nil)
                }
            } else {
                self.copyImageToPrivateDir(asset: asset, media: media, filePrefix: filePrefix) { imagePath in
                    guard let imagePath = imagePath else {
                        let cost = Int((CFAbsoluteTimeGetCurrent() - start) * 1000)
                        NSLog("[PublisherAlbum][PERF] copyMediaToPrivateDir image FAILED: mediaId=%@, cost=%dms",
                              media.id, cost)
                        completionHandler(nil, nil)
                        return
                    }
                    let cost = Int((CFAbsoluteTimeGetCurrent() - start) * 1000)
                    NSLog("[PublisherAlbum][PERF] copyMediaToPrivateDir image: mediaId=%@, cost=%dms, success=true",
                          media.id, cost)
                    let imgFileSize = (try? FileManager.default.attributesOfItem(atPath: imagePath)[.size] as? Int64) ?? 0
                    completionHandler(CopyMediaResult(videoPath: imagePath, coverPath: "", videoFileSize: imgFileSize, videoMd5: "", videoSha1: ""), nil)
                }
            }
        }
    }

    /// 将封面缩略图复制到私有目录，与视频文件保持相同前缀。
    private func copyCoverToPrivateDir(asset: PHAsset, filePrefix: String) -> String {
        let coverFile = self.videoCopyDir()
            .appendingPathComponent("\(filePrefix)_cover")
            .appendingPathExtension("jpg")
        // 缓存判断：封面已存在且大小大于 0 则跳过
        if FileManager.default.fileExists(atPath: coverFile.path),
           let attrs = try? FileManager.default.attributesOfItem(atPath: coverFile.path),
           let size = attrs[.size] as? UInt64, size > 0 {
            NSLog("[PublisherAlbum] copyCoverToPrivateDir: cache hit, dest=%@", coverFile.path)
            return coverFile.path
        }
        // 同步请求缩略图数据
        let options = PHImageRequestOptions()
        options.deliveryMode = .highQualityFormat
        options.isNetworkAccessAllowed = true
        options.isSynchronous = true
        var coverPath = ""
        imageManager.requestImage(
            for: asset,
            targetSize: CGSize(width: 720, height: 720),
            contentMode: .aspectFill,
            options: options
        ) { image, _ in
            guard let image = image, let data = image.jpegData(compressionQuality: 0.85) else {
                return
            }
            do {
                try data.write(to: coverFile, options: .atomic)
                coverPath = coverFile.path
                NSLog("[PublisherAlbum] copyCoverToPrivateDir: success, dest=%@, size=%d", coverFile.path, data.count)
            } catch {
                NSLog("[PublisherAlbum] copyCoverToPrivateDir: write failed: %@", error.localizedDescription)
            }
        }
        return coverPath
    }

    /// 计算文件的 MD5 和 SHA1 摘要（流式读取，适用于大文件）。
    /// - Returns: (md5Hex, sha1Hex)；计算失败时返回空字符串对
    private func calculateFileDigests(filePath: String) -> (String, String) {
        guard let inputStream = InputStream(fileAtPath: filePath) else {
            NSLog("[PublisherAlbum] calculateFileDigests: cannot open file, path=%@", filePath)
            return ("", "")
        }
        inputStream.open()
        defer { inputStream.close() }

        var md5Context = CC_MD5_CTX()
        var sha1Context = CC_SHA1_CTX()
        CC_MD5_Init(&md5Context)
        CC_SHA1_Init(&sha1Context)

        let bufferSize = 8192
        var buffer = [UInt8](repeating: 0, count: bufferSize)
        while inputStream.hasBytesAvailable {
            let bytesRead = inputStream.read(&buffer, maxLength: bufferSize)
            if bytesRead <= 0 { break }
            CC_MD5_Update(&md5Context, buffer, CC_LONG(bytesRead))
            CC_SHA1_Update(&sha1Context, buffer, CC_LONG(bytesRead))
        }

        var md5Digest = [UInt8](repeating: 0, count: Int(CC_MD5_DIGEST_LENGTH))
        var sha1Digest = [UInt8](repeating: 0, count: Int(CC_SHA1_DIGEST_LENGTH))
        CC_MD5_Final(&md5Digest, &md5Context)
        CC_SHA1_Final(&sha1Digest, &sha1Context)

        let md5Hex = md5Digest.map { String(format: "%02x", $0) }.joined()
        let sha1Hex = sha1Digest.map { String(format: "%02x", $0) }.joined()
        NSLog("[PublisherAlbum] calculateFileDigests: md5=%@, sha1=%@", md5Hex, sha1Hex)
        return (md5Hex, sha1Hex)
    }

    /// 将视频 PHAsset 导出到应用私有目录。
    private func copyVideoToPrivateDir(
        asset: PHAsset,
        media: PublisherAlbumMedia,
        filePrefix: String,
        completion: @escaping (String?) -> Void
    ) {
        let options = PHVideoRequestOptions()
        options.deliveryMode = .highQualityFormat
        options.isNetworkAccessAllowed = true
        imageManager.requestAVAsset(forVideo: asset, options: options) { avAsset, _, _ in
            guard let urlAsset = avAsset as? AVURLAsset else {
                // 非 URL 类型的 AVAsset（如 AVComposition），通过 export session 导出。
                self.exportVideoViaSession(avAsset: avAsset, media: media, filePrefix: filePrefix, completion: completion)
                return
            }
            // 直接复制源文件到私有目录。
            let ext = urlAsset.url.pathExtension.isEmpty ? "mp4" : urlAsset.url.pathExtension
            let destURL = self.videoCopyDir()
                .appendingPathComponent(filePrefix)
                .appendingPathExtension(ext)
            // 缓存判断：文件已存在且大小大于 0 则跳过复制
            if FileManager.default.fileExists(atPath: destURL.path),
               let attrs = try? FileManager.default.attributesOfItem(atPath: destURL.path),
               let size = attrs[.size] as? UInt64, size > 0 {
                NSLog("[PublisherAlbum] copyVideoToPrivateDir: cache hit, dest=%@", destURL.path)
                completion(destURL.path)
                return
            }
            do {
                // 如果存在旧的无效文件，先删除
                if FileManager.default.fileExists(atPath: destURL.path) {
                    try FileManager.default.removeItem(at: destURL)
                }
                try FileManager.default.copyItem(at: urlAsset.url, to: destURL)
                completion(destURL.path)
            } catch {
                NSLog("[PublisherAlbum] copyVideoToPrivateDir copyItem failed: %@", error.localizedDescription)
                // 回退到 export session 方式。
                self.exportVideoViaSession(avAsset: avAsset, media: media, filePrefix: filePrefix, completion: completion)
            }
        }
    }

    /// 将已存在的本地视频文件（如转码产物）复制到私有目录。
    private func copyLocalVideoToPrivateDir(
        localPath: String,
        filePrefix: String,
        completion: @escaping (String?) -> Void
    ) {
        let sourceURL = URL(fileURLWithPath: localPath)
        let ext = sourceURL.pathExtension.isEmpty ? "mp4" : sourceURL.pathExtension
        let destURL = self.videoCopyDir()
            .appendingPathComponent(filePrefix)
            .appendingPathExtension(ext)
        // 缓存判断：文件已存在且大小大于 0 则跳过复制
        if FileManager.default.fileExists(atPath: destURL.path),
           let attrs = try? FileManager.default.attributesOfItem(atPath: destURL.path),
           let size = attrs[.size] as? UInt64, size > 0 {
            NSLog("[PublisherAlbum] copyLocalVideoToPrivateDir: cache hit, dest=%@", destURL.path)
            completion(destURL.path)
            return
        }
        do {
            if FileManager.default.fileExists(atPath: destURL.path) {
                try FileManager.default.removeItem(at: destURL)
            }
            try FileManager.default.copyItem(at: sourceURL, to: destURL)
            completion(destURL.path)
        } catch {
            NSLog("[PublisherAlbum] copyLocalVideoToPrivateDir copyItem failed: %@", error.localizedDescription)
            completion(nil)
        }
    }

    /// 通过 AVAssetExportSession 导出视频到私有目录（兜底方案）。
    private func exportVideoViaSession(
        avAsset: AVAsset?,
        media: PublisherAlbumMedia,
        filePrefix: String,
        completion: @escaping (String?) -> Void
    ) {
        guard let avAsset = avAsset,
              let exportSession = AVAssetExportSession(asset: avAsset, presetName: AVAssetExportPresetPassthrough)
        else {
            completion(nil)
            return
        }
        let destURL = self.videoCopyDir()
            .appendingPathComponent("\(filePrefix)_export")
            .appendingPathExtension("mp4")
        // 如果存在旧文件，先删除（ExportSession 不会覆盖已有文件）
        if FileManager.default.fileExists(atPath: destURL.path) {
            try? FileManager.default.removeItem(at: destURL)
        }
        exportSession.outputURL = destURL
        exportSession.outputFileType = .mp4
        exportSession.shouldOptimizeForNetworkUse = false
        exportSession.exportAsynchronously {
            if exportSession.status == .completed {
                completion(destURL.path)
            } else {
                NSLog("[PublisherAlbum] exportVideoViaSession failed: status=%d", exportSession.status.rawValue)
                completion(nil)
            }
        }
    }

    /// 将图片 PHAsset 导出到应用私有目录。
    private func copyImageToPrivateDir(
        asset: PHAsset,
        media: PublisherAlbumMedia,
        filePrefix: String,
        completion: @escaping (String?) -> Void
    ) {
        let options = PHImageRequestOptions()
        options.deliveryMode = .highQualityFormat
        options.isNetworkAccessAllowed = true
        options.isSynchronous = true
        imageManager.requestImageDataAndOrientation(for: asset, options: options) { data, uti, _, _ in
            guard let data = data else {
                completion(nil)
                return
            }
            let ext: String
            if let uti = uti, uti.contains("png") {
                ext = "png"
            } else if let uti = uti, uti.contains("heic") || uti.contains("heif") {
                ext = "heic"
            } else {
                ext = "jpg"
            }
            let destURL = self.videoCopyDir()
                .appendingPathComponent(filePrefix)
                .appendingPathExtension(ext)
            // 缓存判断：文件已存在且大小匹配则跳过复制
            if FileManager.default.fileExists(atPath: destURL.path),
               let attrs = try? FileManager.default.attributesOfItem(atPath: destURL.path),
               let size = attrs[.size] as? UInt64, size == UInt64(data.count) {
                NSLog("[PublisherAlbum] copyImageToPrivateDir: cache hit, dest=%@", destURL.path)
                completion(destURL.path)
                return
            }
            do {
                try data.write(to: destURL, options: .atomic)
                completion(destURL.path)
            } catch {
                NSLog("[PublisherAlbum] copyImageToPrivateDir write failed: %@", error.localizedDescription)
                completion(nil)
            }
        }
    }

    /// 视频/图片复制到私有目录的存储路径。
    private func videoCopyDir() -> URL {
        let dir = FileManager.default.urls(for: .documentDirectory, in: .userDomainMask).first!
            .appendingPathComponent("publisher_video_copy", isDirectory: true)
        try? FileManager.default.createDirectory(at: dir, withIntermediateDirectories: true)
        return dir
    }

    private func currentPermissionState() -> PublisherAlbumPermissionState {
        if #available(iOS 14.0, *) {
            return mapAuthorizationStatus(PHPhotoLibrary.authorizationStatus(for: .readWrite))
        }
        return mapAuthorizationStatus(PHPhotoLibrary.authorizationStatus())
    }

    private func mapAuthorizationStatus(_ status: PHAuthorizationStatus) -> PublisherAlbumPermissionState {
        if #available(iOS 14.0, *), status == .limited {
            return .limited
        }
        switch status {
        case .authorized:
            return .authorized
        case .notDetermined:
            return .unknown
        default:
            return .denied
        }
    }

    // MARK: - 缓存过滤

    /// 从缓存记录中按相册、媒体类型和月份过滤，替代重复的 PhotoKit 查询。
    private func filterRecords(
        _ records: [CachedAssetRecord],
        folderId: String,
        tab: PublisherAlbumMediaTab?,
        monthId: String?
    ) -> [CachedAssetRecord] {
        var result = records

        // 按相册过滤
        if folderId != publisherAlbumAllFolderId {
            if folderId.hasPrefix(publisherAlbumCollectionPrefix) {
                let collectionId = String(folderId.dropFirst(publisherAlbumCollectionPrefix.count))
                result = result.filter { $0.collectionIds.contains(collectionId) }
            }
        }

        // 按媒体类型过滤
        if let tab = tab {
            let targetType: PHAssetMediaType = tab == .video ? .video : .image
            result = result.filter { $0.mediaType == targetType }
        }

        // 按月份过滤
        if let monthId = monthId, monthId != "all",
           let range = Self.monthDateRange(monthId) {
            result = result.filter { record in
                guard let date = record.creationDate else { return false }
                return date >= range.start && date < range.end
            }
        }

        return result
    }

    /// 从缓存记录列表构建 PublisherAlbumMedia 数组。
    private func mediaList(from records: [CachedAssetRecord]) -> [PublisherAlbumMedia] {
        records.map { media(from: $0) }
    }

    /// 从缓存记录构建 PublisherAlbumMedia。
    private func media(from record: CachedAssetRecord) -> PublisherAlbumMedia {
        let isVideo = record.isVideo
        let id = (isVideo ? publisherAlbumVideoPrefix : publisherAlbumImagePrefix) + record.localIdentifier
        let date = record.creationDate ?? Date(timeIntervalSince1970: 0)
        return PublisherAlbumMedia(
            id: id,
            thumbnailUrl: cachedThumbnailPath(forIdentifier: record.localIdentifier),
            path: "ph://\(record.localIdentifier)",
            isVideo: isVideo,
            durationMs: Int64(record.duration * 1000),
            width: Int32(record.pixelWidth),
            height: Int32(record.pixelHeight),
            dateTakenMs: Int64(date.timeIntervalSince1970 * 1000),
            dateText: Self.dateTitleFormatter.string(from: date),
            mimeType: isVideo ? "video/mp4" : "image/jpeg",
            sourceId: record.localIdentifier
        )
    }

    // MARK: - PHAsset 单素材转换（loadMediaDetail / resolveThumbnailUrl 使用）

    private func media(from asset: PHAsset) -> PublisherAlbumMedia {
        let isVideo = asset.mediaType == .video
        // KMM 侧 mediaId 带媒体类型前缀，sourceId 保留 PhotoKit localIdentifier 供重新查询。
        let id = (isVideo ? publisherAlbumVideoPrefix : publisherAlbumImagePrefix) + asset.localIdentifier
        let date = asset.creationDate ?? Date(timeIntervalSince1970: 0)
        return PublisherAlbumMedia(
            id: id,
            thumbnailUrl: cachedThumbnailPath(for: asset),
            path: "ph://\(asset.localIdentifier)",
            isVideo: isVideo,
            durationMs: Int64(asset.duration * 1000),
            width: Int32(asset.pixelWidth),
            height: Int32(asset.pixelHeight),
            dateTakenMs: Int64(date.timeIntervalSince1970 * 1000),
            dateText: Self.dateTitleFormatter.string(from: date),
            mimeType: isVideo ? "video/mp4" : "image/jpeg",
            sourceId: asset.localIdentifier
        )
    }

    private func asset(for media: PublisherAlbumMedia) -> PHAsset? {
        let sourceId = media.sourceId.isEmpty ? sourceId(from: media.id) : media.sourceId
        let assets = PHAsset.fetchAssets(withLocalIdentifiers: [sourceId], options: nil)
        return assets.count > 0 ? assets.object(at: 0) : nil
    }

    private func sourceId(from mediaId: String) -> String {
        mediaId
            .replacingOccurrences(of: publisherAlbumImagePrefix, with: "")
            .replacingOccurrences(of: publisherAlbumVideoPrefix, with: "")
    }

    private func compressImage(
        asset: PHAsset,
        media: PublisherAlbumMedia,
        completionHandler: @escaping (PublisherAlbumTranscodeResult?, Error?) -> Void
    ) {
        DispatchQueue.global(qos: .userInitiated).async {
            let options = PHImageRequestOptions()
            options.deliveryMode = .highQualityFormat
            options.resizeMode = .exact
            options.isSynchronous = true
            options.isNetworkAccessAllowed = true

            var outputImage: UIImage?
            self.imageManager.requestImage(
                for: asset,
                // 只缩放到发布器允许的最大边长，压缩产物仍写入 app 临时目录。
                targetSize: self.transcodeTargetSize(for: asset),
                contentMode: .aspectFit,
                options: options
            ) { image, _ in
                outputImage = image
            }

            guard let image = outputImage,
                  let data = image.jpegData(compressionQuality: 0.9)
            else {
                completionHandler(PublisherAlbumPlatformBridgeKt.publisherAlbumTranscodeFailed(message: "图片压缩失败"), nil)
                return
            }

            let output = self.transcodeCacheDir()
                .appendingPathComponent("image_\(UUID().uuidString)")
                .appendingPathExtension("jpg")
            do {
                try data.write(to: output, options: .atomic)
                let width = Int32(image.size.width * image.scale)
                let height = Int32(image.size.height * image.scale)
                completionHandler(
                    PublisherAlbumPlatformBridgeKt.publisherAlbumTranscodeSuccess(
                        media: self.replacingMedia(
                            media,
                            path: output.path,
                            thumbnailUrl: output.path,
                            width: width,
                            height: height,
                            mimeType: "image/jpeg"
                        )
                    ),
                    nil
                )
            } catch {
                completionHandler(PublisherAlbumPlatformBridgeKt.publisherAlbumTranscodeFailed(message: "图片压缩失败"), nil)
            }
        }
    }

    private func exportVideo(
        asset: PHAsset,
        media: PublisherAlbumMedia,
        completionHandler: @escaping (PublisherAlbumTranscodeResult?, Error?) -> Void
    ) {
        let options = PHVideoRequestOptions()
        options.deliveryMode = .highQualityFormat
        options.isNetworkAccessAllowed = true
        imageManager.requestAVAsset(forVideo: asset, options: options) { avAsset, _, _ in
            guard let avAsset = avAsset,
                  let exportSession = AVAssetExportSession(asset: avAsset, presetName: AVAssetExportPresetMediumQuality)
            else {
                completionHandler(PublisherAlbumPlatformBridgeKt.publisherAlbumTranscodeFailed(message: "视频压缩失败"), nil)
                return
            }

            // 视频转码结果只作为本次发布流程的临时素材，不写回系统相册。
            let output = self.transcodeCacheDir()
                .appendingPathComponent("video_\(UUID().uuidString)")
                .appendingPathExtension("mp4")
            try? FileManager.default.removeItem(at: output)
            exportSession.outputURL = output
            exportSession.outputFileType = .mp4
            exportSession.shouldOptimizeForNetworkUse = true
            exportSession.exportAsynchronously {
                if exportSession.status == .completed {
                    completionHandler(
                        PublisherAlbumPlatformBridgeKt.publisherAlbumTranscodeSuccess(
                            media: self.replacingMedia(media, path: output.path, thumbnailUrl: media.thumbnailUrl)
                        ),
                        nil
                    )
                } else {
                    completionHandler(PublisherAlbumPlatformBridgeKt.publisherAlbumTranscodeFailed(message: "视频压缩失败"), nil)
                }
            }
        }
    }

    private func replacingMedia(
        _ media: PublisherAlbumMedia,
        path: String,
        thumbnailUrl: String,
        width: Int32? = nil,
        height: Int32? = nil,
        mimeType: String? = nil
    ) -> PublisherAlbumMedia {
        PublisherAlbumMedia(
            id: media.id,
            thumbnailUrl: thumbnailUrl,
            path: path,
            isVideo: media.isVideo,
            durationMs: media.durationMs,
            width: width ?? media.width,
            height: height ?? media.height,
            dateTakenMs: media.dateTakenMs,
            dateText: media.dateText,
            mimeType: mimeType ?? media.mimeType,
            sourceId: media.sourceId
        )
    }

    private func transcodeTargetSize(for asset: PHAsset) -> CGSize {
        let width = CGFloat(asset.pixelWidth)
        let height = CGFloat(asset.pixelHeight)
        let maxSide = max(width, height)
        guard maxSide > publisherAlbumMaxImageSide else {
            return CGSize(width: width, height: height)
        }
        let scale = publisherAlbumMaxImageSide / maxSide
        return CGSize(width: width * scale, height: height * scale)
    }

    /// 仅检查缩略图缓存是否存在，不触发生成。
    /// 用于 loadMedia 中快速返回列表数据，避免同步生成所有缩略图阻塞查询。
    private func cachedThumbnailPath(for asset: PHAsset) -> String {
        cachedThumbnailPath(forIdentifier: asset.localIdentifier)
    }

    /// 通过 localIdentifier 检查缩略图缓存是否存在，供缓存记录使用。
    private func cachedThumbnailPath(forIdentifier identifier: String) -> String {
        let cacheFile = thumbnailCacheDir()
            .appendingPathComponent(Self.safeFileName(identifier))
            .appendingPathExtension("jpg")
        return FileManager.default.fileExists(atPath: cacheFile.path) ? cacheFile.path : ""
    }

    /// 缩略图目标尺寸（像素），基于屏幕宽度动态计算。
    /// 3 列网格布局下每个 cell 宽度 = screenWidth / 3，乘以 scale 得到物理像素。
    /// 参考旧微视 TAVAlbumDataManager.defaultThumbnailSizeWidth = screenWidth * 0.333 * 2。
    private static let thumbnailTargetSize: CGSize = {
        let screen = UIScreen.main
        let cellPixelWidth = ceil(screen.bounds.width / 3.0 * screen.scale)
        return CGSize(width: cellPixelWidth, height: cellPixelWidth)
    }()

    /// 同步生成缩略图并写入缓存目录，返回缓存文件路径。
    /// 由 resolveThumbnailUrl / loadFolders 等需要实际缩略图的场景调用。
    private func thumbnailPath(for asset: PHAsset) -> String {
        let cacheFile = thumbnailCacheDir()
            .appendingPathComponent(Self.safeFileName(asset.localIdentifier))
            .appendingPathExtension("jpg")
        if FileManager.default.fileExists(atPath: cacheFile.path) {
            return cacheFile.path
        }

        // Kuikly Image 侧统一消费文件路径，先把 PhotoKit 缩略图落到临时目录。
        let options = PHImageRequestOptions()
        options.deliveryMode = .highQualityFormat
        options.resizeMode = .exact
        options.isSynchronous = true
        options.isNetworkAccessAllowed = true
        imageManager.requestImage(
            for: asset,
            targetSize: Self.thumbnailTargetSize,
            contentMode: .aspectFill,
            options: options
        ) { image, _ in
            guard let data = image?.jpegData(compressionQuality: 0.86) else { return }
            try? data.write(to: cacheFile, options: .atomic)
        }
        return cacheFile.path
    }

    private func thumbnailCacheDir() -> URL {
        let dir = FileManager.default.temporaryDirectory
            .appendingPathComponent("publisher_album_thumb_v2", isDirectory: true)
        try? FileManager.default.createDirectory(at: dir, withIntermediateDirectories: true)
        return dir
    }

    private func transcodeCacheDir() -> URL {
        let dir = FileManager.default.temporaryDirectory
            .appendingPathComponent("publisher_album_transcode", isDirectory: true)
        try? FileManager.default.createDirectory(at: dir, withIntermediateDirectories: true)
        return dir
    }

    private static func safeFileName(_ raw: String) -> String {
        raw
            .replacingOccurrences(of: "/", with: "_")
            .replacingOccurrences(of: ":", with: "_")
    }

    private static func monthDateRange(_ monthId: String) -> (start: Date, end: Date)? {
        guard let start = monthIdFormatter.date(from: monthId),
              let end = Calendar.current.date(byAdding: .month, value: 1, to: start)
        else { return nil }
        return (start, end)
    }

    private static let monthIdFormatter: DateFormatter = {
        let formatter = DateFormatter()
        formatter.locale = Locale(identifier: "zh_CN")
        formatter.dateFormat = "yyyy-MM"
        return formatter
    }()

    private static let monthTitleFormatter: DateFormatter = {
        let formatter = DateFormatter()
        formatter.locale = Locale(identifier: "zh_CN")
        formatter.dateFormat = "yyyy.MM"
        return formatter
    }()

    private static let dateTitleFormatter: DateFormatter = {
        let formatter = DateFormatter()
        formatter.locale = Locale(identifier: "zh_CN")
        formatter.dateFormat = "yyyy年M月d日"
        return formatter
    }()
}

private extension PublisherAlbumPermissionState {
    /// PhotoKit limited 权限仍允许读取用户授权的素材集合。
    var isReadable: Bool {
        self == .authorized || self == .limited
    }
}
