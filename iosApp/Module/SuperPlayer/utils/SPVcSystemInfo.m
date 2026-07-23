/************************************************************
 Copyright (C), 1998-2018, Tencent Tech. Co., Ltd.
 FileName    : SPVcSystemInfo.m
 Author      : ethanyxliu
 Version     : 1.0
 Date        : 17/3/6
 Description :
 History     : 17/3/6 初始版本
 ***********************************************************/

/*
 Erica Sadun, http://ericasadun.com
 iPhone Developer's Cookbook, 6.x Edition
 BSD License, Use at your own risk
 */

// Thanks to Emanuele Vulcano, Kevin Ballard/Eridius, Ryandjohnson, Matt Brown, etc.

#include <sys/socket.h>  // Per msqr
#include <sys/sysctl.h>
#include <net/if.h>
#include <net/if_dl.h>
#import <AdSupport/ASIdentifierManager.h>
#import <mach/machine.h>
#import "SPVcSystemInfo.h"
#import "SPSFHFKeychainUtils.h"
#import "SPNetworkChecker.h"
#import "SPFileHelper.h"

static int const CPU_CHIP_X86    = 100;  //X86架构
static int const CPU_CHIP_X86_64 = 150;  //X86架构

#define ARRAY_SIZE(a) sizeof(a) / sizeof(a[0])
const static char *jb_tool_pathes[] = {
    "/Applications/Cydia.app",
    "/Library/MobileSubstrate/MobileSubstrate.dylib",
    "/etc/apt"};

static NSString *gSPMediaPlayerSDKGuid = @"SPMediaPlayerSDKGuid";

#define LOG_TAG @"SPSystemInfo"

@interface SPVcSystemInfo ()

@property (nonatomic, assign) NSInteger wifiSignalStrength;
@property (nonatomic, assign) long long lastWifiSignalStrengthCheckTime;

@end

@implementation SPVcSystemInfo

+ (SPVcSystemInfo *)sharedInstance {
    static SPVcSystemInfo *systemInfo = nil;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
      systemInfo = [[SPVcSystemInfo alloc] init];

    });
    return systemInfo;
}

- (NSInteger)sstrength {
    return 0;
}

- (NSString *)deviceMachineFamily {
    if (!_deviceMachineFamily) {
        NSString *pattern = @"[a-z 0-9]+";

        NSError *error             = nil;
        NSRegularExpression *regex = [NSRegularExpression regularExpressionWithPattern:pattern
                                                                               options:NSRegularExpressionCaseInsensitive
                                                                                 error:&error];

        if (!error) {
            NSTextCheckingResult *matchResult = [regex firstMatchInString:self.deviceMachineConventionalName
                                                                  options:0
                                                                    range:NSMakeRange(0, self.deviceMachineConventionalName.length)];
            if (matchResult) {
                NSString *result = [self.deviceMachineConventionalName substringWithRange:matchResult.range];
                result           = [result stringByTrimmingCharactersInSet:[NSCharacterSet whitespaceCharacterSet]];  //去除头尾的空格
                SPLOGI(LOG_TAG, @"deviceMachineFamily = %@", result);
                _deviceMachineFamily = result;
            } else {
                SPLOGI(LOG_TAG, @"deviceMachineFamily not match");
            }

        } else {
            SPLOGW(LOG_TAG, @"deviceMachineFamily ");
        }
        if (_deviceMachineFamily.length <= 0) {
            _deviceMachineFamily = self.deviceMachineConventionalName;  //前面获取有错误或者获取的为空
        }
    }
    return _deviceMachineFamily;
}

