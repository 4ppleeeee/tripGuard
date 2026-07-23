//
//  SPLiveInfoData.h
//  SPPlayer
//
//  Created by hemanli on 2019/10/7.
//  Copyright © 2019 tencent. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "SPLivePlayInfo.h"
#import "SPErrorDefine.h"
#import "SPLiveCGIErrorModel.h"

NS_ASSUME_NONNULL_BEGIN

@interface SPLiveInfoData : NSObject

@property (nonatomic, assign) SPJsonErrorCode parseResult;

@property (nonatomic, strong) SPLiveCGIErrorModel *cgiErrorModel;

@property (nonatomic, strong) SPLivePlayInfo *livePlayInfo;

@end

NS_ASSUME_NONNULL_END
