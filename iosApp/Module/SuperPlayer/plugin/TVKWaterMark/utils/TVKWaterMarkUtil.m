/************************************************************
 Copyright (C), 1998-2018, Tencent Tech. Co., Ltd.
 FileName    : TVKWaterMarkUtil.m
 Author      : hemanli
 Version     : 1.0
 Date        : 2017/8/28
 Description :
 History     : 2017/8/28 初始版本
 ***********************************************************/

#import "TVKWaterMarkUtil.h"
#import "TVKRawWaterMarkInfo.h"
#import "TVKWaterMarkInfo.h"
#import "SPVcSystemInfo.h"

@implementation TVKWaterMarkUtil

+ (__kindof NSArray<TVKWaterMarkInfo *> *)waterMarkDisplayInfosFromWaterMarkInfos:(__kindof NSArray<TVKRawWaterMarkInfo *> *)waterMarkInfoArray
                                                                               rw:(int)rw {
    NSMutableArray<TVKWaterMarkInfo *> *displayInfoArray = [[NSMutableArray alloc] initWithCapacity:waterMarkInfoArray.count];
    for (id waterMarkInfo in waterMarkInfoArray) {
        TVKWaterMarkInfo *displayInfo = nil;
        if ([waterMarkInfo isKindOfClass:[TVKVODWaterMarkInfo class]]) {
            displayInfo = [self waterMarkDisplayInfoFromVODWaterMarkInfo:waterMarkInfo rw:rw];
        } else if ([waterMarkInfo isKindOfClass:[TVKLiveWaterMarkInfo class]]) {
            displayInfo = [self waterMarkDisplayInfoFromLiveWaterMarkInfo:waterMarkInfo rw:rw];
        }

        if (displayInfo) {
            [displayInfoArray addObject:displayInfo];
        }
    }

    return displayInfoArray;
}

+ (TVKWaterMarkInfo *)waterMarkDisplayInfoFromVODWaterMarkInfo:(TVKVODWaterMarkInfo *)vodWaterMarkInfo rw:(int)rw {
    NSString *imageUrl = vodWaterMarkInfo.url;
    if (![[SPVcSystemInfo sharedInstance] isAllowsArbitraryLoads]) {
        imageUrl = vodWaterMarkInfo.httpsUrl;
    }

    CGFloat alpha = vodWaterMarkInfo.alpha / 100.0;
    TVKWaterMarkInfo *waterMarkDisplayInfo = [[TVKWaterMarkInfo alloc] initWithWaterMarkMD5:vodWaterMarkInfo.md5
                                                                                   imageUrl:imageUrl
                                                                              imageHttpsUrl:vodWaterMarkInfo.httpsUrl
                                                                             originPosition:vodWaterMarkInfo.position
                                                                                      alpha:alpha
                                                                                     isShow:YES
                                                                                         rw:rw];
    return waterMarkDisplayInfo;
}

+ (TVKWaterMarkInfo *)waterMarkDisplayInfoFromLiveWaterMarkInfo:(TVKLiveWaterMarkInfo *)liveWaterMarkInfo rw:(int)rw {
    TVKWaterMarkInfo *waterMarkDisplayInfo = [[TVKWaterMarkInfo alloc] initWithWaterMarkMD5:liveWaterMarkInfo.md5
                                                                                   imageUrl:liveWaterMarkInfo.url
                                                                              imageHttpsUrl:nil
                                                                             originPosition:liveWaterMarkInfo.position
                                                                                      alpha:1.0
                                                                                     isShow:liveWaterMarkInfo.isShow
                                                                                         rw:rw];
    return waterMarkDisplayInfo;
}
@end
