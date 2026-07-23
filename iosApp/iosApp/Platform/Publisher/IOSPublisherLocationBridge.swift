import CoreLocation
import Foundation
import UIKit
import umbrella

func setupIOSPublisherLocationBridge() {
    PublisherLocationPlatformBridgeRegistry.shared.register(bridge: IOSPublisherLocationBridge())
}

/// iOS 端发布器位置桥，提供前台定位权限与一次性定位。
final class IOSPublisherLocationBridge: NSObject, PublisherLocationPlatformBridge, CLLocationManagerDelegate {

    /// 使用全局共享的 CLLocationManager，避免每次创建新实例导致定位冷启动
    private let manager = CLLocationManager()
    private var permissionCompletion: ((KotlinBoolean?, Error?) -> Void)?
    private var locationCompletion: ((PublisherLocationSignal?, Error?) -> Void)?
    /// 当前已获取到的最佳位置（精度最高的）
    private var bestLocation: CLLocation?
    private var locateRetryCnt = 0

    override init() {
        super.init()
        manager.delegate = self
        // 与 microvision 一致：使用 Kilometer 精度，Wi-Fi/基站即可满足 POI 推荐需求
        manager.desiredAccuracy = kCLLocationAccuracyKilometer
    }

    func isSystemLocationEnabled() -> Bool {
        return CLLocationManager.locationServicesEnabled()
    }

    func hasLocationPermission() -> Bool {
        return Self.isGranted(Self.authorizationStatus(manager: manager))
    }

    func requestLocationPermission(
        completionHandler: @escaping (KotlinBoolean?, Error?) -> Void
    ) {
        DispatchQueue.main.async {
            let status = Self.authorizationStatus(manager: self.manager)
            if status != .notDetermined {
                completionHandler(KotlinBoolean(value: Self.isGranted(status)), nil)
                return
            }
            self.permissionCompletion = completionHandler
            self.manager.requestWhenInUseAuthorization()
        }
    }

    func getCurrentLocation(
        timeoutMs: Int64,
        completionHandler: @escaping (PublisherLocationSignal?, Error?) -> Void
    ) {
        DispatchQueue.main.async {
            let sysEnabled = self.isSystemLocationEnabled()
            let hasPermission = self.hasLocationPermission()
            NSLog("[PublisherLocation] getCurrentLocation: sysEnabled=%@, hasPermission=%@, timeoutMs=%lld", sysEnabled ? "true" : "false", hasPermission ? "true" : "false", timeoutMs)
            guard sysEnabled, hasPermission else {
                NSLog("[PublisherLocation] getCurrentLocation: guard failed, returning signal with nil gps")
                completionHandler(PublisherLocationSignal(
                    gps: nil,
                    wifiMacs: [],
                    systemLocationEnabled: sysEnabled,
                    permissionGranted: hasPermission
                ), nil)
                return
            }
            // 如果已有定位请求在进行中，先取消旧的
            if let oldCompletion = self.locationCompletion {
                NSLog("[PublisherLocation] getCurrentLocation: cancelling previous pending request")
                self.locationCompletion = nil
                self.manager.stopUpdatingLocation()
                oldCompletion(nil, nil)
            }
            self.locationCompletion = completionHandler
            self.bestLocation = nil
            self.locateRetryCnt = 0
            NSLog("[PublisherLocation] getCurrentLocation: calling startUpdatingLocation()")
            self.manager.startUpdatingLocation()

            let timeoutSeconds = Double(max(timeoutMs, 1000)) / 1000.0
            // 第一层超时（5秒）：如果已有位置就快速返回，否则继续等待
            let firstTimeout = min(5.0, timeoutSeconds * 0.5)
            DispatchQueue.main.asyncAfter(deadline: .now() + firstTimeout) { [weak self] in
                guard let self = self, self.locationCompletion != nil else { return }
                if let location = self.bestLocation {
                    NSLog("[PublisherLocation] firstTimeout: have location (lat=%.6f, lng=%.6f, accuracy=%.1f), returning early", location.coordinate.latitude, location.coordinate.longitude, location.horizontalAccuracy)
                    self.sendLocationSuccess(location: location)
                } else {
                    NSLog("[PublisherLocation] firstTimeout: no location yet, waiting for final timeout")
                }
            }
            // 第二层超时（总超时）：最终兜底
            DispatchQueue.main.asyncAfter(deadline: .now() + timeoutSeconds) { [weak self] in
                guard let self = self, let completion = self.locationCompletion else {
                    NSLog("[PublisherLocation] getCurrentLocation: final timeout fired but completion already consumed")
                    return
                }
                self.locationCompletion = nil
                self.manager.stopUpdatingLocation()
                // 如果有任何位置（即使精度不够好），也返回
                if let location = self.bestLocation {
                    NSLog("[PublisherLocation] getCurrentLocation: final TIMEOUT, using bestLocation (lat=%.6f, lng=%.6f, accuracy=%.1f)", location.coordinate.latitude, location.coordinate.longitude, location.horizontalAccuracy)
                    completion(self.buildSignal(location: location), nil)
                    return
                }
                // 尝试用系统缓存的最后已知位置兜底
                if let cachedLocation = self.manager.location {
                    let age = -cachedLocation.timestamp.timeIntervalSinceNow
                    if age < 600 {
                        NSLog("[PublisherLocation] getCurrentLocation: final TIMEOUT, using cached location (age=%.1fs)", age)
                        completion(self.buildSignal(location: cachedLocation), nil)
                        return
                    }
                    NSLog("[PublisherLocation] getCurrentLocation: cached location too old (%.0fs), discarding", age)
                }
                NSLog("[PublisherLocation] getCurrentLocation: final TIMEOUT after %.1fs, no location available, returning nil", timeoutSeconds)
                completion(nil, nil)
            }
        }
    }

