/************************************************************
 Copyright (C), 1998-2018, Tencent Tech. Co., Ltd.
 FileName    : SPNetworkChecker.m
 Author      : 辰
 Version     : 1.0
 Date        : 13-5-30
 Description :
 History     : 13-5-30 初始版本
 ***********************************************************/

#import "SPNetworkChecker.h"
#import "SPReachability.h"
#import <CoreTelephony/CTTelephonyNetworkInfo.h>
#import <CoreTelephony/CTTelephonyNetworkInfo.h>
#import <CoreTelephony/CTCarrier.h>
//#import "SPReportCtlMgr.h"
#import "SPVcSystemInfo.h"
//#import "SPSDKConfigDefines.h"
#define NetworkAlwaysWWAN 0
#define kTADNotificationNetStatus @"TADNotificationNetStatus"

static NSString *const kPingHostName = @"www.qq.com";
static NSString *const kPingHostNameBackup = @"v.qq.com";
static NSString *const kPingHostNameVV = @"vv.video.qq.com";
static NSString *const kPingHostNameBKVV = @"bkvv.video.qq.com";

static const CGFloat kConditionWaitTime = 15.0f;

static NSArray *telecomMNCArray;
static NSArray *unicomMNCArray;
static NSArray *mobileMNCArray;
static NSArray *titongMNCArray;

static NSString *SPefreshConfigNotification = @"SPefreshConfigNotification";

@interface SPNetworkChecker () {
    CTTelephonyNetworkInfo *_netInfo;
    NSCondition *_mCondition;
}
@property (nonatomic, assign) BOOL forceSimulate3G;
@property (nonatomic, assign) NSUInteger speedMeasureTimes;  //网速计算次数,用于统计平均网速
@property (nonatomic, assign) double totalBps;               //统计的Bps之和，用于统计平均网速
@property (nonatomic, assign) float checkSpeedPeriod;        //检查网速的周期，默认是1S
@property (nonatomic, assign) NSUInteger resetSpeedPeroid;   //重置网速的周期，默认是60次重置

@end

@implementation SPNetworkChecker

@synthesize currentWWANBytes;

+ (SPNetworkChecker *)sharedInstance {
    static SPNetworkChecker *instance = nil;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        instance = [[SPNetworkChecker alloc] init];
    });

    return instance;
}

+ (void)initialize {
    [self updataOperatorConfigDic];
}

+ (void)updataOperatorConfigDic {
    NSDictionary *operatorConfigDic = nil;  // 后续再添加SPSDKCONF_OperatorMobileNetworkCodeList ;
    telecomMNCArray = [operatorConfigDic objectForKey:@"telecom"];
    unicomMNCArray = [operatorConfigDic objectForKey:@"unicom"];
    mobileMNCArray = [operatorConfigDic objectForKey:@"mobile"];
    titongMNCArray = [operatorConfigDic objectForKey:@"tietong"];
}

- (void)dealloc {
    [[NSNotificationCenter defaultCenter] removeObserver:self];

    self.pingHostName = nil;
    self.delegatesOfSpeed = nil;
    self.delegatesOfAvailablitiy = nil;
    self.delegatesOfReachabilityChanged = nil;
    self.delegatesOfEnterForeground = nil;

    _mCondition = nil;
    if (_netInfo != nil) {
        _netInfo = nil;
    }
}

static const void *TTRetainNoOp(CFAllocatorRef allocator, const void *value) { return value; }
static void TTReleaseNoOp(CFAllocatorRef allocator, const void *value) {}

NSMutableArray *SPCreateNonRetainingArray() {
    CFArrayCallBacks callbacks = kCFTypeArrayCallBacks;
    callbacks.retain = TTRetainNoOp;
    callbacks.release = TTReleaseNoOp;
    return (NSMutableArray *)CFBridgingRelease(CFArrayCreateMutable(nil, 0, &callbacks));
}

