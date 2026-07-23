#import "IOSResManager.h"

#import <Photos/Photos.h>
#import <PhotosUI/PhotosUI.h>
#import <UIKit/UIKit.h>
#import <SDWebImage/SDWebImageManager.h>

NS_ASSUME_NONNULL_BEGIN

static NSInteger const kIOSResManagerMaxSelectionCount = 9;

@interface IOSResManagerPhotoPickerDelegate : NSObject <PHPickerViewControllerDelegate>
@property (nonatomic, copy, nullable) void (^pendingCallback)(NSArray<NSString *> *paths);
+ (instancetype)shared;
@end

@interface IOSResManagerLegacyPickerDelegate : NSObject <UIImagePickerControllerDelegate, UINavigationControllerDelegate>
@property (nonatomic, copy, nullable) void (^pendingCallback)(NSArray<NSString *> *paths);
+ (instancetype)shared;
@end

@interface IOSResManager ()
+ (UIViewController * _Nullable)topViewController;
+ (UIViewController *)findTopViewControllerFrom:(UIViewController *)viewController;
+ (NSArray<NSBundle *> *)candidateBundles;
+ (NSString * _Nullable)resolveResourcePathForFileName:(NSString *)fileName;
+ (NSString *)normalizeRelativeFileName:(NSString *)fileName;
+ (NSArray<NSString *> *)candidateRelativePathsForFileName:(NSString *)fileName;
+ (NSURL * _Nullable)URLFromPathOrString:(NSString *)pathOrURL;
+ (void)dispatchBlockOnMain:(dispatch_block_t)block;
+ (NSString *)temporaryImagePath;
+ (NSString *)temporaryVideoPathWithExtension:(NSString *)extension;
+ (void)requestPhotoLibraryAddAuthorization:(void (^)(BOOL granted, NSString * _Nullable message))completion;
+ (void)notifySaveVideoStage:(NSString *)stage
                    message:(NSString * _Nullable)message
               stageCallback:(void (^ _Nullable)(NSString *, NSString * _Nullable, NSString * _Nullable))stageCallback;
+ (void)finishSaveVideoWithSuccess:(BOOL)success
                           message:(NSString * _Nullable)message
                     stageCallback:(void (^ _Nullable)(NSString *, NSString * _Nullable, NSString * _Nullable))stageCallback
                        completion:(void (^ _Nullable)(UmbrellaBoolean *, NSString * _Nullable))completion;
+ (void)saveVideoAtLocalFileURL:(NSURL *)localFileURL
               cleanupAfterSave:(BOOL)cleanupAfterSave
                  stageCallback:(void (^ _Nullable)(NSString *, NSString * _Nullable, NSString * _Nullable))stageCallback
                     completion:(void (^ _Nullable)(UmbrellaBoolean *, NSString * _Nullable))completion;
@end

@implementation IOSResManager

- (NSString *)getAssetJsonFileName:(NSString *)fileName {
    NSString *normalizedFileName = [IOSResManager normalizeRelativeFileName:fileName];
    if (normalizedFileName.length == 0) {
        return @"";
    }

    NSString *resourcePath = [IOSResManager resolveResourcePathForFileName:normalizedFileName];
    if (resourcePath.length == 0) {
        NSLog(@"[IOSResManager] getAssetJson failed, resource not found: %@", normalizedFileName);
        return @"";
    }

    NSError *error = nil;
    NSData *data = [NSData dataWithContentsOfFile:resourcePath options:0 error:&error];
    if (data.length == 0 || error != nil) {
        NSLog(@"[IOSResManager] getAssetJson failed, read file error: %@, error=%@", resourcePath, error);
        return @"";
    }

    NSString *content = [[NSString alloc] initWithData:data encoding:NSUTF8StringEncoding];
    if (content != nil) {
        return content;
    }

    content = [[NSString alloc] initWithData:data encoding:NSASCIIStringEncoding];
    return content ?: @"";
}

