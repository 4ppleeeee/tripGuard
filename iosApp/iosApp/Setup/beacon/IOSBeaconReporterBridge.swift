import Foundation
import umbrella

/// iOS 端 Beacon 上报桥接实现
///
/// 通过 Bridging Header 直接使用 Beacon SDK 的 ObjC 类型
/// （BeaconReport / BeaconEvent / BeaconReportResult），
/// 返回 KMP 的 BeaconResult 供 IOSBeaconBridge 注入使用。
enum IOSBeaconReporterBridge {
    private static let fallbackAppKey = "0S000EAOIR2GPC95"

    /// 执行 Beacon 上报并返回 BeaconResult（KMP 类型）
    static func report(eventCode: String, params: [String: String]) -> BeaconResult? {
        let appKey = resolveAppKey()
        let event = BeaconEvent(
            appKey: appKey,
            code: eventCode,
            type: .realTime,
            success: true,
            params: params
        )

        let reportResult = BeaconReport.sharedInstance().report(event)
        return toKmpResult(reportResult)
    }

    // MARK: - Private

    private static func toKmpResult(_ result: BeaconReportResult) -> BeaconResult {
        let eventId: Int64 = Int64(result.eventId ?? "") ?? 0
        let errorCode: Int32 = Int32(result.type.rawValue)
        let errorMsg: String = result.errorMessage ?? ""
        return BeaconResult(eventId: eventId, errorCode: errorCode, errorMsg: errorMsg)
    }

    private static func resolveAppKey() -> String {
        if let path = Bundle.main.path(forResource: "BeaconInfo", ofType: "plist"),
           let dict = NSDictionary(contentsOfFile: path) as? [String: Any] {
            for key in ["beacon_main_appkey", "beacon_appkey", "appKey", "appkey"] {
                if let value = dict[key] as? String,
                   !value.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty {
                    return value
                }
            }
        }
        if let info = Bundle.main.infoDictionary {
            for key in ["BEACON_APP_ID", "beacon_main_appkey", "beacon_appkey"] {
                if let value = info[key] as? String,
                   !value.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty {
                    return value
                }
            }
        }
        return fallbackAppKey
    }
}
