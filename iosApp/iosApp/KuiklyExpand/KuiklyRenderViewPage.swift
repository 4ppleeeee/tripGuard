import Foundation
import SwiftUI

/// 自定义 NavigationController，将状态栏样式控制权委托给子 ViewController。
/// 标准 UINavigationController 默认不会把 preferredStatusBarStyle 传递给子 VC，
/// 重写 childForStatusBarStyle 后，子 VC（KuiklyRenderViewController）的 preferredStatusBarStyle 才会生效。
class KuiklyNavigationController: UINavigationController {
    override var childForStatusBarStyle: UIViewController? {
        return topViewController
    }
    override var childForStatusBarHidden: UIViewController? {
        return topViewController
    }
}

struct KuiklyRenderViewPage : UIViewControllerRepresentable {
    var pageName: String
    var data: Dictionary<String, Any>
    func makeUIViewController(context: Context) -> KuiklyNavigationController {
        let hrVC = KuiklyRenderViewController(pageName: pageName, pageData: data)
        return KuiklyNavigationController(rootViewController: hrVC)
    }

    func updateUIViewController(_ uiViewController: KuiklyNavigationController, context: Context) {

    }

    func dealloc() {

    }

}