- (id)init {
    if (self = [super init]) {
        self.pingHostName = kPingHostName;

        self.isCanceled = NO;

        // 使用非 retain 的数组, 解播放器引用 ethanyxliu 20141020
        _delegatesOfSpeed = [NSPointerArray weakObjectsPointerArray];
        _delegatesOfAvailablitiy = [NSPointerArray weakObjectsPointerArray];
        _delegatesOfReachabilityChanged = [NSPointerArray weakObjectsPointerArray];
        _delegatesOfEnterForeground = [NSPointerArray weakObjectsPointerArray];
        
//        CGFloat checkingPeroid = SPSDKCONF_check_network_speed_peroid_ms / 1000.0f;
//        if (checkingPeroid < 0.5) {
//            checkingPeroid = 1.0f;
//        }
        CGFloat checkingPeroid = 1;
        
        self.checkSpeedPeriod = checkingPeroid;
//        NSUInteger resetSpeedPeroid = SPSDKCONF_reset_network_speed_peroid;
//        if (resetSpeedPeroid <= 0) {
//            resetSpeedPeroid = 60;
//        }
        NSUInteger resetSpeedPeroid = 60;
        
        self.resetSpeedPeroid = resetSpeedPeroid;
        self.networkState = [self getCurrentNetworkState:NO];

        self.currentWWANBytes = 0;
        self.speedMeasureTimes = 0;
        self.totalBps = 0.0;
        NSUInteger wifiSent = 0;
        NSUInteger wifiReceived = 0;
        NSUInteger wwanSent = 0;
        NSUInteger wwanReceived = 0;
        if ([[UIDevice currentDevice] spGetDataCounters:&wifiSent:&wifiReceived:&wwanSent:&wwanReceived]) {
            self.currentWWANBytes = wwanSent + wwanReceived;
        }

        [[NSNotificationCenter defaultCenter] addObserver:self
                                                 selector:@selector(reachabilityChanged:)
                                                     name:kSPReachabilityChangedNotification
                                                   object:nil];

        [[NSNotificationCenter defaultCenter] addObserver:self
                                                 selector:@selector(appEnterForeground:)
                                                     name:UIApplicationWillEnterForegroundNotification
                                                   object:nil];

        _mCondition = [[NSCondition alloc] init];
        [NSThread detachNewThreadSelector:@selector(updateOnThread) toTarget:self withObject:nil];

        self.radioATG = @"unknown";
        self.cellNetType = SPNetworkCheckerCellNetTypeUnknown;

        _netInfo = nil;
        // 监测所用无线访问技术的变化
        //        if (@available(iOS 12.0, *)) {
        //            if (nil !=
        //            (&CTServiceRadioAccessTechnologyDidChangeNotification)) {
        //                _netInfo = [[CTTelephonyNetworkInfo alloc] init];
        //                [[NSNotificationCenter defaultCenter] addObserver:self
        //                                                         selector:@selector(handleCTRadioAccessTechnologyDidChangeNotification)
        //                                                             name:CTServiceRadioAccessTechnologyDidChangeNotification
        //                                                           object:nil];
        //                [self updateRadioATG];
        //            }
        //        } else {
        //            if (nil !=
        //            (&CTRadioAccessTechnologyDidChangeNotification)) {
        //                _netInfo = [[CTTelephonyNetworkInfo alloc] init];
        //                [[NSNotificationCenter defaultCenter] addObserver:self
        //                                                         selector:@selector(handleCTRadioAccessTechnologyDidChangeNotification)
        //                                                             name:CTRadioAccessTechnologyDidChangeNotification
        //                                                           object:nil];
        //                [self updateRadioATG];
        //            }
        //        }

        // add in V5.3 20161214 多终端配置更新时要刷新运营商MNC配置
        [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(handleConfigUpdate:) name:SPefreshConfigNotification object:nil];
    }
    return self;
}

- (void)handleConfigUpdate:(NSNotification *)noti {
    [[self class] updataOperatorConfigDic];
}

