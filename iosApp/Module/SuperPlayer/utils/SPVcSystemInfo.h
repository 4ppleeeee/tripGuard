/************************************************************
 Copyright (C), 1998-2018, Tencent Tech. Co., Ltd.
 FileName    : VcSystemInfo.h
 Author      : ethanyxliu
 Version     : 1.0
 Date        : 17/3/6
 Description : 获取系统设备相关信息
 History     : 17/3/6 初始版本
 ***********************************************************/

#import <Foundation/Foundation.h>

#define SP_IFPGA_NAMESTRING @"iFPGA"

#define SP_IPHONE_2G_NAMESTRING @"iPhone 2G"
#define SP_IPHONE_3G_NAMESTRING @"iPhone 3G"
#define SP_IPHONE_3GS_NAMESTRING @"iPhone 3GS"
#define SP_IPHONE_4_NAMESTRING @"iPhone 4"
#define SP_IPHONE_4S_NAMESTRING @"iPhone 4S"
#define SP_IPHONE_5_NAMESTRING @"iPhone 5"
#define SP_IPHONE_5C_NAMESTRING @"iPhone 5c"
#define SP_IPHONE_5S_NAMESTRING @"iPhone 5s"
#define SP_IPHONE_6_NAMESTRING @"iPhone 6"
#define SP_IPHONE_6_PLUS_NAMESTRING @"iPhone 6 plus"
#define SP_IPHONE_6S_NAMESTRING @"iPhone 6s"
#define SP_IPHONE_6S_PLUS_NAMESTRING @"iPhone 6s plus"
#define SP_IPHONE_SE_NAMESTRING @"iPhone SE"
#define SP_IPHONE_7_NAMESTRING @"iPhone 7"
#define SP_IPHONE_7_PLUS_NAMESTRING @"iPhone 7 plus"

#define SP_IPHONE_UNKNOWN_NAMESTRING @"Unknown iPhone"

#define SP_IPOD_1G_NAMESTRING @"iPod touch 1G"
#define SP_IPOD_2G_NAMESTRING @"iPod touch 2G"
#define SP_IPOD_3G_NAMESTRING @"iPod touch 3G"
#define SP_IPOD_4G_NAMESTRING @"iPod touch 4G"
#define SP_IPOD_5G_NAMESTRING @"iPod touch 5G"
#define SP_IPOD_UNKNOWN_NAMESTRING @"Unknown iPod"

#define SP_IPAD_1G_NAMESTRING @"iPad 1G"
#define SP_IPAD_2_NAMESTRING @"iPad 2"
#define SP_IPAD_3_NAMESTRING @"iPad 3"
#define SP_IPAD_4_NAMESTRING @"iPad 4"
#define SP_IPAD_AIR_NAMESTRING @"iPad Air"
#define SP_IPAD_AIR2_NAMESTRING @"iPad Air2"
#define SP_IPAD_MINI_1G_NAMESTRING @"iPad mini 1G"
#define SP_IPAD_MINI_2_NAMESTRING @"iPad mini 2"
#define SP_IPAD_MINI_3_NAMESTRING @"iPad mini 3"
#define SP_IPAD_UNKNOWN_NAMESTRING @"Unknown iPad"

#define SP_APPLETV_2G_NAMESTRING @"Apple TV 2G"
#define SP_APPLETV_3G_NAMESTRING @"Apple TV 3G"
#define SP_APPLETV_4G_NAMESTRING @"Apple TV 4G"
#define SP_APPLETV_UNKNOWN_NAMESTRING @"Unknown Apple TV"

#define SP_IOS_FAMILY_UNKNOWN_DEVICE @"Unknown iOS device"

#define SP_SIMULATOR_NAMESTRING @"iPhone Simulator"
#define SP_SIMULATOR_IPHONE_NAMESTRING @"iPhone Simulator"
#define SP_SIMULATOR_IPAD_NAMESTRING @"iPad Simulator"
#define SP_SIMULATOR_APPLETV_NAMESTRING @"Apple TV Simulator"  // :)

typedef NS_ENUM(NSUInteger, SPPlatformType) {
    SPPlatformTypeUnknown,
    SPPlatformTypeiPhone,
    SPPlatformTypeiPad,
    SPPlatformTypeTV,
};

@interface SPVcSystemInfo : NSObject

@property (nonatomic, copy) NSString *localGuid;                      // 此guid是sdk从后台配置下发获取到的guid.除非是sdkconfig的配置请求中guid,
                                                                      // guid的获取统一从SPSDKParamsMgr中的guid获取，因为guid可能是外部设置的.
