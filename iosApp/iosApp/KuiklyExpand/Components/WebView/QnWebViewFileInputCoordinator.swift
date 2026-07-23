import AVFoundation
import Foundation
import PhotosUI
import UIKit
import UniformTypeIdentifiers

@objc(QnWebViewFileInputCoordinator)
public final class QnWebViewFileInputCoordinator: NSObject {
    private var completionHandler: (([URL]?) -> Void)?
    private var allowsMultipleSelection = false

    @objc(openPanelFromViewController:allowsMultipleSelection:completionHandler:)
    public func openPanel(
        from viewController: UIViewController,
        allowsMultipleSelection: Bool,
        completionHandler: @escaping ([URL]?) -> Void
    ) {
        cancel()
        self.completionHandler = completionHandler
        self.allowsMultipleSelection = allowsMultipleSelection
        presentSourceMenu(from: visiblePresenter(from: viewController))
    }

    @objc public func cancel() {
        finish(with: nil)
    }

    private func presentSourceMenu(from viewController: UIViewController) {
        let alert = UIAlertController(title: nil, message: nil, preferredStyle: .actionSheet)
        if UIImagePickerController.isSourceTypeAvailable(.camera) {
            alert.addAction(UIAlertAction(title: "拍照", style: .default) { [weak self, weak viewController] _ in
                guard let self, let viewController else {
                    self?.finish(with: nil)
                    return
                }
                self.presentCamera(from: viewController)
            })
        }
        alert.addAction(UIAlertAction(title: "从相册选择", style: .default) { [weak self, weak viewController] _ in
            guard let self, let viewController else {
                self?.finish(with: nil)
                return
            }
            self.presentPhotoPicker(from: viewController)
        })
        alert.addAction(UIAlertAction(title: "选择文件", style: .default) { [weak self, weak viewController] _ in
            guard let self, let viewController else {
                self?.finish(with: nil)
                return
            }
            self.presentDocumentPicker(from: viewController)
        })
        alert.addAction(UIAlertAction(title: "取消", style: .cancel) { [weak self] _ in
            self?.finish(with: nil)
        })
        if let popover = alert.popoverPresentationController {
            popover.sourceView = viewController.view
            popover.sourceRect = CGRect(
                x: viewController.view.bounds.midX,
                y: viewController.view.bounds.midY,
                width: 1,
                height: 1
            )
            popover.permittedArrowDirections = []
        }
        viewController.present(alert, animated: true)
    }

    private func presentPhotoPicker(from viewController: UIViewController) {
        var config = PHPickerConfiguration(photoLibrary: .shared())
        config.selectionLimit = allowsMultipleSelection ? 0 : 1
        config.filter = .any(of: [.images, .videos])
        let picker = PHPickerViewController(configuration: config)
        picker.delegate = self
        viewController.present(picker, animated: true)
    }

    private func presentDocumentPicker(from viewController: UIViewController) {
        let picker = UIDocumentPickerViewController(forOpeningContentTypes: [.item], asCopy: true)
        picker.allowsMultipleSelection = allowsMultipleSelection
        picker.delegate = self
        viewController.present(picker, animated: true)
    }

    private func presentCamera(from viewController: UIViewController) {
        let picker = UIImagePickerController()
        picker.sourceType = .camera
        picker.mediaTypes = [UTType.image.identifier]
        picker.delegate = self
        viewController.present(picker, animated: true)
    }

    private func visiblePresenter(from viewController: UIViewController) -> UIViewController {
        var current = viewController
        while let presented = current.presentedViewController {
            current = presented
        }
        return current
    }

    private func finish(with urls: [URL]?) {
        let completion = completionHandler
        completionHandler = nil
        completion?(urls)
    }
}

extension QnWebViewFileInputCoordinator: PHPickerViewControllerDelegate {
    public func picker(_ picker: PHPickerViewController, didFinishPicking results: [PHPickerResult]) {
        picker.dismiss(animated: true)
        guard !results.isEmpty else {
            finish(with: nil)
            return
        }

        var output: [URL] = []
        let group = DispatchGroup()
        for result in results {
            group.enter()
            copyItemProviderToTemporaryFile(result.itemProvider) { url in
                if let url {
                    output.append(url)
                }
                group.leave()
            }
        }
        group.notify(queue: .main) { [weak self] in
            self?.finish(with: output.isEmpty ? nil : output)
        }
    }
}

extension QnWebViewFileInputCoordinator: UIDocumentPickerDelegate {
    public func documentPicker(_ controller: UIDocumentPickerViewController, didPickDocumentsAt urls: [URL]) {
        finish(with: urls.isEmpty ? nil : urls)
    }

    public func documentPickerWasCancelled(_ controller: UIDocumentPickerViewController) {
        finish(with: nil)
    }
}

