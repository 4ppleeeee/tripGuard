#import "QnLottieDownloader.h"
#import <CommonCrypto/CommonDigest.h>

// ─────────────────────────────────────────────
// 内部常量
// ─────────────────────────────────────────────
static NSString *const kQnLottieCacheDirName = @"QnLottieCache";
/// 缓存的原始文件名前缀（保留原始后缀，便于 lottie-ios 识别格式）
static NSString *const kCachedFileName = @"lottie_raw";

// ─────────────────────────────────────────────
// 辅助：URL → MD5 目录名
// ─────────────────────────────────────────────
static NSString *md5ForString(NSString *str) {
    const char *cStr = [str UTF8String];
    unsigned char digest[CC_MD5_DIGEST_LENGTH];
#pragma clang diagnostic push
#pragma clang diagnostic ignored "-Wdeprecated-declarations"
    CC_MD5(cStr, (CC_LONG)strlen(cStr), digest);
#pragma clang diagnostic pop
    NSMutableString *result = [NSMutableString stringWithCapacity:CC_MD5_DIGEST_LENGTH * 2];
    for (int i = 0; i < CC_MD5_DIGEST_LENGTH; i++) {
        [result appendFormat:@"%02x", digest[i]];
    }
    return result;
}

/// 从 URL 中提取文件后缀（小写），如 "zip"、"lottie"、"json"
static NSString *extensionFromURL(NSString *urlString) {
    NSString *path = [NSURL URLWithString:urlString].path ?: @"";
    return path.pathExtension.lowercaseString ?: @"";
}

// ─────────────────────────────────────────────
// QnLottieDownloader
// ─────────────────────────────────────────────
@interface QnLottieDownloader ()
@property (nonatomic, copy) NSString *cacheRootDir;
@property (nonatomic, strong) NSMutableDictionary<NSString *, NSMutableArray *> *pendingCallbacks;
@property (nonatomic, strong) dispatch_queue_t lockQueue;
@end

@implementation QnLottieDownloader

+ (instancetype)shared {
    static QnLottieDownloader *instance;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        instance = [[QnLottieDownloader alloc] init];
    });
    return instance;
}

- (instancetype)init {
    self = [super init];
    if (self) {
        NSString *cachesDir = NSSearchPathForDirectoriesInDomains(NSCachesDirectory, NSUserDomainMask, YES).firstObject;
        _cacheRootDir = [cachesDir stringByAppendingPathComponent:kQnLottieCacheDirName];
        [NSFileManager.defaultManager createDirectoryAtPath:_cacheRootDir
                                withIntermediateDirectories:YES
                                                 attributes:nil
                                                      error:nil];
        _pendingCallbacks = [NSMutableDictionary dictionary];
        _lockQueue = dispatch_queue_create("com.qn.lottie.downloader.lock", DISPATCH_QUEUE_SERIAL);
    }
    return self;
}

// ─────────────────────────────────────────────
// 公开接口
// ─────────────────────────────────────────────

- (void)downloadLottieWithURL:(NSString *)urlString
                   completion:(QnLottieDownloadCompletion)completion {
    if (urlString.length == 0) {
        dispatch_async(dispatch_get_main_queue(), ^{
            completion(nil, [self errorWithMessage:@"URL 为空"]);
        });
        return;
    }

    // 1. 命中磁盘缓存 → 直接回调
    NSString *cachedPath = [self cachedFilePathForURL:urlString];
    if (cachedPath) {
        dispatch_async(dispatch_get_main_queue(), ^{
            completion(cachedPath, nil);
        });
        return;
    }

    // 2. 合并并发请求
    __block BOOL shouldStartDownload = NO;
    dispatch_sync(_lockQueue, ^{
        NSMutableArray *callbacks = self->_pendingCallbacks[urlString];
        if (callbacks == nil) {
            callbacks = [NSMutableArray array];
            self->_pendingCallbacks[urlString] = callbacks;
            shouldStartDownload = YES;
        }
        [callbacks addObject:[completion copy]];
    });

    if (!shouldStartDownload) return;

    // 3. 发起网络下载
    [self startDownloadURL:urlString];
}

- (void)clearCache {
    dispatch_async(dispatch_get_global_queue(QOS_CLASS_UTILITY, 0), ^{
        [NSFileManager.defaultManager removeItemAtPath:self.cacheRootDir error:nil];
        [NSFileManager.defaultManager createDirectoryAtPath:self.cacheRootDir
                                withIntermediateDirectories:YES
                                                 attributes:nil
                                                      error:nil];
    });
}

