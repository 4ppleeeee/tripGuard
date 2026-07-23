//
//  SPURLManager.h
//  SPPlayer
//
//  Created by liyukuan on 2019/9/27.
//  Copyright © 2019 tencent. All rights reserved.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN
/**
 */
@interface SPURLManager : NSObject

@property (class, nonatomic, readonly) NSString *getvinfoHost;  // 点播getvinfo主域名

@property (class, nonatomic, readonly) NSString *getVInfoBackHost;  // 点播getvinfo备份域名

@property (class, nonatomic, readonly) NSString *getVInfoIPV6Host;

@property (class, nonatomic, readonly) NSString *liveInfoHost;

@property (class, nonatomic, readonly) NSString *liveInfoBackHost;

@end

NS_ASSUME_NONNULL_END
