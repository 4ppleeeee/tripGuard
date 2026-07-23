import Foundation
import umbrella
import SuperPlayer

/// iOS 端 Super Player SDK 的 sdtFrom
private let kSDKSdtFrom = "v3028"

/// iOS 端 Super Player SDK 的 vsAppkey
private let kSDKVsAppkey = "SEYcH8VkKPkzFSIi0YnAlaOmmAAuUbi9640yxJSReHu3IMUv/PBjSAbwoztjZpum8r2siytWYzfqUhiITWT4MIaIMjvybkeychn4j84VGLBgY578J2lga/3zbhEs0AZ05E6jAtS8ddSaTkf7mvSBCSuAVZSqXulTuacNtiVvJYIL0n8dhoGbn0X8X93bZXaKfEOomO3hHdBBrQx+gM1sDyIMdas9rniVe3GWqhKgWD+ZLDZfv2zPVV8W8gVHQVX39mH7+BsTH6k3ImaMDYrw1KVbgJY0Ys/v8YIsB5zvHQy+w9F2Gfd6gMl0beHF40o1tXjF+gPVZDiiorWmqUWCimlnZbKGonuMgt695ucjTeKNSa19+t/zb085NBdMUumBgnUDNSMlyBzw7+Tj3grWDLBlZrWFAVd5PhpYeU5EbewnUyxVvabVDGZ1KxjFqtQx5H3dNGZNeg+JoqnmoAa3HjLXaIl6crZFPJ26Fmlfk9ZUbxngnKUzRpmAt/19TYyh"

private let kLoginTypeQQ: Int = 1
private let kLoginTypeWX: Int = 3

/// iOS 端 Super Player 原生桥接实现
///
/// 通过 OC 直接调用 SuperPlayer SDK 完成初始化和 vid 换链，
/// 避免 cinterop 生成绑定时的兼容性问题。
final class IOSSuperPlayerBridge: NSObject, ISuperPlayerBridge {

    private var sdkInitialized = false
    /// 强引用 delegate wrapper，防止 weak delegate 被释放导致无回调
    fileprivate var activeDelegateWrapper: SPCGIManagerDelegateWrapper?

    func doInitSdk(platformId: String, qimei: String) -> Bool {
        guard !sdkInitialized else { return true }

        SPSDKParamsMgr.sharedInstance().qimei = qimei
        SPSDKParamsMgr.sharedInstance().setGuid(qimei, external: true)

        let regSuccess = SPSDKManager.sharedInstance.register(withPlatform: platformId)
        if regSuccess {
            SPSDKManager.sharedInstance.addGetVInfoPlatform(
                platformId,
                sdtFrom: kSDKSdtFrom,
                vsAppkey: kSDKVsAppkey
            )
            NSLog("[IOSSuperPlayerBridge] initSdk 成功: platformId=%@", platformId)
        } else {
            NSLog("[IOSSuperPlayerBridge] initSdk 失败: 请检查 appkey 和 bundleid 是否匹配")
        }
        sdkInitialized = regSuccess
        return regSuccess
    }

    func getVideoInfo(
        videoId: String,
        cid: String,
        definition: String,
        callback: any ISuperPlayerCallback
    ) {
        // 确保 SDK 已初始化
        guard sdkInitialized else {
            NSLog("[IOSSuperPlayerBridge] getVideoInfo 失败: SDK 未初始化")
            callback.onResult(videoInfo: nil)
            return
        }

        // 设置用户信息
        SPSDKParamsMgr.sharedInstance().userInfo = buildUserInfo()

        // 构建请求参数
        let commonParam = SPPlayCommonParam()
        commonParam.playerSeq = -100
        commonParam.playerQueue = DispatchQueue.global(qos: .default)

        let cgiManager = SPCGIManager(param: commonParam)

        // 使用 delegate 包装器（必须强引用，因为 cgiManager.delegate 是 weak）
        let delegateWrapper = SPCGIManagerDelegateWrapper(
            videoId: videoId,
            callback: callback,
            cgiManager: cgiManager,
            owner: self
        )
        self.activeDelegateWrapper = delegateWrapper
        cgiManager.delegate = delegateWrapper

        // 构建播放参数
        let mediaInfo = SPMediaInfo()
        mediaInfo.videoId = videoId
        mediaInfo.coverId = cid.isEmpty ? videoId : cid
        mediaInfo.definition = definition

        let playParam = SPPlayParam()
        playParam.requestType = .normal
        playParam.playSeq = -100
        playParam.mediaInfo = mediaInfo
        playParam.flowID = "-100_\(videoId)"

        let playingContext = SPPlayingContext()
        playingContext.enableHEVC = true
        playingContext.requiredDefinition = definition
        playParam.playContext = playingContext

        cgiManager.request(with: playParam)
    }

    // MARK: - Private

