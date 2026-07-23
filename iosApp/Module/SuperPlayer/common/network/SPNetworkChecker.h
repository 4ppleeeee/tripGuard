/************************************************************
 Copyright (C), 1998-2018, Tencent Tech. Co., Ltd.
 FileName    : SPNetworkChecker.h
 Author      : 辰
 Version     : 1.0
 Date        : 13-5-30
 Description :
 History     : 13-5-30 初始版本
 ***********************************************************/

#import <Foundation/Foundation.h>
#import <CoreGraphics/CoreGraphics.h>

typedef enum {
    SPNetworkCheckerTypeSpeed                                 = 0x0001,
    SPNetworkCheckerTypeAvailable                             = 0x0010,
    SPNetworkCheckerTypeReachabilityChanged                   = 0x0100,
    SPNetworkCheckerTypeEnterForeground                       = 0x1000,
    SPNetworkCheckerTypeEnterForegroundAndReachabilityChanged = SPNetworkCheckerTypeEnterForeground |
                                                                 SPNetworkCheckerTypeReachabilityChanged,
    SPNetworkCheckerTypeAll                                   = SPNetworkCheckerTypeSpeed |
                                                                 SPNetworkCheckerTypeAvailable |
                                                                 SPNetworkCheckerTypeReachabilityChanged |
                                                                 SPNetworkCheckerTypeEnterForeground,
} SPNetworkCheckerType;

typedef enum {
    SPNetworkCheckerInternetStateUnknown = 0,
    SPNetworkCheckerInternetStateNO,
    SPNetworkCheckerInternetStateConnectingButNoInternet,
    SPNetworkCheckerInternetStateWWAN,
    SPNetworkCheckerInternetStateWLAN,
} SPNetworkCheckerInternetState;

typedef enum {
    SPNetworkCheckerCellNetTypeUnknown = 0,
    SPNetworkCheckerCellNetType2G      = 2,
    SPNetworkCheckerCellNetType3G      = 3,
    SPNetworkCheckerCellNetType4G      = 4,
    SPNetworkCheckerCellNetType5G      = 5,
} SPNetworkCheckerCellNetType;

typedef enum {
    SPNetworkCheckerNewNetTypeNetInavailable = 0,
    SPNetworkCheckerNewNetTypeWiFi           = 1,
    SPNetworkCheckerNewNetType2G             = 2,
    SPNetworkCheckerNewNetType3G             = 3,
    SPNetworkCheckerNewNetType4G             = 4,
    SPNetworkCheckerNewNetType5G             = 5,
} SPNetworkCheckerNewNetType;

//本地运营商标识
typedef NS_ENUM(NSUInteger, SPCarrierOperators) {
    SPCarrierOperatorsUnknown,
    SPCarrierOperatorsChinaMobile,
    SPCarrierOperatorsChinaUnicom,
    SPCarrierOperatorsChinaTelecom,
    SPCarrierOperatorsChinaTietong
};

@protocol SPNetworkCheckerDelegate <NSObject>
@optional
- (void)networkCheckerUpdateFinish;
- (void)reachabilityChanged;
- (void)appEnterForeground;
@end

//改模块处理异步的网络状态信息获取
@interface SPNetworkChecker : NSObject
@property (copy) NSString *pingHostName;

@property (retain) NSPointerArray *delegatesOfSpeed;
@property (retain) NSPointerArray *delegatesOfAvailablitiy;
@property (retain) NSPointerArray *delegatesOfReachabilityChanged;
@property (retain) NSPointerArray *delegatesOfEnterForeground;

@property (assign) BOOL speedIsValid;
@property (assign) CGFloat speed;
@property (assign) CGFloat avgSpeed;  //平均网速大小
@property (assign) CGFloat maxSpeed;  //最大网速，取自speed出现的最大值
@property (assign) NSTimeInterval lastMesureTime;
@property (assign) NSUInteger lastMesureBytes;

@property (assign) NSUInteger currentWWANBytes;

@property (assign) SPNetworkCheckerInternetState networkState;
@property (assign) SPNetworkCheckerInternetState preNetworkState;  //上一个网络状态
@property (assign) NSUInteger networkAvailableRetryCounter;

@property (retain) NSDate *lastDateOfGetNetworkState;

//蜂窝网络类型 __OSX_AVAILABLE_STARTING(__MAC_NA,__IPHONE_7_0);
//CTRadioAccessTechnologyGPRS 2.5G 下载 9.6 Kbps-85.6 Kbps 上传 9.6Kbps-42.8Kbps
//CTRadioAccessTechnologyEdge 作为一个2G和2.5G（GPRS）的延伸，有时被称为2.75G 最高数据速率是384Kbps
//CTRadioAccessTechnologyWCDMA 3G 2.4Mbps
//CTRadioAccessTechnologyHSDPA 称为3.5G，属于W-CDMA技术的延伸
//CTRadioAccessTechnologyHSUPA 因HSDPA上传速度不足（只有384Kb/s）不足而开发的，亦称为3.75G
//CTRadioAccessTechnologyCDMA1x 2G
//CTRadioAccessTechnologyCDMAEVDORev0  3G  2.4Mbps
//CTRadioAccessTechnologyCDMAEVDORevA 3G+ 3.1Mbps
//CTRadioAccessTechnologyCDMAEVDORevB 3G+ 9.3Mbps
//CTRadioAccessTechnologyeHRPD 3G+ 2.4Mbit/s
//CTRadioAccessTechnologyLTE 4G 最高100Mbps
@property (nonatomic, copy) NSString *radioATG;
@property (nonatomic, assign) SPNetworkCheckerCellNetType cellNetType;

+ (SPNetworkChecker *)sharedInstance;
+ (BOOL)networkAvailable;
+ (BOOL)activeWWAN;
+ (BOOL)activeWLAN;
- (void)startCheckingNetwork:(id<SPNetworkCheckerDelegate>)delegate type:(SPNetworkCheckerType)theType;
- (void)stopCheckingNetwork:(id<SPNetworkCheckerDelegate>)delegate type:(SPNetworkCheckerType)theType;
+ (int)reportNetType;
+ (int)newNetType;
- (void)simulate3G:(BOOL)shouldSimulate3G;
//add in V5.2 判断当前运营商
+ (SPCarrierOperators)getCarrierOperators;

@property (assign) BOOL isCanceled;

- (void)removeDelegateObject:(id)delegateObj;

@end