- (void)preloadImageUrl:(NSString *)url onSuccess:(void (^ _Nullable)(void))onSuccess onFail:(void (^ _Nullable)(void))onFail {
    NSURL *imageURL = [IOSResManager URLFromPathOrString:url];
    if (imageURL == nil) {
        [IOSResManager dispatchBlockOnMain:^{
            if (onFail != nil) {
                onFail();
            }
        }];
        return;
    }

    [[SDWebImageManager sharedManager] loadImageWithURL:imageURL
                                                options:SDWebImageHandleCookies
                                               progress:nil
                                              completed:^(UIImage * _Nullable image,
                                                          NSData * _Nullable data,
                                                          NSError * _Nullable error,
                                                          SDImageCacheType cacheType,
                                                          BOOL finished,
                                                          NSURL * _Nullable imageURL) {
        [IOSResManager dispatchBlockOnMain:^{
            if (image != nil && error == nil && finished) {
                if (onSuccess != nil) {
                    onSuccess();
                }
            } else if (onFail != nil) {
                onFail();
            }
        }];
    }];
}

- (void)doCopyToClipboardContent:(NSString *)content {
    if (content.length == 0) {
        return;
    }

    [IOSResManager dispatchBlockOnMain:^{
        [UIPasteboard generalPasteboard].string = content;
    }];
}

- (void)saveImageUrl:(NSString *)url metadata:(NSDictionary<NSString *,NSString *> * _Nullable)metadata {
    (void)metadata;
    NSURL *imageURL = [IOSResManager URLFromPathOrString:url];
    if (imageURL == nil) {
        return;
    }

    void (^saveBlock)(UIImage * _Nullable image) = ^(UIImage * _Nullable image) {
        if (image == nil) {
            NSLog(@"[IOSResManager] saveImage failed, image decode error: %@", url);
            return;
        }
        [IOSResManager dispatchBlockOnMain:^{
            UIImageWriteToSavedPhotosAlbum(image, nil, nil, nil);
        }];
    };

    if (imageURL.isFileURL) {
        saveBlock([UIImage imageWithContentsOfFile:imageURL.path]);
        return;
    }

    [[SDWebImageManager sharedManager] loadImageWithURL:imageURL
                                                options:SDWebImageHandleCookies
                                               progress:nil
                                              completed:^(UIImage * _Nullable image,
                                                          NSData * _Nullable data,
                                                          NSError * _Nullable error,
                                                          SDImageCacheType cacheType,
                                                          BOOL finished,
                                                          NSURL * _Nullable imageURL) {
        if (error != nil) {
            NSLog(@"[IOSResManager] saveImage failed, error=%@, url=%@", error, url);
            return;
        }
        saveBlock(image);
    }];
}

- (void)saveVideoUrl:(NSString *)url
              taskId:(NSString * _Nullable)taskId
       stageCallback:(void (^ _Nullable)(NSString *, NSString * _Nullable, NSString * _Nullable))stageCallback
          completion:(void (^ _Nullable)(UmbrellaBoolean *, NSString * _Nullable))completion {
    (void)taskId;
    NSString *trimmedURL = [url stringByTrimmingCharactersInSet:[NSCharacterSet whitespaceAndNewlineCharacterSet]];
    if (trimmedURL.length == 0) {
        [IOSResManager finishSaveVideoWithSuccess:NO
                                         message:@"视频地址为空"
                                   stageCallback:stageCallback
                                      completion:completion];
        return;
    }

    NSURL *videoURL = [IOSResManager URLFromPathOrString:trimmedURL];
    if (videoURL == nil) {
        [IOSResManager finishSaveVideoWithSuccess:NO
                                         message:@"视频地址无效"
                                   stageCallback:stageCallback
                                      completion:completion];
        return;
    }

    if (videoURL.isFileURL) {
        [IOSResManager saveVideoAtLocalFileURL:videoURL
                              cleanupAfterSave:NO
                                 stageCallback:stageCallback
                                    completion:completion];
        return;
    }

    [IOSResManager notifySaveVideoStage:@"downloading" message:nil stageCallback:stageCallback];
    NSURLSessionDownloadTask *downloadTask = [[NSURLSession sharedSession] downloadTaskWithURL:videoURL
                                                                             completionHandler:^(NSURL * _Nullable location,
                                                                                                 NSURLResponse * _Nullable response,
                                                                                                 NSError * _Nullable error) {
        if (error != nil || location == nil) {
            NSString *message = error.localizedDescription ?: @"视频下载失败";
            NSLog(@"[IOSResManager] saveVideo download failed, error=%@, url=%@", error, trimmedURL);
            [IOSResManager finishSaveVideoWithSuccess:NO
                                             message:message
                                       stageCallback:stageCallback
                                          completion:completion];
            return;
        }

        NSString *extension = response.suggestedFilename.pathExtension;
        if (extension.length == 0) {
            extension = videoURL.pathExtension;
        }
        if (extension.length == 0) {
            extension = @"mp4";
        }

        NSString *targetPath = [IOSResManager temporaryVideoPathWithExtension:extension];
        NSURL *targetURL = [NSURL fileURLWithPath:targetPath];
        NSFileManager *fileManager = [NSFileManager defaultManager];
        NSError *moveError = nil;
        [fileManager removeItemAtURL:targetURL error:nil];
        if (![fileManager moveItemAtURL:location toURL:targetURL error:&moveError]) {
            NSLog(@"[IOSResManager] saveVideo move temp file failed, error=%@, url=%@", moveError, trimmedURL);
            [IOSResManager finishSaveVideoWithSuccess:NO
                                             message:moveError.localizedDescription ?: @"视频文件处理失败"
                                       stageCallback:stageCallback
                                          completion:completion];
            return;
        }

        [IOSResManager saveVideoAtLocalFileURL:targetURL
                              cleanupAfterSave:YES
                                 stageCallback:stageCallback
                                    completion:completion];
    }];
    [downloadTask resume];
}