// NOLINTNEXTLINE
- (NSString *)deviceMachineConventionalName {
    // #lizard forgives
    if (!_deviceMachineConventionalName) {
        NSString *devcieMachine                                                           = [[self deviceMachine] copy];
        if ([devcieMachine isEqualToString:@"iFPGA"]) _deviceMachineConventionalName      = SP_IFPGA_NAMESTRING;
        if ([devcieMachine isEqualToString:@"iPhone1,1"]) _deviceMachineConventionalName  = @"iPhone 1G";
        if ([devcieMachine isEqualToString:@"iPhone1,2"]) _deviceMachineConventionalName  = @"iPhone 3G";
        if ([devcieMachine isEqualToString:@"iPhone2,1"]) _deviceMachineConventionalName  = @"iPhone 3GS";
        if ([devcieMachine isEqualToString:@"iPhone3,1"]) _deviceMachineConventionalName  = @"iPhone 4 (GSM)";
        if ([devcieMachine isEqualToString:@"iPhone3,2"]) _deviceMachineConventionalName  = @"iPhone 4 (GSM Rev A)";
        if ([devcieMachine isEqualToString:@"iPhone3,3"]) _deviceMachineConventionalName  = @"iPhone 4 (CDMA)";
        if ([devcieMachine isEqualToString:@"iPhone4,1"]) _deviceMachineConventionalName  = @"iPhone 4S";
        if ([devcieMachine isEqualToString:@"iPhone5,1"]) _deviceMachineConventionalName  = @"iPhone 5 (GSM)";
        if ([devcieMachine isEqualToString:@"iPhone5,2"]) _deviceMachineConventionalName  = @"iPhone 5 (Global)";
        if ([devcieMachine isEqualToString:@"iPhone5,3"]) _deviceMachineConventionalName  = @"iPhone 5c (GSM)";
        if ([devcieMachine isEqualToString:@"iPhone5,4"]) _deviceMachineConventionalName  = @"iPhone 5c (Global)";
        if ([devcieMachine isEqualToString:@"iPhone6,1"]) _deviceMachineConventionalName  = @"iPhone 5s (GSM)";
        if ([devcieMachine isEqualToString:@"iPhone6,2"]) _deviceMachineConventionalName  = @"iPhone 5s (Global)";
        if ([devcieMachine isEqualToString:@"iPhone7,1"]) _deviceMachineConventionalName  = @"iPhone 6 Plus";
        if ([devcieMachine isEqualToString:@"iPhone7,2"]) _deviceMachineConventionalName  = @"iPhone 6";
        if ([devcieMachine isEqualToString:@"iPhone8,1"]) _deviceMachineConventionalName  = @"iPhone 6s";
        if ([devcieMachine isEqualToString:@"iPhone8,2"]) _deviceMachineConventionalName  = @"iPhone 6s Plus";
        if ([devcieMachine isEqualToString:@"iPhone8,4"]) _deviceMachineConventionalName  = @"iPhone SE";
        if ([devcieMachine isEqualToString:@"iPhone9,1"]) _deviceMachineConventionalName  = @"iPhone 7";
        if ([devcieMachine isEqualToString:@"iPhone9,2"]) _deviceMachineConventionalName  = @"iPhone 7 Plus";
        if ([devcieMachine isEqualToString:@"iPhone9,3"]) _deviceMachineConventionalName  = @"iPhone 7";
        if ([devcieMachine isEqualToString:@"iPhone9,4"]) _deviceMachineConventionalName  = @"iPhone 7 Plus";
        if ([devcieMachine isEqualToString:@"iPhone10,1"]) _deviceMachineConventionalName = @"iPhone 8";       // US (Verizon), China, Japan
        if ([devcieMachine isEqualToString:@"iPhone10,2"]) _deviceMachineConventionalName = @"iPhone 8 Plus";  // US (Verizon), China, Japan
        if ([devcieMachine isEqualToString:@"iPhone10,3"]) _deviceMachineConventionalName = @"iPhone X";       // US (Verizon), China, Japan
        if ([devcieMachine isEqualToString:@"iPhone10,4"]) _deviceMachineConventionalName = @"iPhone 8";       // AT&T, Global
        if ([devcieMachine isEqualToString:@"iPhone10,5"]) _deviceMachineConventionalName = @"iPhone 8 Plus";  // AT&T, Global
        if ([devcieMachine isEqualToString:@"iPhone10,6"]) _deviceMachineConventionalName = @"iPhone X";       // AT&T, Global
        if ([devcieMachine isEqualToString:@"iPhone11,2"]) _deviceMachineConventionalName = @"iPhone Xs";
        if ([devcieMachine isEqualToString:@"iPhone11,4"]) _deviceMachineConventionalName = @"iPhone XsMax";
        if ([devcieMachine isEqualToString:@"iPhone11,6"]) _deviceMachineConventionalName = @"iPhone XsMax";
        if ([devcieMachine isEqualToString:@"iPhone11,8"]) _deviceMachineConventionalName = @"iPhone Xr";

        // iPad http://theiphonewiki.com/wiki/IPad

        if ([devcieMachine isEqualToString:@"iPad1,1"]) _deviceMachineConventionalName = @"iPad 1G";
        if ([devcieMachine isEqualToString:@"iPad2,1"]) _deviceMachineConventionalName = @"iPad 2 (Wi-Fi)";
        if ([devcieMachine isEqualToString:@"iPad2,2"]) _deviceMachineConventionalName = @"iPad 2 (GSM)";
        if ([devcieMachine isEqualToString:@"iPad2,3"]) _deviceMachineConventionalName = @"iPad 2 (CDMA)";
        if ([devcieMachine isEqualToString:@"iPad2,4"]) _deviceMachineConventionalName = @"iPad 2 (Rev A)";
        if ([devcieMachine isEqualToString:@"iPad3,1"]) _deviceMachineConventionalName = @"iPad 3 (Wi-Fi)";
        if ([devcieMachine isEqualToString:@"iPad3,2"]) _deviceMachineConventionalName = @"iPad 3 (GSM)";
        if ([devcieMachine isEqualToString:@"iPad3,3"]) _deviceMachineConventionalName = @"iPad 3 (Global)";
        if ([devcieMachine isEqualToString:@"iPad3,4"]) _deviceMachineConventionalName = @"iPad 4 (Wi-Fi)";
        if ([devcieMachine isEqualToString:@"iPad3,5"]) _deviceMachineConventionalName = @"iPad 4 (GSM)";
        if ([devcieMachine isEqualToString:@"iPad3,6"]) _deviceMachineConventionalName = @"iPad 4 (Global)";

        if ([devcieMachine isEqualToString:@"iPad4,1"]) _deviceMachineConventionalName = @"iPad Air (Wi-Fi)";
        if ([devcieMachine isEqualToString:@"iPad4,2"]) _deviceMachineConventionalName = @"iPad Air (Cellular)";
        if ([devcieMachine isEqualToString:@"iPad5,3"]) _deviceMachineConventionalName = @"iPad Air 2 (Wi-Fi)";
        if ([devcieMachine isEqualToString:@"iPad5,4"]) _deviceMachineConventionalName = @"iPad Air 2 (Cellular)";

        // iPad Mini http://theiphonewiki.com/wiki/IPad_mini

        if ([devcieMachine isEqualToString:@"iPad2,5"]) _deviceMachineConventionalName = @"iPad mini 1G (Wi-Fi)";
        if ([devcieMachine isEqualToString:@"iPad2,6"]) _deviceMachineConventionalName = @"iPad mini 1G (GSM)";
        if ([devcieMachine isEqualToString:@"iPad2,7"]) _deviceMachineConventionalName = @"iPad mini 1G (Global)";
        if ([devcieMachine isEqualToString:@"iPad4,4"]) _deviceMachineConventionalName = @"iPad mini 2G (Wi-Fi)";
        if ([devcieMachine isEqualToString:@"iPad4,5"]) _deviceMachineConventionalName = @"iPad mini 2G (Cellular)";
        // TD-LTE model see https://support.apple.com/en-us/HT201471#iPad-mini2
        if ([devcieMachine isEqualToString:@"iPad4,6"]) _deviceMachineConventionalName = @"iPad mini 2G (Cellular)";
        if ([devcieMachine isEqualToString:@"iPad4,7"]) _deviceMachineConventionalName = @"iPad mini 3G (Wi-Fi)";
        if ([devcieMachine isEqualToString:@"iPad4,8"]) _deviceMachineConventionalName = @"iPad mini 3G (Cellular)";
        if ([devcieMachine isEqualToString:@"iPad4,9"]) _deviceMachineConventionalName = @"iPad mini 3G (Cellular)";
        if ([devcieMachine isEqualToString:@"iPad5,1"]) _deviceMachineConventionalName = @"iPad mini 4G (Wi-Fi)";
        if ([devcieMachine isEqualToString:@"iPad5,2"]) _deviceMachineConventionalName = @"iPad mini 4G (Cellular)";

        // iPad Pro https://www.theiphonewiki.com/wiki/IPad_Pro

        // http://pdadb.net/index.php?m=specs&id=9938&c=apple_ipad_pro_9.7-inch_a1673_wifi_32gb_apple_ipad_6,3
        if ([devcieMachine isEqualToString:@"iPad6,3"]) _deviceMachineConventionalName = @"iPad Pro (9.7 inch) 1G (Wi-Fi)";
        // http://pdadb.net/index.php?m=specs&id=9981&c=apple_ipad_pro_9.7-inch_a1675_td-lte_32gb_apple_ipad_6,4
        if ([devcieMachine isEqualToString:@"iPad6,4"]) _deviceMachineConventionalName = @"iPad Pro (9.7 inch) 1G (Cellular)";
        // http://pdadb.net/index.php?m=specs&id=8960&c=apple_ipad_pro_wifi_a1584_128gb
        if ([devcieMachine isEqualToString:@"iPad6,7"]) _deviceMachineConventionalName = @"iPad Pro (12.9 inch) 1G (Wi-Fi)";
        // http://pdadb.net/index.php?m=specs&id=8965&c=apple_ipad_pro_td-lte_a1652_32gb_apple_ipad_6,8
        if ([devcieMachine isEqualToString:@"iPad6,8"]) _deviceMachineConventionalName = @"iPad Pro (12.9 inch) 1G (Cellular)";

        // iPod http://theiphonewiki.com/wiki/IPod

        if ([devcieMachine isEqualToString:@"iPod1,1"]) _deviceMachineConventionalName = @"iPod touch 1G";
        if ([devcieMachine isEqualToString:@"iPod2,1"]) _deviceMachineConventionalName = @"iPod touch 2G";
        if ([devcieMachine isEqualToString:@"iPod3,1"]) _deviceMachineConventionalName = @"iPod touch 3G";
        if ([devcieMachine isEqualToString:@"iPod4,1"]) _deviceMachineConventionalName = @"iPod touch 4G";
        if ([devcieMachine isEqualToString:@"iPod5,1"]) _deviceMachineConventionalName = @"iPod touch 5G";
        // as 6,1 was never released 7,1 is actually 6th generation
        if ([devcieMachine isEqualToString:@"iPod7,1"]) _deviceMachineConventionalName = @"iPod touch 6G";
        
        // Apple TV https://www.theiphonewiki.com/wiki/Apple_TV

        if ([devcieMachine isEqualToString:@"AppleTV1,1"]) _deviceMachineConventionalName = @"Apple TV 1G";
        if ([devcieMachine isEqualToString:@"AppleTV2,1"]) _deviceMachineConventionalName = @"Apple TV 2G";
        if ([devcieMachine isEqualToString:@"AppleTV3,1"]) _deviceMachineConventionalName = @"Apple TV 3G";
        // small, incremental update over 3,1
        if ([devcieMachine isEqualToString:@"AppleTV3,2"]) _deviceMachineConventionalName = @"Apple TV 3G";
        // as 4,1 was never released, 5,1 is actually 4th generation
        if ([devcieMachine isEqualToString:@"AppleTV5,3"]) _deviceMachineConventionalName = @"Apple TV 4G";

        // Simulator
        if ([devcieMachine hasSuffix:@"86"] || [devcieMachine isEqual:@"x86_64"]) {
            BOOL smallerScreen             = ([[UIScreen mainScreen] bounds].size.width < 768.0);
            _deviceMachineConventionalName = (smallerScreen ? @"iPhone Simulator" : @"iPad Simulator");
        }
        if (!_deviceMachineConventionalName) _deviceMachineConventionalName = devcieMachine;
    }
    return _deviceMachineConventionalName;
}

