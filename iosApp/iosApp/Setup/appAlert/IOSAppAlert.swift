import Foundation
import UIKit
import umbrella
import Toast_Swift

/// iOS 端 IAppAlert 实现
/// 使用 Toast-Swift 库展示 Toast，使用 UIAlertController 展示 Dialog
final class IOSAppAlert: IAppAlert {

    func checkShowPushRemindDialog(type: String) {
        // The seed demo does not wire a push provider on iOS.
    }

    func showToast(msg: String, duration: Double, debug: Bool) {
        DispatchQueue.main.async {
            guard let keyWindow = Self.keyWindow() else { return }
            // 先隐藏当前正在展示的 toast
            keyWindow.hideAllToasts()

            let dismissDelay = duration > 0 ? duration : 1.5
            keyWindow.makeToast(msg, duration: dismissDelay, position: .center)
        }
    }

    func showDialog(title: String, msg: String) {
        DispatchQueue.main.async {
            guard let topVC = Self.topViewController() else {
                // 降级为 toast
                self.showToast(msg: title + "\n" + msg, duration: 3.5, debug: false)
                return
            }
            let alert = UIAlertController(title: title, message: msg, preferredStyle: .alert)
            alert.addAction(UIAlertAction(title: "确定", style: .default))
            topVC.present(alert, animated: true)
        }
    }

    func showDecorDebugView(msg: String) {
        // 默认不处理
    }

    func hideCurrentToast() {
        DispatchQueue.main.async {
            Self.keyWindow()?.hideAllToasts()
        }
    }

    // MARK: - Private Helpers

    private static func keyWindow() -> UIWindow? {
        UIApplication.shared.connectedScenes
            .compactMap { $0 as? UIWindowScene }
            .first { $0.activationState == .foregroundActive }?
            .windows
            .first { $0.isKeyWindow }
    }

    private static func topViewController() -> UIViewController? {
        guard let rootVC = keyWindow()?.rootViewController else { return nil }
        var top = rootVC
        while let presented = top.presentedViewController {
            top = presented
        }
        return top
    }
}