    func openLocationSettings() {
        openAppSettings()
    }

    func openPermissionSettings() {
        openAppSettings()
    }

    func locationManagerDidChangeAuthorization(_ manager: CLLocationManager) {
        completePermissionIfNeeded(status: Self.authorizationStatus(manager: manager))
    }

    func locationManager(_ manager: CLLocationManager, didChangeAuthorization status: CLAuthorizationStatus) {
        completePermissionIfNeeded(status: status)
    }

    func locationManager(_ manager: CLLocationManager, didUpdateLocations locations: [CLLocation]) {
        guard locationCompletion != nil else {
            NSLog("[PublisherLocation] didUpdateLocations: no completion, ignored. locations=%d", locations.count)
            return
        }
        guard let newLocation = locations.last, newLocation.horizontalAccuracy >= 0 else {
            NSLog("[PublisherLocation] didUpdateLocations: invalid location (accuracy < 0), ignored")
            return
        }
        NSLog("[PublisherLocation] didUpdateLocations: lat=%.6f, lng=%.6f, accuracy=%.1f", newLocation.coordinate.latitude, newLocation.coordinate.longitude, newLocation.horizontalAccuracy)
        // 保留精度最好的位置
        if let current = bestLocation {
            if newLocation.horizontalAccuracy < current.horizontalAccuracy {
                bestLocation = newLocation
                NSLog("[PublisherLocation] didUpdateLocations: better accuracy, updated bestLocation")
            }
        } else {
            bestLocation = newLocation
            NSLog("[PublisherLocation] didUpdateLocations: first valid location")
        }
        // 如果精度已经足够好（≤100m），立即返回结果（与 microvision K_DESIRED_ACCURACY 一致）
        if let best = bestLocation, best.horizontalAccuracy <= 100.0 {
            NSLog("[PublisherLocation] didUpdateLocations: accuracy %.1f <= 100m, returning immediately", best.horizontalAccuracy)
            sendLocationSuccess(location: best)
        }
    }

    func locationManager(_ manager: CLLocationManager, didFailWithError error: Error) {
        let clError = error as? CLError
        let errorCode = clError?.code.rawValue ?? -1
        NSLog("[PublisherLocation] didFailWithError: code=%d, %@", errorCode, error.localizedDescription)
        // kCLErrorLocationUnknown(0)：暂时无法确定位置，系统会继续尝试，什么都不做
        // 这是 microvision 的做法，也是 Apple 官方推荐的做法
        if clError?.code == .locationUnknown {
            NSLog("[PublisherLocation] didFailWithError: locationUnknown (transient), keep waiting")
            return
        }
        // kCLErrorDenied：用户拒绝了权限，立即停止
        if clError?.code == .denied {
            NSLog("[PublisherLocation] didFailWithError: denied, stopping")
            guard let completion = locationCompletion else { return }
            locationCompletion = nil
            manager.stopUpdatingLocation()
            completion(nil, nil)
            return
        }
        // 其他错误：重试最多 3 次（与 microvision QZLBSSERVICE_LOCATION_FAIL_MAX_RETRY_COUNT 一致）
        manager.stopUpdatingLocation()
        if locateRetryCnt < 3 {
            locateRetryCnt += 1
            NSLog("[PublisherLocation] didFailWithError: retrying (%d/3)", locateRetryCnt)
            manager.startUpdatingLocation()
        } else {
            NSLog("[PublisherLocation] didFailWithError: max retries reached, giving up")
            guard let completion = locationCompletion else { return }
            locationCompletion = nil
            completion(nil, nil)
        }
    }

    /// 定位成功，返回结果并停止定位
    private func sendLocationSuccess(location: CLLocation) {
        guard let completion = locationCompletion else { return }
        locationCompletion = nil
        manager.stopUpdatingLocation()
        completion(buildSignal(location: location), nil)
    }

    private func completePermissionIfNeeded(status: CLAuthorizationStatus) {
        guard status != .notDetermined, let completion = permissionCompletion else { return }
        permissionCompletion = nil
        completion(KotlinBoolean(value: Self.isGranted(status)), nil)
    }

    private func buildSignal(location: CLLocation?) -> PublisherLocationSignal? {
        guard let location = location else { return nil }
        let gps = PublisherLocationGps(
            type: Int32(0),
            latitude: Float(location.coordinate.latitude),
            longitude: Float(location.coordinate.longitude),
            altitude: Float(location.altitude)
        )
        return PublisherLocationSignal(
            gps: gps,
            wifiMacs: [],
            systemLocationEnabled: isSystemLocationEnabled(),
            permissionGranted: hasLocationPermission()
        )
    }

    private func openAppSettings() {
        DispatchQueue.main.async {
            guard let url = URL(string: UIApplication.openSettingsURLString) else { return }
            UIApplication.shared.open(url, options: [:], completionHandler: nil)
        }
    }

    private static func authorizationStatus(manager: CLLocationManager) -> CLAuthorizationStatus {
        if #available(iOS 14.0, *) {
            return manager.authorizationStatus
        }
        return CLLocationManager.authorizationStatus()
    }

    private static func isGranted(_ status: CLAuthorizationStatus) -> Bool {
        return status == .authorizedAlways || status == .authorizedWhenInUse
    }
}
