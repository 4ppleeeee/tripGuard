import Foundation
import UIKit
import umbrella

// MARK: - Setup

func setupIOSAdJumpAction() {
    IOSAdJumpActionInstaller.install()
}

@objc(IOSAdJumpActionInstaller)
final class IOSAdJumpActionInstaller: NSObject {
    @objc static func install() {
        AdPlatformLogic.shared.adJumpAction = IOSAdJumpAction()
    }
}

// MARK: - IOSAdJumpAction

/// iOS 端广告跳转实现
private class IOSAdJumpAction: NSObject, IAdJumpActionClick {

    // MARK: - 外链底层页（普通 H5）

    func openWebPage(request: AdClickRequest, url: String, cb: ((KotlinBoolean) -> Void)?) {
        AdClickLog().fileLog(msg: "【广告-跳转】openWebPage: \(url)")
        guard let targetUrl = URL(string: url) else {
            cb?(KotlinBoolean(value: false))
            return
        }
        DispatchQueue.main.async {
            UIApplication.shared.open(targetUrl) { success in
                cb?(KotlinBoolean(value: success))
            }
        }
    }

    // MARK: - 微信小程序/小游戏

    func openWxMiniProgram(request: AdClickRequest, data: AdWxLinkData?, cb: @escaping (KotlinBoolean) -> Void) {
        let userName = data?.extInfo?.username ?? ""
        let path = data?.url ?? ""
        AdClickLog().fileLog(msg: "【广告-跳转】openWxMiniProgram: userName=\(userName), path=\(path)")
        // todo genesisli dev: 接入微信 SDK 拉起小程序能力
        cb(KotlinBoolean(value: false))
    }

    func openWxMiniProgram(userName: String, path: String, extData: String, callback: @escaping (KotlinBoolean) -> Void) {
        openWxMiniProgram(userName: userName, path: path, extData: extData, miniProgramType: 0, callback: callback)
    }

    func openWxMiniProgram(userName: String, path: String, extData: String, miniProgramType: Int32, callback: @escaping (KotlinBoolean) -> Void) {
        AdClickLog().fileLog(msg: "【广告-跳转】openWxMiniProgram: userName=\(userName), path=\(path)")
        guard !userName.isEmpty else {
            callback(KotlinBoolean(value: false))
            return
        }
        guard WXApi.isWXAppInstalled(), WXApi.isWXAppSupport() else {
            AdClickLog().fileLog(msg: "【广告-跳转】openWxMiniProgram failed: wx api unavailable or wx not installed")
            callback(KotlinBoolean(value: false))
            return
        }
        let request = WXLaunchMiniProgramReq()
        request.userName = userName
        request.path = path
        request.extMsg = extData
        switch miniProgramType {
        case 1:
            request.miniProgramType = .test
        case 2:
            request.miniProgramType = .preview
        default:
            request.miniProgramType = .release
        }
        DispatchQueue.main.async {
            WXApi.send(request) { success in
                callback(KotlinBoolean(value: success))
            }
        }
    }

    // MARK: - 微信原生页

    func openWxMiniNative(request: AdClickRequest, data: AdWxLinkData?, cb: @escaping (KotlinBoolean) -> Void) {
        AdClickLog().fileLog(msg: "【广告-跳转】openWxMiniNative: \(data?.extInfo?.username ?? "")")
        // todo genesisli dev: 接入微信 SDK 拉起微信原生页能力
        cb(KotlinBoolean(value: false))
    }

    // MARK: - 应用直达（Scheme）

    func openScheme(request: AdClickRequest, scheme: String, cb: @escaping (KotlinBoolean) -> Void) {
        AdClickLog().fileLog(msg: "【广告-跳转】openScheme: \(scheme)")
        DispatchQueue.main.async {
            guard let url = URL(string: scheme) else {
                cb(KotlinBoolean(value: false))
                return
            }
            if UIApplication.shared.canOpenURL(url) {
                UIApplication.shared.open(url, options: [:]) { success in
                    cb(KotlinBoolean(value: success))
                }
            } else {
                cb(KotlinBoolean(value: false))
            }
        }
    }

    // MARK: - DeepLink