- (NSString *)resolution {
    if (!_resolution) {
        _resolution = [NSString stringWithFormat:@"%d*%d", (int)[self screenWidth], (int)[self screenHeight]];
    }
    return _resolution;
}

- (CGFloat)screenWidth {
    if (0 == _screenWidth) {
        CGSize screenSize = [[UIScreen mainScreen] bounds].size;
        _screenWidth      = fmin(screenSize.width, screenSize.height);
    }
    return _screenWidth;
}

- (CGFloat)screenHeight {
    if (0 == _screenHeight) {
        CGSize screenSize = [[UIScreen mainScreen] bounds].size;
        _screenHeight     = fmax(screenSize.width, screenSize.height);
    }
    return _screenHeight;
}
- (NSString *)systemVer {
    if (!_systemVer) {
        _systemVer = [UIDevice currentDevice].systemVersion;
    }
    return _systemVer;
}
- (NSString *)osVer {
    if (!_osVer) {
        NSString *sysVer          = self.systemVer;
        NSMutableArray *component = [NSMutableArray arrayWithArray:[sysVer componentsSeparatedByString:@"."]];
        while (component.count < 3) {
            [component addObject:@"0"];
        }
        while (component.count > 3) {
            [component removeLastObject];
        }
        _osVer = [component componentsJoinedByString:@"."];
    }
    return _osVer;
}
- (NSString *)appver {
    if (!_appver) {
        NSString *appVersion      = [[[NSBundle mainBundle] infoDictionary] objectForKey:@"CFBundleShortVersionString"];
        NSMutableArray *component = [NSMutableArray arrayWithArray:[appVersion componentsSeparatedByString:@"."]];
        while (component.count < 3) {
            [component addObject:@"0"];
        }
        while (component.count > 3) {
            [component removeLastObject];
        }
        appVersion             = [component componentsJoinedByString:@"."];
        NSString *buildVersion = [[[NSBundle mainBundle] infoDictionary] objectForKey:@"CFBundleVersion"];
        _appver                = [appVersion stringByAppendingFormat:@".%@", buildVersion];
    }
    return _appver;
}

