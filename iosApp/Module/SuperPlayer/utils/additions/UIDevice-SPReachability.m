/*
 Erica Sadun, http://ericasadun.com
 iPhone Developer's Cookbook, 3.0 Edition
 BSD License for anything not specifically marked as developed by a third party.
 Apple's code excluded.
 Use at your own risk
 
 Copyright (C) 2020 ericasadun Inc. All Rights Reserved.
 */

#import <SystemConfiguration/SystemConfiguration.h>
#include <arpa/inet.h>
#include <netdb.h>
#include <net/if.h>
#include <ifaddrs.h>
#import "UIDevice-SPReachability.h"
#import <CoreTelephony/CTTelephonyNetworkInfo.h>
#import <CoreTelephony/CTCarrier.h>

#define IOS_CELLULAR    @"pdp_ip0"
#define IOS_WIFI        @"en0"
//#define IOS_VPN       @"utun0"
#define IP_ADDR_IPv4    @"ipv4"
#define IP_ADDR_IPv6    @"ipv6"

static CTTelephonyNetworkInfo *networkInfo;

@implementation UIDevice (Reachability)
SCNetworkConnectionFlags tVKConnectionFlags;

// Matt Brown's get WiFi IP addy solution
// http://mattbsoftware.blogspot.com/2009/04/how-to-get-ip-address-of-iphone-os-v221.html
+ (NSString *)spLocalWiFiIPAddress
{
    BOOL success;
    struct ifaddrs * addrs;
    const struct ifaddrs * cursor;
    NSString * waddr = nil;
    
    success = (getifaddrs(&addrs) == 0);
    if (success) {
        cursor = addrs;
        //waddr必为nil 此处判断多余 tencent:winterlong(20170425)
        //if (waddr == nil || [@"" isEqualToString:waddr]) {
        while (cursor != NULL) {
            if (cursor->ifa_addr->sa_family == AF_INET && (cursor->ifa_flags & IFF_LOOPBACK) == 0) {
                NSString * name = [NSString stringWithUTF8String:cursor->ifa_name];
                if ([name hasPrefix:@"en"]) {//Wifi adapter
                    char addrNamev4[INET_ADDRSTRLEN];
                    const struct sockaddr_in * ipv4 = (const struct sockaddr_in *)cursor->ifa_addr;
                    waddr = [NSString stringWithUTF8String:inet_ntop(ipv4->sin_family, &(ipv4->sin_addr), addrNamev4, INET_ADDRSTRLEN)];

                    //私有投射扫描局域网内设备需要。
                    //自测时候发现手机连接wifi取ip地址的时候，会取不准，发现en0是准确的，en2就不准确了，所以在en0已经有ip地址的时候，不在继续遍历了。
                    if (waddr.length > 0) {
                        break;
                    }
                }
            }
            cursor = cursor->ifa_next;
        }
        //}
        
        if (waddr == nil || [@"" isEqualToString:waddr]) {
            cursor = addrs;
            while (cursor != NULL) {
                if (cursor->ifa_addr->sa_family == AF_INET6 && (cursor->ifa_flags & IFF_LOOPBACK) == 0) {
                    NSString * name = [NSString stringWithUTF8String:cursor->ifa_name];
                    if ([name hasPrefix:@"en"]) {//Wifi adapter
                        char addrNamev6[INET6_ADDRSTRLEN];
                        const struct sockaddr_in6 * ipv6 = (const struct sockaddr_in6*)cursor->ifa_addr;
                        waddr = [NSString stringWithUTF8String:inet_ntop(ipv6->sin6_family, &(ipv6->sin6_addr), addrNamev6, INET6_ADDRSTRLEN)];
                    }
                }
                cursor = cursor->ifa_next;
            }
        }
        
        freeifaddrs(addrs);
    }
    return waddr;
}

//获取设备IP地址
+ (NSString *)spGetDeviceIPAddresses {
    
    return [[self class] spGetIPAddress:YES];
}

