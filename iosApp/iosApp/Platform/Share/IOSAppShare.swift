import Foundation
import Photos
import UIKit
import umbrella
import CocoaLumberjack

final class IOSAppShare: NSObject, IAppShare {

    private let tag = "IOSAppShare"
    private let defaultTitle = "微视分享"
    private let defaultDesc = "来自微视的分享内容"
    private let defaultUrl = "https://h5.weishi.qq.com/weishi/feed/76gOp00e31UBqEYaE"

    // MARK: - Logging

    /// 写入 `Library/WSFolder/` 文件日志（DDLog Warn 级，Release 包也会落盘）。
    /// 用于上线后排障：用户反馈分享异常时，可通过 `IOSAppReport.prepareShareLogData` 拿到。
    /// 同时用 `print` 在 Xcode 控制台输出一份，方便开发期实时调试。
    private func logShareW(_ msg: String) {
        let line = "[\(tag)] \(msg)"
        DDLogWarn(line)
        print(line)
    }

    /// 错误级日志（DDLog Error 级，Release 包必然落盘）。
    private func logShareE(_ msg: String) {
        let line = "[\(tag)] \(msg)"
        DDLogError(line)
        print(line)
    }

    private func logTextMeta(_ value: String) -> String {
        "empty=\(value.isEmpty), len=\(value.count)"
    }

    private func logResourceMeta(_ value: String) -> String {
        guard !value.isEmpty else { return "empty=true" }
        let fileURL: URL?
        if let url = URL(string: value), url.isFileURL {
            fileURL = url
        } else if value.hasPrefix("/") {
            fileURL = URL(fileURLWithPath: value)
        } else {
            fileURL = nil
        }
        guard let url = fileURL else {
            return "local=false, \(logTextMeta(value))"
        }
        let path = url.path
        let exists = FileManager.default.fileExists(atPath: path)
        let readable = FileManager.default.isReadableFile(atPath: path)
        let attributes = try? FileManager.default.attributesOfItem(atPath: path)
        let size = (attributes?[.size] as? NSNumber)?.int64Value ?? 0
        return "local=true, name=\(url.lastPathComponent), exists=\(exists), readable=\(readable), size=\(size)"
    }

    func showSharePage(
        context: (any IKmmContext)?,
        shareData: any IKmmShareData,
        completionHandler: @escaping (Error?) -> Void
    ) {
        shareToChannel(
            context: context,
            shareData: shareData,
            channelId: shareData.channelId ?? "",
            completionHandler: completionHandler
        )
    }

    func shareToChannel(
        context: (any IKmmContext)?,
        shareData: any IKmmShareData,
        channelId: String,
        completionHandler: @escaping (Error?) -> Void
    ) {
        let payload = buildSharePayload(shareData, channelId: channelId)
        // 入口全景日志：上线后排障的起点，记录用户点了哪个渠道、传了什么内容。
        logShareW(
            "shareToChannel: channelId=\(channelId), title=\(logTextMeta(payload.title)), " +
            "desc=\(logTextMeta(payload.desc)), url=\(logTextMeta(payload.url)), " +
            "imageUrl=\(logResourceMeta(payload.imageUrl)), thumbUrl=\(logResourceMeta(payload.thumbUrl)), " +
            "videoUrl=\(logTextMeta(payload.videoUrl)), arkLen=\(payload.arkData.count)"
        )

        DispatchQueue.main.async { [weak self] in
            guard let self = self else {
                completionHandler(nil)
                return
            }
            switch channelId {
            case "WEIXIN":
                self.shareToWeChat(title: payload.title, desc: payload.desc, url: payload.url, thumbUrl: payload.thumbUrl, isMoments: false)
            case "WEIXIN_MOMENTS":
                self.shareToWeChat(title: payload.title, desc: payload.desc, url: payload.url, thumbUrl: payload.thumbUrl, isMoments: true)
            case "QQ":
                self.shareToQQ(title: payload.title, desc: payload.desc, url: payload.url, thumbUrl: payload.thumbUrl, arkData: payload.arkData)
            case "QZONE":
                self.shareToQZone(title: payload.title, desc: payload.desc, url: payload.url, thumbUrl: payload.thumbUrl)
            case "WEIBO":
                self.shareToWeibo(title: payload.title, desc: payload.desc, url: payload.url, thumbUrl: payload.thumbUrl)
            case "WORK_WEIXIN":
                self.shareToWeCom(title: payload.title, desc: payload.desc, url: payload.url, thumbUrl: payload.thumbUrl)
            case "SYSTEM":
                self.shareToSystem(payload: payload)
            case "COPY_LINK":
                self.copyLink(payload.url)
            case "SAVE_IMAGE":
                self.saveImage(from: payload.imageUrl)
            case "SAVE_VIDEO":
                self.saveVideo(from: payload.videoUrl)
            default:
                self.showAlert("暂不支持该分享渠道")
            }
            completionHandler(nil)
        }
    }

    func isShareChannelSupported(channelId: String) -> Bool {
        switch channelId {
        case "WEIXIN", "WEIXIN_MOMENTS":
            _ = IOSSocialLoginSetup.setupWX()
            return WXApi.isWXAppInstalled()
        case "QQ", "QZONE":
            _ = IOSSocialLoginSetup.setupQQ()
            return QQApiInterface.isQQInstalled()
        case "WEIBO":
            _ = IOSSocialLoginSetup.setupWeibo()
            return WeiboSDK.isWeiboAppInstalled()
        case "WORK_WEIXIN":
            _ = IOSSocialLoginSetup.setupWeCom()
            return WWKApi.isAppInstalled()
        case "SYSTEM", "COPY_LINK", "SAVE_IMAGE", "SAVE_VIDEO":
            return true
        default:
            return false
        }
    }

