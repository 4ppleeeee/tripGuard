#import "WSLogFilePacker.h"
#import "WSMinizipHelper.h"

@implementation WSLogFilePacker

+ (instancetype)sharedInstance {
    static WSLogFilePacker *instance = nil;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        instance = [[WSLogFilePacker alloc] initPrivate];
    });
    return instance;
}

- (instancetype)initPrivate {
    self = [super init];
    return self;
}

#pragma mark - TDLogFilePackerProtocol

- (nullable NSString *)packFiles:(nonnull NSArray<NSString *> *)files
                     withZipName:(nonnull NSString *)zipName {
    NSLog(@"[WSLogFilePacker] packFiles() 调用: files.count=%lu, zipName=%@", (unsigned long)files.count, zipName);
    if (files.count == 0 || zipName.length == 0) {
        NSLog(@"[WSLogFilePacker] 参数无效: files.count=%lu, zipName=%@", (unsigned long)files.count, zipName);
        return nil;
    }

    NSFileManager *fm = [NSFileManager defaultManager];

    // zip 输出目录放在 Library/ 下
    NSString *libraryDir = NSSearchPathForDirectoriesInDomains(NSLibraryDirectory, NSUserDomainMask, YES).firstObject;
    NSString *zipPath = [libraryDir stringByAppendingPathComponent:zipName];
    NSLog(@"[WSLogFilePacker] 原始 zipPath: %@", zipPath);

    // 确保 zipName 以 .zip 结尾
    if (![zipPath.pathExtension.lowercaseString isEqualToString:@"zip"]) {
        zipPath = [zipPath stringByAppendingPathExtension:@"zip"];
        NSLog(@"[WSLogFilePacker] 补充 .zip 后缀: %@", zipPath);
    }

    // 清理旧的 zip 文件
    if ([fm fileExistsAtPath:zipPath]) {
        NSLog(@"[WSLogFilePacker] 清理旧 zip 文件: %@", zipPath);
        [fm removeItemAtPath:zipPath error:nil];
    }

    // 创建临时目录，将待压缩文件拷贝进去
    NSString *tmpDir = [libraryDir stringByAppendingPathComponent:@"WSLogFilePacker_tmp"];
    NSLog(@"[WSLogFilePacker] 临时目录: %@", tmpDir);
    if ([fm fileExistsAtPath:tmpDir]) {
        [fm removeItemAtPath:tmpDir error:nil];
    }

    NSError *createError = nil;
    if (![fm createDirectoryAtPath:tmpDir withIntermediateDirectories:YES attributes:nil error:&createError]) {
        NSLog(@"[WSLogFilePacker] 创建临时目录失败: %@", createError.localizedDescription);
        return nil;
    }

    // 拷贝文件到临时目录（使用安全文件名）
    NSUInteger copiedCount = 0;
    for (NSUInteger i = 0; i < files.count; i++) {
        NSString *filePath = files[i];
        if (![fm fileExistsAtPath:filePath]) {
            NSLog(@"[WSLogFilePacker] 文件不存在，跳过: %@", filePath);
            continue;
        }

        // 使用简单安全的文件名，避免特殊字符导致压缩异常
        NSString *ext = filePath.pathExtension.length > 0 ? filePath.pathExtension : @"log";
        NSString *safeName = [NSString stringWithFormat:@"log_%lu.%@", (unsigned long)i, ext];
        NSString *destPath = [tmpDir stringByAppendingPathComponent:safeName];

        NSError *copyError = nil;
        if ([fm copyItemAtPath:filePath toPath:destPath error:&copyError]) {
            NSDictionary *srcAttrs = [fm attributesOfItemAtPath:filePath error:nil];
            unsigned long long srcSize = [srcAttrs fileSize];
            NSLog(@"[WSLogFilePacker] 拷贝成功: %@ -> %@ (size=%llu)", filePath.lastPathComponent, safeName, srcSize);
            copiedCount++;
        } else {
            NSLog(@"[WSLogFilePacker] 拷贝文件失败: %@, error: %@", filePath, copyError.localizedDescription);
        }
    }

    if (copiedCount == 0) {
        NSLog(@"[WSLogFilePacker] 无有效文件可压缩");
        [fm removeItemAtPath:tmpDir error:nil];
        return nil;
    }

    NSLog(@"[WSLogFilePacker] 已拷贝 %lu 个文件到临时目录，开始压缩 -> %@", (unsigned long)copiedCount, zipPath);

    // 使用 WSMinizipHelper 压缩
    NSLog(@"[WSLogFilePacker] 调用 WSMinizipHelper 压缩: zipPath=%@, srcDir=%@", zipPath, tmpDir);
    BOOL zipOk = [WSMinizipHelper createZipFileAtPath:zipPath withContentsOfDirectory:tmpDir];
    NSLog(@"[WSLogFilePacker] WSMinizipHelper 压缩结果: %@", zipOk ? @"YES" : @"NO");

    // 清理临时目录
    NSLog(@"[WSLogFilePacker] 清理临时目录: %@", tmpDir);
    [fm removeItemAtPath:tmpDir error:nil];

    if (!zipOk) {
        NSLog(@"[WSLogFilePacker] 压缩失败");
        [fm removeItemAtPath:zipPath error:nil];
        return nil;
    }

    NSDictionary *attrs = [fm attributesOfItemAtPath:zipPath error:nil];
    unsigned long long zipSize = [attrs fileSize];
    NSLog(@"[WSLogFilePacker] 压缩完成: %@ (size=%llu)", zipPath, zipSize);

    if (zipSize == 0) {
        NSLog(@"[WSLogFilePacker] 压缩后文件为空");
        [fm removeItemAtPath:zipPath error:nil];
        return nil;
    }

    return zipPath;
}

@end