    private func buildUserInfo() -> SPUserInfo {
        let userInfo = SPUserInfo()
        let login = IAppLoginBizKt.appLoginBiz()
        let loginUserInfo = login.getMainLoginUserInfo()

        guard loginUserInfo.isStrictLogin() else {
            // 未登录时 uin 传 qimei
            userInfo.uin = IAppStatusKt.appStatus().getQIMEI36()
            return userInfo
        }

        userInfo.vuserId = VideoAuthService.shared.getVideoUserId()
        userInfo.cookie = VideoAuthService.shared.getCookie()
        userInfo.isVip = VideoAuthService.shared.isVip()

        let loginType = login.getLoginType()
        if loginType == Int32(kLoginTypeQQ) {
            userInfo.uin = login.userOpenId()
        } else if loginType == Int32(kLoginTypeWX) {
            userInfo.wx_openId = login.userOpenId()
        }

        return userInfo
    }
}

// MARK: - SPCGIManagerDelegate Wrapper

/// SPCGIManager 的 delegate 包装器，将回调转发给 ISuperPlayerCallback
private final class SPCGIManagerDelegateWrapper: NSObject, SPCGIManagerDelegate {

    private let videoId: String
    private let callback: ISuperPlayerCallback
    /// 强引用 cgiManager 防止被释放
    private let cgiManager: SPCGIManager
    /// 弱引用 owner，回调完成后清理自身引用
    private weak var owner: IOSSuperPlayerBridge?

    init(videoId: String, callback: any ISuperPlayerCallback, cgiManager: SPCGIManager, owner: IOSSuperPlayerBridge) {
        self.videoId = videoId
        self.callback = callback
        self.cgiManager = cgiManager
        self.owner = owner
        super.init()
    }

    func cgiManager(onGet playInfo: SPMediaPlayInfo, request requestParam: SPPlayParam) {
        let videoInfo = convertPlayInfoToTvkVideoInfo(videoId: videoId, playInfo: playInfo)
        NSLog("[IOSSuperPlayerBridge] getVideoInfo 成功: vid=%@, playUrl=%@", videoId, videoInfo.playUrl)
        callback.onResult(videoInfo: videoInfo)
        owner?.activeDelegateWrapper = nil
    }

    func cgiManager(onError error: Error, request requestParam: SPPlayParam) {
        NSLog("[IOSSuperPlayerBridge] getVideoInfo 失败: vid=%@, error=%@", videoId, error.localizedDescription)
        callback.onResult(videoInfo: nil)
        owner?.activeDelegateWrapper = nil
    }

    func cgiManager(onPlayInfoUpdate playInfo: SPMediaPlayInfo, request requestParam: SPPlayParam) {
        // 不处理更新回调
    }

    // MARK: - 数据转换

    private func convertPlayInfoToTvkVideoInfo(videoId: String, playInfo: SPMediaPlayInfo) -> TvkVideoInfo {
        let vodPlayInfo = playInfo as? SPVODPlayInfo

        // 获取播放 URL
        let playUrl = (playInfo.sectionArray?.first as? SPSection)?.url ?? ""

        // 获取视频宽高
        let videoWidth = Int32(playInfo.videoSize.width)
        let videoHeight = Int32(playInfo.videoSize.height)

        // 获取时长（秒转毫秒）
        let durationMs = Int32((vodPlayInfo?.duration ?? 0) * 1000)

        // 获取清晰度列表
        let definitionList: [TvkDefinitionInfo] = (playInfo.defnModelList as? [SPDefinitionModel])?.compactMap { defModel in
            guard let defn = defModel.fileName, !defn.isEmpty else { return nil }
            return TvkDefinitionInfo(
                defn: defn,
                defnName: defModel.shortText ?? "",
                defnRate: defModel.resolutionText ?? "",
                fileSizeByte: defModel.videoFileSize,
                fps: 0,
                isVipOnly: defModel.isVip
            )
        } ?? []

        let currentDef = playInfo.currentDefinition?.fileName ?? ""

        // 获取试看和付费信息
        let previewStartTimeMs = Int64((vodPlayInfo?.vodPreviewStart ?? 0) * 1000)
        let previewDurationMs = Int64((vodPlayInfo?.vodPreViewTime ?? 0) * 1000)
        let chargeState = Int32(vodPlayInfo?.chargeState ?? 0)
        let vst = Int32(vodPlayInfo?.videoState ?? 2)

        return TvkVideoInfo(
            vid: playInfo.vid?.isEmpty == false ? playInfo.vid! : videoId,
            playUrl: playUrl,
            videoWidth: videoWidth,
            videoHeight: videoHeight,
            duration: durationMs,
            currentDefinition: currentDef,
            definitionList: definitionList,
            previewStartTimeMs: previewStartTimeMs,
            previewDurationMs: previewDurationMs,
            chargeState: chargeState,
            vst: vst
        )
    }
}