    func share2Desktop(context: (any IKmmContext)?, info: ShortCutInfo?) {
        print("[\(tag)] share2Desktop: info=\(String(describing: info))")
    }

    // MARK: - 分享日志到微信 / 企业微信

    func shareLogToWeChat(onResult: @escaping (KotlinBoolean) -> Void) {
        print("[\(tag)] shareLogToWeChat() 调用")
        _ = IOSSocialLoginSetup.setupWX()
        guard WXApi.isWXAppInstalled() else {
            print("[\(tag)] shareLogToWeChat: 未安装微信")
            onResult(KotlinBoolean(value: false))
            return
        }

        IOSAppReport.prepareShareLogData { [weak self] data, fileTitle in
            guard let self = self else {
                onResult(KotlinBoolean(value: false))
                return
            }
            guard var logData = data, let title = fileTitle else {
                onResult(KotlinBoolean(value: false))
                return
            }
            // 超限则截断头部，对齐 OC 的 subdataWithRange
            if logData.count > IOSAppReport.LOG_FILE_MAX_UPLOAD_TO_WX {
                print("[\(self.tag)] shareLogToWeChat: log too large, size=\(logData.count)")
                logData = logData.subdata(in: 0 ..< IOSAppReport.LOG_FILE_MAX_UPLOAD_TO_WX)
            }

            let fileObject = WXFileObject()
            fileObject.fileExtension = "zip"
            fileObject.fileData = logData

            let message = WXMediaMessage()
            message.title = IOSAppReport.logSharePrefix() + title
            message.description = "Log日志"
            message.mediaObject = fileObject

            let req = SendMessageToWXReq()
            req.bText = false
            req.message = message
            req.scene = Int32(WXSceneSession.rawValue)

            WXApi.send(req)
            print("[\(self.tag)] shareLogToWeChat: send invoked, size=\(logData.count)")
            onResult(KotlinBoolean(value: true))
        }
    }

    // MARK: - 分享本地视频到朋友圈

    /// 分享本地视频文件到微信朋友圈。
    /// 对接 KMM 侧 `IAppShare.shareLocalVideoToMoments`，使用 `WXVideoFileObject` 走 `WXSceneTimeline`。
    func shareLocalVideoToMoments(
        context: (any IKmmContext)?,
        localVideoPath: String,
        title: String,
        desc: String,
        completionHandler: @escaping (KotlinBoolean?, Error?) -> Void
    ) {
        guard !localVideoPath.isEmpty else {
            print("[\(tag)] shareLocalVideoToMoments: localVideoPath is empty")
            completionHandler(KotlinBoolean(value: false), nil)
            return
        }

        let fileURL: URL
        if localVideoPath.hasPrefix("file://") {
            guard let url = URL(string: localVideoPath) else {
                print("[\(tag)] shareLocalVideoToMoments: invalid url \(localVideoPath)")
                completionHandler(KotlinBoolean(value: false), nil)
                return
            }
            fileURL = url
        } else {
            fileURL = URL(fileURLWithPath: localVideoPath)
        }

        guard FileManager.default.fileExists(atPath: fileURL.path) else {
            print("[\(tag)] shareLocalVideoToMoments: file not exists \(fileURL.path)")
            completionHandler(KotlinBoolean(value: false), nil)
            return
        }

        DispatchQueue.main.async { [weak self] in
            guard let self = self else {
                completionHandler(KotlinBoolean(value: false), nil)
                return
            }
            _ = IOSSocialLoginSetup.setupWX()
            guard WXApi.isWXAppInstalled() else {
                self.showAlert("未安装微信")
                completionHandler(KotlinBoolean(value: false), nil)
                return
            }

            DispatchQueue.global(qos: .utility).async {
                guard let videoData = try? Data(contentsOf: fileURL) else {
                    print("[\(self.tag)] shareLocalVideoToMoments: read file failed \(fileURL.path)")
                    DispatchQueue.main.async {
                        completionHandler(KotlinBoolean(value: false), nil)
                    }
                    return
                }

                let videoObject = WXVideoFileObject()
                videoObject.videoFileData = videoData

                let message = WXMediaMessage()
                message.title = title.isEmpty ? self.defaultTitle : title
                message.description = desc
                message.mediaObject = videoObject

                let req = SendMessageToWXReq()
                req.bText = false
                req.message = message
                req.scene = Int32(WXSceneTimeline.rawValue)

                DispatchQueue.main.async {
                    WXApi.send(req) { success in
                        print("[\(self.tag)] shareLocalVideoToMoments: send invoked, ok=\(success), size=\(videoData.count)")
                        completionHandler(KotlinBoolean(value: success), nil)
                    }
                }
            }
        }
    }

