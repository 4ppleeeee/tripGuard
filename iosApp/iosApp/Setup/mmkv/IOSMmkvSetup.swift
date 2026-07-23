import Foundation
import umbrella
import MMKV

/// iOS MMKV initialization and storage setup
enum IOSMmkvSetup {

    static func setup() {
        let rootDir = MMKV.initialize(rootDir: nil)
        NSLog("[Startup][MMKV][iOS] initialized, rootDir=\(rootDir ?? "nil")")
        // Delegate to the shared Kotlin KmkvStorage (commonMain implementation)
        KmkvStorageKt.setupKmkvStorage()
    }
}