extension QnWebViewFileInputCoordinator: UIImagePickerControllerDelegate, UINavigationControllerDelegate {
    public func imagePickerController(
        _ picker: UIImagePickerController,
        didFinishPickingMediaWithInfo info: [UIImagePickerController.InfoKey: Any]
    ) {
        picker.dismiss(animated: true)
        if let mediaURL = info[.mediaURL] as? URL {
            finish(with: [mediaURL])
            return
        }
        let image = (info[.editedImage] as? UIImage) ?? (info[.originalImage] as? UIImage)
        guard let image, let url = writeImageToTemporaryFile(image) else {
            finish(with: nil)
            return
        }
        finish(with: [url])
    }

    public func imagePickerControllerDidCancel(_ picker: UIImagePickerController) {
        picker.dismiss(animated: true)
        finish(with: nil)
    }
}

private func copyItemProviderToTemporaryFile(_ provider: NSItemProvider, completion: @escaping (URL?) -> Void) {
    let preferredTypes = [
        UTType.image.identifier,
        UTType.movie.identifier,
        UTType.data.identifier,
    ]
    guard let typeIdentifier = preferredTypes.first(where: { provider.hasItemConformingToTypeIdentifier($0) }) else {
        DispatchQueue.main.async { completion(nil) }
        return
    }
    provider.loadFileRepresentation(forTypeIdentifier: typeIdentifier) { sourceURL, _ in
        if let sourceURL, let copiedURL = copyTemporaryFile(from: sourceURL, fallbackExtension: fallbackExtension(for: typeIdentifier)) {
            DispatchQueue.main.async { completion(copiedURL) }
            return
        }
        if typeIdentifier == UTType.image.identifier {
            provider.loadObject(ofClass: UIImage.self) { object, _ in
                let image = object as? UIImage
                DispatchQueue.main.async {
                    completion(image.flatMap(writeImageToTemporaryFile))
                }
            }
            return
        }
        DispatchQueue.main.async { completion(nil) }
    }
}

private func copyTemporaryFile(from sourceURL: URL, fallbackExtension: String) -> URL? {
    let ext = sourceURL.pathExtension.isEmpty ? fallbackExtension : sourceURL.pathExtension
    let destinationURL = FileManager.default.temporaryDirectory
        .appendingPathComponent("qn_webview_upload_\(UUID().uuidString)")
        .appendingPathExtension(ext)
    do {
        if FileManager.default.fileExists(atPath: destinationURL.path) {
            try FileManager.default.removeItem(at: destinationURL)
        }
        try FileManager.default.copyItem(at: sourceURL, to: destinationURL)
        return destinationURL
    } catch {
        NSLog("[QnWebViewFileInput] copy file failed: %@", error.localizedDescription)
        return nil
    }
}

private func writeImageToTemporaryFile(_ image: UIImage) -> URL? {
    guard let data = image.jpegData(compressionQuality: 0.92) else {
        return nil
    }
    let url = FileManager.default.temporaryDirectory
        .appendingPathComponent("qn_webview_upload_\(UUID().uuidString)")
        .appendingPathExtension("jpg")
    do {
        try data.write(to: url, options: .atomic)
        return url
    } catch {
        NSLog("[QnWebViewFileInput] write image failed: %@", error.localizedDescription)
        return nil
    }
}

private func fallbackExtension(for typeIdentifier: String) -> String {
    if typeIdentifier == UTType.movie.identifier {
        return "mov"
    }
    if typeIdentifier == UTType.image.identifier {
        return "jpg"
    }
    return "dat"
}

@objc(QnWebViewMediaPermissionCoordinator)
public final class QnWebViewMediaPermissionCoordinator: NSObject {
    @objc(requestPermissionForVideo:audio:completion:)
    public static func requestPermission(
        video: Bool,
        audio: Bool,
        completion: @escaping (Bool) -> Void
    ) {
        var mediaTypes: [AVMediaType] = []
        if video {
            mediaTypes.append(.video)
        }
        if audio {
            mediaTypes.append(.audio)
        }
        guard !mediaTypes.isEmpty else {
            completion(false)
            return
        }
        requestPermission(mediaTypes: mediaTypes, index: 0, completion: completion)
    }

    private static func requestPermission(
        mediaTypes: [AVMediaType],
        index: Int,
        completion: @escaping (Bool) -> Void
    ) {
        if index >= mediaTypes.count {
            DispatchQueue.main.async { completion(true) }
            return
        }
        let mediaType = mediaTypes[index]
        switch AVCaptureDevice.authorizationStatus(for: mediaType) {
        case .authorized:
            requestPermission(mediaTypes: mediaTypes, index: index + 1, completion: completion)
        case .notDetermined:
            AVCaptureDevice.requestAccess(for: mediaType) { granted in
                if granted {
                    requestPermission(mediaTypes: mediaTypes, index: index + 1, completion: completion)
                } else {
                    DispatchQueue.main.async { completion(false) }
                }
            }
        case .denied, .restricted:
            DispatchQueue.main.async { completion(false) }
        @unknown default:
            DispatchQueue.main.async { completion(false) }
        }
    }
}