    func shareLogToWeCom(onResult: @escaping (KotlinBoolean) -> Void) {
        print("[\(tag)] shareLogToWeCom() 调用")
        _ = IOSSocialLoginSetup.setupWeCom()
        guard WWKApi.isAppInstalled() else {
            print("[\(tag)] shareLogToWeCom: 未安装企业微信")
            onResult(KotlinBoolean(value: false))
            return
        }

        IOSAppReport.prepareShareLogData { [weak self] data, fileTitle in
            guard let self = self else {
                onResult(KotlinBoolean(value: false))
                return
            }
            guard let logData = data, let title = fileTitle else {
                onResult(KotlinBoolean(value: false))
                return
            }

            let attachment = WWKMessageFileAttachment()
            attachment.data = logData
            attachment.path = nil
            attachment.filename = IOSAppReport.logSharePrefix() + title

            let req = WWKSendMessageReq()
            req.attachment = attachment
            // 企业微信要求 bundleID 必须是 registerApp 时使用的 shareAppId，
            // 否则 SDK 无法识别来源 App，会出现静默失败（点击无反应）。
            req.bundleID = IOSSocialLoginSetup.currentWeComShareAppId
            req.bundleName = Bundle.main.object(forInfoDictionaryKey: "CFBundleDisplayName") as? String
                ?? Bundle.main.object(forInfoDictionaryKey: "CFBundleName") as? String
                ?? "微视"

            let sent = WWKApi.send(req)
            print("[\(self.tag)] shareLogToWeCom: sendReq result=\(sent), size=\(logData.count)")
            if !sent {
                // 对齐 microvision：SDK 直发失败时，降级到 wxwork:// URL Scheme 拉起
                self.manualShareToWeCom(req: req)
            }
            onResult(KotlinBoolean(value: true))
        }
    }

    private struct SharePayload {
        let title: String
        let desc: String
        let url: String
        let thumbUrl: String
        let imageUrl: String
        let videoUrl: String
        let arkData: String
    }

    private func buildSharePayload(_ shareData: any IKmmShareData, channelId: String) -> SharePayload {
        let item = shareData.item
        let imageUrl: String = {
            let weiXinQqImageUrl: String? = {
                if let urls = shareData.option?.imageWeiXinQqUrls,
                   urls.size > 0,
                   let first = urls.get(index: 0) as? String,
                   !first.isEmpty {
                    return first
                }
                return nil
            }()

            let weiBoQZoneImageUrl: String? = {
                if let urls = shareData.option?.imageWeiBoQZoneUrls,
                   urls.size > 0,
                   let first = urls.get(index: 0) as? String,
                   !first.isEmpty {
                    return first
                }
                return nil
            }()

            switch channelId {
            case "WEIBO", "QZONE":
                if let url = weiBoQZoneImageUrl {
                    return url
                }
                if let url = weiXinQqImageUrl {
                    return url
                }
            default:
                if let url = weiXinQqImageUrl {
                    return url
                }
            }
            return item?.flexDto.shareImg ?? ""
        }()
        // 优先使用 option 中的 shareContent（KMM 层透传的真实摘要），
        // 为空时降级为默认文案，对齐 Android 侧行为。
        let optionDesc = shareData.option?.shareContent ?? ""
        let desc = optionDesc.isEmpty ? defaultDesc : optionDesc
        return SharePayload(
            title: item?.flexDto.title.isEmpty == false ? (item?.flexDto.title ?? defaultTitle) : defaultTitle,
            desc: desc,
            url: item?.flexDto.url.isEmpty == false ? (item?.flexDto.url ?? defaultUrl) : defaultUrl,
            thumbUrl: imageUrl,
            imageUrl: imageUrl,
            videoUrl: shareData.option?.videoUrl ?? "",
            arkData: shareData.option?.arkData ?? ""
        )
    }

    // MARK: - WeChat

    private func shareToWeChat(title: String, desc: String, url: String, thumbUrl: String, isMoments: Bool) {
        _ = IOSSocialLoginSetup.setupWX()
        guard WXApi.isWXAppInstalled() else {
            logShareE("shareToWeChat abort: WeChat not installed, isMoments=\(isMoments)")
            showAlert("未安装微信")
            return
        }

        if let imageData = loadLocalImageData(from: thumbUrl),
           let preparedImage = prepareWeChatShareImageData(imageData) {
            let imageObject = WXImageObject()
            imageObject.imageData = preparedImage.imageData

            let message = WXMediaMessage()
            message.title = title
            message.description = desc
            message.mediaObject = imageObject
            message.thumbData = preparedImage.thumbData

            let req = SendMessageToWXReq()
            req.bText = false
            req.message = message
            req.scene = isMoments ? Int32(WXSceneTimeline.rawValue) : Int32(WXSceneSession.rawValue)
            WXApi.send(req)
            logShareW("shareToWeChat branch=IMAGE: send invoked, isMoments=\(isMoments), imageSize=\(preparedImage.imageData.count)")
            return
        }

        let webpageObject = WXWebpageObject()
        webpageObject.webpageUrl = url

        let message = WXMediaMessage()
        message.title = title
        message.description = desc
        message.mediaObject = webpageObject

        downloadThumb(from: thumbUrl) { [weak self] thumbData in
            guard let self = self else { return }
            if let data = thumbData {
                message.thumbData = data
            }
            let req = SendMessageToWXReq()
            req.bText = false
            req.message = message
            req.scene = isMoments ? Int32(WXSceneTimeline.rawValue) : Int32(WXSceneSession.rawValue)
            WXApi.send(req)
            self.logShareW("shareToWeChat branch=WEBPAGE: send invoked, isMoments=\(isMoments), hasThumb=\(thumbData != nil)")
        }
    }

