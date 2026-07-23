/************************************************************
 Copyright (C), 1998-2018, Tencent Tech. Co., Ltd.
 FileName    : SPFileHelper.h
 Author      : jordenwu-Mac
 Version     : 1.0
 Date        : 10-8-20
 Description : 文件创建、删除等工具方法
 History     : 10-8-20 初始版本
 ***********************************************************/

/**
 * 文件管理的工具类
 */
@interface SPFileHelper : NSObject {
}

/**
 * 如果文件不存在，则创建之，然后设置iCloud属性
 */
+ (void)checkAndCreateFileAtPath:(NSString *)filePath iCloud:(BOOL)iCloud;

/**
 * 对文件夹或文件设置iCloud的 Do Not Backup属性
 */
+ (BOOL)addSkipBackupAttributeToURL:(NSURL *)URL;

/**
 * 文件是否存在
 */
+ (BOOL)fileExistsWithPath:(NSString *)filePath;

/**
 * 创建文件
 */
+ (BOOL)createFileWithPath:(NSString *)filePath;

/**
 * 删除文件
 */
+ (BOOL)deleteFileWithPath:(NSString *)filePath;

/**
 * 创建目录
 */
+ (BOOL)createDirectoryWithPath:(NSString *)dirPath;

/**
 * 目录是否存在
 */
+ (BOOL)directoryExistsWithPath:(NSString *)dirPath;

/**
 * 删除目录
 */
+ (BOOL)deleteDirectoryWithPath:(NSString *)dirPath;

/**
 * 获取程序的Documents目录
 */
+ (NSString *)getDocumentsPath;

/**
 * 获取程序目录的Library目录路径
 */
+ (NSString *)getLibraryPath;

/**
 * 获取程序目录的路径
 */
+ (NSString *)getAppPath;

/**
 * 获取程序临时目录路径
 */
+ (NSString *)getTemporaryPath;

/**
 * 获取文件大小（以字节为单位）
 */
+ (unsigned long long)getFileSizeWithPath:(NSString *)filePath;

/**
 * 获取系统可用空间（以字节为单位）
 */
+ (unsigned long long)getFileSystemFreeSize;

/**
 * 移动文件
 */
+ (void)moveFile:(NSString *)file toNewFile:(NSString *)newFile;

/**
 * 复制文件
 */
+ (void)copyFile:(NSString *)file toNewFile:(NSString *)newFile;

/**
 * 判断文件空间是否已满（小于500MB）
 */
//+ (BOOL)isDiskFull;

/**
 *递归寻找指定文件名的文件
 */
+ (NSString *)findFile:(NSString *)dictionary fileName:(NSString *)fileName;

/**
 *获取info.plist的路径
 */
+ (NSString *)getPlistPath;

/**
 *临时数据下载目录
 */
+ (NSString *)getDownloadTempDirectory;

// keyvalue file path,做缓存用
+ (NSString *)getKeyValueFilePath;

/**
 * 获取当前文件夹下的所有文件列表
 * @param dirPath 文件夹路径
 * @return 当前文件路径下的所有文件
 */
+ (NSArray<NSString *> *)getFilenamelistFromDirPath:(NSString *)dirPath;

@end