@property (nonatomic, assign) NSInteger sstrength;                    // 设备网络信号强度
@property (nonatomic, copy) NSString *deviceMachineFamily;            //设备家族名称。形式如iPhone 6s
@property (nonatomic, copy) NSString *deviceMachineConventionalName;  //设备详细名称。 形式如iPhone 5s (Global)
@property (nonatomic, copy) NSString *resolution;                     //设备的分辨率。格式为"宽*高"
@property (nonatomic, assign) CGFloat screenWidth;                    // 屏幕的宽
@property (nonatomic, assign) CGFloat screenHeight;                   // 屏幕的高
@property (nonatomic, copy) NSString *systemVer;                      // 系统版本.[UIDevice currentDevice].systemVersion
@property (nonatomic, copy) NSString *osVer;                          //将systemvew转化为固定的三位，不够的补齐，多余的移除末尾的,后台只认三位.例如:9.3转为9.3.0，9.3.3.3转为9.3.3
@property (nonatomic, copy) NSString *appver;                         //APP版本号。固定四位，mainAppVer+build号。形式如6.2.0.1009
@property (nonatomic, copy) NSString *mainAppVer;                     //APP主版本号，固定三位。形式如6.2.0
@property (nonatomic, copy) NSString *buildVersion;                   //APP主build号，比如6.2.0.1009，1009即build号
@property (nonatomic, copy) NSString *deviceMachine;                  //设备机型，名称如iPhone11,8
@property (nonatomic, copy) NSString *deviceMachineMainCode;          //如iPhone11,8，则MainCode=11
@property (nonatomic, copy) NSString *deviceMachineSubCode;          //如iPhone11,8，则SubCode=8
@property (nonatomic, copy) NSString *deviceModel;                    //设备型号.A1700
@property (nonatomic, copy) NSString *bundlId;                        //app的bundle id
@property (nonatomic, copy) NSString *idfv;                           //IDFV
@property (nonatomic, copy) NSString *deviceId;                       //设备Id,即guid
@property (nonatomic, assign) BOOL isAllowsArbitraryLoads;            //allowsArbitraryLoad
@property (nonatomic, copy) NSString *macAddress;                     //设备的mac address
@property (nonatomic, assign) NSUInteger cpuCount;                    //cpu数量
@property (nonatomic, assign) NSUInteger cpuFrequency;                //cpu频率
@property (nonatomic, assign) NSUInteger cpuSubtype;                  //cpu subtype
@property (nonatomic, assign) BOOL isJBOS;                            //判断系统是否越狱，不使用Jailbreak命名,避免被苹果监测到
@property (nonatomic, assign) BOOL isRetinaDisplay;                   //是否是视网膜屏幕
+ (SPVcSystemInfo *)sharedInstance;

- (long long)currentTime;

/**
 更新GUID，即device Id. guid是后台下发的。此处是保存到设备中

 @param guid guid
 */
- (void)updateLocalGuid:(NSString *)guid;
// IOS 系统判断相关接口
- (BOOL)isIOS93OrLatter;
- (BOOL)isIOS901OrLatter;
- (BOOL)isIOS10OrLatter;
- (BOOL)isIOS9OrLatter;
- (BOOL)isIOS8OrLatter;
- (BOOL)isIOS7OrLatter;
- (BOOL)isIOS6OrLatter;
- (BOOL)isIOS9;

- (BOOL)deviceGreaterThan:(NSString *)deviceMachine;

- (BOOL)deviceGreaterThanOrEqualTo:(NSString *)deviceMachine;

- (BOOL)deviceEqualTo:(NSString *)deviceMachine;

- (BOOL)deviceLessThan:(NSString *)deviceManchine;

- (BOOL)deviceLessThanOrEqualTo:(NSString *)deviceManchine;

- (SPPlatformType)platformType;

@end

/**
 * 系统版本判断, 使用系统版本字符串转为数字类型再进行比较。所以只能比较两位。字符串的比较请使用
 * SYSTEM_VERSION_EQUAL_TO等系列
 */

// 小于某个版本号
#define SYS_VER_LESS_THAN(ver) \
    ([[[SPVcSystemInfo sharedInstance] systemVer] floatValue] < ver)

// 小于等于某个版本号
#define SYS_VER_LESS_EQUAL(ver) \
    ([[[SPVcSystemInfo sharedInstance] systemVer] floatValue] <= ver)

// 大于某个版本号
#define SYS_VER_MORE_THAN(ver) \
    ([[[SPVcSystemInfo sharedInstance] systemVer] floatValue] > ver)

// 大于等于某个版本号
#define SYS_VER_MORE_EQUAL(ver) \
    ([[[SPVcSystemInfo sharedInstance] systemVer] floatValue] >= ver)

/**
 *  系统版本判断, 字符串比较。可用于三位数的比较，比如5.0.1
 */

// 等于某个版本号
#define SYSTEM_VERSION_EQUAL_TO(v) \
    ([[[UIDevice currentDevice] systemVersion] compare:v options:NSNumericSearch] == NSOrderedSame)

// 大于某个版本号
#define SYSTEM_VERSION_GREATER_THAN(v) \
    ([[[UIDevice currentDevice] systemVersion] compare:v options:NSNumericSearch] == NSOrderedDescending)

// 大于等于某个版本号
#define SYSTEM_VERSION_GREATER_THAN_OR_EQUAL_TO(v) \
    ([[[UIDevice currentDevice] systemVersion] compare:v options:NSNumericSearch] != NSOrderedAscending)

// 小于某个版本号
#define SYSTEM_VERSION_LESS_THAN(v) \
    ([[[UIDevice currentDevice] systemVersion] compare:v options:NSNumericSearch] == NSOrderedAscending)

// 小于等于某个版本号
#define SYSTEM_VERSION_LESS_THAN_OR_EQUAL_TO(v) \
    ([[[UIDevice currentDevice] systemVersion] compare:v options:NSNumericSearch] != NSOrderedDescending)