    private struct WeChatShareImageData {
        let imageData: Data
        let thumbData: Data?
    }

    private func prepareWeChatShareImageData(_ data: Data) -> WeChatShareImageData? {
        guard let imageData = fitImageData(data, maxBytes: 10 * 1024 * 1024) else {
            return nil
        }
        return WeChatShareImageData(
            imageData: imageData,
            thumbData: makeWeChatThumbData(from: data)
        )
    }

    private func makeWeChatThumbData(from data: Data) -> Data? {
        guard let image = UIImage(data: data) else {
            return nil
        }
        let size = CGSize(width: 150, height: 150)
        UIGraphicsBeginImageContextWithOptions(size, false, 1.0)
        image.draw(in: CGRect(origin: .zero, size: size))
        let scaledImage = UIGraphicsGetImageFromCurrentImageContext()
        UIGraphicsEndImageContext()

        var quality: CGFloat = 0.85
        var thumbData = scaledImage?.jpegData(compressionQuality: quality)
        while let data = thumbData, data.count > 32 * 1024, quality > 0.1 {
            quality -= 0.1
            thumbData = scaledImage?.jpegData(compressionQuality: quality)
        }
        return thumbData
    }

    // MARK: - QQ

    /// 翻译 QQ Open SDK 的 `QQApiSendResultCode` 为可读字符串，便于线上日志排障。
    /// 对应枚举定义见 `TencentOpenAPI.framework/Headers/QQApiInterfaceObject.h`，
    /// 该枚举底层是 `NS_ENUM(NSInteger, ...)`，含负数（如 -1 表示发送失败）。
    private func qqSendResultDesc(_ code: QQApiSendResultCode) -> String {
        switch Int(code.rawValue) {
        case 0: return "SUCCESS"
        case 1: return "QQ_NOT_INSTALLED"
        case 2: return "QQ_NOT_SUPPORT_API"
        case 3: return "MESSAGE_TYPE_INVALID"
        case 4: return "MESSAGE_CONTENT_NULL"
        case 5: return "MESSAGE_CONTENT_INVALID"
        case 6: return "APP_NOT_REGISTED"
        case 7: return "APP_SHARE_ASYNC"
        case 8: return "QQ_NOT_SUPPORT_API_WITH_ERRORSHOW"
        case 9: return "MESSAGE_ARK_CONTENT_NULL"
        case 10: return "MESSAGE_MINI_CONTENT_NULL"
        case -1: return "SEND_FAILD"
        case -2: return "SHARE_DEST_UNKNOWN"
        case -3: return "TIM_SEND_FAILD"
        case 11: return "TIM_NOT_INSTALLED"
        case 12: return "TIM_NOT_SUPPORT_API"
        case 13: return "INCOMING_PARAM_ERROR"
        case 10000: return "QZONE_NOT_SUPPORT_TEXT"
        case 10001: return "QZONE_NOT_SUPPORT_IMAGE"
        case 10002: return "VERSION_NEED_UPDATE"
        case 20000: return "APP_URL_TYPES_ILLEGALITY"
        case 30001: return "USER_NOT_AGREED_AUTHORIZATION"
        default: return "UNKNOWN(\(code.rawValue))"
        }
    }

    private func shareToQQ(title: String, desc: String, url: String, thumbUrl: String, arkData: String) {
        _ = IOSSocialLoginSetup.setupQQ()

        // 入口日志：上线后用户反馈 QQ 分享异常时，第一时间能定位走的是 image 还是 news 分支。
        let installed = QQApiInterface.isQQInstalled()
        let supportApi = QQApiInterface.isQQSupportApi()
        logShareW(
            "shareToQQ enter: title=\(logTextMeta(title)), url=\(logTextMeta(url)), " +
            "thumbUrl=\(logResourceMeta(thumbUrl)), " +
            "arkLen=\(arkData.count), isQQInstalled=\(installed), isQQSupportApi=\(supportApi)"
        )
        if !installed {
            logShareE("shareToQQ abort: QQ not installed")
            showAlert("未安装 QQ")
            return
        }

        if let imageData = loadLocalImageData(from: thumbUrl),
           let preparedImage = prepareQQShareImageData(imageData) {
            let imageObject = QQApiImageObject.object(
                with: preparedImage.imageData,
                previewImageData: preparedImage.previewImageData,
                title: title,
                description: desc
            ) as? QQApiImageObject
            let req = SendMessageToQQReq(content: imageObject)
            let result = QQApiInterface.send(req)
            logShareW(
                "shareToQQ branch=IMAGE: result=\(qqSendResultDesc(result))(\(result.rawValue)), " +
                "title=\(logTextMeta(title)), imageSize=\(preparedImage.imageData.count)"
            )
            return
        }

        guard let shareUrl = URL(string: url) else {
            logShareE("shareToQQ abort: invalid share url=\(logTextMeta(url))")
            showAlert("分享链接无效")
            return
        }

        let previewURL = thumbUrl.isEmpty ? nil : URL(string: thumbUrl)

        let newsObject = QQApiNewsObject.object(
            with: shareUrl,
            title: title,
            description: desc,
            previewImageURL: previewURL,
            targetContentType: .news
        ) as? QQApiNewsObject

        let req = SendMessageToQQReq(content: newsObject)
        let result = QQApiInterface.send(req)
        logShareW(
            "shareToQQ branch=NEWS: result=\(qqSendResultDesc(result))(\(result.rawValue)), " +
            "title=\(logTextMeta(title)), hasPreviewURL=\(previewURL != nil)"
        )
    }

