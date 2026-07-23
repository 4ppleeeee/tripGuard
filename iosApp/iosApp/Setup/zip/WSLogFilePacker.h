#import <Foundation/Foundation.h>
#import <TDOS_Diagnose/TDLogFilePackerProtocol.h>

NS_ASSUME_NONNULL_BEGIN

/// 自定义日志压缩实现，使用 WSMinizipHelper（LVMiniZipArchive）进行压缩
@interface WSLogFilePacker : NSObject <TDLogFilePackerProtocol>

+ (instancetype)sharedInstance;

- (instancetype)init NS_UNAVAILABLE;
+ (instancetype)new NS_UNAVAILABLE;

@end

NS_ASSUME_NONNULL_END
