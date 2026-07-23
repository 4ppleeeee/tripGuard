import Foundation

enum QimeiSetup {

    private static let beaconAppKey = "0S000EAOIR2GPC95"
    private static let lock = NSLock()

    private static let delegateProxy = QimeiDelegateProxy()
    private static var qimeiService: NSObject?
    private static var didScheduleRefresh = false

    private static var cachedQimei: String = ""
    private static var cachedQimei36: String = ""

    static func setup() {
        _ = ensureService()
        refreshFromService()
    }

    static func currentQimei() -> String {
        setup()
        lock.lock()
        let cached = cachedQimei
        lock.unlock()
        if isValidQimei(cached) {
            return cached
        }
        refreshFromService()
        lock.lock()
        let refreshed = cachedQimei
        lock.unlock()
        return isValidQimei(refreshed) ? refreshed : ""
    }

    static func currentQimei36() -> String {
        setup()
        lock.lock()
        let cached = cachedQimei36
        lock.unlock()
        if isValidQimei36(cached) {
            return cached
        }
        refreshFromService()
        lock.lock()
        let refreshed = cachedQimei36
        lock.unlock()
        return isValidQimei36(refreshed) ? refreshed : ""
    }

    static func getQimeiWithBlock(_ callback: @escaping (String) -> Void) {
        let cached = currentQimei()
        if isValidQimei(cached) {
            callback(cached)
            return
        }

        guard let service = ensureService() else {
            callback(cached)
            return
        }

        let selector = NSSelectorFromString("getQimeiWithBlock:")
        guard service.responds(to: selector) else {
            callback(cached)
            return
        }

        let block: @convention(block) (Any?) -> Void = { content in
            updateCache(with: content)
            callback(currentQimei())
        }
        _ = service.perform(selector, with: block as AnyObject)
    }

    fileprivate static func didReceiveQimei36(_ qimei36: String?) {
        let normalized = normalize(qimei36)
        guard isValidQimei36(normalized) else { return }

        lock.lock()
        cachedQimei36 = normalized
        lock.unlock()
        NSLog("[Startup][Qimei][iOS] qimei36 changed: %@", normalized)
    }

    private static func ensureService() -> NSObject? {
        lock.lock()
        if let service = qimeiService {
            lock.unlock()
            return service
        }
        lock.unlock()

        let created = createService()

        var shouldScheduleRefresh = false
        lock.lock()
        if qimeiService == nil {
            qimeiService = created
            if created != nil && !didScheduleRefresh {
                didScheduleRefresh = true
                shouldScheduleRefresh = true
            }
        }
        let service = qimeiService
        lock.unlock()

        if shouldScheduleRefresh {
            DispatchQueue.global(qos: .background).asyncAfter(deadline: .now() + 60) {
                refreshFromService()
            }
        }
        return service
    }

    private static func createService() -> NSObject? {
        guard let serviceClass = NSClassFromString("QimeiService") as? NSObject.Type else {
            NSLog("[Startup][Qimei][iOS] QimeiService not linked, skip init.")
            return nil
        }

        let createSelector = NSSelectorFromString("serviceWithAppkey:")
        guard
            serviceClass.responds(to: createSelector),
            let service = serviceClass
                .perform(createSelector, with: beaconAppKey)?
                .takeUnretainedValue() as? NSObject
        else {
            NSLog("[Startup][Qimei][iOS] QimeiService create failed, skip init.")
            return nil
        }

        callSelector(service, "setIsMainService:", with: NSNumber(value: true))
        callSelector(service, "setDelegate:", with: delegateProxy)
        if service.responds(to: NSSelectorFromString("setLogBlock:")) {
            let logBlock: @convention(block) (String) -> Void = { message in
                NSLog("[Startup][Qimei][iOS] %@", message)
            }
            _ = service.perform(NSSelectorFromString("setLogBlock:"), with: logBlock as AnyObject)
        }
        callSelector(service, "start")
        callSelector(service, "enableAudit:", with: NSNumber(value: true))

        return service
    }

    private static func refreshFromService() {
        guard let service = ensureService() else { return }
        let selector = NSSelectorFromString("getQimei")
        guard service.responds(to: selector) else { return }
        let content = service.perform(selector)?.takeUnretainedValue()
        updateCache(with: content)
    }

    private static func updateCache(with content: Any?) {
        guard let object = content as? NSObject else { return }

        let qimei = firstStringValue(
            from: object,
            keys: ["qimeiOld", "qimei16", "qimei"]
        )
        let qimei36 = firstStringValue(
            from: object,
            keys: ["qimeiNew", "qimei36"]
        )

        lock.lock()
        if isValidQimei(qimei) {
            cachedQimei = qimei
        }
        if isValidQimei36(qimei36) {
            cachedQimei36 = qimei36
        }
        lock.unlock()
    }

    private static func firstStringValue(from object: NSObject, keys: [String]) -> String {
        for key in keys {
            let selector = NSSelectorFromString(key)
            guard object.responds(to: selector) else { continue }
            let value = normalize(
                object.perform(selector)?
                    .takeUnretainedValue() as? String
            )
            if !value.isEmpty {
                return value
            }
        }
        return ""
    }

    private static func callSelector(_ target: NSObject, _ selectorName: String, with value: Any? = nil) {
        let selector = NSSelectorFromString(selectorName)
        guard target.responds(to: selector) else { return }
        if let unwrappedValue = value {
            _ = target.perform(selector, with: unwrappedValue)
        } else {
            _ = target.perform(selector)
        }
    }

    private static func normalize(_ value: String?) -> String {
        value?.trimmingCharacters(in: .whitespacesAndNewlines) ?? ""
    }

    private static func isValidQimei(_ value: String) -> Bool {
        !value.isEmpty && value != "A3"
    }

    private static func isValidQimei36(_ value: String) -> Bool {
        !value.isEmpty && value != "A153"
    }
}

private final class QimeiDelegateProxy: NSObject {
    @objc func updateQimei36(_ qimei36: NSString?) {
        QimeiSetup.didReceiveQimei36(qimei36 as String?)
    }
}