    private struct QQShareImageData {
        let imageData: Data
        let previewImageData: Data
    }

    private func isLocalImageResource(_ pathOrUrl: String) -> Bool {
        guard !pathOrUrl.isEmpty else { return false }
        if pathOrUrl.hasPrefix("/") {
            return true
        }
        return URL(string: pathOrUrl)?.isFileURL == true
    }

    private func loadLocalImageData(from pathOrUrl: String) -> Data? {
        guard !pathOrUrl.isEmpty else { return nil }

        let fileURL: URL
        if pathOrUrl.hasPrefix("file://") {
            guard let url = URL(string: pathOrUrl), url.isFileURL else {
                return nil
            }
            fileURL = url
        } else if pathOrUrl.hasPrefix("/") {
            fileURL = URL(fileURLWithPath: pathOrUrl)
        } else {
            return nil
        }

        guard FileManager.default.fileExists(atPath: fileURL.path) else {
            return nil
        }
        return try? Data(contentsOf: fileURL)
    }

    private func prepareQQShareImageData(_ data: Data) -> QQShareImageData? {
        guard let imageData = fitImageData(data, maxBytes: 5 * 1024 * 1024),
              let previewImageData = fitImageData(data, maxBytes: 1024 * 1024) else {
            return nil
        }
        return QQShareImageData(imageData: imageData, previewImageData: previewImageData)
    }

    private func fitImageData(_ data: Data, maxBytes: Int) -> Data? {
        if data.count <= maxBytes {
            return data
        }
        guard let image = UIImage(data: data) else {
            return nil
        }

        var quality: CGFloat = 0.9
        while quality >= 0.35 {
            if let compressed = image.jpegData(compressionQuality: quality),
               compressed.count <= maxBytes {
                return compressed
            }
            quality -= 0.15
        }

        let ratio = sqrt(CGFloat(maxBytes) / CGFloat(data.count))
        let targetSize = CGSize(
            width: max(CGFloat(1), image.size.width * ratio),
            height: max(CGFloat(1), image.size.height * ratio)
        )
        UIGraphicsBeginImageContextWithOptions(targetSize, true, image.scale)
        image.draw(in: CGRect(origin: .zero, size: targetSize))
        let resized = UIGraphicsGetImageFromCurrentImageContext()
        UIGraphicsEndImageContext()
        return resized?.jpegData(compressionQuality: 0.85)
    }

    // MARK: - QZone

    private func shareToQZone(title: String, desc: String, url: String, thumbUrl: String) {
        _ = IOSSocialLoginSetup.setupQQ()

        // 入口日志：QZone 也走 QQ Open SDK，安装/支持检查与 QQ 单聊一致。
        let installed = QQApiInterface.isQQInstalled()
        logShareW(
            "shareToQZone enter: title=\(logTextMeta(title)), url=\(logTextMeta(url)), " +
            "thumbUrl=\(logResourceMeta(thumbUrl)), isQQInstalled=\(installed)"
        )
        if !installed {
            logShareE("shareToQZone abort: QQ not installed")
            showAlert("未安装 QQ")
            return
        }

        if let imageData = loadLocalImageData(from: thumbUrl),
           let preparedImage = prepareQQShareImageData(imageData) {
            let qZoneImageObject = QQApiImageArrayForQZoneObject(
                imageArrayData: [preparedImage.imageData],
                title: title,
                extMap: nil
            )
            qZoneImageObject?.cflag = UInt64(kQQAPICtrlFlag.qqapiCtrlFlagQZoneShareOnStart.rawValue)

            let req = SendMessageToQQReq(content: qZoneImageObject)
            let result = QQApiInterface.sendReq(toQZone: req)
            logShareW(
                "shareToQZone branch=IMAGE: result=\(qqSendResultDesc(result))(\(result.rawValue)), " +
                "title=\(logTextMeta(title)), imageSize=\(preparedImage.imageData.count)"
            )
            return
        }

        // 非本地海报路径继续沿用图文卡片兜底。
        guard let shareUrl = URL(string: url) else {
            logShareE("shareToQZone abort: invalid share url=\(logTextMeta(url))")
            showAlert("分享链接无效")
            return
        }

        let previewURL = thumbUrl.isEmpty ? nil : URL(string: thumbUrl)

        let newsObject = QQApiNewsObject.object(
            with: shareUrl,
            title: title,
            description: desc,
            previewImageURL: previewURL,
            targetContentType: .news
        ) as? QQApiNewsObject
        newsObject?.cflag = UInt64(kQQAPICtrlFlag.qqapiCtrlFlagQZoneShareOnStart.rawValue)

        let req = SendMessageToQQReq(content: newsObject)
        let result = QQApiInterface.sendReq(toQZone: req)
        logShareW(
            "shareToQZone branch=NEWS: result=\(qqSendResultDesc(result))(\(result.rawValue)), " +
            "title=\(logTextMeta(title)), hasPreviewURL=\(previewURL != nil)"
        )
    }

    // MARK: - Weibo