- (void)updateRadioATG {
    if (![[SPVcSystemInfo sharedInstance] isIOS7OrLatter]) {
        return;
    }
    
    NSString *atg = @"unknown";
    if (@available(iOS 12.0, *)) {
      atg = [_netInfo.serviceCurrentRadioAccessTechnology.allValues
                 .firstObject copy];
    } else {
      atg = [_netInfo.currentRadioAccessTechnology copy];
    }
    self.radioATG = atg;

    //更新蜂窝网类型
    self.cellNetType = [self.class cellNetTypeWithRadioAccessTechnology:atg];
}

+ (SPNetworkCheckerCellNetType)cellNetTypeWithRadioAccessTechnology:(NSString *)radioAccessTechnology {
    if (![radioAccessTechnology isKindOfClass:[NSString class]]) {
        return SPNetworkCheckerCellNetTypeUnknown;
    }
    if ([self is2GWithRadioAccessTechnology:radioAccessTechnology]) {
        return SPNetworkCheckerCellNetType2G;
    }
    if ([self is3GWithRadioAccessTechnology:radioAccessTechnology]) {
        return SPNetworkCheckerCellNetType3G;
    }
    if ([radioAccessTechnology isEqualToString:CTRadioAccessTechnologyLTE]) {
        return SPNetworkCheckerCellNetType4G;
    }
    if ([self is5GWithRadioAccessTechnology:radioAccessTechnology]) {
        return SPNetworkCheckerCellNetType5G;
    }
    return SPNetworkCheckerCellNetTypeUnknown;
}

+ (BOOL)is2GWithRadioAccessTechnology:(NSString *)radioAccessTechnology {
  if ([radioAccessTechnology isEqualToString:CTRadioAccessTechnologyGPRS] ||
      [radioAccessTechnology isEqualToString:CTRadioAccessTechnologyEdge] ||
      [radioAccessTechnology isEqualToString:CTRadioAccessTechnologyCDMA1x]) {
    return YES;
  }
  return NO;
}

+ (BOOL)is3GWithRadioAccessTechnology:(NSString *)radioAccessTechnology {
  if ([radioAccessTechnology isEqualToString:CTRadioAccessTechnologyWCDMA] ||
      [radioAccessTechnology isEqualToString:CTRadioAccessTechnologyHSDPA] ||
      [radioAccessTechnology isEqualToString:CTRadioAccessTechnologyHSUPA] ||
      [radioAccessTechnology
          isEqualToString:CTRadioAccessTechnologyCDMAEVDORev0] ||
      [radioAccessTechnology
          isEqualToString:CTRadioAccessTechnologyCDMAEVDORevA] ||
      [radioAccessTechnology
          isEqualToString:CTRadioAccessTechnologyCDMAEVDORevB] ||
      [radioAccessTechnology isEqualToString:CTRadioAccessTechnologyeHRPD]) {
    return YES;
  }
  return NO;
}

+ (BOOL)is5GWithRadioAccessTechnology:(NSString *)radioAccessTechnology {
//#if TARGET_IPHONE_SIMULATOR
//    //微视主干合流流水线报x86找不到下面2个枚举，模拟器下屏蔽--jamieling
//    return NO;
//#endif
//    if (@available(iOS 14.0, *)) {
//        if ([radioAccessTechnology isEqualToString:CTRadioAccessTechnologyNRNSA] ||
//            [radioAccessTechnology isEqualToString:CTRadioAccessTechnologyNR]) {
//            return YES;
//        }
//    }
    return NO;
}

+ (BOOL)networkAvailable {
    switch ([SPNetworkChecker sharedInstance].networkState) {
        case SPNetworkCheckerInternetStateWLAN:
        case SPNetworkCheckerInternetStateWWAN:
            return YES;

        default:
            return NO;
    }
}

