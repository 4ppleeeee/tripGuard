/************************************************************
 Copyright (C), 1998-2018, Tencent Tech. Co., Ltd.
 FileName    : SPFileHelper.m
 Author      : jordenwu-Mac
 Version     : 1.0
 Date        : 10-8-20
 Description :
 History     : 10-8-20 初始版本
 ***********************************************************/

#import "SPFileHelper.h"
#import <sys/xattr.h>
#import <sys/stat.h>
#import "SPVcSystemInfo.h"

@implementation SPFileHelper

+ (void)checkAndCreateFileAtPath:(NSString *)filePath iCloud:(BOOL)iCloud {
    if (![SPFileHelper fileExistsWithPath:filePath]) {
        [SPFileHelper createDirectoryWithPath:filePath];
    }
    if (!iCloud) {
        [SPFileHelper addSkipBackupAttributeToURL:[NSURL fileURLWithPath:filePath]];
    }
}

+ (BOOL)addSkipBackupAttributeToURL:(NSURL *)URL {
    BOOL success = NO;

    if (SYSTEM_VERSION_GREATER_THAN_OR_EQUAL_TO(@"5.1")) {
        NSError *error = nil;
        success = [URL setResourceValue:[NSNumber numberWithBool:YES] forKey:NSURLIsExcludedFromBackupKey error:&error];
    } else if (SYSTEM_VERSION_GREATER_THAN_OR_EQUAL_TO(@"5.0.1")) {
        const char *filePath = [[URL path] fileSystemRepresentation];

        const char *attrName = "com.apple.MobileBackup";
        u_int8_t attrValue = 1;

        int result = setxattr(filePath, attrName, &attrValue, sizeof(attrValue), 0, 0);
        success = (result == 0);
    }

    return success;
}

+ (BOOL)fileExistsWithPath:(NSString *)filePath {
    return [[NSFileManager defaultManager] fileExistsAtPath:filePath];
}

+ (BOOL)createFileWithPath:(NSString *)filePath {
    return [[NSFileManager defaultManager] createFileAtPath:filePath contents:nil attributes:nil];
}

+ (BOOL)deleteFileWithPath:(NSString *)filePath {
    return [[NSFileManager defaultManager] removeItemAtPath:filePath error:NULL];
}

+ (BOOL)createDirectoryWithPath:(NSString *)dirPath {
    return [[NSFileManager defaultManager] createDirectoryAtPath:dirPath withIntermediateDirectories:YES attributes:nil error:nil];
}

+ (BOOL)directoryExistsWithPath:(NSString *)dirPath {
    BOOL isDir = YES;
    return [[NSFileManager defaultManager] fileExistsAtPath:dirPath isDirectory:&isDir];
}

+ (BOOL)deleteDirectoryWithPath:(NSString *)dirPath {
    return [[NSFileManager defaultManager] removeItemAtPath:dirPath error:NULL];
}

+ (NSString *)getDocumentsPath {
    NSArray *paths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES);
    return (NSString *)[paths objectAtIndex:0];
}

+ (NSString *)getLibraryPath {
    NSArray *paths = NSSearchPathForDirectoriesInDomains(NSLibraryDirectory, NSUserDomainMask, YES);
    return (NSString *)[paths objectAtIndex:0];
}

+ (NSString *)getAppPath {
    return [[NSBundle mainBundle] bundlePath];
}

+ (NSString *)dataDirectory {
    NSUserDefaults *defaults = [NSUserDefaults standardUserDefaults];

    // 不满足迁移条件，迁移失败，则取Library目录：
    if ((SYSTEM_VERSION_GREATER_THAN_OR_EQUAL_TO(@"5.0") && SYSTEM_VERSION_LESS_THAN(@"5.0.1")) ||
        [defaults objectForKey:@"download_data_transfered"] == nil) {
        return [SPFileHelper getLibraryPath];
    }
    // 取Documents目录：
    return [SPFileHelper getDocumentsPath];
}

+ (NSString *)getTemporaryPath {
    NSString *path = NSTemporaryDirectory();
    return path;
}

+ (unsigned long long)getFileSizeWithPath:(NSString *)filePath {
    NSDictionary *attributes = [[NSFileManager defaultManager] attributesOfItemAtPath:filePath error:nil];
    return (unsigned long long)[[attributes objectForKey:NSFileSize] unsignedLongLongValue];
}

+ (unsigned long long)getFileSystemFreeSize {
    NSDictionary *fattributes = [[NSFileManager defaultManager] attributesOfFileSystemForPath:NSHomeDirectory() error:NULL];
    return [[fattributes objectForKey:NSFileSystemFreeSize] unsignedLongLongValue];
}

+ (void)moveFile:(NSString *)file toNewFile:(NSString *)newFile {
    [[NSFileManager defaultManager] moveItemAtPath:file toPath:newFile error:nil];
}

+ (void)copyFile:(NSString *)file toNewFile:(NSString *)newFile {
    if (file == nil || newFile == nil) {
        return;
    }

    @autoreleasepool {
        [[NSFileManager defaultManager] copyItemAtPath:file toPath:newFile error:nil];
    }
}

+ (NSString *)findFile:(NSString *)dictionary fileName:(NSString *)fileName {
    NSError *error;
    NSArray *pathList = [[NSFileManager defaultManager] contentsOfDirectoryAtPath:dictionary error:&error];
    for (NSString *t_path in pathList) {
        NSString *fullPath = [dictionary stringByAppendingPathComponent:t_path];
        BOOL isDir;
        if ([[NSFileManager defaultManager] fileExistsAtPath:fullPath isDirectory:&isDir]) {
            if (isDir) {
                if ([self findFile:fullPath fileName:fileName] != nil) {
                    return [fullPath stringByAppendingPathComponent:fileName];
                }
            } else if ([t_path isEqualToString:fileName]) {
                return t_path;
            }
        }
    }
    return nil;
}

+ (NSString *)getPlistPath {
    NSString *dataPath = [NSHomeDirectory() stringByAppendingPathComponent:@"qqlive.app/Info.plist"];
    return dataPath;
}

+ (NSString *)getDownloadTempDirectory {
    return [[SPFileHelper dataDirectory] stringByAppendingPathComponent:@"Caches/Temp"];
}
+ (NSString *)getKeyValueFilePath {
    NSString *path = [NSString stringWithFormat:@"%@/Caches/KeyValuePool.data", [SPFileHelper getLibraryPath]];
    return path;
}

+ (NSArray<NSString *> *)getFilenamelistFromDirPath:(NSString *)dirPath {
    NSMutableArray *filenamelist = [NSMutableArray arrayWithCapacity:10];
    NSArray *tmplist = [[NSFileManager defaultManager] contentsOfDirectoryAtPath:dirPath error:nil];

    for (NSString *filename in tmplist) {
        NSString *fullpath = [dirPath stringByAppendingPathComponent:filename];
        if ([SPFileHelper fileExistsWithPath:fullpath]) {
            [filenamelist addObject:fullpath];
        }
    }

    return filenamelist;
}
@end