    private func shareToWeibo(title: String, desc: String, url: String, thumbUrl: String) {
        _ = IOSSocialLoginSetup.setupWeibo()
        guard WeiboSDK.isWeiboAppInstalled() else {
            logShareE("shareToWeibo abort: Weibo not installed")
            showAlert("未安装微博")
            return
        }

        // 对齐 Android 端 buildWeiboText 格式：【标题】摘要 链接 (来自 @微视)
        let shareText = buildWeiboText(title: title, desc: desc, url: url)
        logShareW(
            "shareToWeibo enter: title=\(logTextMeta(title)), url=\(logTextMeta(url)), " +
            "textLen=\(shareText.count), thumbUrl=\(logResourceMeta(thumbUrl))"
        )

        let sendWeiboMessage: (Data?) -> Void = { [weak self] imageData in
            guard let self = self else { return }
            DispatchQueue.main.async {
                let message = WBMessageObject.message() as! WBMessageObject
                message.text = shareText

                if let data = imageData {
                    let imageObject = WBImageObject.object() as! WBImageObject
                    imageObject.imageData = data
                    message.imageObject = imageObject
                }

                let authorizeRequest = WBAuthorizeRequest.request() as! WBAuthorizeRequest
                authorizeRequest.redirectURI = "https://api.weibo.com/oauth2/default.html"
                authorizeRequest.scope = "all"

                guard let request = WBSendMessageToWeiboRequest.request(
                    withMessage: message,
                    authInfo: authorizeRequest,
                    access_token: nil
                ) as? WBSendMessageToWeiboRequest else {
                    self.logShareE("shareToWeibo abort: WBSendMessageToWeiboRequest creation failed")
                    self.showAlert("微博分享请求创建失败")
                    return
                }

                WeiboSDK.send(request) { success in
                    self.logShareW(
                        "shareToWeibo: result=\(success), title=\(self.logTextMeta(title)), " +
                        "url=\(self.logTextMeta(url)), " +
                        "hasImage=\(imageData != nil)"
                    )
                    if !success {
                        self.showAlert("微博分享失败")
                    }
                }
            }
        }

        if isLocalImageResource(thumbUrl) {
            DispatchQueue.global(qos: .utility).async { [weak self] in
                guard let self = self else { return }
                let localImageData = self.loadLocalImageData(from: thumbUrl)
                let preparedImageData = localImageData.flatMap { self.prepareWeiboShareImageData($0) }
                self.logShareW(
                    "shareToWeibo branch=LOCAL_IMAGE: originalSize=\(localImageData?.count ?? 0), " +
                    "preparedSize=\(preparedImageData?.count ?? 0)"
                )
                sendWeiboMessage(preparedImageData)
            }
            return
        }

        downloadImage(from: thumbUrl) { [weak self] imageData in
            DispatchQueue.global(qos: .utility).async { [weak self] in
                guard let self = self else { return }
                let preparedImageData = imageData.flatMap { self.prepareWeiboShareImageData($0) }
                self.logShareW(
                    "shareToWeibo branch=REMOTE_IMAGE: originalSize=\(imageData?.count ?? 0), " +
                    "preparedSize=\(preparedImageData?.count ?? 0)"
                )
                sendWeiboMessage(preparedImageData)
            }
        }
    }

    /// 拼装微博分享文案，对齐 Android 端格式：
    /// "【标题】摘要 分享链接 (来自 @微视)"
    private func buildWeiboText(title: String, desc: String, url: String) -> String {
        var parts: [String] = []
        if !title.isEmpty {
            parts.append("【\(title)】")
        }
        if !desc.isEmpty && desc != defaultDesc {
            parts.append(desc)
        }
        if !url.isEmpty {
            parts.append(url)
        }
        parts.append("(来自 @微视)")
        let text = parts.joined(separator: " ")
        // 微博文案限制 1024 字符
        return String(text.prefix(1024))
    }

    private func prepareWeiboShareImageData(_ data: Data) -> Data? {
        let maxBytes = 10 * 1024 * 1024
        if data.count <= maxBytes {
            return data
        }
        guard let image = UIImage(data: data) else {
            return nil
        }

        var scale = min(CGFloat(1), sqrt(CGFloat(maxBytes) / CGFloat(data.count)))
        while scale >= 0.2 {
            let targetImage: UIImage
            if scale < 0.99 {
                let targetSize = CGSize(
                    width: max(CGFloat(1), floor(image.size.width * scale)),
                    height: max(CGFloat(1), floor(image.size.height * scale))
                )
                UIGraphicsBeginImageContextWithOptions(targetSize, true, image.scale)
                image.draw(in: CGRect(origin: .zero, size: targetSize))
                targetImage = UIGraphicsGetImageFromCurrentImageContext() ?? image
                UIGraphicsEndImageContext()
            } else {
                targetImage = image
            }

            var quality: CGFloat = 0.9
            while quality >= 0.25 {
                if let compressed = targetImage.jpegData(compressionQuality: quality),
                   compressed.count <= maxBytes {
                    return compressed
                }
                quality -= 0.15
            }
            scale *= 0.75
        }

        return nil
    }

    private func downloadImage(from urlString: String, completion: @escaping (Data?) -> Void) {
        guard !urlString.isEmpty, let url = URL(string: urlString) else {
            completion(nil)
            return
        }

        DispatchQueue.global(qos: .utility).async {
            guard let data = try? Data(contentsOf: url),
                  let image = UIImage(data: data) else {
                DispatchQueue.main.async { completion(nil) }
                return
            }

            let imageData = image.pngData()
            DispatchQueue.main.async {
                completion(imageData)
            }
        }
    }