+ (BOOL)activeWWAN {
#ifdef DEBUG
#if NetworkAlwaysWWAN
    return YES;
#endif
    if ([SPNetworkChecker sharedInstance].forceSimulate3G) {
        return YES;
    }
#endif
    // 不能使用 "#ifdef TARGET_IPHONE_SIMULATOR"，因为在device上TARGET_IPHONE_SIMULATOR被定义为0
    //#if TARGET_IPHONE_SIMULATOR
    //    srand((unsigned)time(0));
    //    int rand =  arc4random() % 9;
    //    if (rand == 0) {
    //        SPLogIS(@"activeWWAN");
    //        return YES;
    //    }
    //    return YES;
    //#endif
    switch ([SPNetworkChecker sharedInstance].networkState) {
        case SPNetworkCheckerInternetStateWWAN:
            return YES;

        default:
            return NO;
    }
}

+ (BOOL)activeWLAN {
#ifdef DEBUG
#if NetworkAlwaysWWAN
    return NO;
#endif
    if ([SPNetworkChecker sharedInstance].forceSimulate3G) {
        return NO;
    }
#endif

    //#ifdef TARGET_IPHONE_SIMULATOR
    //    return YES;
    //#endif
    switch ([SPNetworkChecker sharedInstance].networkState) {
        case SPNetworkCheckerInternetStateWLAN:
            return YES;

        default:
            return NO;
    }
}

+ (int)reportNetType {
    if ([SPNetworkChecker networkAvailable]) {
        if ([SPNetworkChecker activeWWAN]) {
            if ([SPNetworkChecker sharedInstance].cellNetType == SPNetworkCheckerCellNetType2G) {
                return 1;
            } else if ([SPNetworkChecker sharedInstance].cellNetType == SPNetworkCheckerCellNetType3G) {
                return 2;
            }
            return 5;
        } else if ([SPNetworkChecker activeWLAN]) {
            return 3;
        }
    }
    return 4;
}

+ (int)newNetType {
    if ([SPNetworkChecker networkAvailable]) {
        if ([SPNetworkChecker activeWWAN]) {
            switch ([SPNetworkChecker sharedInstance].cellNetType) {
                case SPNetworkCheckerCellNetType2G:
                    return SPNetworkCheckerNewNetType2G;
                    break;
                case SPNetworkCheckerCellNetType3G:
                    return SPNetworkCheckerNewNetType3G;
                    break;
                case SPNetworkCheckerCellNetType4G:
                    return SPNetworkCheckerNewNetType4G;
                    break;
                case SPNetworkCheckerCellNetType5G:
                    return SPNetworkCheckerNewNetType5G;
                    break;
                default:
                    return SPNetworkCheckerNewNetTypeNetInavailable;
                    break;
            }
        } else if ([SPNetworkChecker activeWLAN]) {
            return SPNetworkCheckerNewNetTypeWiFi;
        }
    }
    return SPNetworkCheckerNewNetTypeNetInavailable;
}

- (void)simulate3G:(BOOL)shouldSimulate3G {
    self.forceSimulate3G = shouldSimulate3G;
}

- (void)handleCTRadioAccessTechnologyDidChangeNotification {
  //    [self updateRadioATG];
}

- (void)removeDelegateObject:(id)delegateObj
{
    [self removeDelegateObject:delegateObj withDelegateArray:self.delegatesOfSpeed];
    [self removeDelegateObject:delegateObj withDelegateArray:self.delegatesOfAvailablitiy];
    [self removeDelegateObject:delegateObj withDelegateArray:self.delegatesOfReachabilityChanged];
    [self removeDelegateObject:delegateObj withDelegateArray:self.delegatesOfEnterForeground];
}

- (void)addDelegateObject:(id)delegateObj withDelegateArray:(NSPointerArray *)array
{
    if (!delegateObj)
    {
        return;
    }
    
    for (id tmpDelegate in array)
    {
        if (tmpDelegate == delegateObj)
        {
            return;
        }
    }
    
    [array addPointer:(__bridge void* _Nullable)delegateObj];
}

- (void)removeDelegateObject:(id)delegateObj withDelegateArray:(NSPointerArray *)array
{
    if (!delegateObj)
    {
        return;
    }
    
    for (int i = 0; i < array.count; ++i)
    {
        id tmpDelegate = [array pointerAtIndex:i];
        if (tmpDelegate == delegateObj)
        {
            [array removePointerAtIndex:i];
            break;
        }
    }
}