- (NSString *)mainAppVer {
    if (!_mainAppVer) {
        NSString *appVersion      = [[[NSBundle mainBundle] infoDictionary] objectForKey:@"CFBundleShortVersionString"];
        NSMutableArray *component = [NSMutableArray arrayWithArray:[appVersion componentsSeparatedByString:@"."]];
        while (component.count < 3) {
            [component addObject:@"0"];
        }
        while (component.count > 3) {
            [component removeLastObject];
        }

        _mainAppVer = [component componentsJoinedByString:@"."];
    }

    return _mainAppVer;
}

- (NSString *)buildVersion {
    if (!_buildVersion) {
        _buildVersion = [[[NSBundle mainBundle] infoDictionary] objectForKey:@"CFBundleVersion"];
    }
    
    return _buildVersion;
}

- (NSString *)getSysInfoByName:(char *)typeSpecifier {
    size_t size;
    sysctlbyname(typeSpecifier, NULL, &size, NULL, 0);

    char *answer = malloc(size);
    sysctlbyname(typeSpecifier, answer, &size, NULL, 0);
    
    NSString *results = @"i386";
    if (answer != NULL) {
        results = [NSString stringWithCString:answer encoding:NSUTF8StringEncoding];
    }
    
    free(answer);
    return results;
}

- (NSString *)deviceMachine {
    if (!_deviceMachine) {
        _deviceMachine = [self getSysInfoByName:"hw.machine"];
    }
    return _deviceMachine;
}

// Thanks, Tom Harrington (Atomicbird)
- (NSString *)deviceModel {
    if (!_deviceModel) {
        _deviceModel = [self getSysInfoByName:"hw.model"];
    }
    return _deviceModel;
}

- (NSString *)bundleId {
    if (!_bundlId) {
        _bundlId = [[NSBundle mainBundle] bundleIdentifier];
    }
    return _bundlId;
}

- (NSString *)idfv {
    if (!_idfv.length && [self.systemVer floatValue] >= 6.0) {
        _idfv = [[[UIDevice currentDevice] identifierForVendor] UUIDString];
        if (!_idfv.length) {
            _idfv = @"";
        }
    }
    return _idfv;
}