    // MARK: - WeCom

    private func shareToWeCom(title: String, desc: String, url: String, thumbUrl: String) {
        _ = IOSSocialLoginSetup.setupWeCom()
        let isInstalled = WWKApi.isAppInstalled()
        logShareW(
            "shareToWeCom enter: title=\(logTextMeta(title)), url=\(logTextMeta(url)), " +
            "thumbUrl=\(logResourceMeta(thumbUrl)), isAppInstalled=\(isInstalled)"
        )
        guard isInstalled else {
            logShareE("shareToWeCom abort: WeCom not installed")
            showAlert("未安装企业微信")
            return
        }

        let req = WWKSendMessageReq()
        if let imageData = loadLocalImageData(from: thumbUrl),
           let preparedImageData = fitImageData(imageData, maxBytes: 10 * 1024 * 1024) {
            let imageAttachment = WWKMessageImageAttachment()
            imageAttachment.filename = "comment_share.jpg"
            imageAttachment.path = nil
            imageAttachment.data = preparedImageData
            req.attachment = imageAttachment
            logShareW("shareToWeCom branch=IMAGE: imageSize=\(preparedImageData.count), title=\(logTextMeta(title))")
        } else {
            let linkAttachment = WWKMessageLinkAttachment()
            linkAttachment.title = title
            linkAttachment.summary = desc
            linkAttachment.url = url
            if !thumbUrl.isEmpty {
                linkAttachment.iconurl = thumbUrl
            }
            req.attachment = linkAttachment
            logShareW("shareToWeCom branch=LINK: title=\(logTextMeta(title)), url=\(logTextMeta(url))")
        }

        // 企业微信要求 bundleID 必须是 registerApp 时使用的 shareAppId，
        // 否则 SDK 无法识别来源 App，会出现静默失败（点击无反应）。
        req.bundleID = IOSSocialLoginSetup.currentWeComShareAppId
        req.bundleName = Bundle.main.object(forInfoDictionaryKey: "CFBundleDisplayName") as? String
            ?? Bundle.main.object(forInfoDictionaryKey: "CFBundleName") as? String
            ?? "微视"

        let sent = WWKApi.send(req)
        logShareW("shareToWeCom WWKApi.send result=\(sent)")
        if !sent {
            // 对齐 microvision：SDK 直发失败时，降级到 wxwork:// URL Scheme 拉起
            logShareW("shareToWeCom WWKApi.send returned false, fallback to manualShareToWeCom")
            manualShareToWeCom(req: req)
        }
    }

    private func manualShareToWeCom(req: WWKSendMessageReq) {
        let appId = IOSSocialLoginSetup.currentWeComShareAppId
        guard let serializedData = req.serializedData else {
            logShareE("manualShareToWeCom abort: serializedData is nil")
            showAlert("企业微信分享数据构建失败")
            return
        }

        UIPasteboard.general.setData(serializedData, forPasteboardType: appId)

        // 当前 WWKApi 头文件中对应方法是 getApiVersion，Swift 中未导入 getVersion，
        // 这里直接用固定版本号，避免编译失败；企业微信对 v 参数不做严格校验。
        let sdkVersion = "1.0"
        let pbType = appId
        let urlString = "wxwork://app?id=\(appId)&v=\(sdkVersion)&a=1&pbtype=\(pbType)"
        guard let openURL = URL(string: urlString) else {
            logShareE("manualShareToWeCom abort: invalid url=\(logTextMeta(urlString))")
            showAlert("企业微信分享跳转失败")
            return
        }

        UIApplication.shared.open(openURL, options: [:]) { [weak self] success in
            guard let self = self else { return }
            self.logShareW("manualShareToWeCom: open result=\(success)")
            if !success {
                self.showAlert("打开企业微信失败")
            }
        }
    }

    // MARK: - System / Utility

    private func shareToSystem(payload: SharePayload) {
        guard let topVC = topViewController() else { return }
        var activityItems: [Any] = []
        if !payload.title.isEmpty {
            activityItems.append(payload.title)
        }
        if let shareURL = URL(string: payload.url), !payload.url.isEmpty {
            activityItems.append(shareURL)
        } else if !payload.url.isEmpty {
            activityItems.append(payload.url)
        }
        if activityItems.isEmpty {
            activityItems.append(payload.desc)
        }

        let controller = UIActivityViewController(activityItems: activityItems, applicationActivities: nil)
        if let popover = controller.popoverPresentationController {
            popover.sourceView = topVC.view
            popover.sourceRect = CGRect(
                x: topVC.view.bounds.midX,
                y: topVC.view.bounds.maxY,
                width: 1,
                height: 1
            )
        }
        topVC.present(controller, animated: true)
    }

    private func copyLink(_ shareUrl: String) {
        guard !shareUrl.isEmpty else {
            showAlert("链接暂不可用")
            return
        }
        UIPasteboard.general.string = shareUrl
        showAlert("已复制")
    }