- (void)selectImageContext:(id<UmbrellaIKmmContext> _Nullable)context callback:(void (^)(NSArray<NSString *> *))callback {
    (void)context;
    [IOSResManager dispatchBlockOnMain:^{
        UIViewController *topViewController = [IOSResManager topViewController];
        if (topViewController == nil) {
            callback(@[]);
            return;
        }

        if (@available(iOS 14.0, *)) {
            PHPickerConfiguration *configuration = [[PHPickerConfiguration alloc] init];
            configuration.selectionLimit = kIOSResManagerMaxSelectionCount;
            configuration.filter = [PHPickerFilter imagesFilter];

            PHPickerViewController *picker = [[PHPickerViewController alloc] initWithConfiguration:configuration];
            IOSResManagerPhotoPickerDelegate *delegate = [IOSResManagerPhotoPickerDelegate shared];
            delegate.pendingCallback = callback;
            picker.delegate = delegate;
            [topViewController presentViewController:picker animated:YES completion:nil];
            return;
        }

        UIImagePickerController *picker = [[UIImagePickerController alloc] init];
        picker.sourceType = UIImagePickerControllerSourceTypePhotoLibrary;
        picker.allowsEditing = NO;
        IOSResManagerLegacyPickerDelegate *delegate = [IOSResManagerLegacyPickerDelegate shared];
        delegate.pendingCallback = callback;
        picker.delegate = delegate;
        [topViewController presentViewController:picker animated:YES completion:nil];
    }];
}

- (void)getPaletteColorImageUrl:(NSString *)imageUrl
                          param:(UmbrellaPaletteParam *)param
                   defaultColor:(UmbrellaInt * _Nullable)defaultColor
                          onGot:(void (^)(UmbrellaInt *))onGot {
    (void)imageUrl;
    (void)param;
    if (defaultColor != nil) {
        onGot(defaultColor);
    }
}

- (void)preloadLottieToMemoryContext:(id<UmbrellaIKmmContext> _Nullable)context
                                 url:(NSString *)url
                              status:(NSString *)status
                               isDay:(BOOL)isDay {
    (void)context;
    (void)url;
    (void)status;
    (void)isDay;
}

- (void)preloadAlphaVideoUrl:(NSString *)url onSuccess:(void (^ _Nullable)(void))onSuccess onFail:(void (^ _Nullable)(void))onFail {
    (void)url;
    if (onFail != nil) {
        [IOSResManager dispatchBlockOnMain:onFail];
    }
}

+ (UIViewController * _Nullable)topViewController {
    for (UIScene *scene in [UIApplication sharedApplication].connectedScenes) {
        if (![scene isKindOfClass:[UIWindowScene class]]) {
            continue;
        }
        UIWindowScene *windowScene = (UIWindowScene *)scene;
        if (windowScene.activationState != UISceneActivationStateForegroundActive) {
            continue;
        }
        for (UIWindow *window in windowScene.windows) {
            if (!window.isKeyWindow || window.rootViewController == nil) {
                continue;
            }
            return [self findTopViewControllerFrom:window.rootViewController];
        }
    }
    return nil;
}