- (NSString *)deviceId {
    if (_deviceId.length > 0) {
        return [_deviceId copy];
    }
    @synchronized(self) {
        if (_deviceId.length > 0) {
            return [_deviceId copy];
        }
        NSUserDefaults *userDefaults = [NSUserDefaults standardUserDefaults];
        NSString *defaultGUID        = [userDefaults objectForKey:@"guid"];
        if ([defaultGUID isKindOfClass:[NSString class]] && defaultGUID.length) {
            SPLOGI(LOG_TAG, @"从userdefault读取GUID成功，defaultGUID=%@", defaultGUID);
        } else {
            defaultGUID         = nil;
            NSString *devIDPath = [[SPFileHelper getDocumentsPath] stringByAppendingPathComponent:@"SPSDKdevid.dat"];

            if ([SPFileHelper fileExistsWithPath:devIDPath]) {
                NSError *readError = nil;
                NSString *devID    = [[NSString alloc] initWithContentsOfFile:devIDPath encoding:NSUTF8StringEncoding error:&readError];

                if (!readError && [devID isKindOfClass:[NSString class]] && devID.length) {
                    defaultGUID = devID;

                    [userDefaults setObject:defaultGUID forKey:@"guid"];
                    [userDefaults synchronize];

                    SPLOGI(LOG_TAG, @"read dev id from file : %@", devID);
                } else {
                    if (readError) {
                        SPLOGE(LOG_TAG, @"READ ERROR : %@", readError);
                    }

                    // 上报文件读取失败
                    SPLOGE(LOG_TAG, @"read dev id from file failed");
                }
            }

            // userDefaults 和 文件缓存都读取失败
            if (!defaultGUID || !defaultGUID.length) {
                CFUUIDRef uuidObj = CFUUIDCreate(nil);  // create a new UUID
                // get the string representation of the UUID
                NSString *uuidString = (NSString *)CFBridgingRelease(CFUUIDCreateString(nil, uuidObj));
                CFRelease(uuidObj);
                if (uuidString.length > 0) {
                    [userDefaults setObject:uuidString forKey:@"guid"];
                    [userDefaults synchronize];

                    [uuidString writeToFile:devIDPath atomically:YES encoding:NSUTF8StringEncoding error:NULL];

                    defaultGUID = uuidString;
                } else {
                    SPLOGE(LOG_TAG, @"CFUUIDCreate Failed");
                }

                SPLOGE(LOG_TAG, @"app create device id : %@", uuidString);
            }
        }

        if (defaultGUID.length > 0) {
            _deviceId = defaultGUID;
        }

        if (_deviceId.length <= 0) {
            SPLOGE(LOG_TAG, @"从userdefault读取GUID失败");
        }

        return [_deviceId copy];
    }
}

//此guid是sdk从后台配置下发获取到的guid.除非是sdkconfig的配置请求中guid.guid的获取统一从SPSDKParamsMgr中的guid获取，因为guid可能是外部设置的.
- (NSString *)localGuid {
    if (_localGuid.length) {
        return _localGuid;
    }
    NSUserDefaults *userDefaults = [NSUserDefaults standardUserDefaults];
    @synchronized(self) {
        NSString *defaultGUID = [userDefaults objectForKey:[self userDefaultGuidKey]];
        if ([defaultGUID isKindOfClass:[NSString class]] && defaultGUID.length) {
            _localGuid = defaultGUID;
            SPLOGI(LOG_TAG, @"从UserDefaults读取GUID成功 defaultGUID=%@", defaultGUID);
        } else {
            NSString *keyChainGUID = [self getGUIDFromKeychain];
            if (keyChainGUID.length) {
                _localGuid = keyChainGUID;
                SPLOGI(LOG_TAG, @"从KeyChain读取GUID成功 keyChainGUID=%@", keyChainGUID);
            }
        }
        if (_localGuid.length <= 0) {
            SPLOGI(LOG_TAG, @"从KeyChain读取GUID失败");
        }
        return _localGuid;
    }
}

- (void)updateLocalGuid:(NSString *)guid {
    // 简单添加保护，防止重入
    static BOOL isUpdating = NO;

    if (isUpdating) {
        return;
    }

    isUpdating = YES;

    if (guid.length == 0) {
        SPLOGI(LOG_TAG, @"在线拉取 guid 返回,不需要写入keychain，得到guid:%@", guid);
        return;
    }

    NSUserDefaults *userDefaults = [NSUserDefaults standardUserDefaults];

    BOOL needResetGUID = NO;
    //判断是否相同再写
    if (![[self getGUIDFromKeychain] isEqualToString:guid]) {
        NSError *error = nil;

        SPLOGI(LOG_TAG, @"GUID获取成功,准备写入keychain guid=%@", guid);
        BOOL result = [SPSFHFKeychainUtils storeUsername:gSPMediaPlayerSDKGuid
                                                 password:guid
                                           forServiceName:self.bundlId
                                           updateExisting:YES
                                                    error:&error];
        if (!result || error) {
            SPLOGI(LOG_TAG, @"向KeyChain写入GUID失败 error=%@", error);
        } else {
            SPLOGI(LOG_TAG, @"向KeyChain中成功写入GUID guid=%@", guid);
        }

        needResetGUID = YES;
    }

    // 本地做 server guid 的备份，删除重装这个备份会丢失，ethanyxliu(20131202)
    NSString *userDefaultUUID = [userDefaults objectForKey:[self userDefaultGuidKey]];
    if (![userDefaultUUID isKindOfClass:[NSString class]] || ![userDefaultUUID isEqualToString:guid]) {
        [userDefaults setObject:guid forKey:[self userDefaultGuidKey]];
        SPLOGI(LOG_TAG, @"向UserDefaults中成功备份写入GUID guid=%@ , last uuid:%@", guid, userDefaultUUID);
        [userDefaults synchronize];
    }

    isUpdating = NO;
}