- (void)startCheckingNetwork:(id<SPNetworkCheckerDelegate>)delegate type:(SPNetworkCheckerType)theType {
    @synchronized(self) {
        if (theType & SPNetworkCheckerTypeSpeed) {
                [_mCondition lock];
                [self addDelegateObject:delegate withDelegateArray:self.delegatesOfSpeed];
                [_mCondition signal];
                [_mCondition unlock];
        }
        if (theType & SPNetworkCheckerTypeAvailable) {
            [self addDelegateObject:delegate withDelegateArray:self.delegatesOfAvailablitiy];
        }
        if (theType & SPNetworkCheckerTypeReachabilityChanged) {
            [self addDelegateObject:delegate withDelegateArray:self.delegatesOfReachabilityChanged];
        }
        if (theType & SPNetworkCheckerTypeEnterForeground) {
            [self addDelegateObject:delegate withDelegateArray:self.delegatesOfEnterForeground];
        }
    }
}

- (void)stopCheckingNetwork:(id<SPNetworkCheckerDelegate>)delegate type:(SPNetworkCheckerType)theType {
    @synchronized(self) {
        if (theType & SPNetworkCheckerTypeSpeed) {
            [self removeDelegateObject:delegate withDelegateArray:self.delegatesOfSpeed];
        }
        if (theType & SPNetworkCheckerTypeAvailable) {
            [self removeDelegateObject:delegate withDelegateArray:self.delegatesOfAvailablitiy];
            if (self.delegatesOfAvailablitiy.count <= 0 && self.networkState == SPNetworkCheckerInternetStateConnectingButNoInternet) {
                self.preNetworkState = self.networkState;
                self.networkState = [self getCurrentNetworkState:NO];
            }
        }
        if (theType & SPNetworkCheckerTypeReachabilityChanged) {
            [self removeDelegateObject:delegate withDelegateArray:self.delegatesOfReachabilityChanged];
        }
        if (theType & SPNetworkCheckerTypeEnterForeground) {
            [self removeDelegateObject:delegate withDelegateArray:self.delegatesOfEnterForeground];
        }
    }
}

- (SPNetworkCheckerInternetState)getCurrentNetworkState:(BOOL)ping {
    self.lastDateOfGetNetworkState = [NSDate date];
    if ([UIDevice spNetworkAvailable]) {
        if (ping && self.pingHostName.length > 0) {
          if ([self isConnectingButNoInternetWithPingHostName:
                        self.pingHostName]) {
            return SPNetworkCheckerInternetStateConnectingButNoInternet;
          }
        }

        if ([UIDevice spActiveWLAN]) {
            return SPNetworkCheckerInternetStateWLAN;
        } else if ([UIDevice spActiveWWAN]) {
            return SPNetworkCheckerInternetStateWWAN;
        } else {
            return SPNetworkCheckerInternetStateUnknown;
        }
    }
    return SPNetworkCheckerInternetStateNO;
}

- (BOOL)isConnectingButNoInternetWithPingHostName:(NSString *)pingHostName {
  SPReachability *reached =
      [SPReachability reachabilityWithHostName:self.pingHostName];
  if (reached.isReachable) {
    return NO;
  }
  reached = [SPReachability reachabilityWithHostName:kPingHostNameBackup];
  if (reached.isReachable) {
    return NO;
  }
  reached = [SPReachability reachabilityWithHostName:kPingHostNameVV];
  if (reached.isReachable) {
    return NO;
  }
  reached = [SPReachability reachabilityWithHostName:kPingHostNameBKVV];
  if (reached.isReachable) {
    return NO;
  }
  return YES;
}

- (BOOL)shouldChangeForSpeed {
    if (self.delegatesOfSpeed.count <= 0) {
        self.lastMesureTime = 0.0f;
        self.lastMesureBytes = 0.0f;
        self.speedIsValid = NO;
        return NO;
    } else if (![SPNetworkChecker networkAvailable]) {
        self.lastMesureTime = 0.0f;
        self.lastMesureBytes = 0.0f;
        self.speed = 0.0f;
        self.maxSpeed = 0.0f;
        self.speedIsValid = YES;
        return NO;
    }
    return YES;
}

