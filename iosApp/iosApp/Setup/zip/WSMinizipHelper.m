#import "WSMinizipHelper.h"
#import <MiniZip/LVMiniZipArchive.h>

@implementation WSMinizipHelper

+ (BOOL)createZipFileAtPath:(NSString *)zipPath
    withContentsOfDirectory:(NSString *)directoryPath {
    NSLog(@"[WSMinizipHelper] createZipFileAtPath() 调用: zipPath=%@, dir=%@", zipPath, directoryPath);
    if (!zipPath || !directoryPath) {
        NSLog(@"[WSMinizipHelper] 参数为空: zipPath=%@, dir=%@", zipPath, directoryPath);
        return NO;
    }

    NSFileManager *fm = [NSFileManager defaultManager];
    BOOL isDir = NO;
    if (![fm fileExistsAtPath:directoryPath isDirectory:&isDir] || !isDir) {
        NSLog(@"[WSMinizipHelper] 目录不存在: %@", directoryPath);
        return NO;
    }

    NSArray<NSString *> *items = [fm contentsOfDirectoryAtPath:directoryPath error:nil];
    if (items.count == 0) {
        NSLog(@"[WSMinizipHelper] 目录为空: %@", directoryPath);
        return NO;
    }
    NSLog(@"[WSMinizipHelper] 目录下共 %lu 个条目", (unsigned long)items.count);

    // 使用 LVMiniZipArchive 创建 zip 文件
    LVMiniZipArchive *zipArchive = [[LVMiniZipArchive alloc] init];
    if (![zipArchive CreateZipFile2:zipPath]) {
        NSLog(@"[WSMinizipHelper] LVMiniZipArchive CreateZipFile2 失败: %@", zipPath);
        return NO;
    }

    BOOL allSuccess = YES;

    for (NSString *item in items) {
        NSString *fullPath = [directoryPath stringByAppendingPathComponent:item];

        // 跳过子目录
        BOOL itemIsDir = NO;
        if (![fm fileExistsAtPath:fullPath isDirectory:&itemIsDir] || itemIsDir) {
            continue;
        }

        // 添加文件到 zip，newname 使用文件名（不含目录路径）
        NSDictionary *fileAttrs = [fm attributesOfItemAtPath:fullPath error:nil];
        unsigned long long fileSize = [fileAttrs fileSize];
        NSLog(@"[WSMinizipHelper] 正在添加: %@ (size=%llu)", item, fileSize);
        BOOL addOk = [zipArchive addFileToZip:fullPath newname:item];
        if (!addOk) {
            NSLog(@"[WSMinizipHelper] addFileToZip 失败: %@", item);
            allSuccess = NO;
        } else {
            NSLog(@"[WSMinizipHelper] 已添加文件: %@", item);
        }
    }

    BOOL closeOk = [zipArchive CloseZipFile2];
    if (!closeOk) {
        NSLog(@"[WSMinizipHelper] CloseZipFile2 失败");
        allSuccess = NO;
    }

    // 验证 zip 文件大小
    NSDictionary *zipAttrs = [fm attributesOfItemAtPath:zipPath error:nil];
    unsigned long long zipSize = [zipAttrs fileSize];
    NSLog(@"[WSMinizipHelper] 压缩完成: %@, size=%llu, success=%d", zipPath, zipSize, allSuccess);

    return allSuccess && (zipSize > 0);
}

+ (BOOL)createZipFileAtPath:(NSString *)zipPath
       withSubdirectoryFiles:(NSDictionary<NSString *, NSArray<NSString *> *> *)subdirFiles {
    NSLog(@"[WSMinizipHelper] createZipFileAtPath:withSubdirectoryFiles: zipPath=%@, groups=%lu",
          zipPath, (unsigned long)subdirFiles.count);
    if (!zipPath || subdirFiles.count == 0) {
        NSLog(@"[WSMinizipHelper] 参数为空");
        return NO;
    }

    NSFileManager *fm = [NSFileManager defaultManager];

    LVMiniZipArchive *zipArchive = [[LVMiniZipArchive alloc] init];
    if (![zipArchive CreateZipFile2:zipPath]) {
        NSLog(@"[WSMinizipHelper] CreateZipFile2 失败: %@", zipPath);
        return NO;
    }

    BOOL allSuccess = YES;
    NSUInteger totalAdded = 0;

    for (NSString *subDir in subdirFiles) {
        NSArray<NSString *> *files = subdirFiles[subDir];
        if (![files isKindOfClass:[NSArray class]] || files.count == 0) {
            continue;
        }
        for (NSString *fullPath in files) {
            BOOL isDir = NO;
            if (![fm fileExistsAtPath:fullPath isDirectory:&isDir] || isDir) {
                NSLog(@"[WSMinizipHelper] 跳过不存在或目录项: %@", fullPath);
                continue;
            }
            NSDictionary *attrs = [fm attributesOfItemAtPath:fullPath error:nil];
            unsigned long long fileSize = [attrs fileSize];
            if (fileSize == 0) {
                continue;
            }
            NSString *entryName = [subDir stringByAppendingPathComponent:fullPath.lastPathComponent];
            NSLog(@"[WSMinizipHelper] 正在添加: %@ -> %@ (size=%llu)",
                  fullPath.lastPathComponent, entryName, fileSize);
            BOOL addOk = [zipArchive addFileToZip:fullPath newname:entryName];
            if (!addOk) {
                NSLog(@"[WSMinizipHelper] addFileToZip 失败: %@", entryName);
                allSuccess = NO;
            } else {
                totalAdded++;
            }
        }
    }

    BOOL closeOk = [zipArchive CloseZipFile2];
    if (!closeOk) {
        NSLog(@"[WSMinizipHelper] CloseZipFile2 失败");
        allSuccess = NO;
    }

    NSDictionary *zipAttrs = [fm attributesOfItemAtPath:zipPath error:nil];
    unsigned long long zipSize = [zipAttrs fileSize];
    NSLog(@"[WSMinizipHelper] 分层级压缩完成: %@, size=%llu, entries=%lu, success=%d",
          zipPath, zipSize, (unsigned long)totalAdded, allSuccess);

    return allSuccess && (zipSize > 0) && (totalAdded > 0);
}

+ (BOOL)unzipFileAtPath:(NSString *)zipPath toDestination:(NSString *)destinationPath {
    if (!zipPath || !destinationPath) {
        NSLog(@"[WSMinizipHelper] unzip: 参数为空");
        return NO;
    }

    NSFileManager *fm = [NSFileManager defaultManager];
    [fm createDirectoryAtPath:destinationPath withIntermediateDirectories:YES attributes:nil error:nil];

    LVMiniZipArchive *archive = [[LVMiniZipArchive alloc] init];
    if (![archive UnzipOpenFile:zipPath]) {
        NSLog(@"[WSMinizipHelper] unzip: LVMiniZipArchive 打开 zip 失败: %@", zipPath);
        return NO;
    }

    BOOL success = [archive UnzipFileTo:destinationPath overWrite:YES];
    [archive UnzipCloseFile];

    NSLog(@"[WSMinizipHelper] unzip 完成: success=%d, dest=%@", success, destinationPath);
    return success;
}

@end