- (NSString *)getGUIDFromKeychain {
    NSError *error       = nil;
    NSString *serverGUID = [SPSFHFKeychainUtils getPasswordForUsername:gSPMediaPlayerSDKGuid serviceName:self.bundlId error:&error];

    if (serverGUID && !error) {
        return serverGUID;
    } else {
        SPLOGI(LOG_TAG, @"从KeyChain读取GUID失败 error=%@", error);
        return @"";
    }
}

- (NSString *)userDefaultGuidKey {
    return [NSString stringWithFormat:@"%@.%@", self.bundleId, gSPMediaPlayerSDKGuid];
}

- (BOOL)isAllowsArbitraryLoads {
    static BOOL checked = NO;

    if (checked) {
        return _isAllowsArbitraryLoads;
    }

    NSDictionary *infoDic   = [[NSBundle mainBundle] infoDictionary];
    NSDictionary *tSecurity = [infoDic spDictionaryForKey:@"NSAppTransportSecurity"];

    if (!tSecurity) {
        // 没有设置安全选项
        _isAllowsArbitraryLoads = YES;
    } else {
        _isAllowsArbitraryLoads = [tSecurity spBoolForKeySafeModel:@"NSAllowsArbitraryLoads"];
    }

    checked = YES;
    return _isAllowsArbitraryLoads;
}

- (NSString *)macAddress {
    if (!_macAddress) {
        int mib[6];
        size_t len;
        char *buf;
        unsigned char *ptr;
        struct if_msghdr *ifm;
        struct sockaddr_dl *sdl;

        mib[0] = CTL_NET;
        mib[1] = AF_ROUTE;
        mib[2] = 0;
        mib[3] = AF_LINK;
        mib[4] = NET_RT_IFLIST;

        if ((mib[5] = if_nametoindex("en0")) == 0) {
            printf("Error: if_nametoindex error\n");
            return NULL;
        }

        if (sysctl(mib, 6, NULL, &len, NULL, 0) < 0) {
            printf("Error: sysctl, take 1\n");
            return NULL;
        }

        if ((buf = malloc(len)) == NULL) {
            printf("Error: Memory allocation error\n");
            return NULL;
        }

        if (sysctl(mib, 6, buf, &len, NULL, 0) < 0) {
            printf("Error: sysctl, take 2\n");
            free(buf);  // Thanks, Remy "Psy" Demerest
            return NULL;
        }

        ifm                 = (struct if_msghdr *)buf;
        sdl                 = (struct sockaddr_dl *)(ifm + 1);
        ptr                 = (unsigned char *)LLADDR(sdl);
        NSString *outstring = [NSString stringWithFormat:@"%02X:%02X:%02X:%02X:%02X:%02X",
                               *ptr,
                               *(ptr + 1),
                               *(ptr + 2),
                               *(ptr + 3),
                               *(ptr + 4),
                               *(ptr + 5)];

        free(buf);
        _macAddress = outstring;
    }
    return _macAddress;
}

- (NSUInteger)getSysInfo:(uint)typeSpecifier {
    size_t size = sizeof(int);
    int results;
    int mib[2] = {CTL_HW, typeSpecifier};
    sysctl(mib, 2, &results, &size, NULL, 0);
    return (NSUInteger)results;
}

- (NSUInteger)cpuFrequency {
    static BOOL checked = NO;

    if (checked) {
        return _cpuFrequency;
    }
    checked       = YES;
    _cpuFrequency = [self getSysInfo:HW_CPU_FREQ] / 1024;
    return _cpuFrequency;
}

- (NSUInteger)cpuCount {
    static BOOL checked = NO;

    if (checked) {
        return _cpuCount;
    }
    checked   = YES;
    _cpuCount = [self getSysInfo:HW_NCPU];
    return _cpuCount;
}

- (NSString *)getCPUType {
    NSMutableString *cpu = [[NSMutableString alloc] init];
    size_t size;
    cpu_type_t type;
    cpu_subtype_t subtype;
    size = sizeof(type);
    sysctlbyname("hw.cputype", &type, &size, NULL, 0);

    size = sizeof(subtype);
    sysctlbyname("hw.cpusubtype", &subtype, &size, NULL, 0);

    // values for cputype and cpusubtype defined in mach/machine.h
    if (type == CPU_TYPE_X86_64) {
        [cpu appendString:@"x86_64"];
    } else if (type == CPU_TYPE_X86) {
        [cpu appendString:@"x86"];
    } else if (type == CPU_TYPE_ARM) {
        [cpu appendString:@"ARM"];
        switch (subtype) {
            case CPU_SUBTYPE_ARM_V6:
                [cpu appendString:@"V6"];
                break;
            case CPU_SUBTYPE_ARM_V7:
                [cpu appendString:@"V7"];
                break;
            case CPU_SUBTYPE_ARM_V8:
                [cpu appendString:@"V8"];
                break;
        }
    }
    return cpu;
}
- (NSUInteger)cpuSubtype {
    static BOOL checked = NO;

    if (checked) {
        return _cpuSubtype;
    }
    checked = YES;
    size_t size;
    cpu_type_t type;
    cpu_subtype_t subtype;
    size = sizeof(type);
    sysctlbyname("hw.cputype", &type, &size, NULL, 0);
    size = sizeof(subtype);
    sysctlbyname("hw.cpusubtype", &subtype, &size, NULL, 0);
    //x86和mpis取自定义的, arm直接取subtype
    if (type == CPU_TYPE_X86_64) {
        _cpuSubtype = CPU_CHIP_X86_64;
    } else if (type == CPU_TYPE_X86) {
        _cpuSubtype = CPU_CHIP_X86;
    } else if (type == CPU_TYPE_ARM) {
        _cpuSubtype = subtype;
    } else {
        _cpuSubtype = subtype;
    }

    return _cpuSubtype;
}

