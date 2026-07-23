#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

/// 直接使用 minizip C API 压缩文件，绕过 SSZipArchive ObjC 封装层
@interface WSMinizipHelper : NSObject

/// 将指定目录下的所有文件压缩为 zip
/// @param zipPath zip 输出路径
/// @param directoryPath 待压缩的目录路径
/// @return 压缩是否成功
+ (BOOL)createZipFileAtPath:(NSString *)zipPath
    withContentsOfDirectory:(NSString *)directoryPath;

/// 将多个子目录下的文件压缩为带目录层级的 zip。
/// entry 名形如 `{subDir}/{fileName}`，便于日志上传保留层级。
/// @param zipPath zip 输出路径
/// @param subdirFiles key = zip 内子目录名，value = 该子目录下的源文件绝对路径数组
/// @return 压缩是否成功
+ (BOOL)createZipFileAtPath:(NSString *)zipPath
       withSubdirectoryFiles:(NSDictionary<NSString *, NSArray<NSString *> *> *)subdirFiles;

/// 使用 lv_unzip API 解压 zip 文件到指定目录（支持 Data Descriptor 格式）
/// @param zipPath zip 文件路径
/// @param destinationPath 解压目标目录
/// @return 解压是否成功
+ (BOOL)unzipFileAtPath:(NSString *)zipPath toDestination:(NSString *)destinationPath;

@end

NS_ASSUME_NONNULL_END