- (BOOL)shouldChangeForNetworkRecheck {
    if (self.delegatesOfAvailablitiy.count > 0 || ![SPNetworkChecker networkAvailable]) {
        NSDate *dateNow = [NSDate date];
        if (!self.lastDateOfGetNetworkState || [dateNow timeIntervalSinceDate:self.lastDateOfGetNetworkState] > kConditionWaitTime) {
            return YES;
        }
    }
    return NO;
}

- (void)updateOnThread {
    while (!self.isCanceled) {
        @autoreleasepool {
            [_mCondition lock];
            BOOL shouldChangeForSpeed = [self shouldChangeForSpeed];
            BOOL shouldChangeForNetworkRecheck = [self shouldChangeForNetworkRecheck];
            while (!shouldChangeForNetworkRecheck && !shouldChangeForSpeed) {
                @autoreleasepool {
                    [_mCondition waitUntilDate:[NSDate dateWithTimeIntervalSinceNow:kConditionWaitTime]];
                    shouldChangeForSpeed = [self shouldChangeForSpeed];
                    shouldChangeForNetworkRecheck = [self shouldChangeForNetworkRecheck];
                }
            }
            [_mCondition unlock];

            //获取网速
            if (shouldChangeForSpeed) {
              [self internalUpdateNetworSpeed];
            }

            //网路状态
            if (shouldChangeForNetworkRecheck) {
                self.preNetworkState = self.networkState;
                self.networkState = [self getCurrentNetworkState:YES];
            }

            [self performSelectorOnMainThread:@selector(didUpdate) withObject:nil waitUntilDone:NO];
        }

        sleep(self.checkSpeedPeriod);
    }
}

- (void)internalUpdateNetworSpeed {
  NSTimeInterval currentMesureTime = [[NSDate date] timeIntervalSince1970];
  NSUInteger wifiSent = 0;
  NSUInteger wifiReceived = 0;
  NSUInteger wwanSent = 0;
  NSUInteger wwanReceived = 0;
    if ([[UIDevice currentDevice] spGetDataCounters:&
                                           wifiSent:&
                                       wifiReceived:&
                                           wwanSent:&wwanReceived]) {
        self.currentWWANBytes = wwanSent + wwanReceived;
  }

  NSUInteger currentMesureBytes = wifiReceived + wwanReceived;

  if (self.lastMesureTime <= 0) {
    self.speedIsValid = NO;
  } else {
    self.speed = (currentMesureBytes - self.lastMesureBytes) /
                 (currentMesureTime - self.lastMesureTime) / 1000.0f;

    if (self.speedMeasureTimes >
        self.resetSpeedPeroid) { //达到周期后，重置平均网速等
      self.speedMeasureTimes = 0;
      self.totalBps = 0;
      self.avgSpeed = 0;
      self.maxSpeed = 0;
    }
    if (self.speed > self.maxSpeed) {
      self.maxSpeed = self.speed;
    }
    self.speedMeasureTimes++;
    self.totalBps += self.speed;
    if (self.speedMeasureTimes > 0) {
      self.avgSpeed = self.totalBps / self.speedMeasureTimes;
    }

    self.speedIsValid = YES;
  }
  self.lastMesureTime = currentMesureTime;
  self.lastMesureBytes = currentMesureBytes;
  self.currentWWANBytes = wwanSent + wwanReceived;
  // NSLog(@"currentWWANBYTE=%uKB", self.currentWWANBytes/1024);
}