- (BOOL)isFoundJBPath {
    for (int i = 0; i < ARRAY_SIZE(jb_tool_pathes); i++) {
        if ([[NSFileManager defaultManager] fileExistsAtPath:[NSString stringWithUTF8String:jb_tool_pathes[i]]]) {
            NSLog(@"The device is jail broken!");
            return YES;
        }
    }
    NSLog(@"The device is NOT jail broken!");
    return NO;
}

- (BOOL)isCanCallCydia {
    if ([[UIApplication sharedApplication] canOpenURL:[NSURL URLWithString:@"cydia://"]]) {
        NSLog(@"The device is jail broken!");
        return YES;
    }
    NSLog(@"The device is NOT jail broken!");
    return NO;
}

#define USER_APP_PATH @"/User/Applications/"
#define USER_APP_PATH_IOS8 @"/User/Containers/Bundle/Application/"

- (BOOL)isCanAccessUserAppPath {
    if ([[NSFileManager defaultManager] fileExistsAtPath:USER_APP_PATH]) {
        NSLog(@"The device is jail broken!");
        //NSArray *applist = [[NSFileManager defaultManager] contentsOfDirectoryAtPath:USER_APP_PATH error:nil];
        //NSLog(@"applist = %@", applist);
        return YES;
    }
    if ([[self systemVer] floatValue] >= 8) {  //IOS 8之后
        if ([[NSFileManager defaultManager] fileExistsAtPath:USER_APP_PATH_IOS8]) {
            NSLog(@"The device is jail broken!");
            //NSArray *applist = [[NSFileManager defaultManager] contentsOfDirectoryAtPath:USER_APP_PATH error:nil];
            //NSLog(@"applist = %@", applist);
            return YES;
        }
    }

    NSLog(@"The device is NOT jail broken!");
    return NO;
}

- (BOOL)isJBOS {
    static BOOL isCheckted = false;
    if (isCheckted) {
        return _isJBOS;
    }
    if ([self isCanAccessUserAppPath] && [self isCanCallCydia] && [self isFoundJBPath]) {
        _isJBOS = YES;
    } else {
        _isJBOS = NO;
    }
    isCheckted = YES;
    return _isJBOS;
}

- (BOOL)isRetinaDisplay {
    return ([UIScreen instancesRespondToSelector:@selector(scale)] && ([UIScreen mainScreen].scale == 2.0));
}

- (BOOL)isIOS93OrLatter {
    float systemVersion = [self.systemVer floatValue];

    if (systemVersion >= 9.3 || [self.systemVer isEqualToString:@"9.3.0"]) {
        return YES;
    }

    return NO;
}
- (BOOL)isIOS901OrLatter {
    float systemVersion = [self.systemVer floatValue];

    if (systemVersion > 9 || [self.systemVer isEqualToString:@"9.0.1"]) {
        return YES;
    }

    return NO;
}

- (BOOL)isIOS10OrLatter {
    if ([self.systemVer floatValue] >= 10) {
        return YES;
    }

    return NO;
}

// added by tencent:jiachunke(20151002)
- (BOOL)isIOS9OrLatter {
    if ([self.systemVer floatValue] >= 9) {
        return YES;
    }

    return NO;
}

- (BOOL)isIOS8OrLatter {
    if ([self.systemVer floatValue] >= 8) {
        return YES;
    }

    return NO;
}

- (BOOL)isIOS7OrLatter {
    if ([self.systemVer floatValue] >= 7) {
        return YES;
    }

    return NO;
}

- (BOOL)isIOS6OrLatter {
    if ([self.systemVer floatValue] >= 6) {
        return YES;
    }

    return NO;
}

- (BOOL)isIOS9 {
    if ([self.systemVer floatValue] < 10 && [self.systemVer floatValue] >= 9) {
        return YES;
    }
    return NO;
}

- (BOOL)deviceGreaterThan:(NSString *)deviceMachine {
    int mainCode1 = [[self deviceMachineMainCode] intValue];
    int mainCode2 = [[self mainCodeOfDeviceMachine:deviceMachine] intValue];
    if (mainCode1 == mainCode2) {
        int subCode1 = [[self deviceMachineSubCode] intValue];
        int subCode2 = [[self subCodeOfDeviceMachine:deviceMachine] intValue];
        return subCode1 > subCode2;
    }
    
    return mainCode1 > mainCode2;
}

- (BOOL)deviceGreaterThanOrEqualTo:(NSString *)deviceMachine {
    if ([self deviceGreaterThan:deviceMachine]) {
        return YES;
    }
    
    return [self deviceEqualTo:deviceMachine];
}

- (BOOL)deviceEqualTo:(NSString *)deviceMachine {
    return [[self deviceMachine] isEqualToString:deviceMachine];
}