// ─────────────────────────────────────────────
// 私有：缓存查找
// ─────────────────────────────────────────────

/// 返回已缓存的原始文件路径（带后缀），不存在则返回 nil
/// - .zip / .lottie / .json：均直接缓存原始文件，由 lottie-ios 自行解析
- (nullable NSString *)cachedFilePathForURL:(NSString *)urlString {
    NSString *cacheDir = [self cacheDirForURL:urlString];
    NSString *ext = extensionFromURL(urlString);
    NSString *fileName = ext.length > 0
        ? [kCachedFileName stringByAppendingFormat:@".%@", ext]
        : kCachedFileName;
    NSString *filePath = [cacheDir stringByAppendingPathComponent:fileName];
    return [NSFileManager.defaultManager fileExistsAtPath:filePath] ? filePath : nil;
}

- (NSString *)cacheDirForURL:(NSString *)urlString {
    return [_cacheRootDir stringByAppendingPathComponent:md5ForString(urlString)];
}

// ─────────────────────────────────────────────
// 私有：下载
// ─────────────────────────────────────────────

- (void)startDownloadURL:(NSString *)urlString {
    NSURL *url = [NSURL URLWithString:urlString];
    if (!url) {
        [self notifyCallbacksForURL:urlString path:nil error:[self errorWithMessage:@"非法 URL"]];
        return;
    }

    NSURLSessionDataTask *task = [NSURLSession.sharedSession
        dataTaskWithURL:url
      completionHandler:^(NSData *data, NSURLResponse *response, NSError *error) {
        if (error || data.length == 0) {
            NSError *err = error ?: [self errorWithMessage:@"下载数据为空"];
            [self notifyCallbacksForURL:urlString path:nil error:err];
            return;
        }
        dispatch_async(dispatch_get_global_queue(QOS_CLASS_USER_INITIATED, 0), ^{
            NSString *resultPath = [self processDownloadedData:data forURL:urlString];
            if (resultPath) {
                [self notifyCallbacksForURL:urlString path:resultPath error:nil];
            } else {
                [self notifyCallbacksForURL:urlString path:nil
                                      error:[self errorWithMessage:@"文件处理失败"]];
            }
        });
    }];
    [task resume];
}

/// 处理下载数据：直接保存原始文件（不解压），由 lottie-ios 自行解析
/// - .zip  → 保存为 lottie_raw.zip，DotLottieFile 内部解压（lottie-ios 支持 zip 格式）
/// - .lottie → 保存为 lottie_raw.lottie，DotLottieFile 内部解压
/// - .json → 保存为 lottie_raw.json，LottieAnimation 直接加载
- (nullable NSString *)processDownloadedData:(NSData *)data forURL:(NSString *)urlString {
    NSString *cacheDir = [self cacheDirForURL:urlString];
    NSFileManager *fm = NSFileManager.defaultManager;

    // 清理旧缓存，确保目录干净
    [fm removeItemAtPath:cacheDir error:nil];
    [fm createDirectoryAtPath:cacheDir withIntermediateDirectories:YES attributes:nil error:nil];

    NSString *ext = extensionFromURL(urlString);
    NSString *fileName = ext.length > 0
        ? [kCachedFileName stringByAppendingFormat:@".%@", ext]
        : kCachedFileName;
    NSString *filePath = [cacheDir stringByAppendingPathComponent:fileName];
    return [data writeToFile:filePath atomically:YES] ? filePath : nil;
}

// ─────────────────────────────────────────────
// 私有：回调分发
// ─────────────────────────────────────────────

- (void)notifyCallbacksForURL:(NSString *)urlString
                         path:(nullable NSString *)path
                        error:(nullable NSError *)error {
    __block NSArray *callbacks;
    dispatch_sync(_lockQueue, ^{
        callbacks = [self->_pendingCallbacks[urlString] copy];
        [self->_pendingCallbacks removeObjectForKey:urlString];
    });
    dispatch_async(dispatch_get_main_queue(), ^{
        for (QnLottieDownloadCompletion cb in callbacks) {
            cb(path, error);
        }
    });
}

- (NSError *)errorWithMessage:(NSString *)message {
    return [NSError errorWithDomain:@"QnLottieDownloader"
                               code:-1
                           userInfo:@{NSLocalizedDescriptionKey: message}];
}

@end
