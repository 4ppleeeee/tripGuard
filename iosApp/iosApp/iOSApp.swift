import SwiftUI
import umbrella
import startupUmbrella

@main
struct iOSApp: App {
    private let kuiklyRenderBridge = QNKuiklyRenderBridge()
    private let initialPageName: String

    init() {
        initialPageName = ProcessInfo.processInfo.environment["KMM_DEMO_PAGE_NAME"] ?? "/page/demo_home"
        IOSPlatformInit.setup()
        IOSStartupBridge().launch(
            appId: IOSStartupConfigs.appId,
            packageName: IOSStartupConfigs.packageName,
            isDebug: IOSStartupConfigs.isDebug,
            configs: IOSStartupConfigs.all(),
            platformTaskProvider: IOSPlatformTaskProvider()
        )
        kuiklyRenderBridge.setup()
    }

    var body: some Scene {
        WindowGroup {
            KuiklyRenderViewPage(pageName: initialPageName, data: [:])
                .ignoresSafeArea()
                .onReceive(NotificationCenter.default.publisher(for: UIApplication.didBecomeActiveNotification)) { _ in
                    kuiklyRenderBridge.becomeActive()
                }
        }
    }
}