+ (UIViewController *)findTopViewControllerFrom:(UIViewController *)viewController {
    if (viewController.presentedViewController != nil) {
        return [self findTopViewControllerFrom:viewController.presentedViewController];
    }
    if ([viewController isKindOfClass:[UINavigationController class]]) {
        UIViewController *topViewController = ((UINavigationController *)viewController).topViewController;
        if (topViewController != nil) {
            return [self findTopViewControllerFrom:topViewController];
        }
    }
    if ([viewController isKindOfClass:[UITabBarController class]]) {
        UIViewController *selectedViewController = ((UITabBarController *)viewController).selectedViewController;
        if (selectedViewController != nil) {
            return [self findTopViewControllerFrom:selectedViewController];
        }
    }
    return viewController;
}

+ (NSArray<NSBundle *> *)candidateBundles {
    NSMutableOrderedSet<NSBundle *> *bundles = [NSMutableOrderedSet orderedSet];
    if (NSBundle.mainBundle != nil) {
        [bundles addObject:NSBundle.mainBundle];
    }
    for (NSBundle *bundle in NSBundle.allBundles) {
        if (bundle != nil) {
            [bundles addObject:bundle];
        }
    }
    for (NSBundle *bundle in NSBundle.allFrameworks) {
        if (bundle != nil) {
            [bundles addObject:bundle];
        }
    }
    return bundles.array;
}

+ (NSString * _Nullable)resolveResourcePathForFileName:(NSString *)fileName {
    if (fileName.length == 0) {
        return nil;
    }
    if ([[NSFileManager defaultManager] fileExistsAtPath:fileName]) {
        return fileName;
    }

    NSArray<NSString *> *candidatePaths = [self candidateRelativePathsForFileName:fileName];
    for (NSBundle *bundle in [self candidateBundles]) {
        NSString *resourcePath = bundle.resourcePath;
        for (NSString *candidate in candidatePaths) {
            NSString *bundleResolvedPath = [bundle pathForResource:candidate ofType:nil];
            if (bundleResolvedPath.length > 0) {
                return bundleResolvedPath;
            }
            if (resourcePath.length > 0) {
                NSString *fullPath = [resourcePath stringByAppendingPathComponent:candidate];
                if ([[NSFileManager defaultManager] fileExistsAtPath:fullPath]) {
                    return fullPath;
                }
            }
        }
    }
    return nil;
}

+ (NSString *)normalizeRelativeFileName:(NSString *)fileName {
    NSString *normalized = [fileName stringByTrimmingCharactersInSet:[NSCharacterSet whitespaceAndNewlineCharacterSet]];
    while ([normalized hasPrefix:@"/"]) {
        normalized = [normalized substringFromIndex:1];
    }
    return normalized;
}

+ (NSArray<NSString *> *)candidateRelativePathsForFileName:(NSString *)fileName {
    NSString *normalized = [self normalizeRelativeFileName:fileName];
    if (normalized.length == 0) {
        return @[];
    }
    if (normalized.pathExtension.length > 0) {
        return @[normalized];
    }
    return @[
        normalized,
        [normalized stringByAppendingPathExtension:@"json"]
    ];
}

+ (NSURL * _Nullable)URLFromPathOrString:(NSString *)pathOrURL {
    NSString *normalized = [pathOrURL stringByTrimmingCharactersInSet:[NSCharacterSet whitespaceAndNewlineCharacterSet]];
    if (normalized.length == 0) {
        return nil;
    }
    if ([[NSFileManager defaultManager] fileExistsAtPath:normalized]) {
        return [NSURL fileURLWithPath:normalized];
    }
    NSURL *url = [NSURL URLWithString:normalized];
    if (url != nil && url.scheme.length > 0) {
        return url;
    }
    return nil;
}

+ (void)dispatchBlockOnMain:(dispatch_block_t)block {
    if (block == nil) {
        return;
    }
    if ([NSThread isMainThread]) {
        block();
    } else {
        dispatch_async(dispatch_get_main_queue(), block);
    }
}

+ (NSString *)temporaryImagePath {
    NSString *fileName = [NSString stringWithFormat:@"report_img_%lld_%@.jpg",
                          (long long)([[NSDate date] timeIntervalSince1970] * 1000),
                          NSUUID.UUID.UUIDString.lowercaseString];
    return [NSTemporaryDirectory() stringByAppendingPathComponent:fileName];
}

