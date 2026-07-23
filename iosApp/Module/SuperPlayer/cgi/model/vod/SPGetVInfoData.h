//
//  SPGetVInfoData.h
//  SPPlayer
//
//  Created by liyukuan on 2019/10/3.
//  Copyright © 2019 tencent. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "SPVODPlayInfo.h"
#import "SPVODUIInfo.h"
#import "SPClipInfo.h"
#import "SPErrorDefine.h"
#import "SPCGIErrorModel.h"

NS_ASSUME_NONNULL_BEGIN

@interface SPGetVInfoData : NSObject

@property (nonatomic, strong) SPVODPlayInfo *vodPlayInfo;

@property (nonatomic, strong) NSArray<SPVODUIInfo *> *uiInfoArray;

@property (nonatomic, assign) SPXMLParseErrorCode parseResult;

@property (nonatomic, strong) SPCGIErrorModel *cgiErrorModel;

@property (nonatomic, copy) NSString *fvKey;

@property (nonatomic, strong) NSArray<SPClipInfo *> *clipInfoArray;

@property (nonatomic, copy) NSString *fsha;  // TODO:确认是否已经废掉

@property (nonatomic, copy) NSString *fileName;

@end

NS_ASSUME_NONNULL_END
