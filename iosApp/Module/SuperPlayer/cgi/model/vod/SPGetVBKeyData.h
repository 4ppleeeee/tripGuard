//
//  SPGetVBKeyData.h
//  SPPlayer
//
//  Created by liyukuan on 2019/10/3.
//  Copyright © 2019 tencent. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "SPClipInfo.h"
#import "SPErrorDefine.h"
#import "SPCGIErrorModel.h"

NS_ASSUME_NONNULL_BEGIN

@interface SPGetVBKeyData : NSObject

@property (nonatomic, assign) SPXMLParseErrorCode parseResult;

@property (nonatomic, strong) SPCGIErrorModel *cgiErrorModel;

@property (nonatomic, strong) NSDictionary<NSNumber *, SPClipInfo *> *clipInfoDict;

@property (nonatomic, copy) NSString *fileName;

@property (nonatomic, assign) int maxClipIndex;  // 已经请求回来的最大的分片索引

@end

NS_ASSUME_NONNULL_END