- (BOOL)deviceLessThan:(NSString *)deviceMachine {
    int mainCode1 = [[self deviceMachineMainCode] intValue];
    int mainCode2 = [[self mainCodeOfDeviceMachine:deviceMachine] intValue];
    if (mainCode1 == mainCode2) {
        int subCode1 = [[self deviceMachineSubCode] intValue];
        int subCode2 = [[self subCodeOfDeviceMachine:deviceMachine] intValue];
        return subCode1 < subCode2;
    }
    
    return mainCode1 < mainCode2;
}

- (BOOL)deviceLessThanOrEqualTo:(NSString *)deviceManchine {
    if ([self deviceLessThan:deviceManchine]) {
        return YES;
    }
    
    return [self deviceEqualTo:deviceManchine];
}

- (NSString *)deviceMachineMainCode {
    if (_deviceMachineMainCode == nil) {
        _deviceMachineMainCode = [self mainCodeOfDeviceMachine:[self deviceMachine]];
    }
    
    return _deviceMachineMainCode;
}

- (NSString *)deviceMachineSubCode {
    if (_deviceMachineSubCode == nil) {
        _deviceMachineSubCode = [self subCodeOfDeviceMachine:[self deviceMachine]];
    }
    return _deviceMachineSubCode;
}
/**
 * 从deviceMachine解析出mainCode，例iPhone11,2，mainCode=11
 */
- (NSString *)mainCodeOfDeviceMachine:(NSString *)deviceMachine {
    if (deviceMachine == nil) {
        return nil;
    }
    
    NSRange range1 = [deviceMachine rangeOfString:@"iPhone" options:NSCaseInsensitiveSearch];
    if (range1.location == NSNotFound) {
        return nil;
    }
    
    NSRange range2 = [deviceMachine rangeOfString:@"," options:NSCaseInsensitiveSearch];
    if (range2.location != NSNotFound) {
        NSUInteger newLocation = range1.location + range1.length;
        NSUInteger newLength = range2.location - range1.location - range1.length;;
        range1.location = newLocation;
        range1.length = newLength;
    } else {
        range1.location = range1.location + range1.length;
    }
    
    if (range1.location < deviceMachine.length &&
        (range1.location + range1.length - 1  < deviceMachine.length) &&
        range1.length > 0) {
        return [deviceMachine substringWithRange:range1];
    } else {
        return nil;
    }
}

- (NSString *)subCodeOfDeviceMachine:(NSString *)deviceMachine {
    if (deviceMachine == nil) {
        return nil;
    }
    
    NSRange range = [deviceMachine rangeOfString:@"," options:NSCaseInsensitiveSearch];
    if (range.location == NSNotFound) {
        return nil;
    }
    
    if (range.location + range.length < deviceMachine.length) {
        return [deviceMachine substringFromIndex:range.location + range.length];
    } else {
        return nil;
    }
}

- (SPPlatformType)platformType {
    UIUserInterfaceIdiom idiom = [[UIDevice currentDevice] userInterfaceIdiom];
    switch (idiom) {
        case UIUserInterfaceIdiomPhone:
            return SPPlatformTypeiPhone;
            break;
        case UIUserInterfaceIdiomPad:
            return SPPlatformTypeiPad;
            break;
        case UIUserInterfaceIdiomTV:
            return SPPlatformTypeTV;
            break;
        default:
            return SPPlatformTypeUnknown;
            break;
    }
}

- (NSInteger)getWifiSignalStrengthOfiPhoneX {
    NSInteger signalStrength = 0;
    //    if([[UIApplication sharedApplication] respondsToSelector:@selector(valueForKey:)]) {
    //        id statusBarView = [[UIApplication sharedApplication] valueForKey:@"statusBar"];
    //        if ([statusBarView respondsToSelector:@selector(subviews)]) {
    //            NSArray *statusBarViewArray = [statusBarView subviews];
    //            if ([statusBarViewArray respondsToSelector:@selector(lastObject)]) {
    //                id object = [statusBarViewArray lastObject];
    //                if ([object respondsToSelector:@selector(items)]) {
    //                    id statusBarItems = [object items];
    //                    if ([statusBarItems respondsToSelector:@selector(valueForKey:)]) {
    //                        id statusBarWifiItem = [statusBarItems valueForKey:@"_UIStatusBarWifiItem"];
    //                        NSArray * statusBarWifiPros = [SPUtils getAllProperties:statusBarWifiItem];
    //                        if (statusBarWifiPros.count && [statusBarWifiItem respondsToSelector:@selector(valueForKey:)]) {
    //                            id signalView = [statusBarWifiItem valueForKey:[statusBarWifiPros firstObject]];
    //                            if ([signalView respondsToSelector:@selector(numberOfActiveBars)]) {
    //                                signalStrength = [signalView performSelector:@selector(numberOfActiveBars)];
    //                            }
    //                        }
    //                    }
    //                }
    //            }
    //        }
    //    }
    return signalStrength;
}

- (CGFloat)getFullScreenPlayerWidth {
    CGSize screenSize = [[UIScreen mainScreen] bounds].size;
    return fmax(screenSize.width, screenSize.height);
}

- (long long)currentTime {
    return [[[NSDate alloc] init] timeIntervalSince1970] * 1000;
}

@end