+ (NSString *)temporaryVideoPathWithExtension:(NSString *)extension {
    NSString *normalizedExtension = [extension stringByTrimmingCharactersInSet:[NSCharacterSet whitespaceAndNewlineCharacterSet]];
    if (normalizedExtension.length == 0) {
        normalizedExtension = @"mp4";
    }
    NSString *fileName = [NSString stringWithFormat:@"report_video_%lld_%@.%@",
                          (long long)([[NSDate date] timeIntervalSince1970] * 1000),
                          NSUUID.UUID.UUIDString.lowercaseString,
                          normalizedExtension.lowercaseString];
    return [NSTemporaryDirectory() stringByAppendingPathComponent:fileName];
}

+ (void)requestPhotoLibraryAddAuthorization:(void (^)(BOOL granted, NSString * _Nullable message))completion {
    if (completion == nil) {
        return;
    }

    void (^handleStatus)(PHAuthorizationStatus) = ^(PHAuthorizationStatus status) {
        BOOL granted = (status == PHAuthorizationStatusAuthorized || status == PHAuthorizationStatusLimited);
        NSString *message = granted ? nil : @"没有相册写入权限";
        [self dispatchBlockOnMain:^{
            completion(granted, message);
        }];
    };

    if (@available(iOS 14.0, *)) {
        [PHPhotoLibrary requestAuthorizationForAccessLevel:PHAccessLevelAddOnly handler:^(PHAuthorizationStatus status) {
            handleStatus(status);
        }];
        return;
    }

    [PHPhotoLibrary requestAuthorization:^(PHAuthorizationStatus status) {
        handleStatus(status);
    }];
}

+ (void)notifySaveVideoStage:(NSString *)stage
                     message:(NSString * _Nullable)message
               stageCallback:(void (^ _Nullable)(NSString *, NSString * _Nullable, NSString * _Nullable))stageCallback {
    if (stageCallback == nil) {
        return;
    }
    [self dispatchBlockOnMain:^{
        stageCallback(stage, nil, message);
    }];
}

+ (void)finishSaveVideoWithSuccess:(BOOL)success
                           message:(NSString * _Nullable)message
                     stageCallback:(void (^ _Nullable)(NSString *, NSString * _Nullable, NSString * _Nullable))stageCallback
                        completion:(void (^ _Nullable)(UmbrellaBoolean *, NSString * _Nullable))completion {
    [self notifySaveVideoStage:(success ? @"success" : @"failed")
                       message:message
                 stageCallback:stageCallback];
    if (completion == nil) {
        return;
    }
    [self dispatchBlockOnMain:^{
        completion([UmbrellaBoolean numberWithBool:success], message);
    }];
}

+ (void)saveVideoAtLocalFileURL:(NSURL *)localFileURL
               cleanupAfterSave:(BOOL)cleanupAfterSave
                  stageCallback:(void (^ _Nullable)(NSString *, NSString * _Nullable, NSString * _Nullable))stageCallback
                     completion:(void (^ _Nullable)(UmbrellaBoolean *, NSString * _Nullable))completion {
    if (localFileURL == nil || !localFileURL.isFileURL) {
        [self finishSaveVideoWithSuccess:NO
                                 message:@"视频文件无效"
                           stageCallback:stageCallback
                              completion:completion];
        return;
    }

    NSString *localPath = localFileURL.path;
    BOOL fileExists = [[NSFileManager defaultManager] fileExistsAtPath:localPath];
    if (localPath.length == 0 || !fileExists) {
        [self finishSaveVideoWithSuccess:NO
                                 message:@"视频文件不存在"
                           stageCallback:stageCallback
                              completion:completion];
        return;
    }

    [self notifySaveVideoStage:@"saving" message:nil stageCallback:stageCallback];
    [self requestPhotoLibraryAddAuthorization:^(BOOL granted, NSString * _Nullable message) {
        if (!granted) {
            if (cleanupAfterSave) {
                [[NSFileManager defaultManager] removeItemAtURL:localFileURL error:nil];
            }
            [self finishSaveVideoWithSuccess:NO
                                     message:message ?: @"没有相册写入权限"
                               stageCallback:stageCallback
                                  completion:completion];
            return;
        }

        [[PHPhotoLibrary sharedPhotoLibrary] performChanges:^{
            [PHAssetChangeRequest creationRequestForAssetFromVideoAtFileURL:localFileURL];
        } completionHandler:^(BOOL success, NSError * _Nullable error) {
            if (cleanupAfterSave) {
                [[NSFileManager defaultManager] removeItemAtURL:localFileURL error:nil];
            }
            NSString *resultMessage = nil;
            if (!success) {
                resultMessage = error.localizedDescription ?: @"保存视频失败";
                NSLog(@"[IOSResManager] saveVideo to album failed, error=%@, path=%@", error, localPath);
            }
            [self finishSaveVideoWithSuccess:success
                                     message:resultMessage
                               stageCallback:stageCallback
                                  completion:completion];
        }];
    }];
}

