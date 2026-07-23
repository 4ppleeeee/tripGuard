#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

/// 下载完成回调
/// @param localPath 下载成功时的本地原始文件路径：
///   - .zip 文件：原始 zip 文件路径（由 lottie-ios DotLottieFile 自行解压，无需 SSZipArchive）
///   - .lottie 文件：原始 .lottie 文件路径（由 lottie-ios DotLottieFile 自行解压）
///   - .json 文件：直接可用的 JSON 路径
///   失败时为 nil
/// @param error 失败时的错误信息；成功时为 nil
typedef void (^QnLottieDownloadCompletion)(NSString *_Nullable localPath, NSError *_Nullable error);

/**
 * Lottie 文件下载器（单例）
 *
 * 职责：
 * 1. 根据 URL 下载 .lottie（zip 格式）或 .json 文件
 * 2. 将下载结果缓存到磁盘（以 URL MD5 为目录名）
 * 3. 命中缓存时直接回调本地路径，不重复下载
 * 4. 支持并发请求合并（同一 URL 多次请求只发一次网络请求）
 */
@interface QnLottieDownloader : NSObject

+ (instancetype)shared;

/**
 * 下载（或从缓存读取）Lottie 文件
 *
 * @param urlString  远端 URL，支持 .lottie（zip）和 .json 后缀
 * @param completion 完成回调，在主线程调用
 */
- (void)downloadLottieWithURL:(NSString *)urlString
                   completion:(QnLottieDownloadCompletion)completion;

/**
 * 清除所有磁盘缓存
 */
- (void)clearCache;

@end

NS_ASSUME_NONNULL_END
