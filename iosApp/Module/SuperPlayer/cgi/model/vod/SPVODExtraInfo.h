//
//  SPVODExtraInfo.h
//  SPPlayer
//
//  Created by ethanyxliu on 2019/10/17.
//  Copyright © 2019 tencent. All rights reserved.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

@interface SPVODExtraInfo : NSObject

@property (nonatomic, assign) int ct;                   // vkey有效期 单位s

@property (nonatomic, copy) NSString *keyID;            // root.vl.vi.keyid, HLS keyID

@property (nonatomic, copy) NSString *base;             // root.vl.vi.base，加密用密钥

@property (nonatomic, assign) int64_t tm;               // root.vl.vi.tm，UNIX时间戳

@property (nonatomic, copy) NSString *fMD5;             // root.vl.vi.fmd5

@property (atomic, copy) NSString *tstId;               // ABTest测试分组id.用于上报

@property (nonatomic, assign) NSInteger type;           // root.vl.vi.type 用作上报

@property (nonatomic, copy) NSString *ip;               // root.ip，客户端IP

@property (nonatomic, assign) int enc;                  // root.vl.vi.enc，加密标识，0：无，1：客户端加密，2：cdn加密

@property (nonatomic, strong) NSString *xmlString;      // getvinfo返回的整个xml，离线下载给下载组件用

@property (nonatomic, assign) NSTimeInterval createTime;// playinfo创建的时间，用来和ct做比较，判断缓存是否失效或将要失效~

@end

NS_ASSUME_NONNULL_END