+ (NSString *)spGetIPAddress:(BOOL)preferIPv4
{
    NSArray *searchArray = preferIPv4 ?
    @[ IOS_CELLULAR @"/" IP_ADDR_IPv4, IOS_CELLULAR @"/" IP_ADDR_IPv6, IOS_WIFI @"/" IP_ADDR_IPv4, IOS_WIFI @"/" IP_ADDR_IPv6 ] :
    @[ IOS_CELLULAR @"/" IP_ADDR_IPv6, IOS_CELLULAR @"/" IP_ADDR_IPv4, IOS_WIFI @"/" IP_ADDR_IPv6, IOS_WIFI @"/" IP_ADDR_IPv4 ] ;
    
    NSDictionary *addresses = [self spGetIPAddresses];

    __block NSString *address;
    [searchArray enumerateObjectsUsingBlock:^(NSString *key, NSUInteger idx, BOOL *stop)
     {
         address = addresses[key];
         if(address) *stop = YES;
     } ];
    return address ? address : @"0.0.0.0";
}

+ (NSDictionary *)spGetIPAddresses
{
    NSMutableDictionary *addresses = [NSMutableDictionary dictionaryWithCapacity:8];
    
    // retrieve the current interfaces - returns 0 on success
    struct ifaddrs *interfaces;
    if(!getifaddrs(&interfaces)) {
        // Loop through linked list of interfaces
        struct ifaddrs *interface;
        for(interface=interfaces; interface; interface=interface->ifa_next) {
            if(!(interface->ifa_flags & IFF_UP) /* || (interface->ifa_flags & IFF_LOOPBACK) */ ) {
                continue; // deeply nested code harder to read
            }
            const struct sockaddr_in *addr = (const struct sockaddr_in*)interface->ifa_addr;
            char addrBuf[ MAX(INET_ADDRSTRLEN, INET6_ADDRSTRLEN) ];
            if(addr && (addr->sin_family==AF_INET || addr->sin_family==AF_INET6)) {
                NSString *name = [NSString stringWithUTF8String:interface->ifa_name];
                NSString *type;
                if(addr->sin_family == AF_INET) {
                    if(inet_ntop(AF_INET, &addr->sin_addr, addrBuf, INET_ADDRSTRLEN)) {
                        type = IP_ADDR_IPv4;
                    }
                } else {
                    const struct sockaddr_in6 *addr6 = (const struct sockaddr_in6*)interface->ifa_addr;
                    if(inet_ntop(AF_INET6, &addr6->sin6_addr, addrBuf, INET6_ADDRSTRLEN)) {
                        type = IP_ADDR_IPv6;
                    }
                }
                if(type) {
                    NSString *key = [NSString stringWithFormat:@"%@/%@", name, type];
                    addresses[key] = [NSString stringWithUTF8String:addrBuf];
                }
            }
        }
        // Free memory
        freeifaddrs(interfaces);
    }
    return [addresses count] ? addresses : nil;
}

+ (BOOL)spNetworkAvailable
{
    [self spPingReachabilityInternal];
    
    
    BOOL isReachable = ((tVKConnectionFlags & kSCNetworkFlagsReachable) != 0);
    BOOL needsConnection = ((tVKConnectionFlags & kSCNetworkFlagsConnectionRequired) != 0);
    
    BOOL nonWiFi = ((tVKConnectionFlags & kSCNetworkReachabilityFlagsTransientConnection) != 0);
    
    if (nonWiFi) {
        return YES;
    }
    
    return (isReachable && !needsConnection) ? YES : NO;
}

- (NSUInteger)spGetBytesOut
{
	BOOL success;
	struct ifaddrs * addrs;
	const struct ifaddrs * cursor;
	
	success = getifaddrs(&addrs) == 0;
	
	NSUInteger oBytes = 0;
	if (success) {
		cursor = addrs;
		while (cursor != NULL) {
			// the second test keeps from picking up the loopback address
			if (cursor->ifa_addr->sa_family == AF_LINK && (cursor->ifa_flags & IFF_LOOPBACK) == 0)
			{
				NSString *name = [NSString stringWithUTF8String:cursor->ifa_name];
                BOOL isWWAN = [UIDevice spActiveWWAN];
                if ((isWWAN && [name hasPrefix:@"pdp_ip"]) ||
                    (!isWWAN && [name hasPrefix:@"en"]))// cellular adapter / Wi-Fi adapter
				{
					struct if_data *if_data = (struct if_data *)cursor->ifa_data;
					
					if(if_data){
						oBytes += if_data->ifi_obytes;
					}
				}
			}
			cursor = cursor->ifa_next;
		}
		freeifaddrs(addrs);
	}
	
	return oBytes;
}