- (void)didUpdate {
    NSPointerArray *listDelegates = [NSPointerArray weakObjectsPointerArray];
    @synchronized(self) {
        for (id tmpDelegate in self.delegatesOfAvailablitiy)
        {
            [listDelegates addPointer:(__bridge void* _Nullable)tmpDelegate];
        }
        
        if (self.speedIsValid) {
            for (id delegate in self.delegatesOfSpeed) {
                [self addDelegateObject:delegate withDelegateArray:listDelegates];
            }
        }
    }

    for (id<SPNetworkCheckerDelegate> delegate in listDelegates) {
        if ([delegate respondsToSelector:@selector(networkCheckerUpdateFinish)]) {
            [delegate networkCheckerUpdateFinish];
        }
    }
}

- (void)reachabilityChanged:(NSNotification *)notify {
    if (![NSThread isMainThread]) {
        dispatch_async(dispatch_get_main_queue(), ^{
            [self reachabilityChanged:notify];
        });

        return;
    }
    self.preNetworkState = self.networkState;
    self.networkState = [self getCurrentNetworkState:NO];
    SPLOGI(SP_PLAYER_LOG_FILTER, @"preNetworkState :%d, current networkState:%d", self.preNetworkState, self.networkState);
    
    NSPointerArray *listDelegates = [NSPointerArray weakObjectsPointerArray];
    @synchronized(self) {
        for (id tmpDelegate in self.delegatesOfReachabilityChanged)
        {
            [listDelegates addPointer:(__bridge void* _Nullable)tmpDelegate];
        }
    }
    
    for (id<SPNetworkCheckerDelegate> delegate in listDelegates) {
        if ([delegate respondsToSelector:@selector(reachabilityChanged)]) {
            [delegate reachabilityChanged];
        }
    }
}

- (void)appEnterForeground:(NSNotification *)notify {
    self.preNetworkState = self.networkState;
    self.networkState = [self getCurrentNetworkState:NO];
    NSPointerArray *listDelegates = [NSPointerArray weakObjectsPointerArray];
    @synchronized(self) {
        for (id tmpDelegate in self.delegatesOfEnterForeground)
        {
            [listDelegates addPointer:(__bridge void* _Nullable)tmpDelegate];
        }
    }
        
    for (id<SPNetworkCheckerDelegate> delegate in listDelegates) {
        if ([delegate respondsToSelector:@selector(appEnterForeground)]) {
            [delegate appEnterForeground];
        }
    }
}

+ (SPCarrierOperators)getCarrierOperators {
    CTTelephonyNetworkInfo *netInfo = [[CTTelephonyNetworkInfo alloc] init];
    if (netInfo) {
        CTCarrier *carrier = [netInfo subscriberCellularProvider];
        if (carrier) {
            NSString *mobileNetworkCode = [carrier mobileNetworkCode];

            if (mobileNetworkCode.length) {
                SPLOGI(@"NetworkChecher", @"mobileCountryCode=%@ ,carrierName=%@, mobileNetworkCode = %d", [carrier mobileCountryCode],
                        [carrier carrierName], [mobileNetworkCode intValue]);
                if ([telecomMNCArray containsObject:mobileNetworkCode]) {
                    return SPCarrierOperatorsChinaTelecom;
                } else if ([unicomMNCArray containsObject:mobileNetworkCode]) {
                    return SPCarrierOperatorsChinaUnicom;
                } else if ([mobileMNCArray containsObject:mobileNetworkCode]) {
                    return SPCarrierOperatorsChinaMobile;
                } else if ([titongMNCArray containsObject:mobileNetworkCode]) {
                    return SPCarrierOperatorsChinaTietong;
                } else {
                    // add in 5.3 ,如果判断运营商失败，则上报，用于统计次数 20161212 georgema
//                    [[SPReportCtlMgr sharedInstance]
//                        reportEventIdentifier:@"app_get_operators_exception"
//                                       params:@{
//                                           @"mobileCountryCode" : [carrier mobileCountryCode] ? [carrier mobileCountryCode] : @"",
//                                           @"carrierName" : [carrier carrierName] ? [carrier carrierName] : @"",
//                                           @"mobileNetworkCode" : [NSNumber numberWithInt:[mobileNetworkCode intValue]]
//                                       }];
                }
            }
        }
    }
    return SPCarrierOperatorsUnknown;
}

@end
