//
//  SPVODInfoParser.h
//  SPPlayer
//
//  Created by liyukuan on 2019/9/26.
//  Copyright © 2019 tencent. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "SPGetVInfoData.h"
#import "SPGetVBKeyData.h"

NS_ASSUME_NONNULL_BEGIN

@interface SPVODInfoParser : NSObject

+ (SPGetVInfoData *)parseGetVInfoXMLString:(NSString *)xmlString;

+ (SPGetVInfoData *)parseOfflineGetVInfoXMLString:(NSString *)xmlString;

+ (SPGetVBKeyData *)parseGetVBKeyString:(NSString *)xmlString freeFlow:(BOOL)freeFlow;

@end

NS_ASSUME_NONNULL_END
