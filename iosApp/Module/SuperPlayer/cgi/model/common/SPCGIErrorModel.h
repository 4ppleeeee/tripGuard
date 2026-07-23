//
//  SPCGIErrorModel.h
//  SPPlayer
//
//  Created by liyukuan on 2019/10/3.
//  Copyright © 2019 tencent. All rights reserved.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

@interface SPCGIErrorModel : NSObject

@property (nonatomic, assign) int em;

@property (nonatomic, assign) int exem;

@property (nonatomic, copy) NSString *errMsg;

@property (nonatomic, copy) NSString *exInfo;

@property (nonatomic, assign) int64_t curSeverTime;

@property (nonatomic, copy) NSString *randFlag;

@property (nonatomic, assign) BOOL needRetry;

@end

NS_ASSUME_NONNULL_END