    func openDeepLinkAction(request: AdClickRequest, data: AdDeepLinkData, cb: @escaping (KotlinBoolean) -> Void) {
        let deepLinkUrl = data.url ?? ""
        AdClickLog().fileLog(msg: "【广告-跳转】openDeepLinkAction: \(deepLinkUrl)")
        guard !deepLinkUrl.isEmpty, let url = URL(string: deepLinkUrl) else {
            cb(KotlinBoolean(value: false))
            return
        }
        DispatchQueue.main.async {
            if UIApplication.shared.canOpenURL(url) {
                UIApplication.shared.open(url, options: [:]) { success in
                    cb(KotlinBoolean(value: success))
                }
            } else {
                cb(KotlinBoolean(value: false))
            }
        }
    }

    // MARK: - 延迟双开

    func openAfterSysBlock(request: AdClickRequest, cb: @escaping (KotlinBoolean) -> Void) {
        AdClickLog().fileLog(msg: "【广告-跳转】openAfterSysBlock")
        // todo genesisli dev: 实现延迟双开逻辑
        cb(KotlinBoolean(value: false))
    }

    // MARK: - 弹窗展示

    func showDialog(request: AdClickRequest, type: AdProcessorDialogType, cb: ((KotlinBoolean) -> Void)?) {
        AdClickLog().fileLog(msg: "【广告-跳转】showDialog: type=\(type)")
        // todo genesisli dev: 实现各类型弹窗展示（预约、电话、咨询、下载卡、半屏卡）
        cb?(KotlinBoolean(value: false))
    }

    // MARK: - 横版视频跳转 Tab2

    func jumpToTab2(request: AdClickRequest, cloneItem: (any IKmmAdFeedsItem)?) {
        AdClickLog().fileLog(msg: "【广告-跳转】jumpToTab2")
        // todo genesisli dev: 实现横版视频点击跳转到 Tab2
    }

    // MARK: - 尝试跳转 App（Android 特有，iOS 返回 false）

    func androidTryJumpApp(request: AdClickRequest) -> Bool {
        AdClickLog().fileLog(msg: "【广告-跳转】androidTryJumpApp: iOS 不支持")
        return false
    }

    // MARK: - 原生落地页（XJ）

    func openMosaicXJPage(request: AdClickRequest, cb: @escaping (KotlinBoolean) -> Void) {
        AdClickLog().fileLog(msg: "【广告-跳转】openMosaicXJPage")
        // todo genesisli dev: 实现原生落地页（XJ）打开能力
        cb(KotlinBoolean(value: false))
    }

    func checkMosaicXJTemplateExit(templateId: String, scene: AdMosaicXJScene) -> Bool {
        AdClickLog().fileLog(msg: "【广告-跳转】checkMosaicXJTemplateExit: templateId=\(templateId), scene=\(scene.value)")
        // todo genesisli dev: 实现原生落地页模版合法性检查
        return false
    }

    // MARK: - 打开 App Store

    func openAppStore(request: AdClickRequest, cb: @escaping (KotlinBoolean) -> Void) {
        let packageName = request.adOrder?.downloadDto.pkgName ?? ""
        AdClickLog().fileLog(msg: "【广告-跳转】openAppStore: packageName=\(packageName)")
        guard !packageName.isEmpty else {
            cb(KotlinBoolean(value: false))
            return
        }
        DispatchQueue.main.async {
            // iOS 使用 itms-apps:// 协议打开 App Store
            let appStoreUrl = "itms-apps://itunes.apple.com/app/id\(packageName)"
            guard let url = URL(string: appStoreUrl) else {
                cb(KotlinBoolean(value: false))
                return
            }
            if UIApplication.shared.canOpenURL(url) {
                UIApplication.shared.open(url, options: [:]) { success in
                    cb(KotlinBoolean(value: success))
                }
            } else {
                cb(KotlinBoolean(value: false))
            }
        }
    }

    // MARK: - 快应用跳转

    func openHapApp(request: AdClickRequest, scheme: String, cb: @escaping (KotlinBoolean) -> Void) {
        AdClickLog().fileLog(msg: "【广告-跳转】openHapApp: \(scheme)")
        // iOS 端通过 scheme 拉起
        openScheme(request: request, scheme: scheme, cb: cb)
    }

    // MARK: - 快应用弹窗

    func showHapDialog(request: AdClickRequest, hapName: String, onConfirm: @escaping (KotlinBoolean) -> Void) {
        AdClickLog().fileLog(msg: "【广告-跳转】showHapDialog: \(hapName)")
        // todo genesisli dev: 实现快应用确认弹窗
        onConfirm(KotlinBoolean(value: false))
    }

    // MARK: - 已读态保存

    func saveHadRead(request: AdClickRequest) {
        AdClickLog().fileLog(msg: "【广告-跳转】saveHadRead")
        // todo genesisli dev: 实现已读态保存
    }
}
