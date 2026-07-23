import Foundation
import umbrella

/// iOS 端 Compose 桥接类，实现 `IIOSComposeBridge` 协议。
final class IOSComposeBridge: IIOSComposeBridge {
    func getIconFontMapping() -> [String : String] {
        IconFontManager.shared.iconMap
    }
}