    private func saveImage(from urlString: String) {
        guard !urlString.isEmpty else {
            showAlert("图片暂不可用")
            return
        }
        downloadData(from: urlString) { [weak self] data in
            guard let self = self else { return }
            guard let data = data, let image = UIImage(data: data) else {
                self.showAlert("图片下载失败")
                return
            }
            self.requestPhotoLibraryAccess { granted in
                guard granted else {
                    self.showAlert("没有相册写入权限")
                    return
                }
                PHPhotoLibrary.shared().performChanges({
                    PHAssetChangeRequest.creationRequestForAsset(from: image)
                }) { success, _ in
                    DispatchQueue.main.async {
                        self.showAlert(success ? "已保存到相册" : "保存图片失败")
                    }
                }
            }
        }
    }

    private func saveVideo(from urlString: String) {
        guard !urlString.isEmpty else {
            showAlert("视频暂不可用")
            return
        }
        downloadFile(from: urlString, preferredExtension: "mp4") { [weak self] fileURL in
            guard let self = self else { return }
            guard let fileURL = fileURL else {
                self.showAlert("视频下载失败")
                return
            }
            self.requestPhotoLibraryAccess { granted in
                guard granted else {
                    self.showAlert("没有相册写入权限")
                    try? FileManager.default.removeItem(at: fileURL)
                    return
                }
                PHPhotoLibrary.shared().performChanges({
                    PHAssetChangeRequest.creationRequestForAssetFromVideo(atFileURL: fileURL)
                }) { success, _ in
                    try? FileManager.default.removeItem(at: fileURL)
                    DispatchQueue.main.async {
                        self.showAlert(success ? "已保存到相册" : "保存视频失败")
                    }
                }
            }
        }
    }

    private func requestPhotoLibraryAccess(_ completion: @escaping (Bool) -> Void) {
        if #available(iOS 14, *) {
            PHPhotoLibrary.requestAuthorization(for: .addOnly) { status in
                DispatchQueue.main.async {
                    completion(status == .authorized || status == .limited)
                }
            }
        } else {
            PHPhotoLibrary.requestAuthorization { status in
                DispatchQueue.main.async {
                    completion(status == .authorized)
                }
            }
        }
    }

    private func downloadData(from urlString: String, completion: @escaping (Data?) -> Void) {
        guard let url = URL(string: urlString) else {
            completion(nil)
            return
        }
        DispatchQueue.global(qos: .utility).async {
            let data = try? Data(contentsOf: url)
            DispatchQueue.main.async {
                completion(data)
            }
        }
    }

    private func downloadFile(from urlString: String, preferredExtension: String, completion: @escaping (URL?) -> Void) {
        guard let url = URL(string: urlString) else {
            completion(nil)
            return
        }
        DispatchQueue.global(qos: .utility).async {
            guard let data = try? Data(contentsOf: url) else {
                DispatchQueue.main.async { completion(nil) }
                return
            }
            let fileURL = FileManager.default.temporaryDirectory
                .appendingPathComponent("weishi_share_\(UUID().uuidString).\(preferredExtension)")
            do {
                try data.write(to: fileURL, options: .atomic)
                DispatchQueue.main.async { completion(fileURL) }
            } catch {
                DispatchQueue.main.async { completion(nil) }
            }
        }
    }

    // MARK: - Utils

    private func downloadThumb(from urlString: String, completion: @escaping (Data?) -> Void) {
        guard !urlString.isEmpty, let url = URL(string: urlString) else {
            completion(nil)
            return
        }

        DispatchQueue.global(qos: .utility).async {
            guard let data = try? Data(contentsOf: url),
                  let image = UIImage(data: data) else {
                DispatchQueue.main.async { completion(nil) }
                return
            }

            let size = CGSize(width: 150, height: 150)
            UIGraphicsBeginImageContextWithOptions(size, false, 1.0)
            image.draw(in: CGRect(origin: .zero, size: size))
            let scaledImage = UIGraphicsGetImageFromCurrentImageContext()
            UIGraphicsEndImageContext()

            var quality: CGFloat = 0.85
            var thumbData = scaledImage?.jpegData(compressionQuality: quality)
            while let data = thumbData, data.count > 32 * 1024, quality > 0.1 {
                quality -= 0.1
                thumbData = scaledImage?.jpegData(compressionQuality: quality)
            }

            DispatchQueue.main.async {
                completion(thumbData)
            }
        }
    }

    private func showAlert(_ message: String) {
        print("[\(tag)] \(message)")
        guard let topVC = topViewController() else { return }

        let alert = UIAlertController(title: nil, message: message, preferredStyle: .alert)
        topVC.present(alert, animated: true)
        DispatchQueue.main.asyncAfter(deadline: .now() + 1.5) {
            alert.dismiss(animated: true)
        }
    }

    private func topViewController() -> UIViewController? {
        guard let windowScene = UIApplication.shared.connectedScenes
            .compactMap({ $0 as? UIWindowScene })
            .first,
              let rootVC = windowScene.windows.first(where: { $0.isKeyWindow })?.rootViewController
        else {
            return nil
        }
        return findTopViewController(from: rootVC)
    }

    private func findTopViewController(from vc: UIViewController) -> UIViewController {
        if let presented = vc.presentedViewController {
            return findTopViewController(from: presented)
        }
        if let nav = vc as? UINavigationController,
           let visible = nav.visibleViewController {
            return findTopViewController(from: visible)
        }
        if let tab = vc as? UITabBarController,
           let selected = tab.selectedViewController {
            return findTopViewController(from: selected)
        }
        return vc
    }
}
