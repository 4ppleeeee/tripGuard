//
//  SPCGIRequestCommonParam.h
//  SPPlayer
//
//  Created by liyukuan on 2019/10/11.
//  Copyright © 2019 tencent. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "SPCGIDefines.h"

NS_ASSUME_NONNULL_BEGIN
/**
* cgi请求所需要的通用参数
*/
@interface SPCGIRequestCommonParam : NSObject

@property (nonatomic, copy) NSString *platform;

@property (nonatomic, copy) NSString *sdtFrom;

@property (nonatomic, copy) NSString *sysVer;  // 操作系统版本号，例：12.3.1

@property (nonatomic, copy) NSString *deviceModel;

@property (nonatomic, assign) SPCGINetType netType;

@property (nonatomic, assign) SPCGILoginType loginType;

@property (nonatomic, copy) NSString *localeIdentifier;

@property (nonatomic, copy) NSString *guid;

@property (nonatomic, copy) NSString *qimei;

@property (nonatomic, copy) NSString *uin;  // QQ号

@property (nonatomic, copy) NSString *userID;

@property (nonatomic, copy) NSString *wxOpenID;

@property (nonatomic, copy) NSString *cookie;

@property (nonatomic, assign) BOOL isVIP;  // 是否时VIP

@property (nonatomic, copy) NSString *userAgent;

@end

NS_ASSUME_NONNULL_END
