//
//  SPVODURLBuilder.h
//  SPPlayer
//
//  Created by liyukuan on 2019/10/10.
//  Copyright © 2019 tencent. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "SPGetVInfoData.h"
#import "SPSection.h"

NS_ASSUME_NONNULL_BEGIN

@interface SPVODURLBuilder : NSObject

+ (SPSection *)buildHLSURLWithGetVInfoData:(SPGetVInfoData *)getvinfoData
                                    sdtFrom:(NSString *)sdtFrom
                                   freeFlow:(BOOL)freeFlow;


+ (SPSection *)buildWholeMP4URLWithGetVInfoData:(SPGetVInfoData *)getvinfoData
                                         sdtFrom:(NSString *)sdtFrom
                                        freeFlow:(BOOL)freeFlow;

+ (SPSection *)buildMP4ClipURLWith:(SPClipInfo *)clipInfo
                        uiInfoArray:(NSArray<SPVODUIInfo *> *)uiInfoArray
                           fileName:(NSString *)fileName
                               vkey:(NSString *)vkey
                               rate:(int)rate
                               defn:(NSString *)def
                            sdtFrom:(NSString *)sdtFrom;

+ (SPSection *)buildURLDirectlyFromUIInfoArray:(NSArray<SPVODUIInfo *> *)uiInfoArray
                                       duration:(NSTimeInterval)duration
                                       fileSize:(int64_t)fileSize
                                        fileMD5:(NSString *)fileMD5
                                          keyID:(NSString *)keyID;
@end

NS_ASSUME_NONNULL_END