- (NSUInteger)spGetBytesIn
{
	BOOL success;
	struct ifaddrs * addrs;
	const struct ifaddrs * cursor;
	
	success = getifaddrs(&addrs) == 0;
	
	NSUInteger iBytes = 0;
	
	if (success) {
		cursor = addrs;
		while (cursor != NULL) {
			// the second test keeps from picking up the loopback address
			if (cursor->ifa_addr->sa_family == AF_LINK && (cursor->ifa_flags & IFF_LOOPBACK) == 0)
			{
				NSString *name = [NSString stringWithUTF8String:cursor->ifa_name];
                BOOL isWWAN = [UIDevice spActiveWWAN];
                if ((isWWAN && [name hasPrefix:@"pdp_ip"]) ||
                    (!isWWAN && [name hasPrefix:@"en"]))// cellular adapter / Wi-Fi adapter
				{
					struct if_data *if_data = (struct if_data *)cursor->ifa_data;
					
					if(if_data){
						iBytes += if_data->ifi_ibytes;
					}
				}
			}
			cursor = cursor->ifa_next;
		}
		freeifaddrs(addrs);
	}
	
	return iBytes;
}

+ (NSString *)spGetIPAddressByHostName:(NSString *)domain
{
	if(domain.length <= 0){
		return nil;
	}
	struct hostent *hs;
	struct sockaddr_in server;
	if ((hs = gethostbyname([domain UTF8String])) != NULL)
	{
		server.sin_addr = *((struct in_addr*)hs->h_addr_list[0]);
		return [NSString stringWithUTF8String:inet_ntoa(server.sin_addr)];
	}
	return nil;
}

- (BOOL)spGetDataCounters:(NSUInteger*)wifiSent :(NSUInteger*)wifiReceived :(NSUInteger*)wwanSent :(NSUInteger*)wwanReceived
{
    if (wifiSent == nil || wifiReceived == nil || wwanSent == nil || wwanReceived == nil)
        return NO;
    BOOL   success = NO;
    struct ifaddrs *addrs = nil;
    struct ifaddrs *cursor = nil;
    struct if_data *networkStatisc = nil;
    
    NSUInteger WiFiSent = 0;
    NSUInteger WiFiReceived = 0;
    NSUInteger WWANSent = 0;
    NSUInteger WWANReceived = 0;
    
    success = (getifaddrs(&addrs) == 0);
    if (!success)
        return NO;
    cursor = addrs;
    while (cursor != NULL)
    {
        // names of interfaces: en is WiFi ,pdp_ip is WWAN
        
        if (cursor->ifa_addr->sa_family == AF_LINK)
        {
            if ([self isString:cursor->ifa_name hasPrefix:"en" preLen:2])
            {
                networkStatisc = (struct if_data *) cursor->ifa_data;
                WiFiSent+=networkStatisc->ifi_obytes;
                WiFiReceived+=networkStatisc->ifi_ibytes;
            }
            
            if ([self isString:cursor->ifa_name hasPrefix:"pdp_ip" preLen:6])
            {
                networkStatisc = (struct if_data *) cursor->ifa_data;
                WWANSent+=networkStatisc->ifi_obytes;
                WWANReceived+=networkStatisc->ifi_ibytes;
            }
            
        }
        
        cursor = cursor->ifa_next;
    }
    
    freeifaddrs(addrs);
    
    *wifiSent = WiFiSent;
    *wifiReceived = WiFiReceived;
    *wwanSent = WWANSent;
    *wwanReceived = WWANReceived;
    
    return YES;
}

- (BOOL)isString:(const char*)str hasPrefix:(const char*)pre preLen:(int)len
{
    for (int i = 0; i < len; i++) {
        if (str[i] != pre[i])
            return NO;
    }
    
    return YES;
}

#pragma mark Checking Connections

+ (void)spPingReachabilityInternal
{
    @synchronized(self){
        //BOOL ignoresAdHocWiFi = NO;
        struct sockaddr_in ipAddress;
        bzero(&ipAddress, sizeof(ipAddress));
        ipAddress.sin_len = sizeof(ipAddress);
        ipAddress.sin_family = AF_INET;
        //ipAddress.sin_addr.s_addr = htonl(ignoresAdHocWiFi ? INADDR_ANY : IN_LINKLOCALNETNUM);
        
        // Recover reachability flags
        SCNetworkReachabilityRef defaultRouteReachability = SCNetworkReachabilityCreateWithAddress(kCFAllocatorDefault, (struct sockaddr *)&ipAddress);
        BOOL didRetrieveFlags = SCNetworkReachabilityGetFlags(defaultRouteReachability, &tVKConnectionFlags);
        CFRelease(defaultRouteReachability);
        if (!didRetrieveFlags)
            printf("Error. Could not recover network reachability flags\n");
        
    }
}