@end

@implementation IOSResManagerPhotoPickerDelegate

+ (instancetype)shared {
    static IOSResManagerPhotoPickerDelegate *delegate;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        delegate = [[IOSResManagerPhotoPickerDelegate alloc] init];
    });
    return delegate;
}

- (void)picker:(PHPickerViewController *)picker didFinishPicking:(NSArray<PHPickerResult *> *)results API_AVAILABLE(ios(14.0)) {
    [picker dismissViewControllerAnimated:YES completion:nil];

    void (^callback)(NSArray<NSString *> *) = self.pendingCallback;
    self.pendingCallback = nil;
    if (callback == nil) {
        return;
    }
    if (results.count == 0) {
        callback(@[]);
        return;
    }

    dispatch_group_t group = dispatch_group_create();
    NSMutableArray<NSString *> *paths = [NSMutableArray array];

    for (PHPickerResult *result in results) {
        NSItemProvider *itemProvider = result.itemProvider;
        if (![itemProvider canLoadObjectOfClass:[UIImage class]]) {
            continue;
        }

        dispatch_group_enter(group);
        [itemProvider loadObjectOfClass:[UIImage class] completionHandler:^(id<NSItemProviderReading> _Nullable object, NSError * _Nullable error) {
            @try {
                if (error == nil && [object isKindOfClass:[UIImage class]]) {
                    UIImage *image = (UIImage *)object;
                    NSData *data = UIImageJPEGRepresentation(image, 0.9);
                    if (data.length > 0) {
                        NSString *path = [IOSResManager temporaryImagePath];
                        NSError *writeError = nil;
                        if ([data writeToFile:path options:NSDataWritingAtomic error:&writeError]) {
                            @synchronized (paths) {
                                [paths addObject:path];
                            }
                        } else {
                            NSLog(@"[IOSResManager] selectImage write temp file failed: %@", writeError);
                        }
                    }
                }
            } @finally {
                dispatch_group_leave(group);
            }
        }];
    }

    dispatch_group_notify(group, dispatch_get_main_queue(), ^{
        callback([paths copy]);
    });
}

@end

@implementation IOSResManagerLegacyPickerDelegate

+ (instancetype)shared {
    static IOSResManagerLegacyPickerDelegate *delegate;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        delegate = [[IOSResManagerLegacyPickerDelegate alloc] init];
    });
    return delegate;
}

- (void)imagePickerController:(UIImagePickerController *)picker didFinishPickingMediaWithInfo:(NSDictionary<UIImagePickerControllerInfoKey,id> *)info {
    [picker dismissViewControllerAnimated:YES completion:nil];

    void (^callback)(NSArray<NSString *> *) = self.pendingCallback;
    self.pendingCallback = nil;
    if (callback == nil) {
        return;
    }

    UIImage *image = info[UIImagePickerControllerOriginalImage];
    NSData *data = UIImageJPEGRepresentation(image, 0.9);
    if (data.length == 0) {
        callback(@[]);
        return;
    }

    NSString *path = [IOSResManager temporaryImagePath];
    NSError *error = nil;
    if ([data writeToFile:path options:NSDataWritingAtomic error:&error]) {
        callback(@[path]);
    } else {
        NSLog(@"[IOSResManager] legacy selectImage write temp file failed: %@", error);
        callback(@[]);
    }
}

- (void)imagePickerControllerDidCancel:(UIImagePickerController *)picker {
    [picker dismissViewControllerAnimated:YES completion:nil];
    void (^callback)(NSArray<NSString *> *) = self.pendingCallback;
    self.pendingCallback = nil;
    if (callback != nil) {
        callback(@[]);
    }
}

@end

NS_ASSUME_NONNULL_END
