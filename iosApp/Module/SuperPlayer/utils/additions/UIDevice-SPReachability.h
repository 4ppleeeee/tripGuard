/*
 Erica Sadun, http://ericasadun.com
 iPhone Developer's Cookbook, 3.0 Edition
 BSD License for anything not specifically marked as developed by a third party.
 Apple's code excluded.
 Use at your own risk
 
 Copyright (C) 2020 ericasadun Inc. All Rights Reserved.
 */

#import <UIKit/UIKit.h>
@interface UIDevice (Reachability)
+ (NSString *) spLocalWiFiIPAddress;
+ (NSString *) spGetDeviceIPAddresses;
+ (BOOL)spNetworkAvailable;

+ (BOOL)spActiveWLAN;
+ (BOOL)spActiveWWAN;

+ (BOOL)spIsInUSA;
+(BOOL)spIsInChina;
+(BOOL)spIsInHongkong;
+(BOOL)spIsInTaiwan;

+ (NSString *)spMobileCountryCode;

+ (NSString *)spMobileNetworkCode;

-(NSUInteger)spGetBytesIn;
-(NSUInteger)spGetBytesOut;

- (BOOL)spGetDataCounters:(NSUInteger*)wifiSent :(NSUInteger*)wifiReceived :(NSUInteger*)wwanSent :(NSUInteger*)wwanReceived;

+ (NSString *)spGetIPAddressByHostName:(NSString *)domain;

@end