//下面有一些测试驱动配置，测试各种网络情况下的提示是否正确
+ (BOOL)spActiveWWAN
{
#ifdef DEBUG
    //    return YES;
#endif
	if (![self spNetworkAvailable]) return NO;
	return ((tVKConnectionFlags & kSCNetworkReachabilityFlagsIsWWAN) != 0);
}

+ (BOOL)spActiveWLAN
{
    //先判断下是否有wwan标志，有就不必继续判断了，是蜂窝网络
    if (((tVKConnectionFlags & kSCNetworkReachabilityFlagsIsWWAN) != 0)){
        return NO;
    }
    
    //根据苹果官方的Reachability代码判断下网络的标志是否符合wifi特征
    BOOL isNeedcheckWIFIAddr = NO;
    if ((tVKConnectionFlags & kSCNetworkReachabilityFlagsConnectionRequired) == 0)
	{
		isNeedcheckWIFIAddr = YES;
	}
    if ((((tVKConnectionFlags & kSCNetworkReachabilityFlagsConnectionOnDemand ) != 0) ||
         (tVKConnectionFlags & kSCNetworkReachabilityFlagsConnectionOnTraffic) != 0))
	{
        if ((tVKConnectionFlags & kSCNetworkReachabilityFlagsInterventionRequired) == 0)
        {
            isNeedcheckWIFIAddr = YES;
        }
    }
    
    //判断是否需要进一步判断wifi的ip地址
    if (!isNeedcheckWIFIAddr){
        return NO;
    }
    
    //ok,现在可以走第三方的开源代码判断是否有wifi的ip地址，保持跟原来的逻辑一致
    BOOL bRet = ([UIDevice spLocalWiFiIPAddress] != nil);
    return bRet;
}

+ (BOOL)spIsInUSA{
	CTTelephonyNetworkInfo *netInfo = [[CTTelephonyNetworkInfo alloc] init];
	if (netInfo) {
		CTCarrier *carrier = [netInfo subscriberCellularProvider];
		if (carrier) {
			NSString *mcc = [carrier mobileCountryCode];
			if ([mcc isEqualToString:@"[Utils getScreenWidth] - 10"] || [mcc isEqualToString:@"311"]) {
				return YES;
			}
		}
	}
	
	return NO;
}

+ (BOOL)spIsInChina{
	CTTelephonyNetworkInfo *netInfo = [[CTTelephonyNetworkInfo alloc] init];
	if (netInfo) {
		CTCarrier *carrier = [netInfo subscriberCellularProvider];
		if (carrier) {
			NSString *mcc = [carrier mobileCountryCode];
			if ([mcc isEqualToString:@"460"]) {
				return YES;
			}
		}
	}
	
	return NO;
}

+ (BOOL)spIsInHongkong{
	CTTelephonyNetworkInfo *netInfo = [[CTTelephonyNetworkInfo alloc] init];
	if (netInfo) {
		CTCarrier *carrier = [netInfo subscriberCellularProvider];
		if (carrier) {
			NSString *mcc = [carrier mobileCountryCode];
			if ([mcc isEqualToString:@"454"]) {
				return YES;
			}
		}
	}
	
	return NO;
}

+ (BOOL)spIsInTaiwan{
	CTTelephonyNetworkInfo *netInfo = [[CTTelephonyNetworkInfo alloc] init];
	if (netInfo) {
		CTCarrier *carrier = [netInfo subscriberCellularProvider];
		if (carrier) {
			NSString *mcc = [carrier mobileCountryCode];
			if ([mcc isEqualToString:@"466"]) {
				return YES;
			}
		}
	}
	
	return NO;
}

+ (NSString *)spMobileCountryCode
{
    CTCarrier *carrier = [self superPlayerNetworkInfo].subscriberCellularProvider;
    if (carrier) {
        return carrier.mobileCountryCode;
    }
    return nil;
}

+ (NSString *)spMobileNetworkCode
{
    CTCarrier *carrier = [self superPlayerNetworkInfo].subscriberCellularProvider;
    if (carrier) {
        return carrier.mobileNetworkCode;
    }
    return nil;
}

+ (CTTelephonyNetworkInfo *)superPlayerNetworkInfo
{
    if (!networkInfo) {
        networkInfo = [[CTTelephonyNetworkInfo alloc] init];
    }
    return networkInfo;
}

@end
