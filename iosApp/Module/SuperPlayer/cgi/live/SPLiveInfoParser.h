//
//  SPLiveInfoParser.h
//  SPPlayer
//
//  Created by hemanli on 2019/10/7.
//  Copyright © 2019 tencent. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "SPLiveInfoData.h"

@interface SPLiveInfoParser : NSObject

+ (SPLiveInfoData *)parseLiveInfoJson:(NSString *)json;

@end
